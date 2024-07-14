package proiect_licenta.planner.archive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipManager implements ArchiveManager {
	private static final Logger logger = LogManager.getLogger();

	private void addToZip(ZipOutputStream zos, File file, ZipEntry zipEntry) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			zos.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}

			zos.closeEntry();
		}
	}

	private void archiveFile(String filePath, String archivePath, boolean append) {
		try (FileOutputStream fos = new FileOutputStream(archivePath, append);
		     ZipOutputStream zos = new ZipOutputStream(fos)) {

			File fileToArchive = new File(filePath);
			ZipEntry zipEntry = new ZipEntry(fileToArchive.getName());
			addToZip(zos, fileToArchive, zipEntry);

		} catch (IOException e) {
			logger.error(e);
		}

	}
	@Override
	public void archiveFile(String filePath, String archivePath) {
		archiveFile(filePath, archivePath, false);
	}

	@Override
	public void addFile(String filePath, String archivePath) {
		archiveFile(filePath, archivePath, true);
	}

	private void archiveFolder(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
		for (File file : Objects.requireNonNull(folder.listFiles())) {
			if (file.isDirectory()) {
				archiveFolder(file, parentFolder + file.getName() + "/", zos);
				continue;
			}
			ZipEntry zipEntry = new ZipEntry(parentFolder + file.getName());
			addToZip(zos, file, zipEntry);
		}
	}

	@Override
	public void addFolder(String folderPath, String archivePath) {
		File folderToArchive = new File(folderPath);
		try (FileOutputStream fos = new FileOutputStream(archivePath);
		     ZipOutputStream zos = new ZipOutputStream(fos)) {
			archiveFolder(folderToArchive, "", zos);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public void archiveFolder(String folderPath, String archivePath) {
		// delete archive if it exists
		File archive = new File(archivePath);
		if (archive.exists()) {
			archive.delete();
		}
		addFolder(folderPath, archivePath);
	}

	@Override
	public void extractArchive(String archivePath, String extractPath) {

	}
}
