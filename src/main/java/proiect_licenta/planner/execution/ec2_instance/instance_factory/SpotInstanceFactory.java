package proiect_licenta.planner.execution.ec2_instance.instance_factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.analysis.InstanceConfiguration;
import proiect_licenta.planner.execution.ec2_instance.LaunchTemplateWrapper;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpotInstanceFactory {
	private static final Logger logger = LogManager.getLogger();
	private static final Map<String, Subnet> subnetCache = new HashMap<>();
	private final LaunchTemplateWrapper launchTemplateWrapper;
	private final List<InstanceConfiguration> configurations;
	private final Ec2Client client;

	private final Map<InstanceType, InstanceTypeInfo> instanceTypeMap;

	public SpotInstanceFactory(Ec2Client client, List<InstanceConfiguration> configurations, LaunchTemplateWrapper launchTemplateWrapper) {
		this.client = client;
		this.configurations = configurations;
		this.launchTemplateWrapper = launchTemplateWrapper;

		instanceTypeMap = configurations.stream().collect(Collectors.toMap(
				InstanceConfiguration::instanceType,
				InstanceConfiguration::instanceTypeInfo,
				(v1, _) -> v1,
				HashMap::new));
	}

	public String getSubnetID(String availabilityZone) {

		if (subnetCache.containsKey(availabilityZone)) {
			return subnetCache.get(availabilityZone).subnetId();
		}

		Subnet subnet = client.describeSubnets()
				.subnets().stream()
				.filter(s -> s.availabilityZone().equals(availabilityZone))
				.findFirst().orElse(null);
		if (subnet != null) {
			subnetCache.put(availabilityZone, subnet);
			//logger.info("Found subnet {} for availability zone {}", subnet.subnetId(), availabilityZone);
			return subnet.subnetId();
		}
		return null;
	}

	private FleetLaunchTemplateOverridesRequest getOverride(InstanceConfiguration config) {
		return FleetLaunchTemplateOverridesRequest.builder()
				.subnetId(getSubnetID(config.availabilityZone()))
				.instanceType(config.instanceType())
				.weightedCapacity((double) config.vcpuCount())
				.build();
	}

	public List<InstanceWrapper> createInstances(int vcpuCount) throws RuntimeException {
		// get launch template wrapper
		FleetLaunchTemplateSpecificationRequest fleetLaunchTemplateSpecificationRequest = FleetLaunchTemplateSpecificationRequest.builder()
				.launchTemplateName(launchTemplateWrapper.name())
				.version("$Latest")
				.build();

		// get template override with weighted capacity
		List<FleetLaunchTemplateOverridesRequest> fleetLaunchTemplateOverridesRequest = configurations
				.stream()
				.map(this::getOverride)
				.toList();

		//
		FleetLaunchTemplateConfigRequest fleetLaunchTemplateConfigRequest = FleetLaunchTemplateConfigRequest.builder()
				.launchTemplateSpecification(fleetLaunchTemplateSpecificationRequest)
				.overrides(fleetLaunchTemplateOverridesRequest)
				.build();


		// set target capacity
		TargetCapacitySpecificationRequest targetCapacitySpecificationRequest = TargetCapacitySpecificationRequest.builder()
				.totalTargetCapacity(vcpuCount)
				.defaultTargetCapacityType(DefaultTargetCapacityType.SPOT)
				//.targetCapacityUnitType(TargetCapacityUnitType.VCPU)
				.build();

		// set options
		SpotOptionsRequest spotOptionsRequest = SpotOptionsRequest.builder()
				.allocationStrategy(SpotAllocationStrategy.LOWEST_PRICE)
				.instanceInterruptionBehavior(SpotInstanceInterruptionBehavior.TERMINATE)
				.singleAvailabilityZone(true)
				.build();


		CreateFleetRequest createFleetRequest = CreateFleetRequest.builder()
				.launchTemplateConfigs(fleetLaunchTemplateConfigRequest)
				.targetCapacitySpecification(targetCapacitySpecificationRequest)
				.spotOptions(spotOptionsRequest)
				.type(FleetType.INSTANT)
				.build();

		CreateFleetResponse createFleetResponse = client.createFleet(createFleetRequest);
		if (createFleetResponse.instances().isEmpty()) {
			logger.error("No instances found");
			throw new RuntimeException("No instances found: " + createFleetResponse.errors());
		}

		List<String> instanceIDs = createFleetResponse.instances().stream()
				.map(CreateFleetInstance::instanceIds)
				.flatMap(List::stream).toList();

		DescribeInstancesRequest request = DescribeInstancesRequest.builder().instanceIds(instanceIDs).build();
		DescribeInstancesResponse response = client.describeInstances(request);
		var newInstances = response.reservations().getFirst().instances();
		return newInstances.stream()
				.map(instance -> new InstanceWrapper(instance, instanceTypeMap.get(instance.instanceType())))
				.toList();
	}

	public void clean() {

	}
}
