package proiect_licenta.planner.manager;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

public class SecurityGroupWrapper {

    Region region;
    private final Ec2Client client;
    SecurityGroup securityGroup;

    public SecurityGroupWrapper(String securityGroupName, Region region) {
        this.region = region;
        client = Ec2Client.builder().region(region).build();

    }

    private void deleteIfExists(String groupName) {
        client.describeSecurityGroups(
                        DescribeSecurityGroupsRequest.builder()
                                .groupNames(groupName).build()
                ).securityGroups().stream().findFirst()
                .ifPresent(
                        _ -> client.deleteSecurityGroup(
                                DeleteSecurityGroupRequest.builder()
                                        .groupName(groupName).build()
                        )
                );
    }


    public void createSecurityGroup(String groupName) {
        deleteIfExists(groupName);
        CreateSecurityGroupResponse response = client.createSecurityGroup(
                CreateSecurityGroupRequest.builder()
                        .groupName(groupName)
                        .description("My security group")
                        .build()
        );

        DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder().groupNames(groupName).build();
        DescribeSecurityGroupsResponse resp = client.describeSecurityGroups(request);
        securityGroup = resp.securityGroups().getFirst();

    }

    public void authorizeSecurityGroupIngress(String ingressIP) {
        IpRange ipRange = IpRange.builder()
                .cidrIp(ingressIP + "/32").build();

        IpPermission ipPerm = IpPermission.builder()
                .ipProtocol("tcp")
                .toPort(8080)
                .fromPort(8080)
                .ipRanges(ipRange)
                .build();

        IpPermission ipPerm2 = IpPermission.builder()
                .ipProtocol("tcp")
                .toPort(22)
                .fromPort(22)
                .ipRanges(ipRange)
                .build();

        AuthorizeSecurityGroupIngressRequest authRequest =
                AuthorizeSecurityGroupIngressRequest.builder()
                        .groupName(securityGroup.groupName())
                        .ipPermissions(ipPerm, ipPerm2)
                        .build();

        AuthorizeSecurityGroupIngressResponse authResponse =
                client.authorizeSecurityGroupIngress(authRequest);

        System.out.printf(
                "Successfully added ingress policy to Security Group %s",
                securityGroup.groupName());

    }
}
