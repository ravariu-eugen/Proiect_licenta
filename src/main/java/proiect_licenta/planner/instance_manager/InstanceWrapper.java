package proiect_licenta.planner.instance_manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

import java.util.Optional;


public class InstanceWrapper {
	private final Logger logger = LogManager.getLogger();
	private final Ec2Client client;
	private final Instance instance;
	private final String instanceID;

	public InstanceWrapper(Ec2Client client, Instance instance) {

		this.client = client;
		this.instance = instance;
		this.instanceID = instance.instanceId();
	}

	public String getInstanceID() {
		return instanceID;
	}

	public String getIP() {
		return instance.publicIpAddress();
	}


	public void setTag(String key, String value) {
		Tag tag = Tag.builder()
				.key(key)
				.value(value)
				.build();
		CreateTagsRequest request = CreateTagsRequest
				.builder()
				.resources(instanceID)
				.tags(tag)
				.build();
		client.createTags(request);


	}

	public String getTag(String key) {

		DescribeInstancesRequest request = DescribeInstancesRequest.builder().instanceIds(instanceID).build();

		DescribeInstancesResponse response = client.describeInstances(request);

		Optional<Tag> tag = response.reservations()
				.getFirst().instances()
				.getFirst().tags()
				.stream()
				.filter(t -> t.key().equals(key))
				.findFirst();

		return tag.map(Tag::value).orElse(null);
	}

	public void start() {
		StartInstancesRequest request = StartInstancesRequest.builder().instanceIds(instanceID).build();
		client.startInstances(request);
	}

	public void stop() {
		StopInstancesRequest request = StopInstancesRequest
				.builder()
				.instanceIds(instanceID)
				.build();
		client.stopInstances(request);
	}


	public void waitUntilTerminated() {
		DescribeInstancesRequest instanceRequest = DescribeInstancesRequest.builder()
				.instanceIds(instanceID)
				.build();
		try (Ec2Waiter ec2Waiter = Ec2Waiter.builder()
				.overrideConfiguration(b -> b.maxAttempts(100))
				.client(client)
				.build()) {
			WaiterResponse<DescribeInstancesResponse> waiterResponse = ec2Waiter
					.waitUntilInstanceTerminated(instanceRequest);
			waiterResponse.matched().response().ifPresent(_ -> logger.info("terminated instance: {}", instanceID));
		}
	}

	public void terminate() {
		TerminateInstancesRequest request = TerminateInstancesRequest
				.builder()
				.instanceIds(`instanceID)
				.build();
		client.terminateInstances(request);


	}

}
