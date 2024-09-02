package proiect_licenta.planner.jobs;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.dataset.Dataset;
import proiect_licenta.planner.dataset.NullDataset;
import proiect_licenta.planner.dataset.StorageDataset;
import proiect_licenta.planner.execution.OutputAggregator;
import proiect_licenta.planner.execution.worker.WorkerPool;
import proiect_licenta.planner.jobs.requirements.FileNameMapping;
import proiect_licenta.planner.storage.Storage;
import proiect_licenta.planner.task.Task;
import proiect_licenta.planner.task.TaskResult;
import software.amazon.awssdk.utils.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ComputeJob extends Job {
	private final Logger logger = LogManager.getLogger();

	private final String image;

	private final String input;
	private final List<FileNameMapping> shared;
	private final List<String> outputs;
	private final Map<String, String> requirements;
	private WorkerPool workerPool;
	private List<CompletableFuture<TaskResult>> results;
	private CompletableFuture<Boolean> future;

	public ComputeJob(String name, String description, Storage storage,
	                  String image,
	                  String inputDataSet,
	                  List<FileNameMapping> sharedDataSets,
	                  List<String> outputDataSets,
	                  Map<String, String> requirements) {
		super(name, description, storage);

		this.image = image;

		this.input = inputDataSet;
		this.shared = sharedDataSets;
		this.outputs = outputDataSets;
		this.requirements = requirements;
	}

	public void setWorkerPool(WorkerPool workerPool) {
		this.workerPool = workerPool;
	}

	@Override
	public CompletableFuture<Boolean> launch() {
		logger.info("Launching processing job {} with input data set {}", name, input);
		future = CompletableFuture.supplyAsync(() -> {
			// load job dataset
			Instant start = Instant.now();
			Dataset inputDataset;
			try {
				if (input == null) // no input
					inputDataset = new NullDataset();
				else
					inputDataset = new StorageDataset(storage, input);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			logger.info("{} tasks to be sent", inputDataset.getTasks().size());

			List<Task> tasks = inputDataset.getTasks().parallelStream()
					.map(taskData -> new Task(taskData, this, workerPool))
					.toList();
			logger.info("Sending {} tasks", tasks.size());


			results = tasks.parallelStream()
					.map(Task::run)
					.toList();

			logger.info("Waiting for {} tasks", results.size());


			List<TaskResult> taskResults = results.parallelStream()
					.map(CompletableFuture::join)
					.toList();
			logger.info("Finished processing {} tasks", taskResults.size());
			OutputAggregator aggregator = new OutputAggregator();
			try {
				aggregator.mergeResults(taskResults, storage, outputs);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			logger.info("Finished processing job {}", name);
			Instant end = Instant.now();


			logger.info("Total time in {}ms", end.toEpochMilli() - start.toEpochMilli());
			saveTime((int) (end.toEpochMilli() - start.toEpochMilli()));
			return true;
		});
		return future;
	}


	private void saveTime(int timeMilli) {
		try (FileWriter fileWriter = new FileWriter(name + ".txt", true);
		     BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
			bufferedWriter.write(timeMilli + ", ");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Pair<Integer, Integer> getProgress() {
		int completedTasks = (int) results.stream().filter(CompletableFuture::isDone).count();
		return Pair.of(completedTasks, results.size());
	}

	@Override
	public JobType getJobType() {
		return JobType.PROCESSING;
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>(shared.size() + 1);
		if (input != null) {
			dependencies.add(input);
		}
		dependencies.addAll(sharedFilenames());
		return dependencies;
	}


	@Override
	public List<String> getOutputs() {
		return outputs;
	}

	@Override
	public String toString() {
		return "ProcessingJob{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", inputDataSet='" + input + '\'' +
				", sharedDataSets=" + shared +
				", outputDataSets=" + outputs +
				", requirements=" + requirements +
				'}';
	}

	/**
	 * @return the name of the input dataset, or null if the job doesn't have one
	 */
	public String getInput() {
		return input;
	}

	/**
	 * @return the list of file-name mappings for the shared files
	 */
	public List<FileNameMapping> getShared() {
		return shared;
	}

	public List<String> sharedFilenames() {
		return shared.stream().map(FileNameMapping::file).toList();
	}

	/**
	 * @return the name of the image file
	 */
	public String getImage() {
		return image;
	}

	/**
	 * @return the name of the image, without the extension
	 */
	public String imageName() {
		return FilenameUtils.getBaseName(image);
	}


}
