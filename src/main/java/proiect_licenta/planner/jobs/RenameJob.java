package proiect_licenta.planner.jobs;

import java.util.List;

public class RenameJob extends Job {
	private String inputDataSet;
	private String outputDataSet;
	public RenameJob(String name, String description) {
		super(name, description);
	}

	@Override
	public JobType getJobType() {
		return null;
	}

	@Override
	public List<String> getDependencies() {
		return List.of(inputDataSet);
	}

	@Override
	public List<String> getOutputs() {
		return List.of(outputDataSet);
	}
}
