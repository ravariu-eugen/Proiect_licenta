package proiect_licenta.planner.execution;

import kotlin.NotImplementedError;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.archive.ArchiveManager;
import proiect_licenta.planner.archive.ZipManager;
import proiect_licenta.planner.dataset.NullDataset;
import proiect_licenta.planner.storage.Storage;
import proiect_licenta.planner.task.TaskComplete;
import proiect_licenta.planner.task.TaskError;
import proiect_licenta.planner.task.TaskResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OutputAggregator {
	private static final Logger logger = LogManager.getLogger();
	private static int id;
	private final ArchiveManager archiveManager = new ZipManager();
	private String tempJobDir;
	private boolean isNull;

	public OutputAggregator() {
		try {
			// create a temporary folder to store and aggregate the results
			tempJobDir = Files.createTempDirectory("temp").toAbsolutePath().toString();

			tempJobDir = Files.createDirectory(Paths.get("tmp" + id++)).toAbsolutePath().toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static void aggregateOutputs(Path file, String sourceDir, String destinationDir) {
		// the number of directories in the path
		int baseNameCount = Path.of(sourceDir).getNameCount();

		// the name of the task
		String taskName = file.getName(baseNameCount).toString();
		// the name of the output dataset
		String outputName = file.getName(baseNameCount + 1).toString();

		int fileNameCount = file.getNameCount();

		List<String> innerNames = new ArrayList<>();

		for (int i = baseNameCount + 2; i < fileNameCount; i++) {
			innerNames.add(file.getName(i).toString());
		}

		String fileName = FilenameUtils.separatorsToUnix(innerNames.stream().reduce((a, b) -> a + "/" + b).orElse(""));
		String destination = Paths.get(destinationDir, outputName, taskName, fileName).toString();
		try {
			boolean _ = new File(destination).getParentFile().mkdirs();
			Files.move(file, Path.of(destination));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Merge the given list of task results into a temporary folder and create output archives.
	 *
	 * @param results the list of task results to merge
	 * @param storage the storage to use for creating output archives
	 * @param outputs the list of output names to create archives for
	 */
	public void mergeResults(List<TaskResult> results, Storage storage, List<String> outputs) {
		if (results.isEmpty()) {
			return;
		}
		if (results.getFirst() instanceof TaskComplete taskComplete) {
			if (taskComplete.name().equals("null")) {
				isNull = true;
			}
		}

		// extract results into temp folder
		extractResults(results);
		// create output archives
		createOutputs(storage, outputs);
	}

	private void extractTask(TaskComplete taskComplete) {
		logger.debug("Result: {}", taskComplete.toString());
		byte[] data = taskComplete.resultData();
		try {
			archiveManager.extractArchive(new ByteArrayInputStream(data), tempJobDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Creates output archives based on the given list of outputs and stores them in the provided storage.
	 *
	 * @param storage the storage to use for creating output archives
	 * @param outputs the list of output names to create archives for
	 */
	private void createOutputs(Storage storage, List<String> outputs) {
		if (outputs.size() == 1) {
			// single output
			String output = outputs.getFirst();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Path jobDirectoryPath = Paths.get(tempJobDir);
			if (isNull) {
				jobDirectoryPath = jobDirectoryPath.resolve(NullDataset.NAME);
			}

			archiveManager.addFolder(jobDirectoryPath.toString(), outputStream);
			storage.putBytes(output, outputStream.toByteArray());
		} else {

			Path outputSource;

			if (isNull) {
				// remove null folder from path
				outputSource = Paths.get(tempJobDir, "null");

			} else {
				try (var allFiles = Files.walk(Path.of(tempJobDir))) {

					String outputTempDir = Files.createTempDirectory("output").toAbsolutePath().toString();
					//String outputTempDir =  "results";
					outputSource = Paths.get(outputTempDir);


					allFiles.filter(Files::isRegularFile)
							.forEach(file -> aggregateOutputs(file, tempJobDir, outputTempDir));

				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}

			// create output archives
			outputs.forEach(output -> {
				String name = FilenameUtils.getBaseName(output);
				//logger.info("Creating output {}", name);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				archiveManager.addFolder(outputSource.resolve(name).toString(), outputStream);
				storage.putBytes(output, outputStream.toByteArray());
			});


			// multiple outputs

		}
	}

	/**
	 * Extracts the results from a list of TaskResult objects into the temporary folder.
	 *
	 * @param results the list of TaskResult objects to extract results from
	 */
	private void extractResults(List<TaskResult> results) {
		results.forEach(result -> {
			switch (result) {
				// extract the result
				case TaskComplete taskComplete -> extractTask(taskComplete);
				// save the message to a file
				case TaskError taskError -> saveError(taskError);
				case null, default -> throw new RuntimeException("Unexpected value: " + result);
			}
		});
	}


	private void saveError(TaskError taskError) {
		// save the message to a file
		throw new NotImplementedError("TaskError not implemented");
	}
}
