package proiect_licenta.planner.testrun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.archive.ArchiveManager;
import proiect_licenta.planner.archive.ZipManager;
import proiect_licenta.planner.execution.fleet.FleetManager;
import proiect_licenta.planner.execution.instance_factory.InstanceWrapper;
import proiect_licenta.planner.helper.FileDeleter;
import proiect_licenta.planner.helper.Helper;
import proiect_licenta.planner.jobs.JobList;
import proiect_licenta.planner.storage.LocalStorage;
import proiect_licenta.planner.storage.Storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TestRun {
	private static final Logger logger = LogManager.getLogger();

	private final ArchiveManager manager = new ZipManager();

	private final Storage storage = new LocalStorage();

	public TestRun() {
		// TODO Auto-generated constructor stub
	}

	private void prepareArchives(){

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
	}


	public void testFleet(){
		FleetManager manager = new FleetManager();
		logger.info("testFleet");
		var list = manager.createSpotFleet();

		list.stream().map(InstanceWrapper::new).forEach(i -> {
			logger.info(i.toString());
		});
	}
}
