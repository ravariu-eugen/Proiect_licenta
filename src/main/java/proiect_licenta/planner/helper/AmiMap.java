package proiect_licenta.planner.helper;

import software.amazon.awssdk.regions.Region;

import java.util.List;
import java.util.Map;

public class AmiMap {

	private static final Map<Region, String> regionAmiMap = Map.of(
			Region.EU_NORTH_1, "ami-0b7fba27333bd1857",
			Region.US_EAST_1, "ami-042808b7c0b7dc83e"
	);
	public static List<Region> getRegions() {
		return regionAmiMap.keySet().stream().toList();
	}

	public static String getAmi(Region region) {
		if (!regionAmiMap.containsKey(region)) {
			throw new RuntimeException("Region not supported");
		}
		return regionAmiMap.get(region);
	}
}
