package proiect_licenta.planner.instance_manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.helper.Helper;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.Base64;
import java.util.List;

public class InstanceManager {
	private final Logger logger = LogManager.getLogger();
	private List<Instance> instances = null;
	private final SecurityGroupWrapper securityGroupWrapper;
	private final KeyPairWrapper keyPairWrapper;
	private final Ec2Client client;
	private final String managerName;
	private final InstanceType instanceType;
	private final String userData;
	private final String ami;

	public InstanceManager(Ec2Client client, String instanceName, InstanceType instanceType, String ami, String userData) {
		this.client = client;
		this.managerName = instanceName;
		this.instanceType = instanceType;
		this.userData = userData;
		this.ami = ami;

		logger.info("instanceName: {}", instanceName);
		logger.info("instanceType: {}", instanceType);
		logger.info("region: {}", client.serviceClientConfiguration().region());
		clearRemainingInstances();
		keyPairWrapper = new KeyPairWrapper(client,
				instanceName + "-key",
				"rsa");
		securityGroupWrapper = new SecurityGroupWrapper(client,
				instanceName + "-sg",
				"SG for " + instanceName);
		securityGroupWrapper.authorizeIngress(Helper.myIP());
	}

	/**
	 * A method to clear all remaining instances with the tag InstanceManager.
	 */
	public void clearRemainingInstances() {
		// list all instances with tag InstanceManager
		Filter tagFilter = Filter.builder().name("tag:InstanceManager").values(managerName).build();
		DescribeInstancesRequest request = DescribeInstancesRequest.builder().filters(tagFilter).build();

		// get all instances with tag InstanceManager
		DescribeInstancesResponse response = client.describeInstances(request);
		if (response.reservations().isEmpty()) {
			logger.info("no instances to clear");
			return;
		}
		List<Instance> instances = response.reservations().getFirst().instances();
		logger.info("clearing {} instances", instances.size());
		// terminate all instances with tag InstanceManager
		terminate(instances.stream().map(Instance::instanceId).toList());
	}

	/**
	 * A method to create instances based on the provided count. It creates a request with the specified parameters,
	 * runs the instances, sets tags for each instance, updates the list of instances, and returns the count of created instances.
	 *
	 * @param  count  the number of instances to create
	 * @return       the count of created instances
	 */
	public int createInstances(int count) {
		logger.info("createInstances: {}", count);

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
		int instanceCount = response.instances().size();

		logger.info("created instances: {}", instanceCount);
		instances = response.instances();

		setTag("InstanceManager", managerName);
		for (int i = 0; i < instanceCount; i++) {
			setTag("Name", managerName + "-" + i, instances.get(i).instanceId());
		}

		updateInstances();
		return instanceCount;

	}


	/**
	 * updates the list of instances
	 */
	private void updateInstances() {
		logger.info("updateInstances");
		DescribeInstancesRequest request = DescribeInstancesRequest.builder().instanceIds(getInstancesIds()).build();
		DescribeInstancesResponse response = client.describeInstances(request);
		instances = response.reservations().getFirst().instances();
	}

	/**
	 * @return the list of public IPs for each instance
	 */
	public List<String> getIPs() {
		logger.info("getIPs");
		if (instances == null) {
			return List.of();
		}
		return instances.stream().map(Instance::publicIpAddress).toList();
	}

	/**
	 * @return the list of instance ids for each instance
	 */
	private List<String> getInstancesIds() {
		logger.info("getInstancesIds");
		if (instances == null) {
			return List.of();
		}
		return instances.stream().map(Instance::instanceId).toList();
	}

	/** sets a tag for each instance in the list
	 * @param key the key of the tag
	 * @param value the value of the tag
	 * @param instancesIds the list of instance ids that should have the tag
	 */
	private void setTag(String key, String value, List<String> instancesIds) {
		Tag tag = Tag.builder()
				.key(key)
				.value(value)
				.build();
		CreateTagsRequest request = CreateTagsRequest
				.builder()
				.resources(instancesIds)
				.tags(tag)
				.build();
		client.createTags(request);

	}

	/** sets a tag for an instance
	 * @param key the key of the tag
	 * @param value the value of the tag
	 * @param instanceId the instance id that should have the tag
	 */
	private void setTag(String key, String value, String instanceId) {
		setTag(key, value, List.of(instanceId));
	}

	/** sets a tag for all instances
	 * @param key the key of the tag
	 * @param value the value of the tag
	 */
	private void setTag(String key, String value) {
		setTag(key, value, getInstancesIds());
	}


	/** terminates the list of instances
	 * @param instancesIds the list of instance ids that should be terminated
	 */
	private void terminate(@NotNull List<String> instancesIds) {
		if (instancesIds.isEmpty()) {
			logger.info("no instances to terminate");
			return;
		}
		logger.info("terminate: {}", instancesIds);
		TerminateInstancesRequest request = TerminateInstancesRequest.builder()
				.instanceIds(instancesIds).build();
		client.terminateInstances(request);
	}

	/**
	 * terminates all instances
	 */
	private void terminate() {
		logger.info("terminateAll");
		terminate(getInstancesIds());
	}


	/** waits for the termination of the list of instances
	 * @param instancesIds the list of instance ids that should be terminated
	 */
	private void waitForTermination(@NotNull List<String> instancesIds) {
		if (instancesIds.isEmpty()) {
			logger.info("no instances to wait for");
			return;
		}
		logger.info("waitForTermination: {}", instancesIds);
		DescribeInstancesRequest instanceRequest = DescribeInstancesRequest.builder()
				.instanceIds(instancesIds)
				.build();
		try (Ec2Waiter ec2Waiter = Ec2Waiter.builder()
				.overrideConfiguration(b -> b.maxAttempts(100))
				.client(client)
				.build()) {
			ec2Waiter.waitUntilInstanceTerminated(instanceRequest);
			logger.info("all instances terminated");
		}
	}

	/**
	 * waits for the termination of all instances
	 */
	private void waitForTermination() {
		logger.info("waitForTermination");
		waitForTermination(getInstancesIds());
	}


	/**
	 * cleans up all resources
	 */
	public void cleanUp() {
		logger.info("cleanUp");
		terminate();
		waitForTermination();
		securityGroupWrapper.delete();
		keyPairWrapper.delete();

	}
}
