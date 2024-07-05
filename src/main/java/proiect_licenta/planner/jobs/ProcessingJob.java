package proiect_licenta.planner.jobs;

import proiect_licenta.planner.instance_manager.InstanceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessingJob extends Job {
	private String inputDataSet;
	private List<String> sharedDataSets;
	private List<String> outputDataSets;

	private List<InstanceManager> managers;
	private Map<String, String> requirements;

	public ProcessingJob(String name, String description, String inputDataSets, List<String> sharedDataSets, List<String> outputDataSets, Map<String, String> requirements) {
		super(name, description);

		this.inputDataSet = inputDataSets;
		this.sharedDataSets = sharedDataSets;
		this.outputDataSets = outputDataSets;
		this.managers = new ArrayList<>();
		this.requirements = requirements;
	}


	public void launch() {
		// TODO
	}

	@Override
	public JobType getJobType() {
		return JobType.PROCESSING;
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new ArrayList<>(sharedDataSets.size() + 1);
		dependencies.add(inputDataSet);
		dependencies.addAll(sharedDataSets);
		return dependencies;
	}



	@Override
	public List<String> getOutputs() {
		return outputDataSets;
	}
}
