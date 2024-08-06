package proiect_licenta.planner.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileDeleter {

	private  static Logger logger = LogManager.getLogger();

	public static void deleteAllFilesInFolder(String folderPath) {
		try (Stream<Path> paths = Files.walk(Path.of(folderPath))) {
			paths.filter(Files::isRegularFile)
					.forEach(FileDeleter::deleteFile);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static void deleteFile(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
}