package proiect_licenta.planner.manager;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;


public class InstanceWrapper {
    Region region;
    Ec2Client client;
    Instance instance;

    public InstanceWrapper(Ec2Client client, Region region) {
        this.region = region;
        this.client = client;
    }

    public void create(String amiId, InstanceType instanceType, String userData) {
        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(instanceType)
                .maxCount(1).minCount(1)
                .build();

        RunInstancesResponse response = client.runInstances(runRequest);
        instance = response.instances().getFirst();
    }

    public void delete() {
        client.terminateInstances(instance);
    }

}
