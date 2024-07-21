package proiect_licenta.planner.dataset;

public record TaskID(int id) {

	public String toString() {
		return String.valueOf(id);
	}


	public static int nextID = 0;
	public static TaskID getNextID() {
		return new TaskID(nextID++);
	}
}
