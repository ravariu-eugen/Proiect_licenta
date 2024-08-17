package proiect_licenta.planner.execution.worker;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.StringJoiner;

public record WorkerMetrics(
		@JsonProperty("cpuUsage") double cpuUsage,
		@JsonProperty("memoryUtilization") double memoryUsage,
		@JsonProperty("remainingStorage") int remainingStorage) {
	@Override
	public String toString() {
		return new StringJoiner(", ", WorkerMetrics.class.getSimpleName() + "[", "]")
				.add("cpuUsage=" + cpuUsage)
				.add("memoryUsage=" + memoryUsage)
				.add("remainingStorage=" + remainingStorage)
				.toString();
	}
}
