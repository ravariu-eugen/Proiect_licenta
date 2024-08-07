package proiect_licenta.planner.execution.worker.load_balancing;

import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.jobs.ProcessingJob;

import java.util.List;

public interface LoadBalancer {

	Worker pickWorker(List<Worker> workers, ProcessingJob job);
}
