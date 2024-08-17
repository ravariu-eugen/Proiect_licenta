package proiect_licenta.planner.archive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
		logger.info("Archiving {} to {}", filePath, archivePath);
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

	public void archiveFolder(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
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
	public void addFolder(String folderPath, OutputStream outputStream) {
		File folderToArchive = new File(folderPath);
		try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
			archiveFolder(folderToArchive, "", zos);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public void addFolder(String folderPath, String archivePath) {
		//logger.info("Adding {} to {}", folderPath, archivePath);

		try {
			addFolder(folderPath, new FileOutputStream(archivePath));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void archiveFolder(String sourceFolderPath, String archivePath) {
		// delete archive if it exists
		File archive = new File(archivePath);
		if (archive.exists()) {
			archive.delete();
		}
		addFolder(sourceFolderPath, archivePath);
	}

	@Override
	public void extractArchive(String archivePath, String extractPath) {
		try {
			extractArchive(new FileInputStream(archivePath), extractPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void extractArchive(InputStream inputStream, String extractPath) throws IOException {
		try (ZipInputStream zipIn = new ZipInputStream(inputStream)) {
			ZipEntry entry = zipIn.getNextEntry();
			while (entry != null) {
				File entryDestination = new File(extractPath, entry.getName());
				if (entry.isDirectory()) {
					entryDestination.mkdirs();
				} else {
					entryDestination.getParentFile().mkdirs();
					try (FileOutputStream fos = new FileOutputStream(entryDestination)) {
						byte[] buffer = new byte[4096];
						int len;
						while ((len = zipIn.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					}
				}
				entry = zipIn.getNextEntry();
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
