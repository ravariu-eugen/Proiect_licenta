package proiect_licenta.planner.dataset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.ExecutionManager;
import proiect_licenta.planner.execution.Worker;
import proiect_licenta.planner.jobs.ProcessingJob;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Task {
	private static final Logger logger = LogManager.getLogger();

	private final TaskData taskData;
	private final ProcessingJob job;
	private final ExecutionManager manager;

	private Worker worker;



	private final TaskID taskID = TaskID.getNextID();

	public Task(TaskData data, ProcessingJob job, ExecutionManager manager) {
		this.taskData = data;
		this.job = job;
		this.manager = manager;
	}

	private TaskResult pollWorker(){
		return worker.getResult(taskID);
	}


	private CompletableFuture<TaskResult> run() {
		worker = manager.requestWorker(job);
		worker.sendTask(job.getName(), taskData);

		return CompletableFuture.supplyAsync(() -> {
			while (true) {
				TaskResult result = pollWorker();
				if (result instanceof TaskComplete taskComplete) {
					return taskComplete;
				}
				else if (result instanceof TaskError taskError) {
					throw new RuntimeException(String.valueOf(taskError));
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		});

	}
}
