package proiect_licenta.planner.archive;

public interface ArchiveManager {


	/**
	 * Archives a file at the specified file path and saves it to the specified archive path.
	 *
	 * @param filePath    the path of the file to be archived
	 * @param archivePath the path where the archived file will be saved
	 */
	public void archiveFile(String filePath, String archivePath);

	/**
	 * Adds a file to the archive at the specified file path and saves it to the specified archive path.
	 *
	 * @param filePath    the path of the file to be added to the archive
	 * @param archivePath the path where the file will be saved in the archive
	 */
	public void addFile(String filePath, String archivePath);

	/**
	 * Archives a folder at the specified folder path and saves it to the specified archive path.
	 *
	 * @param folderPath  the path of the folder to be archived
	 * @param archivePath the path where the archived folder will be saved
	 */
	public void archiveFolder(String folderPath, String archivePath);


	/**
	 * Adds a folder to the archive at the specified folder path and saves it to the specified archive path.
	 *
	 * @param  folderPath	the path of the folder to be added to the archive
	 * @param  archivePath the path where the folder will be saved in the archive
	 */
	public void addFolder(String folderPath, String archivePath);

	/**
	 * Extracts an archive file at the specified archive path and saves it to the specified extract path.
	 *
	 * @param archivePath the path of the archive file to be extracted
	 * @param extractPath the path where the extracted files will be saved
	 */
	public void extractArchive(String archivePath, String extractPath);





}
