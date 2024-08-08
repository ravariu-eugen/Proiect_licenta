package proiect_licenta.planner.execution.ec2_instance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.dataset.TaskData;
import proiect_licenta.planner.execution.ec2_instance.instance_factory.InstanceWrapper;
import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.execution.worker.WorkerMetrics;
import proiect_licenta.planner.execution.worker.WorkerState;
import proiect_licenta.planner.jobs.ProcessingJob;
import proiect_licenta.planner.task.TaskResult;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EC2Worker implements Worker {
	private static final Logger logger = LogManager.getLogger();
	private static int counter = 0;
	private final EC2InstanceController controller;

	private final List<ProcessingJob> activeJobs = new ArrayList<>();

	private final List<String> activeTasks = new ArrayList<>();
	private final int id = counter++;
	private final ResultMap results = new ResultMap();
	private final Queue<QueueTask> queue = new LinkedList<>();
	private WorkerMetrics status;
	private WorkerState state = WorkerState.ACTIVE;

	public EC2Worker(InstanceWrapper instance) {
		this.controller = new EC2InstanceController(instance);
	}


	@Override
	public String getID() {
		return Integer.toString(id);
	}

	public WorkerState getState() {
		return state;
	}


	public void terminate() {
		state = WorkerState.TERMINATED;
	}


	private void monitor() {
		// check if the job is still active
		logger.info("monitor");
		status = controller.getMetrics();
		logger.info("status: {}", status);
	}


	private void setUpJob(ProcessingJob job) throws IOException {

		synchronized (activeJobs) {
			// check if the job is already active
			if (activeJobs.contains(job)) {
				logger.info("Job {} is already active", job.getName());
				return;
			}
			if (!controller.checkConnection())
				throw new IOException("Could not connect to " + controller.instanceURL());

			// send the files
			controller.uploadImage(job.getStorage(), job.getImage());
			job.getShared().forEach(shared -> controller.uploadShared(job.getStorage(), shared));
			activeJobs.add(job);
		}
	}


	private void startTask(QueueTask qt) {
		// TODO: start the task
		// If the instance can handle it, start the task
		// If the instance can't handle it, keep the task in a queue


		controller.sendTask(qt.jobName(), qt.jobImage(), qt.taskData());
	}


	public CompletableFuture<TaskResult> submitTask(ProcessingJob job, TaskData taskData) {
		CompletableFuture<TaskResult> future = new CompletableFuture<>();
		QueueTask qt = new QueueTask(job, taskData, future);

		// TODO: submit the task into a queue
		// If the instace can handle it, send the task to execution
		// If the instance can't handle it, keep the task in a queue
		queue.add(qt);
		return future;
	}

	@Override
	public WorkerMetrics getStatus() {
		return status;
	}

	@Override
	public List<ProcessingJob> assignedJobs() {
		return List.of();
	}

	@Override
	public List<String> getActiveTasks() {
		return activeTasks;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", EC2Worker.class.getSimpleName() + "[", "]")
				.add("id=" + id)
				.add("state=" + state)
				.toString();
	}

	private record QueueTask(ProcessingJob job, TaskData taskData, CompletableFuture<TaskResult> future) {
		public String jobName() {
			return job.getName();
		}

		public String jobImage() {
			return job.getImage();
		}
	}

	private record ActiveTask(String job, String task, CompletableFuture<TaskResult> future) {
	}

	private static class ResultMap {
		private final Map<JobTask, TaskResult> results = new HashMap<>();

		public void put(String job, String task, TaskResult result) {
			results.put(new JobTask(job, task), result);
		}

		public TaskResult get(String job, String task) {
			return results.get(new JobTask(job, task));
		}

		public boolean contains(String job, String task) {
			return results.containsKey(new JobTask(job, task));
		}

		private record JobTask(String job, String task) {
		}

	}
}
