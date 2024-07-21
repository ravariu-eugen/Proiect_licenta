package proiect_licenta.planner.jobs;

import proiect_licenta.planner.storage.BucketStorage;
import proiect_licenta.planner.helper.Helper;
import proiect_licenta.planner.storage.Storage;

import java.util.List;
import java.util.StringJoiner;

public class CopyJob extends Job {
	private final String inputDataSet;
	private final String outputDataSet;
	private final Storage storage;
	public CopyJob(String name, String description, Storage storage, String inputDataSet, String outputDataSet) {
		super(name, description, storage);
		this.inputDataSet = inputDataSet;
		this.outputDataSet = outputDataSet;

		this.storage = new BucketStorage(Helper.getBucketName());
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
		storage.copy(inputDataSet, outputDataSet);
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
