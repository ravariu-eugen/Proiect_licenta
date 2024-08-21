package proiect_licenta.planner.jobs.joblist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.worker.WorkerPool;
import proiect_licenta.planner.helper.AmiMap;
import proiect_licenta.planner.jobs.ComputeJob;
import proiect_licenta.planner.jobs.Job;
import proiect_licenta.planner.jobs.JobFactory;
import proiect_licenta.planner.storage.Storage;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JobList {
	private static final Logger logger = LogManager.getLogger();
	private final List<Job> jobs;
	private final JobGraph jobGraph;

	private JobList(List<Job> jobs) {
		this.jobs = jobs;
		jobGraph = new JobGraph(jobs);
	}

	public static JobList createJobList(String jobListJSON, Storage storage) throws IllegalArgumentException {
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

		var jobList = new JobList(jobs);
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
		List<String> currentFiles = new ArrayList<>(jobs.getFirst().getStorage().listObjects());


		for (Job job : jobs) {

			List<String> dependencies = job.getDependencies();
			for (String dependency : dependencies) {
				if (!currentFiles.contains(dependency)) {
					throw new IllegalArgumentException("Job " + job.getName() + " accesses a file that does not exist: " + dependency);
				}
			}
			if (job.getJobType().removesFile()) {
				currentFiles.removeAll(job.getDependencies());
			}
			var outputs = job.getOutputs();
			for (String output : outputs) {
				if (currentFiles.contains(output)) {
					throw new IllegalArgumentException("Job " + job.getName() + " creates a file that already exists: " + output);
				}
				currentFiles.add(output);
			}
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

		WorkerPool workerPool = new WorkerPool(AmiMap.getRegions(), 20);
		logger.info("Launching jobs");
		// check for read after delete
		// TODO add dependency check
		for (Job job : getJobs()) {
			if (job instanceof ComputeJob computeJob) {
				computeJob.setWorkerPool(workerPool);
			}
		}
		ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
		executor.scheduleAtFixedRate(() -> {
			//


		}, 0, 10, TimeUnit.MILLISECONDS);
		jobGraph.launchJobs();
		workerPool.close();
	}


	static class JobGraph {
		private final Map<Job, JobNode> nodes;

		public JobGraph(List<Job> jobs) {
			nodes = jobs.stream().collect(Collectors.toMap(Function.identity(), JobNode::new));
			for (Job job : jobs) {
				var jobNode = nodes.get(job);
				jobs.forEach(otherJob -> {
					if (job.dependsOn(otherJob)) {
						JobNode otherNode = nodes.get(otherJob);
						jobNode.dependencies.add(otherNode);
					}
				});
			}

			// check for cycles
			if (hasCycle()) {
				throw new IllegalArgumentException("Job graph has cycles");
			}
		}

		private static boolean hasCycleRecursive(JobNode node, Set<JobNode> visited, Set<JobNode> recursionStack) {
			// if node is part of the stack, it means there is a cycle
			if (recursionStack.contains(node)) {
				return true;
			}
			// otherwise, if node is already visited
			if (visited.contains(node)) {
				return false;
			}

			visited.add(node);
			recursionStack.add(node);
			// check all neighbors
			for (JobNode neighbor : node.dependencies) {
				if (hasCycleRecursive(neighbor, visited, recursionStack)) {
					return true;
				}
			}

			recursionStack.remove(node);
			return false;
		}

		public void launchJobs() {
			getNodes().parallelStream().forEach(JobNode::launchJob);
		}

		private List<JobNode> getNodes() {
			return nodes.values().stream().toList();
		}

		public boolean hasCycle() {
			Set<JobNode> visited = new HashSet<>();
			Set<JobNode> recursionStack = new HashSet<>();

			for (JobNode node : getNodes()) {
				if (hasCycleRecursive(node, visited, recursionStack)) {
					return true;
				}
			}

			return false;
		}

		private static class JobNode {
			private final CountDownLatch latch = new CountDownLatch(1);
			private final List<JobNode> dependencies; // the jobs that this job depends on
			private final Job job; // the job to launch

			public JobNode(Job job) {
				this.job = job;
				this.dependencies = new ArrayList<>();
			}

			public void awaitCompletion() throws InterruptedException {
				latch.await();
			}

			private void notifyCompletion() {
				latch.countDown();
			}

			public void launchJob() {

				for (JobNode node : dependencies) {
					try {
						// wait for all dependencies to finish
						node.awaitCompletion();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				job.launch();
				job.waitUntilFinished();
				// notify all dependants
				notifyCompletion();
			}


		}


	}
}
// TODO : each job needs a status and a list of dependencies based on the dataset names