package proiect_licenta.planner.jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

class JobGraph {

	private static final Logger logger = LogManager.getLogger();
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

	public void launchJobs() {

		List<JobNode> nodes = getNodes();
		if (nodes.isEmpty()) {
			return;
		}
		// launch jobs in parallel
		nodes.parallelStream().forEach(JobNode::launchJob);


		// wait for all jobs to finish
		logger.info("Waiting for all jobs to finish");
		var results = nodes.stream().map(JobNode::awaitCompletion).toList();
		logger.info("{} jobs finished", results.stream().filter(Boolean::booleanValue).count());
		logger.info("All jobs finished");
	}

	private static class JobNode {
		private final List<JobNode> dependencies; // the jobs that this job depends on
		private final Job job; // the job to launch
		private CompletableFuture<Boolean> future; // the future that completes when the job is finished
		// true if the job is finished, false otherwise

		public JobNode(Job job) {
			this.job = job;
			this.dependencies = new ArrayList<>();
		}

		public boolean awaitCompletion() {
			return future.join();
		}

		public void launchJob() {

			future = CompletableFuture.supplyAsync(() -> {
				logger.info("Launching job {}", job);
				var list = dependencies.parallelStream().map(JobNode::awaitCompletion).toList();
				if (list.contains(false)) {
					logger.error("Failed to launch job {} due to dependencies", job.getName());
					return false;
				}
				return job.launch().exceptionally(e -> {
					logger.error("Failed to launch job {} due to {}", job.getName(), e);
					return false;
				}).join();

			});
		}
	}
}
