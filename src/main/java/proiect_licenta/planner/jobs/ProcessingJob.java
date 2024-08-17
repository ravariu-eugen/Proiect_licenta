package proiect_licenta.planner.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.ExecutionManager;
import proiect_licenta.planner.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessingJob extends Job {
	private final Logger logger = LogManager.getLogger();

	private final String image;

	private final String input;
	private final List<String> shared;
	private final List<String> output;
	private final Map<String, String> requirements;
	private ExecutionManager executionManager;

	public ProcessingJob(String name, String description, Storage storage,
	                     String image,
	                     String inputDataSet, List<String> sharedDataSets,
	                     List<String> outputDataSets, Map<String, String> requirements) {
		super(name, description, storage);

		this.image = image;

		this.input = inputDataSet;
		this.shared = sharedDataSets;
		this.output = outputDataSets;
		this.requirements = requirements;
	}

	public void setExecutionManager(ExecutionManager executionManager) {
		this.executionManager = executionManager;
	}


	@Override
	public void launch() {
		logger.info("Launching processing job {} with input data set {}", name, input);
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
		List<String> dependencies = new ArrayList<>(shared.size() + 1);
		dependencies.add(input);
		dependencies.addAll(shared);
		return dependencies;
	}

	@Override
	public List<String> getOutputs() {
		return output;
	}

	@Override
	public String toString() {
		return "ProcessingJob{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", inputDataSet='" + input + '\'' +
				", sharedDataSets=" + shared +
				", outputDataSets=" + output +
				", requirements=" + requirements +
				'}';
	}

	public String getInput() {
		return input;
	}


	public List<String> getShared() {
		return shared;
	}

	public String getImage() {
		return image;
	}
}
