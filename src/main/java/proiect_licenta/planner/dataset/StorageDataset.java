package proiect_licenta.planner.dataset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.archive.ArchiveManager;
import proiect_licenta.planner.archive.ZipManager;
import proiect_licenta.planner.helper.TempDir;
import proiect_licenta.planner.storage.Storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class StorageDataset implements Dataset {
	private static final Logger logger = LogManager.getLogger();

	private final String objectName;
	private final List<TaskData> tasks;

	public StorageDataset(Storage storage, String objectName) throws IOException {

		logger.info("create dataset: {}", objectName);
		this.objectName = objectName;
		// create temporary directory
		TempDir tempDir = new TempDir("dataset-" + objectName);
		// put the object in the temporary directory

		logger.info("get archive: {}", tempDir);
		getArchive(storage, objectName, tempDir.getDir());
		// take the tasks

		try (var stream = Files.list(Paths.get(tempDir.getDir()))) {
			this.tasks = getTasks(stream);
		}

		// clear the temporary directory
		tempDir.delete();


	}

	private static @NotNull List<TaskData> getTasks(Stream<Path> stream) {

		return stream
				.filter(Files::isDirectory)
				.map(Path::toAbsolutePath)
				.map(Path::toString)
				.map(TaskData::from)
				.toList();
	}

	public void saveDataset(String destinationDir) throws IOException {
		// create a directory for the dataset
		Path path = Files.createDirectories(Paths.get(destinationDir + "/" + objectName));


		for (TaskData taskData : tasks) {
			// save the task
			var taskPath = Paths.get(path + "/" + taskData.name() + ".zip");
			Files.createFile(taskPath);
			try (var stream = Files.newOutputStream(taskPath)) {
				stream.write(taskData.data());

			}
		}

	}

	private void getArchive(Storage storage, String objectName, String destinationDir) throws IOException {
		String archivePath = destinationDir + "/" + objectName;
		logger.info("getArchive: {}", archivePath);
		// load the archive into local storage
		storage.get(objectName, archivePath).join();
		// extract the archive
		ArchiveManager archiveManager = new ZipManager();
		archiveManager.extractArchive(archivePath, destinationDir);
		// remove the archive
		Files.delete(Paths.get(archivePath));
	}

	@Override
	public List<TaskData> getTasks() {
		return tasks;
	}

	@Override
	public String getName() {
		return objectName;
	}
}
