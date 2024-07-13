package proiect_licenta.planner.jobs;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class RenameJob extends Job {
	private final String inputDataSet;
	private final String outputDataSet;
	public RenameJob(String name, String description, String inputDataSet, String outputDataSet) {
		super(name, description);
		this.inputDataSet = inputDataSet;
		this.outputDataSet = outputDataSet;
	}

	public static @NotNull RenameJob builder(@NotNull Map<String, Object> jobMap) {

		String name = (String) jobMap.get("name");
		if (name == null) throw new IllegalArgumentException("Job name cannot be null");

		String description = (String) jobMap.get("description");

		String inputDataSet = (String) jobMap.get("inputDataSet");
		if (inputDataSet == null) throw new IllegalArgumentException("Input data set cannot be null");

		String outputDataSet = (String) jobMap.get("outputDataSet");
		if (outputDataSet == null) throw new IllegalArgumentException("Output data set cannot be null");

		return new RenameJob(name, description, inputDataSet, outputDataSet);
	}
	@Override
	public JobType getJobType() {
		return JobType.RENAME;
	}

	@Override
	public List<String> getDependencies() {
		return List.of(inputDataSet);
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
		return new StringJoiner(", ", RenameJob.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("description='" + description + "'")
				.add("inputDataSet='" + inputDataSet + "'")
				.add("outputDataSet='" + outputDataSet + "'")
				.toString();
	}
}
