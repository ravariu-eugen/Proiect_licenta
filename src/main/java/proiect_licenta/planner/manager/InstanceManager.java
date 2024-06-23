package proiect_licenta.planner.manager;

import proiect_licenta.planner.helper.Helper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.InstanceType;

public class InstanceManager {
    private final InstanceWrapper instanceWrapper;
    private final SecurityGroupWrapper securityGroupWrapper;
    private final KeyPairWrapper keyPairWrapper;


    public InstanceManager(Region region, String amiId, InstanceType instanceType) {
        instanceWrapper = new InstanceWrapper(region, amiId, instanceType);
        securityGroupWrapper = new SecurityGroupWrapper("proiect-licenta", region);
        keyPairWrapper = new KeyPairWrapper(region);
    }

    public void createInstance() {
        instanceWrapper.createInstance();
        securityGroupWrapper.createSecurityGroup("proiect-licenta");
        securityGroupWrapper.authorizeSecurityGroupIngress(Helper.myIP());
        keyPairWrapper.createKeyPair("proiect-licenta", "rsa");
    }
}
