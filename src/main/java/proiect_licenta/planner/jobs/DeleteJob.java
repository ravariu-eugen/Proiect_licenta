package proiect_licenta.planner.jobs;

import proiect_licenta.planner.storage.Storage;

import java.util.List;
import java.util.StringJoiner;

public class DeleteJob extends Job {
	private final String inputDataSet;
	public DeleteJob(String name, String description, Storage storage, String inputDataSet) {
		super(name, description, storage);
		this.inputDataSet = inputDataSet;
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

	@Override
	public void launch() {
		storage.delete(inputDataSet);
	}

	@Override
	public void waitUntilFinished() {
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
