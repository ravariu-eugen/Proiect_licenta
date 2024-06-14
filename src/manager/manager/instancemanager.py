from manager.compute.instance import InstanceWrapper
from manager.compute.keypair import KeyPairWrapper
from manager.compute.securitygroup import SecurityGroupWrapper
from manager.compute.elasticIP import ElasticIpWrapper

import boto3
import urllib.request
import paramiko
import logging

logger = logging.getLogger("main")


class InstanceManager:
    def __init__(self, name, region="us-east-1"):

        self.inst_wrapper = InstanceWrapper(region)
        self.key_wrapper = KeyPairWrapper(region)
        self.sg_wrapper = SecurityGroupWrapper(region)
        self.eip_wrapper = ElasticIpWrapper(region)
        self.ssm_client = boto3.client("ssm", region_name=region)
        self.region = region
        self.name = name

        self.key_pair_name = name + "-kp"
        self.security_group_name = name + "-sg"
        self.elastic_ip_name = name + "-eip"

    def cleanup(self):
        """
        1. Disassociate and delete the previously created Elastic IP.
        2. Terminate the previously created instance.
        3. Delete the previously created security group.
        4. Delete the previously created key pair.
        """
        logger.info("Let's clean everything up. This example created these resources:")
        logger.info(f"\tElastic IP: {self.eip_wrapper.elastic_ip.allocation_id}")
        logger.info(f"\tInstance: {self.inst_wrapper.instance.id}")
        logger.info(f"\tSecurity group: {self.sg_wrapper.security_group.id}")
        logger.info(f"\tKey pair: {self.key_wrapper.key_pair.name}")
        self.eip_wrapper.disassociate()
        logger.info("Disassociated the Elastic IP from the instance.")
        self.eip_wrapper.release()
        logger.info("Released the Elastic IP.")
        logger.info("Terminating the instance and waiting for it to terminate...")
        self.inst_wrapper.terminate()
        logger.info("Instance terminated.")
        self.sg_wrapper.delete()
        logger.info("Deleted security group.")
        self.key_wrapper.delete_keypair()
        logger.info("Deleted key pair.")

    def create_key_pair(self, key_name):
        """
        1. Creates an RSA key pair and saves its private key data as a .pem file in secure
           temporary storage. The private key data is deleted after the example completes.
        2. Lists the first five key pairs for the current account.
        """
        self.key_wrapper.create_keypair(key_name)

    def create_security_group(self, sg_name, description):
        """
        1. Creates a security group for the default VPC.
        2. Adds an inbound rule to allow SSH. The SSH rule allows only
           inbound traffic from the current computer’s public IPv4 address.
        3. Displays information about the security group.

        This function uses 'http://checkip.amazonaws.com' to get the current public IP
        address of the computer that is running the example. This method works in most
        cases. However, depending on how your computer connects to the internet, you
        might have to manually add your public IP address to the security group by using
        the AWS Management Console.
        """

        security_group = self.sg_wrapper.create(sg_name, description)
        logger.info(
            f"Created security group {security_group.group_name} in your default "
            f"VPC {security_group.vpc_id}.\n"
        )

        ip_response = urllib.request.urlopen("http://checkip.amazonaws.com")
        current_ip_address = ip_response.read().decode("utf-8").strip()
        logger.info("current_ip_address: %s", current_ip_address)
        response = self.sg_wrapper.authorize_ingress(current_ip_address)
        if response["Return"]:
            logger.info("Security group rules updated.")
        else:
            logger.info("Couldn't update security group rules.")
        self.sg_wrapper.describe()

    def create_instance(self, image_id, instance_type):
        """
        1. Gets a list of Amazon Linux 2 AMIs from AWS Systems Manager. Specifying the
           '/aws/service/ami-amazon-linux-latest' path returns only the latest AMIs.
        2. Gets and displays information about the available AMIs and lets you select one.
        3. Gets a list of instance types that are compatible with the selected AMI and
           lets you select one.
        4. Creates an instance with the previously created key pair and security group,
           and the selected AMI and instance type.
        5. Waits for the instance to be running and then displays its information.
        """
        image_choice = self.inst_wrapper.get_images([image_id])[0]

        logger.info("Creating your instance and waiting for it to start...")
        self.inst_wrapper.create(
            image_choice,
            instance_type,
            self.key_wrapper.key_pair,
            [self.sg_wrapper.security_group],
        )
        logger.info(f"Your instance is ready:\n")
        self.inst_wrapper.display()

    def _display_ssh_info(self):
        """
        Displays an SSH connection string that can be used to connect to a running
        instance.
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

    def associate_elastic_ip(self):
        """
        1. Allocates an Elastic IP address and associates it with the instance.
        2. Displays an SSH connection string that uses the Elastic IP address.
        """
        logger.info(
            "You can allocate an Elastic IP address and associate it with your instance\n"
            "to keep a consistent IP address even when your instance restarts."
        )
        elastic_ip = self.eip_wrapper.allocate()
        logger.info(f"Allocated static Elastic IP address: {elastic_ip.public_ip}.")
        self.eip_wrapper.associate(self.inst_wrapper.instance)
        logger.info(f"Associated your Elastic IP with your instance.")
        logger.info(
            "You can now use SSH to connect to your instance by using the Elastic IP."
        )
        self._display_ssh_info()

    def stop_and_start_instance(self):
        """
        1. Stops the instance and waits for it to stop.
        2. Starts the instance and waits for it to start.
        3. Displays information about the instance.
        4. Displays an SSH connection string. When an Elastic IP address is associated
           with the instance, the IP address stays consistent when the instance stops
           and starts.
        """
        logger.info("Let's stop and start your instance to see what changes.")
        logger.info("Stopping your instance and waiting until it's stopped...")
        self.inst_wrapper.stop()
        logger.info("Your instance is stopped. Restarting...")
        self.inst_wrapper.start()
        logger.info("Your instance is running.")
        self.inst_wrapper.display()
        if self.eip_wrapper.elastic_ip is None:
            logger.info(
                "Every time your instance is restarted, its public IP address changes."
            )
        else:
            logger.info(
                "Because you have associated an Elastic IP with your instance, you can \n"
                "connect by using a consistent IP address after the instance restarts."
            )
        self._display_ssh_info()

    def run(self):
        self.create_key_pair(self.key_pair_name)
        self.create_security_group(self.security_group_name, "My security group")

        self.create_instance("ami-08a0d1e16fc3f61ea", "t2.micro")
        self.associate_elastic_ip()
        
        logger.info("Let's run a command on your instance.")
        
        logger.info("Running command: ls -l /home")

        try:
            ssh = paramiko.SSHClient()
            ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            ssh.connect(
                hostname=self.inst_wrapper.instance.public_ip_address,
                username="ec2-user",
                key_filename=self.key_wrapper.key_file_path,
            )
            stdin, stdout, stderr = ssh.exec_command("ls -l /home")
            logger.info(stdout.read().decode())
            ssh.close()
        except Exception as e:
            logger.exception(f"Error executing command: {e}")


        self.cleanup()
