package proiect_licenta.planner.execution.ec2_instance.instance_factory;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;

import java.util.Base64;
import java.util.List;

public class OnDemandInstFactory extends InstanceFactoryAbs {


	public OnDemandInstFactory(Ec2Client client, InstanceType instanceType, String ami, String userData) {
		super(client, instanceType, ami, userData);
	}

	@Override
	public List<InstanceWrapper> createInstances(int count) {
		String base64UserData = Base64.getEncoder().encodeToString(userData.getBytes());
		// create request
		RunInstancesRequest request = RunInstancesRequest.builder()
				.keyName(keyPairWrapper.getKeyName())
				.imageId(ami)
				.instanceType(instanceType)
				.securityGroups(securityGroupWrapper.getSecurityGroupName())
				.userData(base64UserData)
				.minCount(count)
				.maxCount(count)
				.build();
		RunInstancesResponse response = client.runInstances(request);
		return response.instances().stream().map(InstanceWrapper::new).toList();
	}
}
