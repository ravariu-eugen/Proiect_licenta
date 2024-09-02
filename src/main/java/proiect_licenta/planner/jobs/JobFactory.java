package proiect_licenta.planner.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.jobs.requirements.FileNameMapping;
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

	private static FileNameMapping extractFileNameMapping(Map map) {
		String file = (String) map.get("file");
		if (file == null) {
			throw new IllegalArgumentException("no file field");
		}
		String name = (String) map.get("name");
		if (name == null) {
			throw new IllegalArgumentException("no name field");
		}
		return new FileNameMapping(file, name);
	}

	private static FileNameMapping extractFileNameMapping(String filename) {
		String name = FilenameUtils.getBaseName(filename);
		return new FileNameMapping(filename, name);
	}

	public Job apply(String jobJson) {
		// TODO: refactor so that processing job can have no inputs and so that shared files can have a name mapping
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
		return switch (req) {
			case null -> Collections.emptyList();
			case String s -> Collections.singletonList(s);
			case List list -> {
				List<String> reqList = new ArrayList<>();
				for (Object obj : list) {
					if (obj instanceof String) {
						reqList.add((String) obj);
					} else {
						throw new IllegalArgumentException("Job " + key + " must contain only strings");
					}
				}
				yield Collections.unmodifiableList(reqList);
			}
			default -> throw new IllegalArgumentException("Job " + key + " must be a string or a list of strings");
		};
	}

	private List<FileNameMapping> parseSharedReq(Map<String, Object> jobMap) {
		Object req = jobMap.get("shared");
		return switch (req) {
			case null -> Collections.emptyList();
			case String filename -> Collections.singletonList(extractFileNameMapping(filename));
			case Map map -> Collections.singletonList(extractFileNameMapping(map));

			case List list -> list.stream()
					.map(obj -> switch (obj) {
						case String filename -> extractFileNameMapping(filename);
						case Map map -> extractFileNameMapping(map);
						default -> throw new IllegalStateException("Unexpected value: " + obj);
					}).toList();
			default -> throw new IllegalArgumentException("Job shared must be a list of maps");
		};
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
				parseSharedReq(jobMap),
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


		//logger.info("{} job", type);
		return switch (type.toLowerCase()) {
			case "compute" -> computeJob(name, description, req, jobMap);
			case "copy" -> copyJob(name, description, req);
			case "rename" -> renameJob(name, description, req);
			case "delete" -> deleteJob(name, description, req);
			default -> throw new IllegalArgumentException("Unknown job type: " + type);
		};
	}


	public @NotNull ComputeJob computeJob(String name, String description, JobRequirements requirements, @NotNull Map<String, Object> jobMap) {


		String image = parseLine(jobMap, "image");
		if (image == null) throw new IllegalArgumentException("Image cannot be null");

		Map<String, String> otherRequirements = (Map<String, String>) jobMap.get("requirements");
		if (otherRequirements == null) otherRequirements = Collections.emptyMap();

		String inputDataSet = requirements.inputs().isEmpty() ? null : requirements.inputs().getFirst();
		List<FileNameMapping> sharedDataSets = requirements.shared();
		List<String> outputDataSets = requirements.outputs();
		if (outputDataSets.isEmpty())
			throw new IllegalArgumentException("Output data set missing");

		return new ComputeJob(name, description, storage,
				image,
				inputDataSet,
				sharedDataSets,
				outputDataSets,
				otherRequirements);
	}

	public @NotNull CopyJob copyJob(String name, String description, JobRequirements requirements) {


		if (requirements.inputs().size() > 1)
			throw new IllegalArgumentException("Input data set must at most a single file");
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
