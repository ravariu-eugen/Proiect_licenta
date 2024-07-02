package proiect_licenta.planner.instance_manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.helper.Helper;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

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
		keyPairWrapper = new KeyPairWrapper(client, instanceName + "-key", "rsa");
		securityGroupWrapper = new SecurityGroupWrapper(client, instanceName + "-sg", "SG for " + instanceName);
		securityGroupWrapper.authorizeIngress(Helper.myIP());
	}

	public void clearRemainingInstances() {
		// list all instances with tag InstanceManager
		Filter tagFilter = Filter.builder().name("tag:InstanceManager").values(managerName).build();
		DescribeInstancesRequest request = DescribeInstancesRequest.builder().filters(tagFilter).build();

		// get all instances with tag InstanceManager
		DescribeInstancesResponse response = client.describeInstances(request);
		List<Instance> instances = response.reservations().getFirst().instances();

		// terminate all instances with tag InstanceManager
		terminate(instances.stream().map(Instance::instanceId).toList());
	}


	public int createInstances(int count) {
		logger.info("createInstances: {}", count);
		// create request
		RunInstancesRequest request = RunInstancesRequest.builder()
				.keyName(keyPairWrapper.getKeyName())
				.imageId(ami)
				.instanceType(instanceType)
				.securityGroups(securityGroupWrapper.getSecurityGroupName())
				.userData(userData)
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


		return instanceCount;

	}
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
	private void setTag(String key, String value, String instanceId) {
		setTag(key, value, List.of(instanceId));
	}

	private void setTag(String key, String value) {
		setTag(key, value, getInstancesIds());
	}


	private List<String> getInstancesIds() {
		logger.info("getInstancesIds");
		if (instances == null) {
			return List.of();
		}
		return instances.stream().map(Instance::instanceId).toList();
	}
	private void terminate() {
		logger.info("terminateAll");
		terminate(getInstancesIds());
	}

	private void terminate(List<String> instancesIds) {
		if (instancesIds.isEmpty()) {
			logger.info("no instances to terminate");
			return;
		}
		logger.info("terminate: {}", instancesIds);
		TerminateInstancesRequest request = TerminateInstancesRequest.builder()
				.instanceIds(instancesIds).build();
		client.terminateInstances(request);
	}


	public void cleanUp() {
		logger.info("cleanUp");
		terminate();
		securityGroupWrapper.delete();
		keyPairWrapper.delete();

	}
}
