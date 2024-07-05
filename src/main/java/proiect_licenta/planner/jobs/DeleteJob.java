package proiect_licenta.planner.jobs;

import java.util.List;

public class DeleteJob extends Job {
	private final String inputDataSet;
	public DeleteJob(String name, String description, String inputDataSet) {
		super(name, description);

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
}
