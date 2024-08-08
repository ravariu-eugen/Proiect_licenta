package proiect_licenta.planner.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.archive.ArchiveManager;
import proiect_licenta.planner.archive.ZipManager;
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
import java.util.List;

public class OutputAggregator {
	private static final Logger logger = LogManager.getLogger();
	private final String tempTaskDir;
	private final ArchiveManager archiveManager = new ZipManager();

	public OutputAggregator() {
		try {
			// create a temporary folder to store and aggregate the results
			tempTaskDir = Files.createTempDirectory("temp").toAbsolutePath().toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void aggregateOutputs(Path file, String taskTempDir, String outputTempDir) {
		int base = Path.of(taskTempDir).getNameCount();
		String taskName = file.getName(base).toString();
		String outputName = file.getName(base + 1).toString();

		String filePrefix = Paths.get(taskTempDir, taskName, outputName).toString();
		String fileName = file.toAbsolutePath().toString().substring(filePrefix.length() + 1);
		String destination = Paths.get(outputTempDir, outputName, taskName, fileName).toString();
		try {
			boolean _ = new File(destination).getParentFile().mkdirs();
			Files.copy(file, Path.of(destination));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void extractTask(TaskComplete taskComplete) {
		logger.debug("Result: {}", taskComplete.toString());
		byte[] data = taskComplete.resultData();
		try {
			archiveManager.extractArchive(new ByteArrayInputStream(data), tempTaskDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void mergeResults(List<TaskResult> results, Storage storage, List<String> outputs) {
		logger.info("Merging results {}", results.size());
		// extract results into temp folder
		extractResults(results);
		// create output archives
		createOutputs(storage, outputs);
	}

	private void createOutputs(Storage storage, List<String> outputs) {
		if (outputs.size() == 1) {
			// single output
			String output = outputs.getFirst();
			archiveManager.archiveFolder(tempTaskDir, output);
			storage.put(output, output);
		} else {
			// multiple outputs
			try (var stream = Files.walk(Path.of(tempTaskDir))) {

				String outputTempDir = Files.createTempDirectory("output").toAbsolutePath().toString();
				//String outputTempDir =  "results";

				stream.filter(Files::isRegularFile)
						.forEach(file -> {
							aggregateOutputs(file, tempTaskDir, outputTempDir);
						});

				outputs.forEach(output -> {
					String name = output.substring(0, output.lastIndexOf('.'));
					logger.info("Creating output {}", name);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					archiveManager.addFolder(outputTempDir + "/" + name, outputStream);
					storage.putBytes(output, outputStream.toByteArray());
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void extractResults(List<TaskResult> results) {
		results.forEach(result -> {
			switch (result) {
				case TaskComplete taskComplete -> {
					// extract the result
					extractTask(taskComplete);
				}
				case TaskError taskError -> {
					// save the message to a file
				}
				case null, default -> {
					throw new RuntimeException("Unexpected value: " + result);
				}
			}
		});
	}
}
