package proiect_licenta.planner.execution.worker;

import java.util.StringJoiner;

public record WorkerStatus(WorkerState state, double cpuUsage, double memoryUsage) {
	@Override
	public String toString() {
		return new StringJoiner(", ", WorkerStatus.class.getSimpleName() + "[", "]")
				.add("state=" + state)
				.add("cpuUsage=" + cpuUsage)
				.add("memoryUsage=" + memoryUsage)
				.toString();
	}
}
