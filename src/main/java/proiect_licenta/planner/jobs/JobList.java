package proiect_licenta.planner.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.worker.WorkerPool;
import proiect_licenta.planner.helper.AmiMap;
import proiect_licenta.planner.storage.Storage;

import java.util.*;

public class JobList {
	private static final Logger logger = LogManager.getLogger();
	private final List<Job> jobs;
	private final JobGraph jobGraph;
	private final int num_vcpus;

	private JobList(int num_vcpus, List<Job> jobs) {
		this.jobs = jobs;
		this.num_vcpus = num_vcpus;
		jobGraph = new JobGraph(jobs);
	}


	public static JobList createJobList(String jobListJSON, Storage storage) throws IllegalArgumentException {
		ObjectMapper mapper = new ObjectMapper();
		List<Object> list;
		int num_vcpus;
		try {
			var result = mapper.readValue(jobListJSON, HashMap.class);
			list = (List<Object>) result.get("jobs");
			Integer num = (Integer) result.get("vcpu_count");
			if (num != null) {
				num_vcpus = num;
			} else
				num_vcpus = 4;


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

		var jobList = new JobList(num_vcpus, jobs);
		if (!jobList.isValid()) {
			throw new IllegalArgumentException("Job list is not valid");
		}
		return jobList;

	}

	public List<Job> getJobs() {
		return jobs;
	}

	/**
	 * checks if a job accesses a file that does not exist
	 *
	 * @return true if there are any jobs that depend on a file that is being removed
	 */
	public boolean hasInvalidAccess() throws IllegalArgumentException {
		logger.info("Checking if there are any jobs that access a file that does not exist");
		Set<String> currentFiles = new HashSet<>(jobs.getFirst().getStorage().listObjects().join());
		logger.info(currentFiles);

		for (Job job : jobs) {

			List<String> dependencies = job.getDependencies();
			for (String dependency : dependencies) {
				if (!currentFiles.contains(dependency)) {
					throw new IllegalArgumentException("Job " + job.getName() + " accesses a file that does not exist: " + dependency);
				}
			}
			if (job.getJobType().removesFile()) {
				job.getDependencies().forEach(currentFiles::remove);
			}
			currentFiles.addAll(job.getOutputs());
		}
		return false;
	}


	/**
	 * @return true if all job names are unique
	 */
	private boolean uniqueJobNames() {
		logger.info("Checking if all job names are unique");
		return jobs.stream().map(Job::getName).distinct().count() == jobs.size();
	}

	/**
	 * check if the job list is valid
	 *
	 * @return true if the job list is valid
	 */
	public boolean isValid() {
		return uniqueJobNames() && !hasInvalidAccess();
	}

	public void launch() {

		WorkerPool workerPool = new WorkerPool(AmiMap.getRegions(), num_vcpus);
		logger.info("Launching jobs");
		// check for read after delete
		// TODO add dependency check
		for (Job job : getJobs()) {
			if (job instanceof ComputeJob computeJob) {
				computeJob.setWorkerPool(workerPool);
			}
		}
		jobGraph.launchJobs();
		logger.info("Jobs launched");


		workerPool.close();
	}


}
// TODO : each job needs a status and a list of dependencies based on the dataset names