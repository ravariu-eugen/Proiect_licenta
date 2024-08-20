package proiect_licenta.planner.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.dataset.Dataset;
import proiect_licenta.planner.dataset.StorageDataset;
import proiect_licenta.planner.execution.worker.WorkerPool;
import proiect_licenta.planner.helper.AmiMap;
import proiect_licenta.planner.jobs.ComputeJob;
import proiect_licenta.planner.task.Task;
import proiect_licenta.planner.task.TaskResult;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class ExecutionManager {
	private static final Logger logger = LogManager.getLogger();
	private final WorkerPool workerPool;

	// create thread to analyse the market and choose the cheapest region, then keep analysing


	public ExecutionManager() {
		logger.info("Starting execution manager");
		workerPool = new WorkerPool(AmiMap.getRegions(), 20);
	}

	public static String printStatus(List<Boolean> status) {
		return status.stream()
				.map(aBoolean -> aBoolean ? "■" : "□")
				.collect(Collectors.joining()) + " " + status.stream()
				.map(aBoolean -> aBoolean ? 1 : 0)
				.reduce(0, Integer::sum) + "/" + status.size();
	}

	public void launch(ComputeJob job) {


		// load job dataset
		Instant start = Instant.now();
		logger.info("Launching processing job {}", job.getName());
		Dataset inputDataset = null;
		try {
			inputDataset = new StorageDataset(job.getStorage(), job.getInput());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		logger.info("{} tasks to be sent", inputDataset.getTasks().size());
		Instant datasetLoaded = Instant.now();

		List<Task> tasks = inputDataset.getTasks().parallelStream()
				.map(taskData -> new Task(taskData, job, workerPool))
				.toList();
		logger.info("Sending {} tasks", tasks.size());

		List<CompletableFuture<TaskResult>> results = tasks.parallelStream()
				.map(Task::run)
				.toList();

		AtomicReference<List<Boolean>> results2 = new AtomicReference<>(results.stream().map(CompletableFuture::isDone).toList());


		results.forEach(future -> future.whenComplete((x, y) -> {
			results2.set(results.stream().map(CompletableFuture::isDone).toList());
			logger.info("Job {} Results: {}", job.getName(), printStatus(results2.get()));
		}));
		Instant tasksSent = Instant.now();
		logger.info("Waiting for {} tasks", results.size());
		List<TaskResult> taskResults = results.parallelStream()
				.map(CompletableFuture::join)
				.toList();
		logger.info("Finished processing {} tasks", taskResults.size());
		Instant tasksFinished = Instant.now();
		OutputAggregator aggregator = new OutputAggregator();
		aggregator.mergeResults(taskResults, job.getStorage(), job.getOutputs());
		logger.info("Finished processing job {}", job.getName());
		Instant end = Instant.now();


		logger.info("Dataset loaded in {}ms", datasetLoaded.toEpochMilli() - start.toEpochMilli());
		logger.info("Tasks sent in {}ms", tasksSent.toEpochMilli() - datasetLoaded.toEpochMilli());
		logger.info("Tasks finished in {}ms", tasksFinished.toEpochMilli() - tasksSent.toEpochMilli());
		logger.info("Total time in {}ms", end.toEpochMilli() - start.toEpochMilli());
	}


	public void close() {

		workerPool.close();
	}
}
