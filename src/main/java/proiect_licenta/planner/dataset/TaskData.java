package proiect_licenta.planner.dataset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.archive.ZipManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipOutputStream;

public record TaskData(String name, byte[] data) {
	private static final Logger logger = LogManager.getLogger();

	public static TaskData from(String taskDir) {

		String taskName = Paths.get(taskDir).getFileName().toString();
		//logger.info("task name: {}", taskName);
		//logger.info("task dir: {}", taskDir);
		// get task folder
		ZipManager zipManager = new ZipManager();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

			// zip task folder
			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				zipManager.archiveFolder(new File(taskDir), "", zos);
			}

			// write zip to byte array
			return new TaskData(taskName, baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
