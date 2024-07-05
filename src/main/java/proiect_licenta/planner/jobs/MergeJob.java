package proiect_licenta.planner.jobs;

import java.util.List;

public class MergeJob extends Job {
	private List<String> inputDataSets;
	private String outputDataSet;
	public MergeJob(String name, String description) {
		super(name, description);
	}

	@Override
	public JobType getJobType() {
		return JobType.MERGE;
	}

	@Override
	public List<String> getDependencies() {
		return inputDataSets;
	}

	@Override
	public List<String> getOutputs() {
		return List.of(outputDataSet);
	}
}
