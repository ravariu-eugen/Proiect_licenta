package proiect_licenta.planner.execution.ec2_instance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

public class SecurityGroupWrapper {

	private final Ec2Client client;
	private final String securityGroupName;
	private final Logger logger = LogManager.getLogger();
	private String securityGroupID;

	public SecurityGroupWrapper(Ec2Client client, String securityGroupNamePrefix, String description) {
		this.client = client;
		int i = 0;
		String sgName = securityGroupNamePrefix + i;
		logger.debug("securityGroupNamePrefix: {}", securityGroupNamePrefix);
		while (checkIfExists(sgName)) {
			try {
				deleteSecurityGroup(sgName);
				logger.debug("Deleted security group {}", sgName);
				break;
			} catch (Exception e) {
				logger.error(e.getMessage());
				i++;
				sgName = securityGroupNamePrefix + i;

			}
		}

		this.securityGroupName = sgName;

		logger.debug("create security group: {}", securityGroupName);
		create(client, securityGroupName, description);
	}

	public String getSecurityGroupName() {
		return securityGroupName;
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
		DescribeSecurityGroupsRequest describeRequest = DescribeSecurityGroupsRequest.builder().build();
		DescribeSecurityGroupsResponse response = client.describeSecurityGroups(describeRequest);

		var names = response.securityGroups().stream().map(SecurityGroup::groupName).toList();
		return names.contains(groupName);
	}

	private void deleteSecurityGroup(String securityGroupName) throws Exception {
		if (!checkIfExists(securityGroupName)) {
			return;
		}
		logger.debug("deleteIfExists: {}", securityGroupName);
		DeleteSecurityGroupRequest deleteRequest = DeleteSecurityGroupRequest.builder()
				.groupName(securityGroupName).build();
		var response = client.deleteSecurityGroup(deleteRequest);

		logger.debug(response.toString());
	}


	public void authorizeAll() {
		authorizeIP("0.0.0.0/0");
	}

	private boolean authorizeIP(String IP) {
		IpRange ipRange = IpRange.builder().cidrIp(IP).build();

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

		return authResponse.returnValue();
	}

	public void authorizeIngress(String ingressIP) {
		logger.debug("authorizeIngress: {}", ingressIP);
		boolean result = authorizeIP(ingressIP + "/32");
		if (result) {
			logger.info("Authorized ingress IP: {}", ingressIP);
		}
	}

	public void delete() {
		try {
			deleteSecurityGroup(securityGroupName);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}


	public String getSecurityGroupID() {
		return securityGroupID;
	}
}
