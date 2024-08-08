package proiect_licenta.planner.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.dataset.Dataset;
import proiect_licenta.planner.execution.worker.WorkerPool;
import proiect_licenta.planner.helper.AmiMap;
import proiect_licenta.planner.jobs.ProcessingJob;
import proiect_licenta.planner.task.Task;
import proiect_licenta.planner.task.TaskResult;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ExecutionManager {
	private static final Logger logger = LogManager.getLogger();
	private final WorkerPool workerPool = new WorkerPool(AmiMap.getRegions(), 4);

	// create thread to analyse the market and choose the cheapest region, then keep analysing


	public ExecutionManager() {

	}


	public void launch(ProcessingJob job) {
		logger.info("Launching processing job {}", job.getName());
		Dataset inputDataset = null;
		try {
			inputDataset = new Dataset(job.getStorage(), job.getInput());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		//workers.parallelStream().forEach(worker -> worker.setUpJob(job));
		logger.info("{} tasks to be sent", inputDataset.getTasks().size());


		List<Task> tasks = inputDataset.getTasks().parallelStream()
				.map(taskData -> new Task(taskData, job, workerPool))
				.toList();


		List<CompletableFuture<TaskResult>> results = tasks.parallelStream()
				.map(Task::run)
				.toList();


		List<TaskResult> taskResults = results.parallelStream()
				.map(CompletableFuture::join)
				.toList();

		logger.info("Finished processing {} tasks", taskResults.size());

		OutputAggregator aggregator = new OutputAggregator();
		aggregator.mergeResults(taskResults, job.getStorage(), job.getOutputs());
		logger.info("Finished processing job {}", job.getName());

	}


	public void close() {

		workerPool.close();
	}
}
