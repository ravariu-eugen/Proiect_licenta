package proiect_licenta.planner.jobs;

import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.instance_manager.InstanceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProcessingJob extends Job {
	private String inputDataSet;
	private List<String> sharedDataSets;
	private List<String> outputDataSets;

	private final List<InstanceManager> managers;
	private final Map<String, String> requirements;

	public ProcessingJob(String name, String description, String inputDataSet, List<String> sharedDataSets, List<String> outputDataSets, Map<String, String> requirements) {
		super(name, description);

		this.inputDataSet = inputDataSet;
		this.sharedDataSets = sharedDataSets;
		this.outputDataSets = outputDataSets;
		this.requirements = requirements;

		this.managers = new ArrayList<>();
	}

	public static @NotNull ProcessingJob builder(@NotNull Map<String, Object> jobMap) {

		String name = (String) jobMap.get("name");
		if (name == null) throw new IllegalArgumentException("Job name cannot be null");

		String description = (String) jobMap.get("description");

		String inputDataSet = (String) jobMap.get("inputDataSet");
		if (inputDataSet == null) throw new IllegalArgumentException("Input data set cannot be null");

		List<String> sharedDataSets = (List<String>) jobMap.get("sharedDataSets");
		if (sharedDataSets == null) sharedDataSets = Collections.emptyList();

		List<String> outputDataSets = (List<String>) jobMap.get("outputDataSets");
		if (outputDataSets == null) outputDataSets = Collections.emptyList();
		if (outputDataSets.isEmpty()) throw new IllegalArgumentException("Output data sets cannot be empty");

		Map<String, String> requirements = (Map<String, String>) jobMap.get("requirements");
		if (requirements == null) requirements = Collections.emptyMap();

		return new ProcessingJob(name, description, inputDataSet, sharedDataSets, outputDataSets, requirements);
	}


	public void launch() {
		// TODO
	}

	@Override
	public void waitUntilFinished() {

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

	@Override
	public String toString() {
		return "ProcessingJob{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", inputDataSet='" + inputDataSet + '\'' +
				", sharedDataSets=" + sharedDataSets +
				", outputDataSets=" + outputDataSets +
				", managers=" + managers +
				", requirements=" + requirements +
				'}';
	}
}
