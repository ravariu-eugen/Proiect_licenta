package proiect_licenta.planner.execution.worker;

import proiect_licenta.planner.dataset.TaskData;
import proiect_licenta.planner.jobs.ProcessingJob;
import proiect_licenta.planner.task.TaskResult;

import java.util.List;


public interface Worker {


	/**
	 * @return the id of this worker
	 */
	String getID();


	/**
	 * sends a task to be executed
	 *
	 * @param job  the job to be sent
	 * @param data the task to be sent
	 */
	void sendTask(ProcessingJob job, TaskData data);

	WorkerState getState();

	TaskResult getResult(String job, String task);

	/**
	 * @return the status of the worker
	 */
	WorkerMetrics getStatus();


	String toString();


	List<ProcessingJob> assignedJobs();


	List<String> getActiveTasks();

}
