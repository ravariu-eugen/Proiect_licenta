package proiect_licenta.planner.jobs.requirements;

import proiect_licenta.planner.storage.Storage;

import java.util.List;
import java.util.stream.Stream;


public record JobRequirements(List<String> inputs, List<FileNameMapping> shared, List<String> outputs) {

	/**
	 * checks if all required files are in the storage
	 *
	 * @param storage the storage
	 * @return true if all required files are in the storage, false otherwise
	 */
	public boolean isValid(Storage storage) {

		var sharedFiles = shared.stream().map(FileNameMapping::file).toList();
		var allFiles = Stream.concat(inputs.stream(), sharedFiles.stream()).toList();
		var f = storage.objectsExist(allFiles);


		return f.join().stream().allMatch(Boolean::booleanValue);


	}
}
