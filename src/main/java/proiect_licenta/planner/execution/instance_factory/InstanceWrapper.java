package proiect_licenta.planner.execution.instance_factory;

import proiect_licenta.planner.execution.worker.InstanceMetrics;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

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
