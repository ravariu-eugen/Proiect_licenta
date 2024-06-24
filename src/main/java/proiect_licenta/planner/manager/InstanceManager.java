package proiect_licenta.planner.manager;

import proiect_licenta.planner.helper.Helper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.InstanceType;

public class InstanceManager {
    private InstanceWrapper instanceWrapper;
    private SecurityGroupWrapper securityGroupWrapper;
    private final KeyPairWrapper keyPairWrapper;
    private final Ec2Client client;

    public InstanceManager(Region region, String amiId, InstanceType instanceType) {

        client = Ec2Client.builder()
                .region(region)
                .build();
        keyPairWrapper = new KeyPairWrapper(client, region).create("name", "rsa");
//        securityGroupWrapper = new SecurityGroupWrapper("proiect-licenta", region);
//        instanceWrapper = new InstanceWrapper(region, amiId, instanceType, null);
    }

    public void cleanUp() {
        instanceWrapper.delete();
        securityGroupWrapper.delete();
        keyPairWrapper.delete();
    }
}
