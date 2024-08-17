package proiect_licenta.planner.execution.ec2_instance.instance_factory;

import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.InstanceTypeInfo;

import java.util.StringJoiner;

public class InstanceWrapper {

	private final InstanceTypeInfo instanceTypeInfo;
	private Instance instance;
	private boolean isTerminated = false;

	public InstanceWrapper(Instance instance, InstanceTypeInfo instanceTypeInfo) {

		this.instance = instance;
		this.instanceTypeInfo = instanceTypeInfo;
	}

	public void update(Instance instance) {
		if (instance == null) {
			this.isTerminated = true;
		}
		this.instance = instance;
	}

	public int vcpuCount() {
		return instanceTypeInfo.vCpuInfo().defaultVCpus();
	}

	public String publicIpAddress() {
		return instance.publicIpAddress();
	}

	public String instanceId() {
		return instance.instanceId();
	}

	public InstanceType instanceType() {
		return instance.instanceType();
	}


	@Override
	public String toString() {
		return new StringJoiner(", ", InstanceWrapper.class.getSimpleName() + "[", "]")
				.add("instance=" + instanceId())
				.add("publicIpAddress=" + publicIpAddress())
				.toString();
	}
}
