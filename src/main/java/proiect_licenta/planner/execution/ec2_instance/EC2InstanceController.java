package proiect_licenta.planner.execution.ec2_instance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.dataset.TaskData;
import proiect_licenta.planner.execution.ec2_instance.instance_factory.InstanceWrapper;
import proiect_licenta.planner.execution.worker.WorkerMetrics;
import proiect_licenta.planner.jobs.ComputeJob;
import proiect_licenta.planner.jobs.requirements.FileNameMapping;
import proiect_licenta.planner.storage.Storage;
import proiect_licenta.planner.task.TaskComplete;
import proiect_licenta.planner.task.TaskError;
import proiect_licenta.planner.task.TaskPending;
import proiect_licenta.planner.task.TaskResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EC2InstanceController {

	private static final Logger logger = LogManager.getLogger();
	private final InstanceWrapper instance;
	private final List<String> uploadedFiles = new ArrayList<>();
	private final OkHttpClient client = new OkHttpClient.Builder()
			.callTimeout(100, TimeUnit.SECONDS)
			.readTimeout(100, TimeUnit.SECONDS).build();

	public EC2InstanceController(InstanceWrapper instance) {
		this.instance = instance;
	}

	private static @NotNull RequestBody sendFileBody(String name, byte[] data) {
		return new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("file", name,
						RequestBody.create(data, MediaType.parse("application/octet-stream")))
				.build();
	}

	private static @NotNull RequestBody sendTaskBody(TaskData taskData, String jobName, String imageName, List<FileNameMapping> mappings) throws JsonProcessingException {
		return new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("file", taskData.name() + ".zip",
						RequestBody.create(taskData.data(), MediaType.parse("application/octet-stream")))
				.addFormDataPart("job", jobName)
				.addFormDataPart("image", imageName)
				.addFormDataPart("mappings", new ObjectMapper().writeValueAsString(mappings))
				.build();
	}

	public void sendTask(ComputeJob job, TaskData taskData) {
		String jobName = job.getName();
		String imageName = job.imageName();
		List<FileNameMapping> mappings = job.getShared();


		logger.debug("Send task {} {} {}", taskData.name(), job.getName(), job.getImage());
		// send job name, job image and list of shared file-name pairs
		Request request = null;
		try {
			request = new Request.Builder()
					.url(instanceURL() + "/tasks")
					.post(sendTaskBody(taskData, jobName, imageName, mappings))
					.build();
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful()) {
				logger.debug("Send task SUCCESS {} {}", response.code(), response.body().string());
			} else {
				logger.error("Send task ERROR {} {}", response.code(), response.body().string());
			}
		} catch (IOException e) {
			logger.error(e.toString());
			throw new RuntimeException(e);
		}
	}


	public String instanceURL() {
		int port = 8080;
		return "http://" + instance.publicIpAddress() + ":" + port;
	}

	public boolean checkConnection() {

		Request request = new Request.Builder()
				.url(instanceURL())
				.build();
		int num_retries = 20;
		while (num_retries-- > 0) {
			try (Response response = client.newCall(request).execute()) {
				if (response.isSuccessful()) {
					return true;

				}
			} catch (IOException _) {
				logger.debug("Could not connect to {}", instanceURL());
			}
		}
		return false;
	}

	public void uploadImage(Storage storage, String image) {
		//logger.info("upload image {}", image);
		uploadFile(image, storage, "images");
		//logger.info("uploaded image {}", image);
	}

	public void uploadShared(Storage storage, String shared) {
		//logger.info("upload shared {}", shared);
		uploadFile(shared, storage, "shared");
		//logger.info("uploaded shared {}", shared);
	}

	public TaskResult getResult(String jobName, String taskName) {
//		var status = getStatus();
//		logger.info("status {}", status);
		Request request = new Request.Builder()
				.url(instanceURL() + "/tasks/" + jobName + "/" + taskName)
				.build();

		try (Response response = client.newCall(request).execute()) {
			switch (response.code()) {
				case 200:
					logger.debug("complete result {} {}", jobName, taskName);
					byte[] bytes = response.body().bytes();
					return new TaskComplete(taskName, bytes);
				case 204:
					logger.debug("no result {} {}", jobName, taskName);
					return new TaskPending(taskName);
				case 404:
					logger.info("not found {} {}", jobName, taskName);
					return new TaskError(taskName, "task not found");
				default:
					throw new RuntimeException(response.body().string());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public WorkerMetrics getMetrics() {
		Request request = new Request.Builder()
				.url(instanceURL() + "/metrics")
				.build();
		try (Response response = client.newCall(request).execute()) {
			ObjectMapper mapper = new ObjectMapper();
			String responseBody = response.body().string();
			JsonNode jsonNode = mapper.readTree(responseBody);
			double cpuUsage = jsonNode.get("cpuUsage").asDouble();
			double memoryUsage = jsonNode.get("memoryUsage").asDouble();
			double remainingStorage = jsonNode.get("remainingStorage").asDouble();
			return new WorkerMetrics(cpuUsage, memoryUsage, ((int) remainingStorage));
		} catch (IOException e) {
			logger.error("Get metrics error {}", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private void uploadFile(String name, Storage storage, String destination) {

		if (uploadedFiles.contains(name)) {
			logger.info(uploadedFiles);
			logger.info("file {} already uploaded", name);
			return;
		}


		var data = storage.getBytes(name).join();
		logger.debug("upload file {} {} {}", name, destination, data.length);

		Request request = new Request.Builder()
				.url(instanceURL() + "/" + destination)
				.post(sendFileBody(name, data))
				.build();

		logger.debug("send request {} {}", name, instanceURL());


		try (Response response = client.newCall(request).execute()) {

			if (response.isSuccessful()) {
				uploadedFiles.add(name);
			}
			logger.debug("Response {} {}", response.code(), response.body().string());
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
}
