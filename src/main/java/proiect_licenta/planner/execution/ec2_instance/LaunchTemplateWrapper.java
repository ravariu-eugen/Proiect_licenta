package proiect_licenta.planner.execution.ec2_instance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public final class LaunchTemplateWrapper {
	private static final Logger logger = LogManager.getLogger();

	private void deleteLaunchTemplate(String name) {
		logger.debug("Deleting launch template: {}", name);
		DeleteLaunchTemplateRequest request = DeleteLaunchTemplateRequest.builder().launchTemplateName(name).build();
		client.deleteLaunchTemplate(request);

	}

	private boolean exists() {
		return client.describeLaunchTemplates().launchTemplates().stream().anyMatch(lt -> lt.launchTemplateName().equals(name));
	}

	private void delete() {
		deleteLaunchTemplate(name);
	}


	private static final Map<String, Subnet> subnetCache = new HashMap<>();
	private final @NotNull Ec2Client client;
	private final @NotNull String name;
	private final @NotNull String ami;
	private final @NotNull InstanceType instanceType;
	private final @NotNull String keyName;
	private final @NotNull String securityGroupName;
	private final String userData;
	private final String availabilityZone;

	public LaunchTemplateWrapper(@NotNull Ec2Client client,
	                             @NotNull String name,
	                             @NotNull String ami,
	                             @NotNull InstanceType instanceType,
	                             @NotNull String keyName,
	                             @NotNull String securityGroupName,
	                             String userData,
	                             String availabilityZone) {
		this.client = client;
		this.name = name;
		this.ami = ami;
		this.instanceType = instanceType;
		this.keyName = keyName;
		this.securityGroupName = securityGroupName;
		this.userData = userData;
		this.availabilityZone = availabilityZone;
	}


	public LaunchTemplateWrapper(@NotNull Ec2Client client,
	                             @NotNull String name,
	                             @NotNull String ami,
	                             @NotNull InstanceType instanceType,
	                             @NotNull String keyName,
	                             @NotNull String securityGroupName,
	                             String userData) {
		this(client, name, ami, instanceType, keyName, securityGroupName, userData, null);
	}

	public LaunchTemplateWrapper(@NotNull Ec2Client client,
	                             @NotNull String name,
	                             @NotNull String ami,
	                             @NotNull InstanceType instanceType,
	                             @NotNull String keyName,
	                             @NotNull String securityGroupName) {
		this(client, name, ami, instanceType, keyName, securityGroupName, null);
	}

	private String getSubnetID(String availabilityZone) {

		if (subnetCache.containsKey(availabilityZone)) {
			return subnetCache.get(availabilityZone).subnetId();
		}

		Subnet subnet = client.describeSubnets()
				.subnets().stream()
				.filter(s -> s.availabilityZone().equals(availabilityZone))
				.findFirst().orElse(null);
		if (subnet != null) {
			subnetCache.put(availabilityZone, subnet);
			return subnet.subnetId();
		}
		return null;
	}


	public void create() {
		if (exists()) {
			// clear existing launch template
			delete();
		}


		RequestLaunchTemplateData.Builder launchTemplateDataBuilder = RequestLaunchTemplateData.builder()
				.imageId(ami)
				.instanceType(instanceType)
				.keyName(keyName)
				.securityGroups(securityGroupName);


		if (userData != null) {
			String base64UserData = Base64.getEncoder().encodeToString(userData.getBytes());

			launchTemplateDataBuilder.userData(base64UserData);
		}

		if (availabilityZone != null) {
			launchTemplateDataBuilder.networkInterfaces(ni -> ni.subnetId(getSubnetID(availabilityZone)));
		}

		CreateLaunchTemplateRequest request = CreateLaunchTemplateRequest.builder()
				.launchTemplateName(name)
				.launchTemplateData(launchTemplateDataBuilder.build())
				.build();

		var response = client.createLaunchTemplate(request);
		logger.debug(response.launchTemplate().launchTemplateId());
	}

	public @NotNull Ec2Client client() {
		return client;
	}

	public @NotNull String name() {
		return name;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (LaunchTemplateWrapper) obj;
		return Objects.equals(this.client, that.client) &&
				Objects.equals(this.name, that.name) &&
				Objects.equals(this.ami, that.ami) &&
				Objects.equals(this.instanceType, that.instanceType) &&
				Objects.equals(this.keyName, that.keyName) &&
				Objects.equals(this.securityGroupName, that.securityGroupName) &&
				Objects.equals(this.userData, that.userData) &&
				Objects.equals(this.availabilityZone, that.availabilityZone);
	}

	@Override
	public int hashCode() {
		return Objects.hash(client, name, ami, instanceType, keyName, securityGroupName, userData, availabilityZone);
	}

	@Override
	public String toString() {
		return "LaunchTemplateWrapper[" +
				"client=" + client + ", " +
				"name=" + name + ", " +
				"ami=" + ami + ", " +
				"instanceType=" + instanceType + ", " +
				"keyName=" + keyName + ", " +
				"securityGroupName=" + securityGroupName + ", " +
				"userData=" + userData + ", " +
				"availabilityZone=" + availabilityZone + ']';
	}


}
