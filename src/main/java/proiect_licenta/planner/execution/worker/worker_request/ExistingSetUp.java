package proiect_licenta.planner.execution.worker.worker_request;

import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.jobs.ProcessingJob;

import java.util.List;

public class ExistingSetUp implements AllocationStrategy {
	@Override
	public Worker pickWorker(List<Worker> workers, ProcessingJob job) {
		List<Worker> availableWorkers = workers.stream().filter(w -> w.getStatus().cpuUsage() <= 95 && w.getStatus().memoryUsage() <= 95).toList();



		if (availableWorkers.isEmpty()) {
			throw new RuntimeException("No workers available");
		}

		return availableWorkers.stream()
				.filter(w-> w.assignedJobs().contains(job))
				.findFirst()
				.orElse(availableWorkers.getFirst());
	}
}
