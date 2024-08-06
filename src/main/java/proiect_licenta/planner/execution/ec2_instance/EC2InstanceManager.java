package proiect_licenta.planner.execution.ec2_instance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import proiect_licenta.planner.execution.WorkerManager;
import proiect_licenta.planner.execution.analysis.InstanceConfiguration;
import proiect_licenta.planner.execution.instance_factory.InstanceFactory;
import proiect_licenta.planner.execution.instance_factory.InstanceWrapper;
import proiect_licenta.planner.execution.instance_factory.SpotInstanceFactory;
import proiect_licenta.planner.execution.worker.EC2Worker;
import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.helper.AmiMap;
import proiect_licenta.planner.helper.ClientHelper;
import proiect_licenta.planner.helper.Helper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class EC2InstanceManager implements WorkerManager {
	private static final Logger logger = LogManager.getLogger();
	private static final Set<String> IS_TERMINATED = Set.of("terminated", "shutting-down");
	private final Map<String, InstanceWrapper> instances = new HashMap<>();
	private final Ec2Client client;
	private final String managerName;
	private final InstanceFactory instanceFactory;

	private final Map<String, EC2Worker> workers = new HashMap<>(); // <1>

	public EC2InstanceManager(String managerName, InstanceConfiguration config) {
		this(managerName, config.region(), config.instanceType(), config.availabilityZone());
	}

	public EC2InstanceManager(String managerName, Region region, InstanceType instanceType, String availabilityZone) {
		this(ClientHelper.createEC2Client(region),
				managerName,
				instanceType,
				AmiMap.getAmi(region),
				Helper.getUserData(),
				availabilityZone
		);
	}

	public EC2InstanceManager(Ec2Client client, String managerName, InstanceType instanceType, String ami, String userData, String availabilityZone) {
		this.client = client;
		this.managerName = managerName;

		logger.debug("instanceName: {}", managerName);
		logger.debug("instanceType: {}", instanceType);
		logger.debug("region: {}", client.serviceClientConfiguration().region());
		clearRemainingInstances();

		this.instanceFactory = new SpotInstanceFactory(client, instanceType, ami, userData, availabilityZone);
	}

	/**
	 * A method to clear all remaining instances with the tag InstanceManager.
	 */
	private void clearRemainingInstances() {
		// list all instances with tag InstanceManager
		Filter tagFilter = Filter.builder().name("tag:InstanceManager").values(managerName).build();
		DescribeInstancesRequest request = DescribeInstancesRequest.builder()
				.filters(tagFilter)
				.build();

		// get all instances with tag InstanceManager
		DescribeInstancesResponse response = client.describeInstances(request);
		if (response.reservations().isEmpty()) {
			logger.debug("no instances to clear");
			return;
		}
		List<Instance> instances = response.reservations().getFirst().instances();
		logger.debug("clearing {} instances", instances.size());
		// terminate all instances with tag InstanceManager
		terminate(instances.stream().map(Instance::instanceId).toList());
	}


	/**
	 * updates the list of instances
	 */
	private void updateInstances() {
		logger.debug("updateInstances");


		DescribeInstancesRequest request = DescribeInstancesRequest.builder()
				.instanceIds(getInstancesIds())
				.build();

		DescribeInstancesResponse response = client.describeInstances(request);

		var newInstances = response.reservations().getFirst().instances();
		newInstances.forEach(instance -> logger.info("Instance: {} {}", instance.instanceId(), instance.state()));


		Map<Boolean, List<Instance>> partitionedInstances = newInstances.stream()
				.collect(Collectors.partitioningBy(instance -> IS_TERMINATED.contains(instance.state().toString())));

		List<Instance> activeInstances = partitionedInstances.get(false);
		List<Instance> terminatedInstances = partitionedInstances.get(true);

		// update active instances
		activeInstances.forEach(instance -> instances.get(instance.instanceId()).update(instance));

		// terminate terminated instances
		terminatedInstances.forEach(instance -> workers.get(instance.instanceId()).terminate());

		// create new workers
		if (!terminatedInstances.isEmpty())
			createWorkers(terminatedInstances.size());
	}


	/**
	 * @return the list of instance ids for each instance
	 */
	private List<String> getInstancesIds() {
		logger.debug("getInstancesIds");
		return instances.values().stream().map(InstanceWrapper::instanceId).collect(toList());
	}

	/**
	 * sets a tag for each instance in the list
	 *
	 * @param key          the key of the tag
	 * @param value        the value of the tag
	 * @param instancesIds the list of instance ids that should have the tag
	 */
	private void setTag(String key, String value, List<String> instancesIds) {
		Tag tag = Tag.builder().key(key).value(value).build();
		CreateTagsRequest request = CreateTagsRequest.builder().resources(instancesIds).tags(tag).build();
		client.createTags(request);
	}

	/**
	 * sets a tag for an instance
	 *
	 * @param key        the key of the tag
	 * @param value      the value of the tag
	 * @param instanceId the instance id that should have the tag
	 */
	private void setTag(String key, String value, String instanceId) {
		setTag(key, value, List.of(instanceId));
	}

	/**
	 * sets a tag for all instances
	 *
	 * @param key   the key of the tag
	 * @param value the value of the tag
	 */
	private void setTag(String key, String value) {
		setTag(key, value, getInstancesIds());
	}


	/**
	 * terminates the list of instances
	 *
	 * @param instancesIds the list of instance ids that should be terminated
	 */
	private void terminate(@NotNull List<String> instancesIds) {
		if (instancesIds.isEmpty()) {
			logger.info("no instances to terminate");
			return;
		}
		logger.debug("terminate: {}", instancesIds);
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
		logger.info("terminatedAll");
	}


	/**
	 * waits for the termination of the list of instances
	 *
	 * @param instancesIds the list of instance ids that should be terminated
	 */
	private void waitForTermination(@NotNull List<String> instancesIds) {
		if (instancesIds.isEmpty()) {
			logger.info("no instances to wait for");
			return;
		}
		logger.info("waitForTermination: {}", instancesIds);
		DescribeInstancesRequest instanceRequest = DescribeInstancesRequest.builder()
				.instanceIds(instancesIds).build();
		try (Ec2Waiter ec2Waiter = Ec2Waiter.builder()
				.overrideConfiguration(b -> b.maxAttempts(100))
				.client(client).build()) {
			ec2Waiter.waitUntilInstanceTerminated(instanceRequest);
			logger.info("{} terminated", managerName);
		}
	}

	/**
	 * waits for the termination of all instances
	 */
	private void waitForTermination() {
		logger.info("waitForTermination");
		waitForTermination(getInstancesIds());
	}


	@Override
	public List<Worker> createWorkers(int count) {
		if(count <= 0) {
			throw new IllegalArgumentException("count must be greater than 0");
		}
		logger.info("createInstances: {}", count);


		var newInstances = instanceFactory.createInstances(count);
		int newInstanceCount = newInstances.size();

		newInstances.forEach(instance -> instances.put(instance.instanceId(), instance));

		setTag("InstanceManager", managerName);
		for (int i = 0; i < newInstanceCount; i++) {
			setTag("Name", managerName + "-" + i, newInstances.get(i).instanceId());
		}

		Map<String, EC2Worker> newWorkers = newInstances.stream()
				.collect(Collectors.toMap(InstanceWrapper::instanceId, EC2Worker::new));
		workers.putAll(newWorkers);
		updateInstances();
		return newWorkers.values().stream().map(w -> (Worker) w).toList();
	}

	@Override
	public List<Worker> getWorkers() {
		return workers.values().stream().map(w -> (Worker) w).toList();
	}

	public void close() {
		logger.info("cleanUp");
		terminate();
		instanceFactory.clean();
	}
}
