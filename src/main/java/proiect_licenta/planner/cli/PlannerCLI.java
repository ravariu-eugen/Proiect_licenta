package proiect_licenta.planner.cli;


import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import proiect_licenta.planner.helper.ClientHelper;
import proiect_licenta.planner.jobs.JobList;
import proiect_licenta.planner.storage.BucketStorage;
import proiect_licenta.planner.storage.LocalStorage;
import proiect_licenta.planner.storage.Storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "run", description = "Run the planner", mixinStandardHelpOptions = true)
public class PlannerCLI implements Callable<Integer> {


	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit")
	private boolean help;

	@Option(names = {"-b", "--bucket"}, description = "The name of the S3 bucket; exactly one of --bucket and --local must be specified")
	private String bucketName;

	@Option(names = {"-l", "--local"}, description = "The name of the local folder; exactly one of --bucket and --local must be specified")
	private String localFolder;

	@CommandLine.Parameters(arity = "0..", description = "The paths of the joblists")
	private List<String> jobListPaths;


	@Option(names = {"-a", "--aws"}, description = "The name of the AWS credentials file", required = true)
	private String awsCredentialsFile;

	private String readFile(String path) {
		try {
			return Files.readString(Paths.get(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<JobList> loadJobLists(Storage storage) {
		System.out.println(jobListPaths);

		return jobListPaths.parallelStream()
				.map(Paths::get)
				.map(Path::toAbsolutePath)
				.map(path -> readFile(path.toString()))
				.map(json -> JobList.createJobList(json, storage))
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

		if (jobListPaths == null) {

			storage.listObjects().join().forEach(System.out::println);
			return 0;
		}

		List<JobList> jobLists = loadJobLists(storage);

		jobLists.parallelStream().forEach(JobList::launch);
		return 0;
	}


}
