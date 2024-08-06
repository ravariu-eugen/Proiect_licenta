package proiect_licenta.planner.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.storage.Storage;
import proiect_licenta.planner.execution.ExecutionManager;

import java.util.*;

public class JobList {
	private static final Logger logger = LogManager.getLogger();
	private ExecutionManager executionManager;

	public enum JobStatus {
		PENDING,
		RUNNING,
		SUCCEEDED,
		FAILED

	}
	private Storage storage;
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

	public boolean readAfterDelete() {
		var deleteJobTypes = List.of(Job.JobType.DELETE, Job.JobType.MERGE);

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

		executionManager = new ExecutionManager();
		logger.info("Launching jobs");
		// check for read after delete
		// TODO add dependency check
		for (Job job : getJobs()) {
			if (job instanceof ProcessingJob processingJob) {
				processingJob.setExecutionManager(executionManager);
			}
			job.launch();
			job.waitUntilFinished();
		}
		executionManager.close();
	}


	public static JobList createJobList(String jobListJSON, Storage storage) {
		// TODO

		ObjectMapper mapper = new ObjectMapper();
		List<Object> list = null;
		try {
			var result = mapper.readValue(jobListJSON, HashMap.class);
			list = (List<Object>) result.get("jobs");
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		if (list == null) {
			throw new IllegalArgumentException("Job list cannot be null");
		}


		JobFactory jobFactory = new JobFactory(storage);
		List<Job> jobs = list.stream()
				.map(o -> (Map<String, Object>) o)
				.map(jobFactory::apply)
				.toList();

		return new JobList(jobs);
	}
}
// TODO
// each job needs a status and a list of dependencies based on the dataset names