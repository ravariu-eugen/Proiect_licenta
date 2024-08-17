package proiect_licenta.planner.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.jobs.requirements.JobRequirements;
import proiect_licenta.planner.storage.Storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class JobFactory {
	private static final Logger logger = LogManager.getLogger();
	private final Storage storage;

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


	private List<String> parseReq(Map<String, Object> jobMap, String key) {
		Object req = jobMap.get(key);
		switch (req) {
			case null -> {
				return Collections.emptyList();
			}
			case String s -> {
				return Collections.singletonList(s);
			}
			case List list -> {
				List<String> reqList = new ArrayList<>();
				for (Object obj : list) {
					if (obj instanceof String) {
						reqList.add((String) obj);
					} else {
						throw new IllegalArgumentException("Job " + key + " must contain only strings");
					}
				}
				return Collections.unmodifiableList(reqList);
			}
			default -> throw new IllegalArgumentException("Job " + key + " must be a string or a list of strings");
		}
	}

	private String parseLine(Map<String, Object> jobMap, String key) {
		Object req = jobMap.get(key);
		switch (req) {
			case null -> {
				return null;
			}
			case String s -> {
				return s;
			}
			default -> throw new IllegalArgumentException("Job " + key + " must be a string");
		}
	}

	private JobRequirements extractJobRequirements(Map<String, Object> jobMap) {
		return new JobRequirements(
				parseReq(jobMap, "input"),
				parseReq(jobMap, "shared"),
				parseReq(jobMap, "output")
		);
	}

	public Job apply(Map<String, Object> jobMap) {

		String type = parseLine(jobMap, "type");
		if (type == null) throw new IllegalArgumentException("Job type cannot be null");
		String name = parseLine(jobMap, "name");
		if (name == null) throw new IllegalArgumentException("Job name cannot be null");
		String description = (String) jobMap.get("description");

		JobRequirements req = extractJobRequirements(jobMap);
		if (!req.isValid(storage)) {
			//throw new IllegalArgumentException("Job requirements are not valid");
		}


		//logger.info("{} job", type);
		return switch (type.toLowerCase()) {
			case "processing" -> processingJob(name, description, req, jobMap);
			case "copy" -> copyJob(name, description, req);
			case "rename" -> renameJob(name, description, req);
			case "delete" -> deleteJob(name, description, req);
			case "merge" -> mergeJob(name, description, req);
			default -> throw new IllegalArgumentException("Unknown job type: " + type);
		};
	}


	public @NotNull ProcessingJob processingJob(String name, String description, JobRequirements requirements, @NotNull Map<String, Object> jobMap) {


		String image = parseLine(jobMap, "image");
		if (image == null) throw new IllegalArgumentException("Image cannot be null");

		Map<String, String> otherRequirements = (Map<String, String>) jobMap.get("requirements");
		if (otherRequirements == null) otherRequirements = Collections.emptyMap();


		if (requirements.inputs().size() != 1)
			throw new IllegalArgumentException("Input data set must be a single file");
		String inputDataSet = requirements.inputs().getFirst();
		List<String> sharedDataSets = requirements.shared();
		List<String> outputDataSets = requirements.outputs();
		if (outputDataSets.isEmpty())
			throw new IllegalArgumentException("Output data set missing");

		return new ProcessingJob(name, description, storage,
				image,
				inputDataSet,
				sharedDataSets,
				outputDataSets,
				otherRequirements);
	}

	public @NotNull CopyJob copyJob(String name, String description, JobRequirements requirements) {


		if (requirements.inputs().size() != 1)
			throw new IllegalArgumentException("Input data set must be a single file");
		String inputDataSet = requirements.inputs().getFirst();

		if (requirements.outputs().size() != 1)
			throw new IllegalArgumentException("Output data set must be a single file");
		String outputDataSet = requirements.outputs().getFirst();

		if (!requirements.shared().isEmpty())
			throw new IllegalArgumentException("Shared data sets are not supported for copy jobs");

		return new CopyJob(name, description, storage, inputDataSet, outputDataSet);
	}

	public @NotNull DeleteJob deleteJob(String name, String description, JobRequirements requirements) {

		if (requirements.inputs().size() != 1)
			throw new IllegalArgumentException("Input data set must be a single file");
		String inputDataSet = requirements.inputs().getFirst();

		if (!requirements.outputs().isEmpty())
			throw new IllegalArgumentException("Output data sets are not supported for delete jobs");

		if (!requirements.shared().isEmpty())
			throw new IllegalArgumentException("Shared data sets are not supported for delete jobs");

		return new DeleteJob(name, description, storage, inputDataSet);
	}

	public @NotNull MergeJob mergeJob(String name, String description, JobRequirements requirements) {

		List<String> inputDataSets = requirements.inputs();
		if (inputDataSets.size() <= 2)
			throw new IllegalArgumentException("Input data sets must be at least two files");

		if (requirements.outputs().size() != 1)
			throw new IllegalArgumentException("Output data set must be a single file");
		String outputDataSet = requirements.outputs().getFirst();

		if (!requirements.shared().isEmpty())
			throw new IllegalArgumentException("Shared data sets are not supported for merge jobs");

		return new MergeJob(name, description, storage, inputDataSets, outputDataSet);
	}

	public @NotNull RenameJob renameJob(String name, String description, JobRequirements requirements) {


		if (requirements.inputs().size() != 1)
			throw new IllegalArgumentException("Input data set must be a single file");
		String inputDataSet = requirements.inputs().getFirst();

		if (requirements.outputs().size() != 1)
			throw new IllegalArgumentException("Output data set must be a single file");
		String outputDataSet = requirements.outputs().getFirst();

		if (!requirements.shared().isEmpty())
			throw new IllegalArgumentException("Shared data sets are not supported for rename jobs");

		return new RenameJob(name, description, storage, inputDataSet, outputDataSet);
	}


}
