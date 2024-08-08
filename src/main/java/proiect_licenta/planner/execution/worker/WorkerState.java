package proiect_licenta.planner.execution.worker;

public enum WorkerState {
	ACTIVE, // can receive tasks
	TERMINATED, // the worker has terminated
	STOPPING // the worker no longer accepts tasks
}
