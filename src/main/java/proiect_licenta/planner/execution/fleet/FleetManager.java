package proiect_licenta.planner.execution.fleet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.ec2_instance.KeyPairWrapper;
import proiect_licenta.planner.execution.ec2_instance.LaunchTemplateWrapper;
import proiect_licenta.planner.execution.ec2_instance.SecurityGroupWrapper;
import proiect_licenta.planner.helper.AmiMap;
import proiect_licenta.planner.helper.ClientHelper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;


public class FleetManager {

	private static final Logger logger = LogManager.getLogger();
	private final LaunchTemplateWrapper launchTemplateWrapper;
	private final Ec2Client client;
	private final SecurityGroupWrapper securityGroupWrapper;
	private final KeyPairWrapper keyPairWrapper;

	public FleetManager() {
		client = ClientHelper.createEC2Client(Region.EU_NORTH_1);
		securityGroupWrapper = new SecurityGroupWrapper(client, "sg", "my-sg-description");
		keyPairWrapper = new KeyPairWrapper(client, "my-key-pair", "rsa");

		launchTemplateWrapper = new LaunchTemplateWrapper(
				client,
				"my-launch-template",
				AmiMap.getAmi(Region.EU_NORTH_1),
				InstanceType.T3_MICRO,
				keyPairWrapper.getKeyName(),
				securityGroupWrapper.getSecurityGroupName()
		);
		launchTemplateWrapper.create();

		logger.info("Created launch template: {}", launchTemplateWrapper.name());
		logger.info("Created security group: {}", securityGroupWrapper.getSecurityGroupName());
		logger.info("Created key pair: {}", keyPairWrapper.getKeyName());
	}


	public List<Instance> createSpotFleet() {


		FleetLaunchTemplateSpecificationRequest fleetLaunchTemplateSpecificationRequest = FleetLaunchTemplateSpecificationRequest.builder()
				.launchTemplateName(launchTemplateWrapper.name())
				.version("$Latest")
				.build();

		FleetLaunchTemplateConfigRequest fleetLaunchTemplateConfigRequest = FleetLaunchTemplateConfigRequest.builder()
				.launchTemplateSpecification(fleetLaunchTemplateSpecificationRequest)
				.build();

		TargetCapacitySpecificationRequest targetCapacitySpecificationRequest = TargetCapacitySpecificationRequest.builder()
				.totalTargetCapacity(5)
				.defaultTargetCapacityType(DefaultTargetCapacityType.SPOT)
				.build();

		FleetSpotCapacityRebalanceRequest capacityRebalance = FleetSpotCapacityRebalanceRequest.builder()
				.replacementStrategy(FleetReplacementStrategy.LAUNCH)
				.build();

		FleetSpotMaintenanceStrategiesRequest maintenanceStrategies = FleetSpotMaintenanceStrategiesRequest.builder()
				.capacityRebalance(capacityRebalance)
				.build();

		SpotOptionsRequest spotOptionsRequest = SpotOptionsRequest.builder()
				.allocationStrategy(SpotAllocationStrategy.LOWEST_PRICE)
				.maintenanceStrategies(maintenanceStrategies)
				.build();

		CreateFleetRequest createFleetRequest = CreateFleetRequest.builder()
				.launchTemplateConfigs(fleetLaunchTemplateConfigRequest)
				.targetCapacitySpecification(targetCapacitySpecificationRequest)
				.spotOptions(spotOptionsRequest)
				.type(FleetType.REQUEST)
				.build();

		CreateFleetResponse createFleetResponse = client.createFleet(createFleetRequest);
		logger.info("Created spot fleet: {}", createFleetResponse.fleetId());
		logger.info("Created instances: {}", createFleetResponse.instances());
		logger.info("Created tags: {}", createFleetResponse.hasInstances());

		if (createFleetResponse.instances().isEmpty()) {
			logger.error("No instances found");
			throw new RuntimeException("No instances found: " +  createFleetResponse.errors());
		}

		List<String> instanceIDs = createFleetResponse.instances().stream()
				.map(CreateFleetInstance::instanceIds)
				.flatMap(List::stream).toList();

		DescribeInstancesRequest request = DescribeInstancesRequest.builder().instanceIds(instanceIDs).build();
		DescribeInstancesResponse response = client.describeInstances(request);
		var newInstances = response.reservations().getFirst().instances();
		return newInstances.stream().toList();
	}


}
