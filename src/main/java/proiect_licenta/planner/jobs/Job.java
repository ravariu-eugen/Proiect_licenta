package proiect_licenta.planner.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.storage.Storage;
import software.amazon.awssdk.utils.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Job {

	private static final Logger logger = LogManager.getLogger();
	protected String name;
	protected String description;
	protected Storage storage;

	public Job(String name, String description, Storage storage) {
		this.name = name;
		this.description = description;
		this.storage = storage;
	}

	public Storage getStorage() {
		return storage;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public abstract JobType getJobType();

	public abstract List<String> getDependencies();

	public abstract List<String> getOutputs();

	public boolean dependsOn(Job other) {
		// the files in this jobs dependencies
		Set<String> dependenciesSet = new HashSet<>(getDependencies());

		// the files in the other jobs outputs
		Set<String> outputsSet = new HashSet<>(other.getOutputs());

		// check if there is any overlap
		return !Collections.disjoint(dependenciesSet, outputsSet);
	}

	public abstract void launch();

	public abstract void waitUntilFinished();

	public abstract Pair<Integer, Integer> getProgress();

	public enum JobType {
		PROCESSING,
		COPY,
		RENAME,
		DELETE,
		MERGE;

		public boolean removesFile() {
			return this == DELETE || this == RENAME;
		}
	}

}
