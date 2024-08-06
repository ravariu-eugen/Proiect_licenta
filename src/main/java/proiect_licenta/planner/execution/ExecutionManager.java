package proiect_licenta.planner.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.archive.ArchiveManager;
import proiect_licenta.planner.archive.ZipManager;
import proiect_licenta.planner.dataset.Dataset;
import proiect_licenta.planner.execution.analysis.InstanceConfiguration;
import proiect_licenta.planner.execution.analysis.MarketAnalyzer;
import proiect_licenta.planner.execution.ec2_instance.EC2InstanceManager;
import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.helper.AmiMap;
import proiect_licenta.planner.jobs.ProcessingJob;
import proiect_licenta.planner.storage.Storage;
import proiect_licenta.planner.task.Task;
import proiect_licenta.planner.task.TaskComplete;
import proiect_licenta.planner.task.TaskError;
import proiect_licenta.planner.task.TaskResult;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.InstanceType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;


public class ExecutionManager {
	private static final Logger logger = LogManager.getLogger();
	private final WorkerPool workerPool = new WorkerPool();

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


		List<Task> tasks = inputDataset.getTasks().stream()
				.map(taskData -> new Task(taskData, job, workerPool))
				.toList();


		List<CompletableFuture<TaskResult>> results = tasks.stream()
				.map(Task::run)
				.toList();


		List<TaskResult> taskResults = results.stream()
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
