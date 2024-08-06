package proiect_licenta.planner.dataset;

import proiect_licenta.planner.archive.ZipManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipOutputStream;

public record TaskData(String name, byte[] data) {

	public static TaskData from(String taskDir) {

		String taskName = Paths.get(taskDir).getFileName().toString();

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