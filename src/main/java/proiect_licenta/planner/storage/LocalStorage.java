package proiect_licenta.planner.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStorage implements Storage {
	private static final Logger logger = LogManager.getLogger();
	private final String dirPath;

	public LocalStorage() {
		try {
			this.dirPath = Files.createTempDirectory("storage").toAbsolutePath().toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public LocalStorage(String dirPath){
		this.dirPath = dirPath;
	}

	@Override
	public boolean get(String objectName, String destinationPath) {
		Path source = Paths.get(dirPath + "/" + objectName);
		Path destination = Paths.get(destinationPath);
		try {
			Files.copy(source, destination);
			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public byte[] getBytes(String objectName) {
		Path source = Paths.get(dirPath + "/" + objectName);
		try {
			return Files.readAllBytes(source);
		} catch (IOException e) {
			return null;
		}
	}


	@Override
	public boolean put(String objectName, String sourcePath) {
		Path source = Paths.get(sourcePath);
		Path destination = Paths.get(dirPath + "/" + objectName);
		try {
			Files.copy(source, destination);
			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean putBytes(String objectName, byte[] bytes) {
		Path destination = Paths.get(dirPath + "/" + objectName);
		try {
			Files.write(destination, bytes);
			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public boolean delete(String objectName) {
		Path filePath = Paths.get(dirPath + "/" + objectName);
		try {
			if (Files.exists(filePath)) {
				Files.delete(filePath);
				return true;
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean copy(String initialObjectName, String copyObjectName) {
		Path source = Paths.get(dirPath + "/" + initialObjectName);
		Path destination = Paths.get(dirPath + "/" + copyObjectName);
		try {
			Files.copy(source, destination);
			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean rename(String initialObjectName, String copyObjectName) {
		Path source = Paths.get(dirPath + "/" + initialObjectName);
		Path destination = Paths.get(dirPath + "/" + copyObjectName);
		try {
			Files.move(source, destination);
			return true;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> listObjects() {
		try (Stream<Path> paths = Files.list(Paths.get(dirPath))) {
			return paths.map(Path::getFileName).map(Path::toString).toList();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean objectExists(String objectName) {

		try (Stream<Path> paths = Files.list(Paths.get(dirPath))) {
			return paths.map(Path::getFileName).map(Path::toString).anyMatch(objectName::equals);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean objectsExist(List<String> objectNames) {
		try (Stream<Path> paths = Files.list(Paths.get(dirPath))) {
			Set<String> fileNames = paths.map(Path::getFileName)
					.map(Path::toString)
					.collect(Collectors.toSet());
			return fileNames.containsAll(objectNames);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
