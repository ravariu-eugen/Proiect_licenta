package proiect_licenta.planner.execution.worker;

import proiect_licenta.planner.dataset.TaskData;
import proiect_licenta.planner.jobs.ComputeJob;
import proiect_licenta.planner.task.TaskResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface Worker {


	/**
	 * @return the id of this worker
	 */
	String getID();


	WorkerState getState();


	void terminate();

	CompletableFuture<TaskResult> submitTask(ComputeJob job, TaskData taskData);

	/**
	 * @return the status of the worker
	 */
	WorkerMetrics getStatus();


	String toString();


	List<ComputeJob> assignedJobs();


	List<String> getActiveTasks();

}
