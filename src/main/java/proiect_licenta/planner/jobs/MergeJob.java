package proiect_licenta.planner.jobs;

import proiect_licenta.planner.storage.Storage;

import java.util.List;
import java.util.StringJoiner;

public class MergeJob extends Job {
	private final List<String> inputDataSets;
	private final String outputDataSet;
	public MergeJob(String name, String description, Storage storage, List<String> inputDataSets, String outputDataSet) {
		super(name, description, storage);
		this.inputDataSets = inputDataSets;
		this.outputDataSet = outputDataSet;
	}



	@Override
	public JobType getJobType() {
		return JobType.MERGE;
	}

	@Override
	public List<String> getDependencies() {
		return List.copyOf(inputDataSets);
	}

	@Override
	public List<String> getOutputs() {
		return List.of(outputDataSet);
	}

	@Override
	public void launch() {

	}

	@Override
	public void waitUntilFinished() {

	}

	@Override
	public String toString() {
		return new StringJoiner(", ", MergeJob.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("description='" + description + "'")
				.add("inputDataSets=" + inputDataSets)
				.add("outputDataSet='" + outputDataSet + "'")
				.toString();
	}
}
