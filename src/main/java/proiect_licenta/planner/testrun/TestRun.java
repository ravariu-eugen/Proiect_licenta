package proiect_licenta.planner.testrun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import picocli.CommandLine;
import proiect_licenta.planner.archive.ArchiveManager;
import proiect_licenta.planner.archive.ZipManager;
import proiect_licenta.planner.cli.PlannerCLI;
import proiect_licenta.planner.helper.FileDeleter;
import proiect_licenta.planner.helper.Helper;
import proiect_licenta.planner.jobs.joblist.JobList;
import proiect_licenta.planner.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TestRun {
	private static final Logger logger = LogManager.getLogger();

	private final ArchiveManager manager = new ZipManager();

	private final Storage storage;

	public TestRun(Storage storage) {
		this.storage = storage;
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {


		String[] args2 = new String[1];
		args2[0] = "-h";
		System.out.println(Arrays.toString(args2));

		int exitCode = new CommandLine(new PlannerCLI()).execute(args2);
		System.exit(exitCode);
	}

	private void prepareArchives() {

		FileDeleter.deleteAllFilesInFolder("arch");
		// code archives

		List<String> tasks = List.of(
				"number_multiplier", "copy", "longtask"
		);

		List<String> data = List.of(
				"numbers"
		);
		tasks.forEach(task -> manager.archiveFolder("data/test_tasks/" + task, "arch/" + task + ".zip"));

		data.forEach(task -> manager.archiveFolder("data/test_data/" + task, "arch/" + task + ".zip"));

	}

	public void loadData() {

		Path directory = Paths.get("arch");
		try (Stream<Path> stream = Files.list(directory)) {
			stream.forEach(path -> storage.put(path.getFileName().toString(), path.toString()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<JobList> loadJobLists() {
		try (var stream = Files.list(Paths.get("src/main/resources/joblists"))) {
			return stream
					.filter(Files::isRegularFile)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(fileName -> fileName.endsWith(".json"))
					.map(path -> JobList.createJobList(Helper.getResourceAsString("joblists/" + path), storage))
					.toList();
		} catch (IOException e) {
			throw new RuntimeException(e);

		}
	}

	private void getStorage(String destination) {

		//
		try {
			FileUtils.cleanDirectory(new File(destination));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Path directory = Paths.get(destination);
		var files = storage.listObjects();
		for (String file : files) {
			storage.get(file, directory.resolve(file).toString());
		}
	}

	public void run() {

		// 0. prepare archives
		logger.info("Preparing archives");
		prepareArchives();


		// 1. load data into storage
		logger.info("Loading data into storage");
		loadData();
		logger.info("Storage objects: {}", storage.listObjects());


		// 2. load job lists
		logger.info("Loading job lists");
		List<JobList> jobLists = loadJobLists();

		// 3. launch jobs
		logger.info("Launching jobs");
		for (JobList jobList : jobLists) {
			jobList.launch();
		}


		// 4. get results from storage
		logger.info("Getting results from storage");
		logger.info("Storage objects: {}", storage.listObjects());
		getStorage("results");
	}

}
