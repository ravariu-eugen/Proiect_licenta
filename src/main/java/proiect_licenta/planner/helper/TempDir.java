package proiect_licenta.planner.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

public class TempDir {
	private final Path tempDir;

	public TempDir(String prefix) throws IOException {

		tempDir = Files.createTempDirectory(prefix);
	}

	public String getDir() {
		return tempDir.toAbsolutePath().toString();
	}

	public Path getPath() {
		return tempDir;
	}


	public void delete() throws IOException {
		FileDeleter.deleteAllFilesInFolder(getDir());
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", TempDir.class.getSimpleName() + "[", "]")
				.add("tempDir=" + tempDir)
				.toString();
	}
}
