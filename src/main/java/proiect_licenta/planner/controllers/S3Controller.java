package proiect_licenta.planner.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proiect_licenta.planner.helper.Helper;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.s3.S3Client;

@RestController
@RequestMapping("/s3")
public class S3Controller {
	private static final Logger logger = LogManager.getLogger();
	private static Region region = Region.US_EAST_1;
	private static S3Client s3;

	private static S3Client createS3Client(Region region) {
		AwsCredentials awsCredentials = Helper.getCredentials();
		AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCredentials);
		return S3Client.builder()
				.region(region).credentialsProvider(provider)
				.build();
	}

	/**
	 * @return a json list of all buckets in the region
	 */
	@GetMapping("/buckets")
	public String buckets() {
		logger.info("buckets");
		return "hello";
/*
		Region region = Region.US_EAST_1;
		S3Client s3 = S3Client.builder()
				.region(region)
				.build();

		// List buckets
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
		List<String> buckets = listBucketsResponse.buckets().stream().map(Bucket::name).toList();
		return buckets.toString();
*/
	}

	@GetMapping("/buckets/{bucket_name}")
	public ResponseEntity<String> describeBucket(@NotNull String bucket_name) {
		// TODO: implement describe bucket
		return ResponseEntity.ok("describe bucket");
	}

	@GetMapping("/buckets/{bucket_name}/list")
	public ResponseEntity<String> bucketItems(@NotNull String bucket_name) {
		// TODO: implement list bucket
		return ResponseEntity.ok("list bucket items");
	}

	@GetMapping("/buckets/{bucketName}/{fileName}")
	public String getFile(@NotNull String bucketName, @NotNull String fileName) {
		logger.info("s3File");
		Region region = Region.US_EAST_1;
		S3Client s3 = S3Client.builder()
				.region(region)
				.build();
		return s3.toString();
	}


	@PostMapping("/region")
	public ResponseEntity<String> setRegion() {
		// TODO: implement set region
		return ResponseEntity.ok("set region");
	}
}
