package proiect_licenta.planner.helper;

import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.pricing.PricingClient;

public class ClientHelper {
	public static @NotNull AwsCredentials getCredentials() {
	    String awsAccessKey = Helper.getSecret("aws_access_key_id");
	    String awsSecretKey = Helper.getSecret("aws_secret_access_key");
	    return AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
	}

	public static Ec2Client createEC2Client(Region region) {
	    AwsCredentials awsCredentials = getCredentials();
	    AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCredentials);
	    return Ec2Client.builder()
	            .region(region)
	            .credentialsProvider(provider)
	            .build();
	}

	public static CloudWatchAsyncClient createCloudWatchClient(Region region) {
	    AwsCredentials awsCredentials = getCredentials();
	    AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCredentials);
	    return CloudWatchAsyncClient.builder()
	            .region(region)
	            .credentialsProvider(provider)
	            .build();
	}


	public static PricingClient createPricingClient(Region region) {
	    AwsCredentials awsCredentials = getCredentials();
	    AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCredentials);
	    return PricingClient.builder()
	            .region(region)
	            .credentialsProvider(provider)
	            .build();
	}
}
