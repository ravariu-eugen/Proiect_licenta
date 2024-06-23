package proiect_licenta.planner.manager;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;


public class InstanceWrapper {
    Region region = Region.US_EAST_1;
    Ec2Client ec2 = Ec2Client.builder()
            .region(region)
            .build();
    String amiId = "ami-0c55b159cbfafe1f0";
    InstanceType instanceType = InstanceType.T1_MICRO;
    Instance instance;

    public InstanceWrapper(Region region, String amiId, InstanceType instanceType) {
        this.region = region;
        this.amiId = amiId;
        this.instanceType = instanceType;

    }

    public void createInstance() {
        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(instanceType)
                .maxCount(1)
                .minCount(1)
                .build();

        RunInstancesResponse response = ec2.runInstances(runRequest);
        instance = response.instances().getFirst();
    }


}
