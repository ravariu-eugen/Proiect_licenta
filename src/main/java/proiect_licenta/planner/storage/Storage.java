package proiect_licenta.planner.storage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Storage {


	/**
	 * get a file from storage
	 *
	 * @param objectName      name of the object to get
	 * @param destinationPath where to save the object
	 * @return true if the object was successfully retrieved
	 */
	CompletableFuture<Boolean> get(String objectName, String destinationPath);


	/**
	 * get a file from storage
	 *
	 * @param objectName name of the object to get
	 * @return the content of the object
	 */
	CompletableFuture<byte[]> getBytes(String objectName);


	/**
	 * put a file in storage
	 *
	 * @param objectName name of the object to put
	 * @param sourcePath where to get the object
	 * @return true if the object was successfully put
	 */
	CompletableFuture<Boolean> put(String objectName, String sourcePath);


	/**
	 * put a file in storage
	 *
	 * @param objectName
	 * @param bytes
	 * @return
	 */
	CompletableFuture<Boolean> putBytes(String objectName, byte[] bytes);

	/**
	 * delete a file from storage
	 *
	 * @param objectName name of the object to delete
	 * @return true if the object was successfully deleted, false otherwise
	 */
	CompletableFuture<Boolean> delete(String objectName);

	/**
	 * copy a file in storage
	 *
	 * @param initialObjectName name of the object to copy
	 * @param copyObjectName    name of the new object
	 * @return true if the object was successfully copied
	 */
	CompletableFuture<Boolean> copy(String initialObjectName, String copyObjectName);

	/**
	 * Renames an object in the storage.
	 *
	 * @param initialObjectName the current name of the object
	 * @param copyObjectName    the new name of the object
	 * @return true if the object was successfully renamed, false otherwise
	 */
	CompletableFuture<Boolean> rename(String initialObjectName, String copyObjectName);

	/**
	 * Lists all the objects in the storage.
	 *
	 * @return a list of strings containing the names of all objects
	 */
	CompletableFuture<List<String>> listObjects();

	/**
	 * Check if the specified object exists in storage.
	 *
	 * @param objectName name of the object to check for existence
	 * @return true if the object exists, false otherwise
	 */
	CompletableFuture<Boolean> objectExists(String objectName);

	/**
	 * Checks if all the specified objects exist in the storage.
	 *
	 * @param objectNames a list of strings containing the names of objects to check for existence
	 * @return for each object, true if object exists, false otherwise
	 */
	CompletableFuture<List<Boolean>> objectsExist(List<String> objectNames);
}
