package proiect_licenta.planner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import proiect_licenta.planner.instance_manager.InstanceManager;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.InstanceType;

import java.io.*;
import java.util.Scanner;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application {
	private static final Logger logger = LogManager.getLogger();

	private static @NotNull String getSecret(String secretName) {
		try {
			File file = new File("/run/secrets/" + secretName);
			StringBuilder sb = new StringBuilder();
			try (Scanner scanner = new Scanner(file)) {
				while (scanner.hasNextLine()) {
					sb.append(scanner.nextLine());
				}
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}


	private static @NotNull AwsCredentials getCredentials() {
		String awsAccessKey = getSecret("aws_access_key_id");
		String awsSecretKey = getSecret("aws_secret_access_key");
		return AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
	}

	private static Ec2Client createEC2Client(Region region) {

		AwsCredentials awsCredentials = getCredentials();
		AwsCredentialsProvider provider = StaticCredentialsProvider.create(awsCredentials);
		return Ec2Client.builder()
				.region(region).credentialsProvider(provider)
				.build();
	}
	private static final String userDataFile = "instanceUserData.txt";

	private static String getUserData() {
		try (InputStream inputStream = Application.class.getClassLoader().getResourceAsStream(userDataFile);
		     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return reader.lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {


		Ec2Client client = createEC2Client(Region.EU_NORTH_1);
		logger.info(client.serviceClientConfiguration().region());
		logger.info(getUserData());
		String userData = null;
		String ami = "ami-01b1be742d950fb7f";
		InstanceManager i = new InstanceManager(client, "inst", InstanceType.T3_MICRO, ami, userData);
		i.createInstances(3);
		i.cleanUp();
		client.close();
//		  SpringApplication.run(DemoApplication.class, args);
	}


}