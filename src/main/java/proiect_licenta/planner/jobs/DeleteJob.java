package proiect_licenta.planner.jobs;

import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.storage.BucketStorage;
import proiect_licenta.planner.helper.Helper;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DeleteJob extends Job {
	private final String inputDataSet;
	private final BucketStorage bucketManager;
	public DeleteJob(String name, String description, String inputDataSet) {
		super(name, description);
		this.inputDataSet = inputDataSet;
		this.bucketManager = new BucketStorage(Helper.getBucketName());
	}

	public static @NotNull DeleteJob builder(@NotNull Map<String, Object> jobMap) {

		String name = (String) jobMap.get("name");
		if (name == null) throw new IllegalArgumentException("Job name cannot be null");

		String description = (String) jobMap.get("description");

		String inputDataSet = (String) jobMap.get("inputDataSet");
		if (inputDataSet == null) throw new IllegalArgumentException("Input data set cannot be null");

		return new DeleteJob(name, description, inputDataSet);
	}
	@Override
	public JobType getJobType() {
		return JobType.DELETE;
	}

	@Override
	public List<String> getDependencies() {
		return List.of(inputDataSet);
	}

	@Override
	public List<String> getOutputs() {
		return List.of();
	}
	private Future<Boolean> future;
	@Override
	public void launch() {

		future = bucketManager.deleteObject(inputDataSet);
	}

	@Override
	public void waitUntilFinished() {
		try {
			future.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", DeleteJob.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("description='" + description + "'")
				.add("inputDataSet='" + inputDataSet + "'")
				.toString();
	}
}
