package proiect_licenta.planner.cli;


import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import proiect_licenta.planner.helper.ClientHelper;
import proiect_licenta.planner.jobs.joblist.JobList;
import proiect_licenta.planner.storage.BucketStorage;
import proiect_licenta.planner.storage.LocalStorage;
import proiect_licenta.planner.storage.Storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "run", description = "Run the planner", mixinStandardHelpOptions = true)
public class PlannerCLI implements Callable<Integer> {


	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit")
	private boolean help;

	@Option(names = {"-b", "--bucket"}, description = "The name of the S3 bucket; at least one of --bucket and --local must be specified")
	private String bucketName;

	@Option(names = {"-l", "--local"}, description = "The name of the local folder; at least one of --bucket and --local must be specified")
	private String localFolder;

	private String credentialsFile;

	@CommandLine.Parameters(arity = "0..", description = "The paths of the joblists")
	private List<String> jobListPaths;


	@Option(names = {"-a", "--aws"}, description = "The name of the AWS credentials file")
	private String awsCredentialsFile;


	private Storage storage;

	public List<JobList> loadJobLists() {


		return jobListPaths.parallelStream()
				.map(Paths::get)
				.map(Path::toAbsolutePath)
				.map(path -> JobList.createJobList(path.toString(), storage))
				.toList();


	}

	@Override
	public Integer call() throws Exception {
		if (help) {
			CommandLine.usage(this, System.out);
			return 0;
		}

		if (awsCredentialsFile != null) {

			ClientHelper.FileCredentials(awsCredentialsFile);
		}

		if (bucketName != null && localFolder != null) {
			System.out.println("The bucket name and the local folder cannot be specified at the same time.");
			return 1;
		}


		if (bucketName == null && localFolder == null) {
			System.out.println("The bucket name or the local folder must be specified.");
			return 1;
		}
		Storage storage = bucketName != null
				? new BucketStorage(bucketName)
				: new LocalStorage(localFolder);


		List<JobList> jobLists = loadJobLists();

		jobLists.parallelStream().forEach(JobList::launch);
		return 0;
	}


}
