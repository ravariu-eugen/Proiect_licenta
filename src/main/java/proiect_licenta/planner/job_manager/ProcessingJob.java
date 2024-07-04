package proiect_licenta.planner.job_manager;

import proiect_licenta.planner.instance_manager.InstanceManager;

import java.util.List;

public class ProcessingJob extends Job {
	private String inputDataSet;
	private List<String> sharedDataSets;
	private List<String> outputDataSets;

	private List<InstanceManager> managers;

	public ProcessingJob(String name, String description, String inputDataSets, List<String> sharedDataSets, List<String> outputDataSets) {
		this.name = name;
		this.description = description;
		this.inputDataSet = inputDataSets;
		this.sharedDataSets = sharedDataSets;
		this.outputDataSets = outputDataSets;
	}

	public ProcessingJob(String jobJSON){

	}


	public void launch() {
		// TODO
	}
}
