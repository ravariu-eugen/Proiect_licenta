package proiect_licenta.planner.execution.analysis;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.helper.ClientHelper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ec2.paginators.DescribeInstanceTypesIterable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MarketAnalyzer {
	private static final Logger logger = LogManager.getLogger();

	private static final List<String> GPprefixes = List.of("m*", "m6.*", "m7.*");
	private static final List<InstanceType> generalPurposeInstances = getGeneralPurposeInstances(8);

	private static final List<String> COprefixes = List.of("c4.*", "c5.*", "c6.*", "c7.*");
	private static final List<InstanceType> computeOptimizedInstances = getComputeOptimizedInstances(8);

	private static final List<String> MOprefixes = List.of("r5.*", "r6.*", "r7.*");
	private static final List<InstanceType> memoryOptimizedInstances = getMemoryOptimizedInstances(8);

	public final List<InstanceConfiguration> spotPrices = new ArrayList<>();
	private final List<Region> usedRegions;
	private final List<InstanceType> usedInstanceTypes;
	private final Map<InstanceType, InstanceTypeInfo> usedInstanceInfo;


	public MarketAnalyzer(List<Region> usedRegions, List<InstanceType> usedInstanceTypes) {
		this.usedRegions = usedRegions;
		this.usedInstanceTypes = usedInstanceTypes;
		System.out.println(usedInstanceTypes.stream().sorted().collect(Collectors.toList()));
		this.usedInstanceInfo = loadUsedInstanceInfo(usedInstanceTypes);

		updateSpotPrices();
	}

	private static List<InstanceTypeInfo> getInfo(List<InstanceType> instanceTypes) {
		if (instanceTypes.size() > 100) {
			throw new IllegalArgumentException("Too many instance types");
		}

		try (Ec2Client ec2Client = ClientHelper.createEC2Client(Region.US_EAST_1)) {
			DescribeInstanceTypesRequest describeInstanceTypesRequest = DescribeInstanceTypesRequest.builder()
					.instanceTypes(instanceTypes)
					.build();
			DescribeInstanceTypesResponse response = ec2Client.describeInstanceTypes(describeInstanceTypesRequest);
			return response.instanceTypes();
		}
	}

	private static Map<InstanceType, InstanceTypeInfo> loadUsedInstanceInfo(List<InstanceType> instanceTypes) {


		var instanceTypeInfoList = instanceTypes.stream()
				.collect(Collectors.groupingBy(it -> (instanceTypes.indexOf(it) / 100)))
				.values()
				.stream()
				.map(MarketAnalyzer::getInfo)
				.flatMap(List::stream)
				.toList();

		return instanceTypeInfoList.parallelStream()
				.collect(Collectors.toMap(InstanceTypeInfo::instanceType, Function.identity()));


	}

	public static List<InstanceType> getInstanceTypes(int maxVCPU, List<String> names) {
		try (Ec2Client ec2Client = ClientHelper.createEC2Client(Region.US_EAST_2)) {


			// no burstable
			Filter noBurstable = Filter.builder().name("burstable-performance-supported").values("false").build();
			// only current gen
			Filter currentGen = Filter.builder().name("current-generation").values("true").build();
			// only x86
			Filter x86 = Filter.builder().name("processor-info.supported-architecture").values("x86_64").build();
			// no baremetal
			Filter noBareMetal = Filter.builder().name("bare-metal").values("false").build();
			// for spot
			Filter spot = Filter.builder().name("supported-usage-class").values("spot").build();
			// only names
			Filter namesFilter = Filter.builder().name("instance-type").values(names).build();
			// max cpu
			List<String> vcpuValues = IntStream.rangeClosed(1, maxVCPU).mapToObj(String::valueOf).toList();
			Filter maxVCPUFilter = Filter.builder().name("vcpu-info.default-vcpus").values(vcpuValues).build();


			DescribeInstanceTypesRequest describeInstanceTypesRequest = DescribeInstanceTypesRequest.builder()
					.filters(namesFilter, currentGen, x86, noBareMetal, spot, noBurstable, maxVCPUFilter)
					.build();
			DescribeInstanceTypesIterable response = ec2Client.describeInstanceTypesPaginator(describeInstanceTypesRequest);

			var instanceTypeInfoList = response.instanceTypes();


			return instanceTypeInfoList.stream()
					.filter(it -> it.vCpuInfo().defaultVCpus() <= maxVCPU)
					.map(InstanceTypeInfo::instanceType)
					.sorted(Comparator.comparing(Enum::name))
					.filter(it -> !it.toString().equals("null"))
					.filter(it -> !it.toString().substring(0, 3).contains("a"))

					.toList();
		}
	}

	public static List<InstanceType> getAllInstanceTypes() {
		return getInstanceTypes(Integer.MAX_VALUE, List.of());
	}

	public static List<InstanceType> getGeneralPurposeInstances(int maxVCPU) {
		return getInstanceTypes(maxVCPU, GPprefixes);
	}

	public static List<InstanceType> getComputeOptimizedInstances(int maxVCPU) {
		return getInstanceTypes(maxVCPU, COprefixes);
	}

	public static List<InstanceType> getMemoryOptimizedInstances(int maxVCPU) {

		return getInstanceTypes(maxVCPU,
				MOprefixes);
	}

	public static void main(String[] args) {
		List.of(InstanceType.T3_MICRO,
				InstanceType.T2_MICRO,
				InstanceType.T2_SMALL,
				InstanceType.T3_SMALL,
				InstanceType.C3_2_XLARGE);
		MarketAnalyzer marketAnalyzer = new MarketAnalyzer(
				List.of(Region.US_EAST_1, Region.EU_NORTH_1),
				getGeneralPurposeInstances(8));


		marketAnalyzer.getTopN(10).forEach(i -> System.out.println(i.availabilityZone() + " " + i.instanceType() + " " + i.pricePerVCPU()));
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


}

