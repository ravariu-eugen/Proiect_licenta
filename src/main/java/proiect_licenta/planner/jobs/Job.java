package proiect_licenta.planner.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		String name = (String) jobMap.get("name");
		String description = (String) jobMap.get("description");
		String type = (String) jobMap.get("type");

		if (type.equalsIgnoreCase("processing")) {

			String inputDataSet = (String) jobMap.get("inputDataSet");
			List<String> sharedDataSets = (List<String>) jobMap.get("sharedDataSets");
			if (sharedDataSets == null) {
				sharedDataSets = new ArrayList<>();
			}
			List<String> outputDataSets = (List<String>) jobMap.get("outputDataSets");
			if (outputDataSets == null) {
				outputDataSets = new ArrayList<>();
			}
			Map<String, String> requirements = (Map<String, String>) jobMap.get("requirements");
			if (requirements == null) {
				requirements = new HashMap<>();
			}
			return new ProcessingJob(name, description, inputDataSet, sharedDataSets, outputDataSets, requirements);
		} else if (type.equalsIgnoreCase("manipulation")) {

		}
		return null;
	}
}
