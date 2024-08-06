package proiect_licenta.planner.execution.analysis;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.InstanceTypeInfo;
import software.amazon.awssdk.services.ec2.model.SpotPrice;

public record InstanceConfiguration(Region region,
                                    InstanceTypeInfo instanceTypeInfo,
                                    SpotPrice spotPrice) {


	double price() {
		return Double.parseDouble(spotPrice.spotPrice());
	}

	int vcpuCount() {
		return instanceTypeInfo.vCpuInfo().defaultVCpus();
	}

	long memInMB() {
		return instanceTypeInfo.memoryInfo().sizeInMiB();
	}

	double pricePerVCPU() {
		return price() / vcpuCount();
	}

	public String availabilityZone() {
		return spotPrice.availabilityZone();
	}

	public InstanceType instanceType() {
		return instanceTypeInfo.instanceType();
	}

	@Override
	public String toString() {
		return "InstanceConfiguration{" +
				", availabilityZone='" + availabilityZone() + '\'' +
				", instanceType=" + instanceTypeInfo.instanceType() +
				", spotPrice=" + spotPrice.spotPrice() +
				'}';
	}
}
