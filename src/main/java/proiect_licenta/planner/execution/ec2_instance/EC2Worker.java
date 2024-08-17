package proiect_licenta.planner.execution.ec2_instance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.dataset.TaskData;
import proiect_licenta.planner.execution.ec2_instance.instance_factory.InstanceWrapper;
import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.execution.worker.WorkerMetrics;
import proiect_licenta.planner.execution.worker.WorkerState;
import proiect_licenta.planner.jobs.ProcessingJob;
import proiect_licenta.planner.task.TaskPending;
import proiect_licenta.planner.task.TaskResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.*;

public class EC2Worker implements Worker {
	private static final Logger logger = LogManager.getLogger();
	private static final int period = 100;
	private static final int cpuThreshold = 95;
	private static final int memoryThreshold = 95;
	private static int counter = 0;


	private final EC2InstanceController controller;
	private final List<ProcessingJob> activeJobs = new ArrayList<>();
	private final List<String> activeTasks = new ArrayList<>();
	private final int id = counter++;
	private final BlockingQueue<QueueTask> queue = new LinkedBlockingQueue<>();
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(16);
	private final Future<?> queueLoopFuture;
	private WorkerMetrics status;
	private WorkerState state = WorkerState.ACTIVE;

	public EC2Worker(InstanceWrapper instance) {
		this.controller = new EC2InstanceController(instance);


//		if (!controller.checkConnection()) {
//			logger.error("Instance {} is not reachable", instance.instanceId());
//			state = WorkerState.TERMINATED;
//			throw new RuntimeException("Instance is not reachable");
//		}
		logger.info("Instance {} is reachable", instance.instanceId());

		queueLoopFuture = executor.submit(this::queueLoop);


	}


	@Override
	public String getID() {
		return Integer.toString(id);
	}

	public WorkerState getState() {
		return state;
	}


	@Override
	public void terminate() {
		state = WorkerState.TERMINATED;
		queueLoopFuture.cancel(true);
		executor.shutdown();
	}


	private void queueLoop() {
		// check if the job is still active
		while (state == WorkerState.ACTIVE) {
			try {
				processQueue();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}


	private void setUpJob(ProcessingJob job) {

		synchronized (job) {
			// check if the job is already active
			if (activeJobs.contains(job)) {
				logger.debug("Job {} is already active", job.getName());
				return;
			}


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
		logger.info("start task {}-{}", qt.jobName(), qt.taskData().name());
		setUpJob(qt.job());
		controller.sendTask(qt.jobImage(), qt.jobName(), qt.taskData());
		//logger.info("task {}-{} started", qt.jobName(), qt.taskData().name());

		ScheduledFuture<?> scheduledFuture = executor.scheduleWithFixedDelay(() -> {
			//logger.info("Monitor task {}-{}", qt.jobName(), qt.taskData().name());
			if (getState() == WorkerState.TERMINATED) {
				logger.info("Worker {} terminated", toString());
				qt.future().complete(null);
			}
			TaskResult res = controller.getResult(qt.jobName(), qt.taskData().name());

			if (!(res instanceof TaskPending)) {
				// task is completed or error
				qt.future().complete(res);
				logger.info("Task {}-{} completed", qt.jobName(), qt.taskName());
			}
		}, 1, period, TimeUnit.MILLISECONDS);
		qt.future().whenComplete((res, t) -> scheduledFuture.cancel(false));
	}

	private void processQueue() throws InterruptedException {


		logger.info("Process queue");
		// check if the instance can handle the job
		// if yes, start the task
		// if no, keep the task in the queue
		if (!controller.checkConnection())
			throw new RuntimeException("Could not connect to " + controller.instanceURL());
		var metrics = controller.getMetrics();
		logger.info("Metrics: {}", metrics);

		if (metrics.cpuUsage() > cpuThreshold || metrics.memoryUsage() > memoryThreshold) {
			logger.info("Instance is overloaded: CPU: {}%, Memory: {}%", metrics.cpuUsage(), metrics.memoryUsage());
			return;
		}

		QueueTask qt = queue.take();
		logger.info("Take task {}-{} from queue", qt.jobName(), qt.taskData().name());
		executor.schedule(() -> startTask(qt), 0, TimeUnit.MILLISECONDS);
	}

	@Override
	public CompletableFuture<TaskResult> submitTask(ProcessingJob job, TaskData taskData) {
		CompletableFuture<TaskResult> future = new CompletableFuture<>();
		QueueTask qt = new QueueTask(job, taskData, future);

		// submit the task into a queue
		queue.add(qt);
		logger.info("Added task {}-{} to queue", qt.jobName(), qt.taskName());
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
		return Collections.unmodifiableList(activeTasks);
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
			return job.getImage().substring(0, job.getImage().lastIndexOf("."));
		}


		public String taskName() {
			return taskData.name();
		}
	}
}
