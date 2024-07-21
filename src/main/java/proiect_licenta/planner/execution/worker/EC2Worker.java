package proiect_licenta.planner.execution.worker;

import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.dataset.*;
import proiect_licenta.planner.execution.Worker;
import proiect_licenta.planner.execution.WorkerManager;
import proiect_licenta.planner.execution.WorkerState;
import proiect_licenta.planner.execution.WorkerStatus;
import proiect_licenta.planner.jobs.ProcessingJob;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EC2Worker implements Worker {
	private static final Logger logger = LogManager.getLogger();
	private final String instanceURL;
	private final Instance instance;
	private final OkHttpClient client = new OkHttpClient();


	private WorkerState state = WorkerState.ACTIVE;

	public EC2Worker(Instance instance) {
		this.instance = instance;
		instanceURL = "http://" + instance.publicIpAddress() + ":8080";
	}

	@Override
	public WorkerManager getManager() {
		return null;
	}

	@Override
	public int getID() {
		return 0;
	}

	public void setUpJob(ProcessingJob job) {

		Request request = new Request.Builder()
				.url(instanceURL).get()
				.build();

		try {
			Response response = client.newCall(request).execute();

			logger.info(response.body().string());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private record JobTask(String job, String task){
	}


	private Map<JobTask, TaskResult> results = new HashMap<>();


	@Override
	public void sendTask(String jobName, TaskData taskData) {

		HttpUrl url = HttpUrl.parse(instanceURL).newBuilder()
				.addPathSegment("task")
				.addQueryParameter("job", jobName)
				.addQueryParameter("task", taskData.name())
				.build();

		Request request = new Request.Builder()
				.url(url)
				.post(RequestBody.create(taskData.data(), MediaType.get("application/octet-stream")))
				.build();

		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

				results.put(new JobTask(jobName, taskData.name()), new TaskPending());
			}

			@Override
			public void onFailure(Call call, IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public TaskComplete getResult(TaskID task) {
		return null;
	}

	@Override
	public WorkerStatus getStatus() {
		return new WorkerStatus(state);
	}

}
