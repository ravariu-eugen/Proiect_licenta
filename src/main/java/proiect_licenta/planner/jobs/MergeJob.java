package proiect_licenta.planner.jobs;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class MergeJob extends Job {
	private final List<String> inputDataSets;
	private final String outputDataSet;
	public MergeJob(String name, String description, List<String> inputDataSets, String outputDataSet) {
		super(name, description);
		this.inputDataSets = inputDataSets;
		this.outputDataSet = outputDataSet;
	}

	public static @NotNull MergeJob builder(@NotNull Map<String, Object> jobMap) {
		String name = (String) jobMap.get("name");
		String description = (String) jobMap.get("description");
		List<String> inputDataSets = (List<String>) jobMap.get("inputDataSets");
		String outputDataSet = (String) jobMap.get("outputDataSet");
		return new MergeJob(name, description, inputDataSets, outputDataSet);
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
