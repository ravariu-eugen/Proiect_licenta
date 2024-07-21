package proiect_licenta.planner.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.storage.Storage;

import java.util.*;

public abstract class Job {

	public enum JobType {
		PROCESSING,
		COPY,
		RENAME,
		DELETE,
		MERGE
	}

	private static final Logger logger = LogManager.getLogger();

	public Job(String name, String description, Storage storage) {
		this.name = name;
		this.description = description;
		this.storage = storage;
	}

	protected String name;
	protected String description;
	protected Storage storage;

	public Storage getStorage(){
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


	public boolean dependsOn(Job job) {
		List<String> dependencies = getDependencies();
		List<String> jobOutputs = job.getOutputs();
		return dependencies.stream().anyMatch(jobOutputs::contains);
	}

	public abstract void launch();

	public abstract void waitUntilFinished();


}
