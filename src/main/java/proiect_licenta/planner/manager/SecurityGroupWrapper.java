package proiect_licenta.planner.manager;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

public class SecurityGroupWrapper {

    Region region;
    private final Ec2Client client;
    SecurityGroup securityGroup;

    public SecurityGroupWrapper(String securityGroupName, Region region) {
        client = Ec2Client.builder().region(region).build();
        deleteIfExists(securityGroupName);
        this.region = region;
        CreateSecurityGroupResponse response = client.createSecurityGroup(CreateSecurityGroupRequest.builder().groupName(securityGroupName).description("My security group").build());
    }


    public void create(String name, String description){

    }

    private void deleteIfExists(String groupName) {
        DescribeSecurityGroupsRequest describeRequest = DescribeSecurityGroupsRequest.builder()
                .groupNames(groupName).build();
        DeleteSecurityGroupRequest deleteRequest = DeleteSecurityGroupRequest.builder()
                .groupName(groupName).build();
        client.describeSecurityGroups(describeRequest).securityGroups()
                .stream().findFirst().ifPresent(_ -> client.deleteSecurityGroup(deleteRequest));
    }


    public void authorizeIngress(String ingressIP) {
        IpRange ipRange = IpRange.builder().cidrIp(ingressIP + "/32").build();

        IpPermission ipPerm = IpPermission.builder()
                .ipProtocol("tcp").toPort(8080).fromPort(8080)
                .ipRanges(ipRange).build();

        IpPermission ipPerm2 = IpPermission.builder()
                .ipProtocol("tcp").toPort(22).fromPort(22)
                .ipRanges(ipRange).build();

        AuthorizeSecurityGroupIngressRequest authRequest = AuthorizeSecurityGroupIngressRequest.builder()
                .groupName(securityGroup.groupName()).ipPermissions(ipPerm, ipPerm2).build();

        AuthorizeSecurityGroupIngressResponse authResponse = client.authorizeSecurityGroupIngress(authRequest);

        System.out.println(authResponse.toString());

        System.out.printf("Successfully added ingress policy to Security Group %s", securityGroup.groupName());

    }

    public void delete() {
        DeleteSecurityGroupRequest request = DeleteSecurityGroupRequest.builder()
                .groupName(securityGroup.groupName()).build();
        client.deleteSecurityGroup(request);
    }
}
