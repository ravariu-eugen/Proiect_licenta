package proiect_licenta.planner.storage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LocalStorage implements StorageInterface {
	private final String tempDirectoryPath;

	public LocalStorage() {
		try {
			this.tempDirectoryPath = Files.createTempDirectory("planner").toAbsolutePath().toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean get(String objectName, String destinationPath) {
		Path source = Paths.get(tempDirectoryPath + "/" + objectName);
		Path destination = Paths.get(destinationPath);
		try {
			Files.copy(source, destination);
			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}


	@Override
	public boolean put(String objectName, String sourcePath) {
		Path source = Paths.get(sourcePath);
		Path destination = Paths.get(tempDirectoryPath + "/" + objectName);
		try {
			Files.copy(source, destination);
			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public boolean delete(String objectName) {
		return false;
	}

	@Override
	public boolean copy(String initialObjectName, String copyObjectName) {
		return false;
	}

	@Override
	public boolean rename(String initialObjectName, String copyObjectName) {
		return false;
	}

	@Override
	public List<String> listObjects() {
		return List.of();
	}

	@Override
	public boolean objectExists(String objectName) {
		return false;
	}

	@Override
	public boolean objectsExist(List<String> objectNames) {
		return false;
	}
}
