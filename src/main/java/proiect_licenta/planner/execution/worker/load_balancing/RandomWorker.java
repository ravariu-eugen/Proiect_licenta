package proiect_licenta.planner.execution.worker.load_balancing;

import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.jobs.ComputeJob;

import java.util.List;
import java.util.Random;

public class RandomWorker implements LoadBalancer {
	@Override
	public Worker pickWorker(List<Worker> workers, ComputeJob job) {
		if (workers.isEmpty()) {
			throw new RuntimeException("No workers available");
		}
		Random random = new Random();
		return workers.get(random.nextInt(workers.size()));
	}
}
