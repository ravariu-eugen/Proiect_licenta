package proiect_licenta.planner.helper;

import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.pricing.PricingClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ClientHelper {
	private static AwsCredentials credentials = SecretCredentials();


	public static void FileCredentials(String filePath) {
		try {
			Path path = Paths.get(filePath);
			List<String> lines = Files.readAllLines(path);
			String awsAccessKey = lines.get(0);
			String awsSecretKey = lines.get(1);
			credentials = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}


	public static AwsCredentials SecretCredentials() {
		String awsAccessKey = Helper.getSecret("aws_access_key_id");
		String awsSecretKey = Helper.getSecret("aws_secret_access_key");
		return AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
	}

	public static @NotNull AwsCredentials getCredentials() {
		return credentials;
	}

	public static Ec2Client createEC2Client(Region region) {
		AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);
		return Ec2Client.builder()
				.region(region)
				.credentialsProvider(provider)
				.build();
	}

	public static CloudWatchAsyncClient createCloudWatchClient(Region region) {
		AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);
		return CloudWatchAsyncClient.builder()
				.region(region)
				.credentialsProvider(provider)
				.build();
	}


	public static PricingClient createPricingClient(Region region) {
		AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);
		return PricingClient.builder()
				.region(region)
				.credentialsProvider(provider)
				.build();
	}

	public static S3Client createS3Client(Region region) {
		AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);
		return S3Client.builder()
				.region(region)
				.credentialsProvider(provider)
				.build();
	}


	public static EfsClient createEfsClient(Region region) {
		AwsCredentialsProvider provider = StaticCredentialsProvider.create(credentials);
		return EfsClient.builder()
				.region(region)
				.credentialsProvider(provider)
				.build();
	}
}
