package proiect_licenta.planner.execution.analysis;


import proiect_licenta.planner.helper.ClientHelper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MarketAnalyzer {

	private final List<Region> usedRegions;
	private final List<InstanceType> usedInstanceTypes;
	private final Map<InstanceType, InstanceTypeInfo> usedInstanceInfo;
	private static final List<InstanceType> generalPurposeInstances = getGeneralPurposeInstances(8);
	private static final List<InstanceType> computeOptimizedInstances = getComputeOptimizedInstances(8);
	private static final List<InstanceType> memoryOptimizedInstances = getMemoryOptimizedInstances(8);


	public final List<InstanceConfiguration> spotPrices = new ArrayList<>();

	public MarketAnalyzer(List<Region> usedRegions, List<InstanceType> usedInstanceTypes) {
		this.usedRegions = usedRegions;
		this.usedInstanceTypes = usedInstanceTypes;
		this.usedInstanceInfo = loadUsedInstanceInfo(usedInstanceTypes);

		updateSpotPrices();
	}


	public List<InstanceConfiguration> getTopN(int n) {

		return spotPrices.stream()
				.sorted(Comparator.comparingDouble(InstanceConfiguration::pricePerVCPU))
				.limit(n).toList();
	}


	private void updateSpotPrices() {
		spotPrices.clear();
		spotPrices.addAll(usedRegions.stream()
				.map(r -> getSpotPrices(r, usedInstanceTypes))
				.flatMap(List::stream).toList());
	}


	public List<InstanceConfiguration> getSpotPrices(Region region, List<InstanceType> instanceTypes) {

		try (Ec2Client pricingClient = ClientHelper.createEC2Client(region)) {

			// only Linux/UNIX
			Filter filter = Filter.builder()
					.name("product-description")
					.values("Linux/UNIX")
					.build();

			// get latest price
			DescribeSpotPriceHistoryRequest request = DescribeSpotPriceHistoryRequest.builder()
					.instanceTypes(instanceTypes)
					.startTime(Instant.now())
					.endTime(Instant.now())
					.filters(filter)
					.build();


			DescribeSpotPriceHistoryResponse response = pricingClient.describeSpotPriceHistory(request);

			return response.spotPriceHistory().stream().map(
					sp -> new InstanceConfiguration(
							region,
							usedInstanceInfo.get(sp.instanceType()),
							sp))
					.toList();
		}
	}


	private static Map<InstanceType, InstanceTypeInfo> loadUsedInstanceInfo(List<InstanceType> instanceTypes) {


		try (Ec2Client ec2Client = ClientHelper.createEC2Client(Region.US_EAST_1)) {
			DescribeInstanceTypesRequest describeInstanceTypesRequest = DescribeInstanceTypesRequest.builder()
					.instanceTypes(instanceTypes)
					.build();
			DescribeInstanceTypesResponse response = ec2Client.describeInstanceTypes(describeInstanceTypesRequest);
			var instanceTypeInfoList = response.instanceTypes();

			return instanceTypeInfoList.parallelStream()
					.collect(Collectors.toMap(InstanceTypeInfo::instanceType, Function.identity()));
		}

	}

	public static List<InstanceType> getInstanceTypes(int maxVCPU, List<String> names) {
		try (Ec2Client ec2Client = ClientHelper.createEC2Client(Region.US_EAST_1)) {


			// no burstable
			Filter noBurstable = Filter.builder()
					.name("burstable-performance-supported")
					.values("false")
					.build();

			// only current gen
			Filter currentGen = Filter.builder()
					.name("current-generation")
					.values("true")
					.build();

			// only x86
			Filter x86 = Filter.builder()
					.name("processor-info.supported-architecture")
					.values("x86_64")
					.build();

			// no baremetal
			Filter noBareMetal = Filter.builder()
					.name("bare-metal")
					.values("false")
					.build();

			// for spot

			Filter spot = Filter.builder()
					.name("supported-usage-class")
					.values("spot")
					.build();


			Filter namesFilter = Filter.builder()
					.name("instance-type")
					.values(names)
					.build();

			List<String> vcpuValues = IntStream.rangeClosed(1, maxVCPU).mapToObj(String::valueOf).toList();
			// max cpu
			Filter maxVCPUFilter = Filter.builder()
					.name("vcpu-info.default-vcpus")
					.values(vcpuValues)
					.build();


			DescribeInstanceTypesRequest describeInstanceTypesRequest = DescribeInstanceTypesRequest.builder()
					.filters(noBurstable, currentGen, x86, noBareMetal, maxVCPUFilter, spot, namesFilter)
					.build();
			DescribeInstanceTypesResponse response = ec2Client.describeInstanceTypes(describeInstanceTypesRequest);
			var instanceTypeInfoList = response.instanceTypes();
			return instanceTypeInfoList.stream().map(InstanceTypeInfo::instanceType).filter(it -> !it.toString().equals("null")).filter(it -> !it.toString().substring(0,3).contains("a")).toList();
		}
	}

	public static List<InstanceType> getAllInstanceTypes() {
		return getInstanceTypes(Integer.MAX_VALUE, List.of());
	}


	public static List<InstanceType> getGeneralPurposeInstances(int maxVCPU) {
		return getInstanceTypes(maxVCPU,
				List.of("m*"));
	}

	public static List<InstanceType> getComputeOptimizedInstances(int maxVCPU) {
		return getInstanceTypes(maxVCPU,
				List.of("c*"));
	}


	public static List<InstanceType> getMemoryOptimizedInstances(int maxVCPU) {
		return getInstanceTypes(maxVCPU,
				List.of("r*"));
	}

	public static void main(String[] args) {
		List.of(InstanceType.T3_MICRO,
				InstanceType.T2_MICRO,
				InstanceType.T2_SMALL,
				InstanceType.T3_SMALL,
				InstanceType.C3_2_XLARGE);
		MarketAnalyzer marketAnalyzer = new MarketAnalyzer(
				List.of(Region.US_EAST_1, Region.EU_NORTH_1),
				getAllInstanceTypes());

		;

		marketAnalyzer.getTopN(10).forEach(System.out::println);
	}


}

