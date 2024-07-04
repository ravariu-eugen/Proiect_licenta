package proiect_licenta.planner.job_manager;

import java.util.List;

public class JobList {

	public enum JobType {
		PROCESSING,
		MANIPULATION
	}

	public enum JobStatus {
		FAILED,
		SUCCEEDED
	}

	private class JobWrapper {
		public Job job;
		public JobStatus status;
		public List<JobWrapper> dependencies;
	}


	private List<Job> jobs;
	private List<JobStatus> jobStatus;


	public static JobList createJobList(String jobListJSON) {
		// TODO
		return null;
	}
}
// TODO
// each job needs a status and a list of dependencies based on the dataset names