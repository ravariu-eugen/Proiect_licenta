package proiect_licenta.planner.manager;

import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeyPairWrapper {
    public String getKeyName() {
        return keyName;
    }

    public String getKeyType() {
        return keyType;
    }

    public Region getRegion() {
        return region;
    }

    public Path getKeyPairFilePath() {
        return keyPairFilePath;
    }

    private final Ec2Client client;
    private final Region region;
    private String keyName;
    private String keyType;
    private Path keyPairFilePath = null;

    public KeyPairWrapper(Ec2Client client, Region region) {
        this.client = client;
        this.region = region;
        System.out.println(client.toString());
        System.out.println(region.toString());
    }

    private void deleteIfExists(String keyName) {
        DescribeKeyPairsRequest request = DescribeKeyPairsRequest.builder().keyNames(keyName).build();
        client.describeKeyPairs(request)
                .keyPairs()
                .forEach(_ -> client.deleteKeyPair(DeleteKeyPairRequest.builder().keyName(keyName).build()));
    }
    public KeyPairWrapper create(String keyName, String keyType) {
        this.keyName = keyName;
        this.keyType = keyType;
        deleteIfExists(keyName);

        // create request
        CreateKeyPairRequest request =
                CreateKeyPairRequest.builder()
                        .keyType(keyType).keyName(keyName).build();

        // send request
        CreateKeyPairResponse response = client.createKeyPair(request);
        // save key pair
        String keyMaterial = response.keyMaterial();
        System.out.println(keyMaterial);

        try {
            saveKeyPair(keyMaterial);
        } catch (IOException e) {
            delete();
            throw new RuntimeException(e);
        }
        return this;
    }

    private void saveKeyPair(@NotNull String keyMaterial) throws IOException {
        // create temporary directory
        Path tempFolder = Files.createTempDirectory("key-pair-" + keyName);

        // save key pair
        keyPairFilePath = tempFolder.resolve(keyName + ".pem");
        Files.write(keyPairFilePath, keyMaterial.getBytes());
    }



    public void delete() {

        // delete key pair file
        if (keyPairFilePath != null) {
            try {
                Files.delete(keyPairFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        // delete key pair
        client.deleteKeyPair(
                DeleteKeyPairRequest.builder()
                        .keyName(keyName).build()
        );

    }
}
