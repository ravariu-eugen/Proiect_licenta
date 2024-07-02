package proiect_licenta.planner.instance_manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeyPairWrapper {
	private final Logger logger = LogManager.getLogger();
	private final Ec2Client client;

	public String getKeyName() {
		return keyName;
	}

	public Path getKeyPairFilePath() {
		return keyPairFilePath;
	}

	private final String keyName;
	private final String keyType;
	private Path keyPairFilePath = null;


	public boolean isCreated() {
		return keyPairFilePath != null;
	}

	public KeyPairWrapper(Ec2Client client, String keyName, String keyType) {
		logger.info("keyName: {}", keyName);
		logger.info("keyType: {}", keyType);
		logger.info("region: {}", client.serviceClientConfiguration().region());
		this.client = client;
		this.keyName = keyName;
		this.keyType = keyType;

		logger.info("create key pair: {}", keyName);
		deleteKeyPair();
		// create request
		CreateKeyPairRequest request =
				CreateKeyPairRequest.builder()
						.keyType(keyType)
						.keyName(keyName)
						.build();

		// send request
		CreateKeyPairResponse response = client.createKeyPair(request);

		try {
			// save key pair
			String keyMaterial = response.keyMaterial();
			logger.debug("keyMaterial: {}", keyMaterial);
			saveKeyPair(keyMaterial);
		} catch (IOException e) {
			// delete if failed to save key pair
			delete();
		}
	}


	private void saveKeyPair(@NotNull String keyMaterial) throws IOException {
		// create temporary directory
		Path tempFolder = Files.createTempDirectory("key-pair-" + keyName);

		// save key pair
		keyPairFilePath = tempFolder.resolve(keyName + ".pem");
		Files.write(keyPairFilePath, keyMaterial.getBytes());
	}

	private void deleteFile() {
		if (keyPairFilePath != null) {
			try {
				Files.delete(keyPairFilePath);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void deleteKeyPair() {
		DeleteKeyPairRequest deleteRequest = DeleteKeyPairRequest
				.builder()
				.keyName(keyName)
				.build();
		client.deleteKeyPair(deleteRequest);
	}

	public void delete() {

		// delete key pair file
		deleteFile();
		// delete key pair
		deleteKeyPair();

	}
}
