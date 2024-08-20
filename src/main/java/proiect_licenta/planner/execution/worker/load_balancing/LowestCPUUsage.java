package proiect_licenta.planner.execution.worker.load_balancing;

import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.jobs.ComputeJob;

import java.util.Comparator;
import java.util.List;

public class LowestCPUUsage implements LoadBalancer {
	@Override
	public Worker pickWorker(List<Worker> workers, ComputeJob job) {
		return workers.stream().min(Comparator.comparingDouble(w -> w.getStatus().cpuUsage())).orElseThrow(() -> new RuntimeException("No workers available"));
	}
}
