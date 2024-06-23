package proiect_licenta.planner.manager;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KeyPairWrapper {
    Region region;
    Ec2Client client;
    KeyPairInfo keyPair;
    String keyName = null;
    Path keyPairFilePath = null;

    public KeyPairWrapper(Region region) {
        this.region = region;

        client = Ec2Client.builder().region(region).build();
    }


    private void deleteIfExists(String keyName) {
        DescribeKeyPairsRequest request = DescribeKeyPairsRequest.builder().keyNames(keyName).build();
        client.describeKeyPairs(request)
                .keyPairs().stream().findFirst()
                .ifPresent(_ -> client.deleteKeyPair(DeleteKeyPairRequest.builder().keyName(keyName).build()));
    }


    public void createKeyPair(String keyName, String keyType) {


        deleteIfExists(keyName);

        // create request
        CreateKeyPairRequest request =
                CreateKeyPairRequest.builder()
                        .keyType(keyType).keyName(keyName).build();

        // send request
        CreateKeyPairResponse response = client.createKeyPair(request);

        // save key pair
        String keyMaterial = response.keyMaterial();


        try {
            // create temporary directory
            Path tempFolder = Files.createTempDirectory("key-pair-" + keyName);

            // save key pair
            keyPairFilePath = tempFolder.resolve(keyName + ".pem");
            Files.write(keyPairFilePath, keyMaterial.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void deleteKeyPair() {

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
