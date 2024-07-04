package proiect_licenta.planner.job_manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public abstract class Job {
	private static final Logger logger = LogManager.getLogger();



	protected String name;
	protected String description;


	public abstract List<String> getDependencies();

	public abstract List<String> getOutputs();


	public static Job jobFactory(String jobJson) {
		ObjectMapper mapper = new ObjectMapper();
		Map jobMap = null;
		try {
			jobMap = mapper.readValue(jobJson, Map.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		logger.info("jobMap: {}", jobMap);
		return null;
	}
}
