package proiect_licenta.planner.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.dataset.TaskData;
import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.execution.worker.WorkerPool;
import proiect_licenta.planner.jobs.ComputeJob;

import java.util.concurrent.CompletableFuture;

public class Task {
	private static final Logger logger = LogManager.getLogger();

	private final TaskData taskData;
	private final ComputeJob job;
	private final WorkerPool workerPool;

	private Worker worker;


	public Task(TaskData data, ComputeJob job, WorkerPool workerPool) {
		this.taskData = data;
		this.job = job;
		this.workerPool = workerPool;
	}

	private void acquireWorker() {
		worker = workerPool.requestWorker(job).join();
	}

	public CompletableFuture<TaskResult> run() {

		return CompletableFuture.supplyAsync(
				() -> {
					while (true) {
						acquireWorker();
						//logger.info("Task {} running on worker {}", taskData.name(), worker.toString());

						// TODO: wait 1
						var result = worker.submitTask(job, taskData).join();

						// worker has been terminated before finishing the task
						if (result != null) {
							//logger.info("Task {} finished on worker {}", taskData.name(), worker.toString());
							return result;
						}
					}
				}
		);
	}
}
