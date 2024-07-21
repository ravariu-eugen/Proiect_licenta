package proiect_licenta.planner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.storage.BucketStorage;
import proiect_licenta.planner.helper.Helper;
import proiect_licenta.planner.testrun.TestRun;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Application {
	private static final Logger logger = LogManager.getLogger();


	public static void testBM(){
		BucketStorage bucketManager = new BucketStorage(Helper.getBucketName());
		logger.info(bucketManager.listObjects());
		bucketManager.get("numbers1.txt", "numbers.txt");


		// read file
		try {
			String content = Files.readString(Paths.get("numbers1.txt"));
			logger.info(content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		bucketManager.put("numbers1.txt", "numbers1.txt");
		logger.info(bucketManager.listObjects());
		boolean deleted = bucketManager.delete("numbers1.txt");
		logger.info(deleted);
		logger.info(bucketManager.listObjects());
		deleted = bucketManager.delete("numbs1.txt");
		logger.info(deleted);
		logger.info(bucketManager.listObjects());
		logger.info(bucketManager.objectExists("numbers.txt"));
	}







	public static void main(String[] args) {
		TestRun testRun = new TestRun();
		testRun.run();



	}




}