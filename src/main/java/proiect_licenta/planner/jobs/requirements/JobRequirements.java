package proiect_licenta.planner.jobs.requirements;

import proiect_licenta.planner.storage.Storage;

import java.util.List;

public record JobRequirements(List<String> inputs, List<String> shared, List<String> outputs) {

	/** checks if all required files are in the storage
	 * @param storage the storage
	 * @return true if all required files are in the storage, false otherwise
	 */
	public boolean isValid(Storage storage) {
		return storage.objectsExist(inputs) && storage.objectsExist(shared) && storage.objectsExist(outputs);
	}
}
