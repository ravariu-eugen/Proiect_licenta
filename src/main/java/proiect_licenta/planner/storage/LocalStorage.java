package proiect_licenta.planner.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStorage implements Storage {
	private static final Logger logger = LogManager.getLogger();
	private final String dirPath;

	public LocalStorage(String dirPath) {
		this.dirPath = dirPath;
	}

	@Override
	public CompletableFuture<Boolean> get(String objectName, String destinationPath) {
		return CompletableFuture.supplyAsync(() -> {
			Path source = Paths.get(dirPath + "/" + objectName);
			Path destination = Paths.get(destinationPath);
			try {
				Files.copy(source, destination);
				return true;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public CompletableFuture<byte[]> getBytes(String objectName) {
		return CompletableFuture.supplyAsync(() -> {
			Path source = Paths.get(dirPath + "/" + objectName);
			try {
				return Files.readAllBytes(source);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}


	@Override
	public CompletableFuture<Boolean> put(String objectName, String sourcePath) {
		return CompletableFuture.supplyAsync(() -> {
			Path source = Paths.get(sourcePath);
			Path destination = Paths.get(dirPath + "/" + objectName);
			try {
				Files.copy(source, destination);
				return true;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public CompletableFuture<Boolean> putBytes(String objectName, byte[] bytes) {
		return CompletableFuture.supplyAsync(() -> {
			Path destination = Paths.get(dirPath + "/" + objectName);
			try {
				Files.write(destination, bytes);
				return true;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}


	@Override
	public CompletableFuture<Boolean> delete(String objectName) {
		return CompletableFuture.supplyAsync(() -> {
			Path filePath = Paths.get(dirPath + "/" + objectName);
			try {
				Files.delete(filePath);
				return true;
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			return false;
		});
	}

	@Override
	public CompletableFuture<Boolean> copy(String initialObjectName, String copyObjectName) {
		return CompletableFuture.supplyAsync(() -> {
			Path source = Paths.get(dirPath + "/" + initialObjectName);
			Path destination = Paths.get(dirPath + "/" + copyObjectName);
			try {
				Files.copy(source, destination);
				return true;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public CompletableFuture<Boolean> rename(String initialObjectName, String copyObjectName) {
		return CompletableFuture.supplyAsync(() -> {
			Path source = Paths.get(dirPath + "/" + initialObjectName);
			Path destination = Paths.get(dirPath + "/" + copyObjectName);
			try {
				Files.move(source, destination);
				return true;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

	}

	@Override
	public CompletableFuture<List<String>> listObjects() {
		return CompletableFuture.supplyAsync(() -> {
			try (Stream<Path> paths = Files.list(Paths.get(dirPath))) {
				return paths.map(Path::getFileName).map(Path::toString).toList();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

	}

	@Override
	public CompletableFuture<Boolean> objectExists(String objectName) {
		return listObjects().thenApply(objects -> objects.contains(objectName));
	}

	@Override
	public CompletableFuture<List<Boolean>> objectsExist(List<String> objectNames) {
		return listObjects().thenApply(objects -> objects.stream().map(objects::contains).collect(Collectors.toList()));
	}
}
