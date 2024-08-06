package proiect_licenta.planner.execution.worker_request;

import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.jobs.ProcessingJob;

import java.util.List;
import java.util.Random;

public class RandomWorkerStrategy implements AllocationStrategy {
	@Override
	public Worker pickWorker(List<Worker> workers, ProcessingJob job) {
		if (workers.isEmpty()) {
			throw new RuntimeException("No workers available");
		}
		Random random = new Random();
		return workers.get(random.nextInt(workers.size()));
	}
}
