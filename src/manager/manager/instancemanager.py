from manager.compute.instance import InstanceWrapper
from manager.compute.keypair import KeyPairWrapper
from manager.compute.securitygroup import SecurityGroupWrapper

import boto3
import paramiko
import logging
import manager.helper

logger = logging.getLogger("main")


class InstanceManager:
    def __init__(self, name, region="us-east-1"):

        self.inst_wrapper = InstanceWrapper(region)
        self.key_wrapper = KeyPairWrapper(region)
        self.sg_wrapper = SecurityGroupWrapper(region)
        self.ssm_client = boto3.client("ssm", region_name=region)
        self.region = region
        self.name = name

        self.key_pair_name = name + "-kp"
        self.security_group_name = name + "-sg"

    def cleanup(self):
        """
        1. Disassociate and delete the previously created Elastic IP.
        2. Terminate the previously created instance.
        3. Delete the previously created security group.
        4. Delete the previously created key pair.
        """
        logger.info("Let's clean everything up. This example created these resources:")
        logger.info(f"\tInstance: {self.inst_wrapper.instance.id}")
        logger.info(f"\tSecurity group: {self.sg_wrapper.security_group.id}")
        logger.info(f"\tKey pair: {self.key_wrapper.key_pair.name}")

        logger.info("Terminating the instance and waiting for it to terminate...")
        self.inst_wrapper.terminate()
        logger.info("Instance terminated.")
        self.sg_wrapper.delete()
        logger.info("Deleted security group.")
        self.key_wrapper.delete_keypair()
        logger.info("Deleted key pair.")

    def create_security_group(self, sg_name, description):
        """
        1. Creates a security group for the default VPC.
        2. Adds an inbound rule to allow SSH. The SSH rule allows only
           inbound traffic from the current computer’s public IPv4 address.
        3. Displays information about the security group.
        """

        self.sg_wrapper.create(sg_name, description)
        logger.info(
            f"Created security group {self.sg_wrapper.security_group.group_name} in your default "
            f"VPC {self.sg_wrapper.security_group.vpc_id}.\n"
        )

        current_ip_address = manager.helper.current_IP()
        logger.info("current_ip_address: %s", current_ip_address)

        response = self.sg_wrapper.authorize_ingress(current_ip_address)
        if response["Return"]:
            logger.info("Security group rules updated.")
        else:
            logger.info("Couldn't update security group rules.")
        self.sg_wrapper.describe()

    def create_instance(self, image_id, instance_type, userData):
        """
        1. Creates an instance with the selected image and instance type.
        2. Waits for the instance to be running and then displays its information.
        """
        image_choice = self.inst_wrapper.get_images([image_id])[0]

        logger.info("Creating your instance and waiting for it to start...")
        self.inst_wrapper.create(
            image_choice,
            instance_type,
            self.key_wrapper.key_pair,
            userData,
            [self.sg_wrapper.security_group],
        )
        logger.info(f"Your instance is ready:\n")
        self.inst_wrapper.display()

    def _display_ssh_info(self):
        """
        Displays an SSH connection string that can be used to connect to a running instance.
        """
        logger.info(
            "To connect, open another command prompt and run the following command:"
        )
        if self.eip_wrapper.elastic_ip is None:
            logger.info(
                f"\tssh -i {self.key_wrapper.key_file_path} "
                f"ec2-user@{self.inst_wrapper.instance.public_ip_address}"
            )
        else:
            logger.info(
                f"\tssh -i {self.key_wrapper.key_file_path} "
                f"ec2-user@{self.eip_wrapper.elastic_ip.public_ip}"
            )

    def getIP(self):
        return self.inst_wrapper.instance.public_ip_address

    def stop_instance(self):
        """
        Stops the instance and waits for it to stop.
        """
        logger.info("Stopping your instance and waiting until it's stopped...")
        self.inst_wrapper.stop()
        logger.info("Your instance is stopped.")

    def start_instance(self):
        """
        Starts the instance and waits for it to start.
        """
        logger.info("Starting your instance and waiting until it's running...")
        self.inst_wrapper.start()
        logger.info("Your instance is running.")

    def create(
        self, instance_type="t2.micro", 
        image_id="ami-08a0d1e16fc3f61ea", 
        userData=None
    ):
        # create keypair
        self.key_wrapper.create_keypair(self.key_pair_name, "ed25519")
        
        # create security group
        self.create_security_group(self.security_group_name, "My security group")

        # create instance
        self.create_instance(image_id, instance_type, userData)

    def run_command(self, command):
        logger.info("Let's run a command on your instance and see what happens.")
        self.inst_wrapper.instance.wait_until_running()
        logger.info("Running command: " + command)
        try:
            ssh = paramiko.SSHClient()
            ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            ssh.connect(
                hostname=self.inst_wrapper.instance.public_ip_address,
                username="ec2-user",
                key_filename=self.key_wrapper.key_file_path,
            )
            stdin, stdout, stderr = ssh.exec_command(command)
            logger.info(stdout.read().decode())
            ssh.close()
        except Exception as e:
            logger.exception(f"Error executing command: {e}")
