package proiect_licenta.planner.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.storage.StorageInterface;

import java.util.*;

public class JobList {
	private static Logger logger = LogManager.getLogger();


	public enum JobStatus {
		PENDING,
		RUNNING,
		SUCCEEDED,
		FAILED

	}
	private StorageInterface storage;
	private static class JobWrapper {
		public Job job;
		public JobStatus status;
		public List<JobWrapper> dependencies;

		public JobWrapper(Job job) {
			this.job = job;
			this.status = JobStatus.PENDING;
			this.dependencies = List.of();
		}

	}

	private List<JobWrapper> jobs;

	private JobList(List<Job> jobs) {
		this.jobs = jobs.stream().map(JobWrapper::new).toList();
	}

	public List<Job> getJobs() {
		return jobs.stream().map(j -> j.job).toList();
	}

	private static List<Job.JobType> deleteJobTypes = List.of(Job.JobType.DELETE, Job.JobType.MERGE);
	public boolean readAfterDelete() {


		for (int i = 1; i < jobs.size(); i++) {
			for (int j = 0; j < i; j++) {
				Job current = jobs.get(i).job;
				Job previous = jobs.get(j).job;
				Set<String> currentDependencies = new HashSet<>(current.getDependencies());
				Set<String> previousOutputs = new HashSet<>(previous.getOutputs());

				if(
					deleteJobTypes.contains(current.getJobType()) &&
					currentDependencies.containsAll(previousOutputs)
				)
					return true;


			}
		}
		return false;
	}

	public void launch() {
		logger.info("Launching jobs");
		// check for read after delete
		// TODO
	}


	public static JobList createJobList(String jobListJSON) {
		// TODO

		ObjectMapper mapper = new ObjectMapper();
		List<Object> list = null;
		try {
			Map<String, Object> result = mapper.readValue(jobListJSON, HashMap.class);
			list = (List<Object>) result.get("jobs");
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		if (list == null) {
			throw new IllegalArgumentException("Job list cannot be null");
		}

		List<Job> jobs = list.stream().map(o -> {

			Map<String, Object> jobMap = (Map<String, Object>) o;
			return Job.jobFactory(jobMap);
		}).toList();
		return new JobList(jobs);
	}
}
// TODO
// each job needs a status and a list of dependencies based on the dataset names