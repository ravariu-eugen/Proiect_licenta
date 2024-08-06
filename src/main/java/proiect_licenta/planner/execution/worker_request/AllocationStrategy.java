package proiect_licenta.planner.execution.worker_request;

import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.jobs.ProcessingJob;

import java.util.List;

public interface AllocationStrategy {

	Worker pickWorker(List<Worker> workers, ProcessingJob job);
}
