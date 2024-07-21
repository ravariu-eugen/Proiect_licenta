package proiect_licenta.planner.execution;

import proiect_licenta.planner.dataset.TaskData;
import proiect_licenta.planner.dataset.TaskID;
import proiect_licenta.planner.dataset.TaskComplete;
import proiect_licenta.planner.jobs.ProcessingJob;


public interface Worker {



	/**
	 * @return the manager that created this worker
	 */
	WorkerManager getManager();

	/**
	 * @return the id of this worker
	 */
	int getID();


	/**
	 * prepares a job for execution by loading the required data into the worker's storage
	 * @param job the job to be prepared
	 */
	void setUpJob(ProcessingJob job);


	/**
	 * sends a task to be executed
	 * @param jobName the job to be sent
	 * @param data the task to be sent
	 *
	 */
	void sendTask(String jobName, TaskData data);



	TaskComplete getResult(TaskID task);

	/**
	 * @return the status of the worker
	 */
	WorkerStatus getStatus();





}
