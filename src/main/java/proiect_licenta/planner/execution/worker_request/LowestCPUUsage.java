package proiect_licenta.planner.execution.worker_request;

import proiect_licenta.planner.execution.worker.EC2Worker;
import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.jobs.ProcessingJob;

import java.util.Comparator;
import java.util.List;

public class LowestCPUUsage implements AllocationStrategy {
	@Override
	public Worker pickWorker(List<Worker> workers, ProcessingJob job) {
		return workers.stream().min(Comparator.comparingDouble(w -> w.getStatus().cpuUsage())).orElseThrow(() -> new RuntimeException("No workers available"));
	}
}
