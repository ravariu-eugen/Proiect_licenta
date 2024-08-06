package proiect_licenta.planner.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.dataset.TaskData;
import proiect_licenta.planner.execution.worker.WorkerPool;
import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.execution.worker.WorkerState;
import proiect_licenta.planner.jobs.ProcessingJob;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Task {
	private static final Logger logger = LogManager.getLogger();

	private final TaskData taskData;
	private final ProcessingJob job;
	private final WorkerPool workerPool;

	private Worker worker;


	public Task(TaskData data, ProcessingJob job, WorkerPool workerPool) {
		this.taskData = data;
		this.job = job;
		this.workerPool = workerPool;
	}

	private TaskResult pollWorker() {
		return worker.getResult(job.getName(), taskData.name());
	}

	private void aquireWorker() {
		worker = workerPool.requestWorker(job).join();
	}

	public CompletableFuture<TaskResult> run() {

		aquireWorker();
		logger.info("Task {} running on worker {}", taskData.name(), worker.toString());
		worker.sendTask(job, taskData);

		int time = 100;


		CompletableFuture<TaskResult> future = new CompletableFuture<>();
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

		executor.scheduleWithFixedDelay(() -> {
			logger.debug("Polling worker {} for task {}", worker.toString(), taskData.name());
			if (worker.getState() == WorkerState.TERMINATED) {
				logger.info("Worker {} terminated", worker.toString());
				aquireWorker();
				logger.info("Task {} running on worker {}", taskData.name(), worker.toString());
				worker.sendTask(job, taskData);
			}
			TaskResult res = pollWorker();

			if (!(res instanceof TaskPending)) {
				future.complete(res);
				logger.info("Task {} completed", taskData.name());
				executor.shutdown();
			}
		}, 0, time, TimeUnit.MILLISECONDS);

		future.whenComplete((t, e) -> executor.shutdown());

		return future;
//


	}
}
