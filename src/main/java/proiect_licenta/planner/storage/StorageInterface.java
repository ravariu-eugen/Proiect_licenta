package proiect_licenta.planner.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StorageInterface {



	/** get a file from storage
	 * @param objectName name of the object to get
	 * @param destinationPath where to save the object
	 * @return true if the object was successfully retrieved
	 */
	public boolean get(String objectName, String destinationPath);


	/** put a file in storage
	 * @param objectName name of the object to put
	 * @param sourcePath where to get the object
	 * @return true if the object was successfully put
	 */
	public boolean put(String objectName, String sourcePath);


	/** delete a file from storage
	 * @param objectName name of the object to delete
	 * @return true if the object was successfully deleted, false otherwise
	 */
	public boolean delete(String objectName);

	/** copy a file in storage
	 * @param initialObjectName name of the object to copy
	 * @param copyObjectName name of the new object
	 * @return true if the object was successfully copied
	 */
	public boolean copy(String initialObjectName, String copyObjectName);

	/**
	 * Renames an object in the storage.
	 *
	 * @param  initialObjectName the current name of the object
	 * @param  copyObjectName    the new name of the object
	 * @return                   true if the object was successfully renamed, false otherwise
	 */
	public boolean rename(String initialObjectName, String copyObjectName);

	/**
	 * Lists all the objects in the storage.
	 *
	 * @return          a list of strings containing the names of all objects
	 */
	public List<String> listObjects();

	/**
	 * Check if the specified object exists in storage.
	 *
	 * @param  objectName   name of the object to check for existence
	 * @return              true if the object exists, false otherwise
	 */
	public boolean objectExists(String objectName);

	/**
	 * Checks if all the specified objects exist in the storage.
	 *
	 * @param  objectNames  a list of strings containing the names of objects to check for existence
	 * @return              true if all objects exist, false otherwise
	 */
	public boolean objectsExist(List<String> objectNames);
}
