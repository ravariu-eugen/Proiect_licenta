package proiect_licenta.planner.jobs;

import proiect_licenta.planner.storage.Storage;

import java.util.List;
import java.util.StringJoiner;

public class RenameJob extends Job {
	private final String inputDataSet;
	private final String outputDataSet;
	public RenameJob(String name, String description, Storage storage, String inputDataSet, String outputDataSet) {
		super(name, description, storage);
		this.inputDataSet = inputDataSet;
		this.outputDataSet = outputDataSet;
	}


	@Override
	public JobType getJobType() {
		return JobType.RENAME;
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
		storage.rename(inputDataSet, outputDataSet);
	}

	@Override
	public void waitUntilFinished() {

	}

	@Override
	public String toString() {
		return new StringJoiner(", ", RenameJob.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("description='" + description + "'")
				.add("inputDataSet='" + inputDataSet + "'")
				.add("outputDataSet='" + outputDataSet + "'")
				.toString();
	}
}
