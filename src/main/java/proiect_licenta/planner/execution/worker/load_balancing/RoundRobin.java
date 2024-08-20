package proiect_licenta.planner.execution.worker.load_balancing;

import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.jobs.ComputeJob;

import java.util.List;

public class RoundRobin implements LoadBalancer {
	int counter = 0;

	@Override
	public Worker pickWorker(List<Worker> workers, ComputeJob job) {
		return workers.get(counter++ % workers.size());
	}
}
