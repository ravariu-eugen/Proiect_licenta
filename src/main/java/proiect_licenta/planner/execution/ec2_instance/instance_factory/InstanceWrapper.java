package proiect_licenta.planner.execution.ec2_instance.instance_factory;

import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.StringJoiner;

public class InstanceWrapper {

	private Instance instance;
	private boolean isTerminated = false;

	public InstanceWrapper(Instance instance) {

		this.instance = instance;
	}

	public void update(Instance instance) {
		if (instance == null) {
			this.isTerminated = true;
		}
		this.instance = instance;
	}

	public String publicIpAddress() {
		return instance.publicIpAddress();
	}

	public String instanceId() {
		return instance.instanceId();
	}





	@Override
	public String toString() {
		return new StringJoiner(", ", InstanceWrapper.class.getSimpleName() + "[", "]")
				.add("instance=" + instanceId())
				.add("publicIpAddress=" + publicIpAddress())
				.toString();
	}
}
