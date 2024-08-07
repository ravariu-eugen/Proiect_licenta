package proiect_licenta.planner.execution.ec2_instance;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.dataset.TaskData;
import proiect_licenta.planner.execution.ec2_instance.instance_factory.InstanceWrapper;
import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.execution.worker.WorkerState;
import proiect_licenta.planner.execution.worker.WorkerStatus;
import proiect_licenta.planner.jobs.ProcessingJob;
import proiect_licenta.planner.storage.Storage;
import proiect_licenta.planner.task.TaskComplete;
import proiect_licenta.planner.task.TaskError;
import proiect_licenta.planner.task.TaskPending;
import proiect_licenta.planner.task.TaskResult;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EC2Worker implements Worker {
	private static final Logger logger = LogManager.getLogger();
	private static int counter = 0;
	
	private final InstanceWrapper instance;
	private final OkHttpClient client = new OkHttpClient.Builder()
			.callTimeout(100, TimeUnit.SECONDS)
			.readTimeout(100, TimeUnit.SECONDS).build();
	private final List<ProcessingJob> activeJobs = new ArrayList<>();
	private final List<String> uploadedFiles = new ArrayList<>();
	private final List<String> activeTasks = new ArrayList<>();
	private final int id = counter++;
	private final ResultMap results = new ResultMap();
	private WorkerStatus status;
	private WorkerState state = WorkerState.ACTIVE;

	public EC2Worker(InstanceWrapper instance) {
		this.instance = instance;
	}

	private static @NotNull RequestBody sendFileBody(String name, byte[] data) {
		return new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("file", name,
						RequestBody.create(data, MediaType.parse("application/octet-stream")))
				.build();
	}

	private static @NotNull RequestBody sendTaskBody(TaskData taskData, String taskName, String jobName, String imageName) {
		return new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("file", taskName + ".zip",
						RequestBody.create(taskData.data(), MediaType.parse("application/octet-stream")))
				.addFormDataPart("job", jobName)
				.addFormDataPart("image", imageName)
				.build();
	}

	public void terminate() {
		state = WorkerState.TERMINATED;
	}

	public WorkerState getState() {
		return state;
	}

	private String instanceURL() {
		int port = 8080;
		return "http://" + instance.publicIpAddress() + ":" + port;
	}

	@Override
	public String getID() {
		return instance.instanceId();
	}

	private boolean tryConnect() {

		Request request = new Request.Builder()
				.url(instanceURL())
				.build();
		int num_retries = 20;
		boolean success = false;
		while (!success && num_retries-- > 0) {
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					success = true;

				}
			} catch (IOException _) {
				logger.debug("Could not connect to {}", instanceURL());
			}
		}
		return success;
	}

	private void monitor() {
		// check if the job is still active
		logger.info("monitor");
//		if (!tryConnect()) {
//			logger.debug("Could not connect to {}", instanceURL());
//			state = WorkerState.TERMINATED;
//			return;
//		}


		Request request = new Request.Builder()
				.url(instanceURL() + "/metrics")
				.build();
		logger.info("monitoring {}", instanceURL());
		try (Response response = client.newCall(request).execute()) {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> json;
			try {
				json = mapper.readValue(response.body().string(), Map.class);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			logger.info("metrics: {}", json);
			double cpuUsage = Double.parseDouble(json.get("cpuUsage"));
			logger.info("cpuUsage: {}", cpuUsage);
			double memoryUsage = Double.parseDouble(json.get("memoryUsage"));
			logger.info("memoryUsage: {}", memoryUsage);
			int remainingStorage = Integer.parseInt(json.get("remainingStorage"));
			logger.info("remainingStorage: {}", remainingStorage);
			status = new WorkerStatus(state, cpuUsage, memoryUsage, remainingStorage);
			logger.info(status);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}

	}

	private void showStatus() {
		monitor();
		logger.info(status);
	}

	private void setUpJob(ProcessingJob job) throws IOException {

		synchronized (activeJobs) {
			// check if the job is already active
			if (activeJobs.contains(job)) {
				logger.info("Job {} is already active", job.getName());
				return;
			}
			if (!tryConnect())
				throw new IOException("Could not connect to " + instanceURL());

			// send the files
			logger.info("set up {} {}", instance.publicIpAddress(), instance.instanceId());
			uploadImage(job.getStorage(), job.getImage());
			job.getShared().forEach(shared -> uploadShared(job.getStorage(), shared));
			activeJobs.add(job);
		}
	}

	private void uploadImage(Storage storage, String image) {
		logger.info("upload image {}", image);
		uploadFile(image, storage, "images");
	}

	private void uploadShared(Storage storage, String shared) {
		logger.info("upload shared {}", shared);
		uploadFile(shared, storage, "shared");
	}

	private void uploadFile(String name, Storage storage, String destination) {

		if (uploadedFiles.contains(name)) {
			logger.info(uploadedFiles);
			logger.info("file {} already uploaded", name);
			return;
		}


		var data = storage.getBytes(name);
		logger.info("upload file {} {} {}", name, destination, data.length);

		Request request = new Request.Builder()
				.url(instanceURL() + "/" + destination)
				.post(sendFileBody(name, data))
				.build();

		logger.info("send request {} {}", name, instanceURL());


		try (Response response = client.newCall(request).execute()) {

			if (response.isSuccessful()) {
				uploadedFiles.add(name);
			}
			logger.debug("{} {}", response.code(), response.body().string());
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}


	}

	public void listUploadedImages() {
		logger.info("list uploaded images");
		Request request = new Request.Builder()
				.url(instanceURL() + "/images")
				.build();
		try (Response response = client.newCall(request).execute()) {
			logger.info(response.body().string());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void listUploadedShared() {
		logger.info("list uploaded shared");
		Request request = new Request.Builder()
				.url(instanceURL() + "/shared")
				.build();
		try (Response response = client.newCall(request).execute()) {
			logger.info(response.body().string());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void sendTask(ProcessingJob job, TaskData taskData) {
		try {
			setUpJob(job);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String jobName = job.getName();
		String taskName = taskData.name();
		logger.info("send task {} {}", jobName, taskData);


		if (results.contains(jobName, taskName)) {
			logger.info("task {} already sent", taskName);
			return;
		}

		String imageName = job.getImage().substring(0, job.getImage().lastIndexOf('.'));

		Request request = new Request.Builder()
				.url(instanceURL() + "/tasks")
				.post(sendTaskBody(taskData, taskName, jobName, imageName))
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful()) {
				logger.debug("{} {}", response.code(), response.body().string());
			} else {
				logger.error("{} {}", response.code(), response.body().string());
			}


		} catch (IOException e) {
			logger.error(e.toString());
			throw new RuntimeException(e);
		}
		activeTasks.add(taskName);
		results.put(jobName, taskName, new TaskPending(taskName));
	}

	@Override
	public TaskResult getResult(String job, String task) {
//		var status = getStatus();
//		logger.info("status {}", status);

		logger.info("get result {} {}", job, task);
		Request request = new Request.Builder()
				.url(instanceURL() + "/tasks/" + job + "/" + task)
				.build();
		showStatus();
		try (Response response = client.newCall(request).execute()) {
			switch (response.code()) {
				case 200:
					logger.info("complete result {} {}", job, task);
					byte[] bytes = response.body().bytes();
					activeTasks.remove(task);
					results.put(job, task, new TaskComplete(task, bytes));
					break;
				case 204:
					logger.info("no result {} {}", job, task);
					break;
				case 404:
					logger.info("not found {} {}", job, task);
					return new TaskError(task, "task not found");
				default:
					throw new RuntimeException(response.body().string());
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		logger.debug("result {} {} {}", job, task, results.get(job, task));
		return results.get(job, task);
	}

	@Override
	public WorkerStatus getStatus() {
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
				.add("instance=" + instance.instanceId())
				.toString();
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
