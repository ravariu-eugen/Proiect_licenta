package proiect_licenta.planner.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.storage.Storage;
import proiect_licenta.planner.execution.ExecutionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessingJob extends Job {
	private final Logger logger = LogManager.getLogger();

	private final String inputDataSet;
	private final List<String> sharedDataSets;
	private final List<String> outputDataSets;
	private final Map<String, String> requirements;
	private ExecutionManager executionManager;

	public ProcessingJob(String name, String description, Storage storage,
	                     String inputDataSet, List<String> sharedDataSets,
	                     List<String> outputDataSets, Map<String, String> requirements) {
		super(name, description, storage);

		this.inputDataSet = inputDataSet;
		this.sharedDataSets = sharedDataSets;
		this.outputDataSets = outputDataSets;
		this.requirements = requirements;
	}

	public void setExecutionManager(ExecutionManager executionManager) {
		this.executionManager = executionManager;
	}




	@Override
	public void launch() {
		// TODO
		logger.info("Launching processing job {} with input data set {}", name, inputDataSet);
		logger.info(storage.listObjects());
		executionManager.launch(this);
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
				", requirements=" + requirements +
				'}';
	}

	public String getInputDataSet() {
		return inputDataSet;
	}
}
