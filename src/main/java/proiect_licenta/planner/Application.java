package proiect_licenta.planner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import proiect_licenta.planner.storage.BucketStorage;
import proiect_licenta.planner.helper.Helper;
import proiect_licenta.planner.jobs.JobList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class Application {
	private static final Logger logger = LogManager.getLogger();



	public static void main(String[] args) {



//		String jobListJSON = Helper.getResourceAsString("joblists/joblist1.json");
//		JobList jobList = JobList.createJobList(jobListJSON);
//		jobList.getJobs().forEach(job -> {
//			logger.info(job.toString());
//		});
//		BucketStorage bucketManager = new BucketStorage(Helper.getBucketName());
//		logger.info(bucketManager.listObjects());
//		bucketManager.downloadFile("numbers1.txt", "numbers.txt");
//
//
//		// read file
//		try {
//			String content = Files.readString(Paths.get("numbers1.txt"));
//			logger.info(content);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//
//		bucketManager.uploadFile("numbers1.txt", "numbers1.txt");
//		logger.info(bucketManager.listObjects());
//		boolean deleted = bucketManager.deleteObject("numbers1.txt");
//		logger.info(deleted);
//		logger.info(bucketManager.listObjects());
//		deleted = bucketManager.deleteObject("numbs1.txt");
//		logger.info(deleted);
//		logger.info(bucketManager.listObjects());
//		logger.info(bucketManager.objectExists("numbers.txt"));
		SpringApplication.run(Application.class, args);
	}


}