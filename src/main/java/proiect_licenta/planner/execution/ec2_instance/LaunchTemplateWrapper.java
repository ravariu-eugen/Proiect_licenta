package proiect_licenta.planner.execution.ec2_instance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateLaunchTemplateRequest;
import software.amazon.awssdk.services.ec2.model.DeleteLaunchTemplateRequest;
import software.amazon.awssdk.services.ec2.model.RequestLaunchTemplateData;

import java.util.Base64;
import java.util.Objects;


public final class LaunchTemplateWrapper {
	private static final Logger logger = LogManager.getLogger();

	private final @NotNull Ec2Client client;
	private final @NotNull String name;
	private final @NotNull String ami;
	private final @NotNull String keyName;
	private final @NotNull String securityGroupID;
	private final String userData;

	public LaunchTemplateWrapper(@NotNull Ec2Client client,
	                             @NotNull String name,
	                             @NotNull String ami,
	                             @NotNull String keyName,
	                             @NotNull String securityGroupID,
	                             String userData) {
		this.client = client;
		this.name = name;
		this.ami = ami;
		this.keyName = keyName;
		this.securityGroupID = securityGroupID;
		this.userData = userData;
	}

	public LaunchTemplateWrapper(@NotNull Ec2Client client,
	                             @NotNull String name,
	                             @NotNull String ami,
	                             @NotNull String keyName,
	                             @NotNull String securityGroupName) {
		this(client, name, ami, keyName, securityGroupName, null);
	}

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


	public void create() {
		if (exists()) {
			// clear existing launch template
			delete();
		}

		RequestLaunchTemplateData.Builder launchTemplateDataBuilder = RequestLaunchTemplateData.builder()
				.imageId(ami)
				.keyName(keyName)
				.securityGroupIds(securityGroupID);

		if (userData != null) {
			String base64UserData = Base64.getEncoder().encodeToString(userData.getBytes());

			launchTemplateDataBuilder.userData(base64UserData);
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
				Objects.equals(this.keyName, that.keyName) &&
				Objects.equals(this.securityGroupID, that.securityGroupID) &&
				Objects.equals(this.userData, that.userData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(client, name, ami, keyName, securityGroupID, userData);
	}

	@Override
	public String toString() {
		return "LaunchTemplateWrapper[" +
				"client=" + client + ", " +
				"name=" + name + ", " +
				"ami=" + ami + ", " +
				"keyName=" + keyName + ", " +
				"securityGroupName=" + securityGroupID + ", " +
				"userData=" + userData + ']';
	}


}
