package proiect_licenta.planner.execution;

import proiect_licenta.planner.execution.worker.Worker;

import java.util.List;

public interface WorkerManager {


	/**
	 * attempts to create a number of workers
	 * @param count the numbers of workers to create
	 * @return the created workers
	 */
	List<Worker> createWorkers(int count);


	/**
	 *
	 * @return
	 */
	List<Worker> getWorkers();


	void close();
}
