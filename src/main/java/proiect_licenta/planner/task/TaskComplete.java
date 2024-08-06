package proiect_licenta.planner.task;

import java.util.Arrays;
import java.util.StringJoiner;

public record TaskComplete(String name,byte[] resultData) implements TaskResult{


	@Override
	public String toString() {
		return new StringJoiner(", ", TaskComplete.class.getSimpleName() + "[", "]")
				.add("resultData=" + Arrays.toString(resultData))
				.toString();
	}
}
