package proiect_licenta.planner.instance_manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

public class SecurityGroupWrapper {

	private final Ec2Client client;
	private final String securityGroupName;
	private Logger logger = LogManager.getLogger();
	private boolean isCreated = false;

	public String getSecurityGroupName() {
		return securityGroupName;
	}

	private String securityGroupID;

	public SecurityGroupWrapper(Ec2Client client, String securityGroupName, String description) {
		this.client = client;
		this.securityGroupName = securityGroupName;
		if (checkIfExists(securityGroupName)) {
			deleteSecurityGroup();
		}
		logger.info("create security group: {}", securityGroupName);
		create(client, securityGroupName, description);
		logger.info("created security group: {}", securityGroupID);
	}

	private void create(Ec2Client client, String securityGroupName, String description) {
		CreateSecurityGroupRequest request = CreateSecurityGroupRequest
				.builder()
				.groupName(securityGroupName)
				.description(description)
				.build();

		CreateSecurityGroupResponse response = client.createSecurityGroup(request);
		securityGroupID = response.groupId();
	}

	private boolean checkIfExists(String groupName) {
		logger.info("checkIfExists: {}", groupName);
		DescribeSecurityGroupsRequest describeRequest = DescribeSecurityGroupsRequest.builder().build();
		DescribeSecurityGroupsResponse response = client.describeSecurityGroups(describeRequest);
		return response.securityGroups().stream()
				.anyMatch(sg -> sg.groupName().equals(groupName));
	}

	private void deleteSecurityGroup() {
		if (!checkIfExists(securityGroupName)) {
			return;
		}
		logger.info("deleteIfExists: {}", securityGroupName);
		DeleteSecurityGroupRequest deleteRequest = DeleteSecurityGroupRequest.builder()
				.groupName(securityGroupName).build();
		client.deleteSecurityGroup(deleteRequest);

	}


	public void authorizeIngress(String ingressIP) {
		logger.info("authorizeIngress: {}", ingressIP);
		IpRange ipRange = IpRange.builder().cidrIp(ingressIP + "/32").build();

		IpPermission ipPerm = IpPermission.builder()
				.ipProtocol("tcp")
				.toPort(8080)
				.fromPort(8080)
				.ipRanges(ipRange).build();

		IpPermission ipPerm2 = IpPermission.builder()
				.ipProtocol("tcp")
				.toPort(22)
				.fromPort(22)
				.ipRanges(ipRange).build();

		AuthorizeSecurityGroupIngressRequest authRequest = AuthorizeSecurityGroupIngressRequest.builder()
				.groupName(securityGroupName)
				.ipPermissions(ipPerm, ipPerm2)
				.build();

		AuthorizeSecurityGroupIngressResponse authResponse = client.authorizeSecurityGroupIngress(authRequest);

		isCreated = authResponse.returnValue();
		if (isCreated) {
			logger.info("Successfully added ingress policy to Security Group {}", securityGroupName);
		}

	}

	public void delete() {
		logger.info("delete: {}", securityGroupName);
		DeleteSecurityGroupRequest request = DeleteSecurityGroupRequest.builder()
				.groupName(securityGroupName).build();
		client.deleteSecurityGroup(request);
	}
}
