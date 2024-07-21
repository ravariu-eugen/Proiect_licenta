package proiect_licenta.planner.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.storage.Storage;


public class JobFactory {
	private static Logger logger = LogManager.getLogger();
	private Storage storage;

	public JobFactory(Storage storage) {
		this.storage = storage;
	}

	public Job apply(String jobJson) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, Object> jobMap = mapper.readValue(jobJson, Map.class);
			return apply(jobMap);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public Job apply(Map<String, Object> jobMap) {

		Map<String, Function<Map<String, Object>, Job>> jobBuilders = Map.of(
				"processing", this::processingJob,
				"copy", this::copyJob,
				"rename", this::renameJob,
				"delete", this::deleteJob,
				"merge", this::mergeJob
		);

		String type = (String) jobMap.get("type");
		if (type == null) {
			logger.error("Job type not specified");
			throw new IllegalArgumentException("Job type not specified");
		}

		Function<Map<String, Object>, Job> jobBuilder = jobBuilders.get(type.toLowerCase());
		if (jobBuilder == null) {
			throw new IllegalArgumentException("Unsupported job type: " + type);
		}

		logger.info("{} job", type);
		return jobBuilder.apply(jobMap);
	}




	public @NotNull ProcessingJob processingJob(@NotNull Map<String, Object> jobMap) {

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

		return new ProcessingJob(name, description, storage, inputDataSet, sharedDataSets, outputDataSets, requirements);
	}

	public @NotNull CopyJob copyJob(@NotNull Map<String, Object> jobMap) {

		String name = (String) jobMap.get("name");
		if (name == null) throw new IllegalArgumentException("Job name cannot be null");

		String description = (String) jobMap.get("description");

		String inputDataSet = (String) jobMap.get("inputDataSet");
		if (inputDataSet == null) throw new IllegalArgumentException("Input data set cannot be null");

		String outputDataSet = (String) jobMap.get("outputDataSet");
		if (outputDataSet == null) throw new IllegalArgumentException("Output data set cannot be null");

		return new CopyJob(name, description, storage, inputDataSet, outputDataSet);
	}

	public @NotNull DeleteJob deleteJob(@NotNull Map<String, Object> jobMap) {

		String name = (String) jobMap.get("name");
		if (name == null) throw new IllegalArgumentException("Job name cannot be null");

		String description = (String) jobMap.get("description");

		String inputDataSet = (String) jobMap.get("inputDataSet");
		if (inputDataSet == null) throw new IllegalArgumentException("Input data set cannot be null");

		return new DeleteJob(name, description, storage, inputDataSet);
	}

	public @NotNull MergeJob mergeJob(@NotNull Map<String, Object> jobMap) {
		String name = (String) jobMap.get("name");
		String description = (String) jobMap.get("description");
		List<String> inputDataSets = (List<String>) jobMap.get("inputDataSets");
		String outputDataSet = (String) jobMap.get("outputDataSet");
		return new MergeJob(name, description, storage, inputDataSets, outputDataSet);
	}

	public @NotNull RenameJob renameJob(@NotNull Map<String, Object> jobMap) {

		String name = (String) jobMap.get("name");
		if (name == null) throw new IllegalArgumentException("Job name cannot be null");

		String description = (String) jobMap.get("description");

		String inputDataSet = (String) jobMap.get("inputDataSet");
		if (inputDataSet == null) throw new IllegalArgumentException("Input data set cannot be null");

		String outputDataSet = (String) jobMap.get("outputDataSet");
		if (outputDataSet == null) throw new IllegalArgumentException("Output data set cannot be null");

		return new RenameJob(name, description, storage, inputDataSet, outputDataSet);
	}


}
