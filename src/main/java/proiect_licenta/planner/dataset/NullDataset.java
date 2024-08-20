package proiect_licenta.planner.dataset;

import java.util.List;

public class NullDataset implements Dataset {

	public static final String NAME = "null";

	@Override
	public List<TaskData> getTasks() {
		return List.of(new TaskData(NAME, new byte[1]));
	}

	@Override
	public String getName() {
		return NAME;
	}
}
