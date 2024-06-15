import boto3
from botocore.exceptions import ClientError
import logging

logger = logging.getLogger("main")


class SecurityGroupWrapper:
    """Encapsulates Amazon Elastic Compute Cloud (Amazon EC2) security group actions."""

    def __init__(self, region):
        """
        :param region: The region in which the security group is created.
        """
        self.ec2_resource = boto3.resource("ec2", region_name=region)
        self.ec2_client = boto3.client("ec2", region_name=region)
        self.security_group = None

    def create(self, group_name, group_description):
        """
        Creates a security group in the default virtual private cloud (VPC) of the
        current account.

        :param group_name: The name of the security group to create.
        :param group_description: The description of the security group to create.
        """
        logger.info("Creating security group %s.", group_name)
        response = self.ec2_client.describe_security_groups()
        try:

            # check if it already exists
            existing_group = next(
                (
                    group
                    for group in response["SecurityGroups"]
                    if group["GroupName"] == group_name
                ),
                None,
            )
            # delete if it already exists
            if existing_group:
                logger.info(
                    "Security group %s - %s already exists.",
                    group_name,
                    existing_group["GroupId"],
                )
                self.ec2_client.delete_security_group(GroupName=group_name)

            # create
            self.security_group = self.ec2_resource.create_security_group(
                GroupName=group_name, Description=group_description
            )

            logger.info("Created security group %s.", self.security_group.id)
        except ClientError as err:
            logger.error(
                "Couldn't create security group %s. Here's why: %s: %s",
                group_name,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise

    def authorize_ingress(self, ssh_ingress_ip):
        """
        Adds a rule to the security group to allow access to SSH.

        :param ssh_ingress_ip: The IP address that is granted inbound access to connect
                               to port 22 over TCP, used for SSH.
        :return: The response to the authorization request. The 'Return' field of the
                 response indicates whether the request succeeded or failed.
        """
        logger.info("Authorizing inbound rules for %s.", self.security_group.id)
        if self.security_group is None:
            logger.info("No security group to update.")
            return

        try:
            ip_permissions = [
                {
                    # SSH ingress open to only the specified IP address.
                    "IpProtocol": "tcp",
                    "FromPort": 22,
                    "ToPort": 22,
                    "IpRanges": [{"CidrIp": f"{ssh_ingress_ip}/32"}],
                },
                {
                    # HTTP ingress open to only the specified IP address.
                    "IpProtocol": "tcp",
                    "FromPort": 8080,
                    "ToPort": 8080,
                    "IpRanges": [{"CidrIp": f"{ssh_ingress_ip}/32"}],
                },
            ]
            response = self.security_group.authorize_ingress(
                IpPermissions=ip_permissions
            )
            logger.info("Authorized inbound rules for %s.", self.security_group.id)
            return response
        except ClientError as err:
            logger.error(
                "Couldn't authorize inbound rules for %s. Here's why: %s: %s",
                self.security_group.id,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise

    def describe(self):
        """
        Retrieves information about the security group.
        """
        if self.security_group is None:
            logger.info("No security group to describe.")
            return

        logger.info("Getting data for security group %s.", self.security_group.id)
        try:
            return {
                "GroupName": self.security_group.group_name,
                "ID": self.security_group.id,
                "VPC": self.security_group.vpc_id,
                "InboundPermissions": self.security_group.ip_permissions,
            }
        except ClientError as err:
            logger.error(
                "Couldn't get data for security group %s. Here's why: %s: %s",
                self.security_group.id,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise

    def delete(self):
        """
        Deletes the security group.
        """
        logger.info("Deleting security group %s.", self.security_group.id)
        if self.security_group is None:
            logger.info("No security group to delete.")
            return

        group_id = self.security_group.id
        try:
            self.security_group.delete()
            logger.info("Deleted security group %s.", group_id)
        except ClientError as err:
            logger.error(
                "Couldn't delete security group %s. Here's why: %s: %s",
                group_id,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise
