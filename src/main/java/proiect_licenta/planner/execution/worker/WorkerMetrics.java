package proiect_licenta.planner.execution.worker;

import java.util.StringJoiner;

public record WorkerMetrics(double cpuUsage, double memoryUsage, int remainingStorage) {
	@Override
	public String toString() {
		return new StringJoiner(", ", WorkerMetrics.class.getSimpleName() + "[", "]")
				.add("cpuUsage=" + cpuUsage)
				.add("memoryUsage=" + memoryUsage)
				.add("remainingStorage=" + remainingStorage)
				.toString();
	}
}
