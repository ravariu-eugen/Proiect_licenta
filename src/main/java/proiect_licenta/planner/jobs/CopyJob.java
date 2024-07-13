package proiect_licenta.planner.jobs;

import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.storage.BucketStorage;
import proiect_licenta.planner.helper.Helper;
import proiect_licenta.planner.storage.StorageInterface;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class CopyJob extends Job {
	private final String inputDataSet;
	private final String outputDataSet;
	private final StorageInterface storage;
	public CopyJob(String name, String description, String inputDataSet, String outputDataSet) {
		super(name, description);
		this.inputDataSet = inputDataSet;
		this.outputDataSet = outputDataSet;

		this.storage = new BucketStorage(Helper.getBucketName());
	}
	public static @NotNull CopyJob builder(@NotNull Map<String, Object> jobMap) {

		String name = (String) jobMap.get("name");
		if (name == null) throw new IllegalArgumentException("Job name cannot be null");

		String description = (String) jobMap.get("description");

		String inputDataSet = (String) jobMap.get("inputDataSet");
		if (inputDataSet == null) throw new IllegalArgumentException("Input data set cannot be null");

		String outputDataSet = (String) jobMap.get("outputDataSet");
		if (outputDataSet == null) throw new IllegalArgumentException("Output data set cannot be null");

		return new CopyJob(name, description, inputDataSet, outputDataSet);
	}
	@Override
	public JobType getJobType() {
		return JobType.COPY;
	}

	@Override
	public List<String> getDependencies() {
		return List.of(inputDataSet);
	}

	@Override
	public List<String> getOutputs() {
		return List.of(outputDataSet);
	}

	@Override
	public void launch() {
		boolean response = storage.copy(inputDataSet, outputDataSet);
	}

	@Override
	public void waitUntilFinished() {

	}

	@Override
	public String toString() {
		return new StringJoiner(", ", CopyJob.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("description='" + description + "'")
				.add("inputDataSet='" + inputDataSet + "'")
				.add("outputDataSet='" + outputDataSet + "'")
				.toString();
	}
}
