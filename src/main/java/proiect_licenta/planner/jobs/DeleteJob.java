package proiect_licenta.planner.jobs;

import proiect_licenta.planner.storage.Storage;
import software.amazon.awssdk.utils.Pair;

import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class DeleteJob extends Job {
	private final String inputDataSet;
	private CompletableFuture<Boolean> future;

	public DeleteJob(String name, String description, Storage storage, String inputDataSet) {
		super(name, description, storage);
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

	@Override
	public void launch() {
		future = storage.delete(inputDataSet);
	}

	@Override
	public void waitUntilFinished() {
		future.join();
	}

	@Override
	public Pair<Integer, Integer> getProgress() {
		return Pair.of(future.isDone() ? 1 : 0, 1);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", DeleteJob.class.getSimpleName() + "[", "]")
				.add("name='" + name + "'")
				.add("description='" + description + "'")
				.add("inputDataSet='" + inputDataSet + "'")
				.toString();
	}
}
