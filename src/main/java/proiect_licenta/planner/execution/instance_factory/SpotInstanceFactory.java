package proiect_licenta.planner.execution.instance_factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.ec2_instance.LaunchTemplateWrapper;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;

public class SpotInstanceFactory extends InstanceFactoryAbs {
	private static final Logger logger = LogManager.getLogger();
	private final LaunchTemplateWrapper launchTemplateWrapper;



	public SpotInstanceFactory(Ec2Client client, InstanceType instanceType, String ami, String userData) {
		this(client, instanceType, ami, userData, null);
	}
	public SpotInstanceFactory(Ec2Client client, InstanceType instanceType, String ami, String userData, String availabilityZone) {
		super(client, instanceType, ami, userData);

		launchTemplateWrapper = new LaunchTemplateWrapper(client, "launxh",
				ami, instanceType,
				keyPairWrapper.getKeyName(),
				securityGroupWrapper.getSecurityGroupName(),
				userData, availabilityZone);
		launchTemplateWrapper.create();

	}

	@Override
	public List<InstanceWrapper> createInstances(int count) {
		FleetLaunchTemplateSpecificationRequest fleetLaunchTemplateSpecificationRequest = FleetLaunchTemplateSpecificationRequest.builder()
				.launchTemplateName(launchTemplateWrapper.name())
				.version("$Latest")
				.build();


		FleetLaunchTemplateConfigRequest fleetLaunchTemplateConfigRequest = FleetLaunchTemplateConfigRequest.builder()
				.launchTemplateSpecification(fleetLaunchTemplateSpecificationRequest)
				.build();

		TargetCapacitySpecificationRequest targetCapacitySpecificationRequest = TargetCapacitySpecificationRequest.builder()
				.spotTargetCapacity(count)
				.totalTargetCapacity(count)
				.defaultTargetCapacityType(DefaultTargetCapacityType.SPOT)
				.build();


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
		return newInstances.stream().map(InstanceWrapper::new).toList();
	}
}
