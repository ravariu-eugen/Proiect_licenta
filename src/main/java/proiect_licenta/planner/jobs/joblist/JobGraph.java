package proiect_licenta.planner.jobs.joblist;

import proiect_licenta.planner.jobs.Job;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

class JobGraph {
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
