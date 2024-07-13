package proiect_licenta.planner.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

public abstract class Job {

	public enum JobType {
		PROCESSING,
		COPY,
		RENAME,
		DELETE,
		MERGE
	}

	private static final Logger logger = LogManager.getLogger();

	public Job(String name, String description) {
		this.name = name;
		this.description = description;
	}

	protected String name;
	protected String description;


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

	public static Job jobFactory(String jobJson) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, Object> jobMap = mapper.readValue(jobJson, Map.class);
			return jobFactory(jobMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static Job jobFactory(Map<String, Object> jobMap) {

		Map<String, Function<Map<String, Object>, Job>> jobBuilders = Map.of(
				"processing", ProcessingJob::builder,
				"copy", CopyJob::builder,
				"rename", RenameJob::builder,
				"delete", DeleteJob::builder,
				"merge", MergeJob::builder
		);

		String type = (String) jobMap.get("type");
		if (type == null) {
			throw new IllegalArgumentException("Job type not specified");
		}

		Function<Map<String, Object>, Job> jobBuilder = jobBuilders.get(type.toLowerCase());
		if (jobBuilder == null) {
			throw new IllegalArgumentException("Unsupported job type: " + type);
		}

		logger.info("{} job", type);
		return jobBuilder.apply(jobMap);
	}
}
