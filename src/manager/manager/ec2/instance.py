import boto3
from botocore.exceptions import ClientError
from flask import current_app

logger = current_app.logger


class InstanceWrapper:
    """Encapsulates Amazon Elastic Compute Cloud (Amazon EC2) instance actions."""

    def __init__(self, ec2_resource, instance=None):
        """
        :param ec2_resource: A Boto3 Amazon EC2 resource. This high-level resource
                             is used to create additional high-level objects
                             that wrap low-level Amazon EC2 service actions.
        :param instance: A Boto3 Instance object. This is a high-level object that
                           wraps instance actions.
        """
        self.ec2_resource = ec2_resource
        self.instance = instance

    @classmethod
    def from_resource(cls):
        ec2_resource = boto3.resource("ec2")
        return cls(ec2_resource)

    def create(self, image, instance_type, key_pair, security_groups=None):
        """
        Creates a new EC2 instance. The instance starts immediately after
        it is created.

        The instance is created in the default VPC of the current account.

        :param image: A Boto3 Image object that represents an Amazon Machine Image (AMI)
                      that defines attributes of the instance that is created. The AMI
                      defines things like the kind of operating system and the type of
                      storage used by the instance.
        :param instance_type: The type of instance to create, such as 't2.micro'.
                              The instance type defines things like the number of CPUs and
                              the amount of memory.
        :param key_pair: A Boto3 KeyPair or KeyPairInfo object that represents the key
                         pair that is used to secure connections to the instance.
        :param security_groups: A list of Boto3 SecurityGroup objects that represents the
                                security groups that are used to grant access to the
                                instance. When no security groups are specified, the
                                default security group of the VPC is used.
        :return: A Boto3 Instance object that represents the newly created instance.
        """
        logger.info(
            "Creating instance. "
            "Image: %s, "
            "Instance type: %s, "
            "Key pair: %s, "
            "Security groups: %s",
            image.id,
            instance_type,
            key_pair.name,
            security_groups,
        )
        try:
            instance_params = {
                "ImageId": image.id,
                "InstanceType": instance_type,
                "KeyName": key_pair.name,
            }
            if security_groups is not None:
                instance_params["SecurityGroupIds"] = [sg.id for sg in security_groups]
            self.instance = self.ec2_resource.create_instances(
                **instance_params, MinCount=1, MaxCount=1
            )[0]
            self.instance.wait_until_running()
            logger.info("Created instance %s.", self.instance.id)

            return self.instance
        except ClientError as err:
            logger.error(
                "Couldn't create instance with image %s, instance type %s, and key %s. "
                "Here's why: %s: %s",
                image.id,
                instance_type,
                key_pair.name,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise

    def display(self):
        """
        Displays information about an instance and returns it as a dictionary.
        """
        if self.instance is None:
            logger.info("No instance to display.")
            return None

        logger.info("Displaying instance %s.", self.instance.id)
        try:
            self.instance.load()
            result = {
                "ID": self.instance.id,
                "Image ID": self.instance.image_id,
                "Instance type": self.instance.instance_type,
                "Key name": self.instance.key_name,
                "VPC ID": self.instance.vpc_id,
                "Public IP": self.instance.public_ip_address,
                "State": self.instance.state["Name"],
            }
            return result
        except ClientError as err:
            logger.error(
                "Couldn't display your instance. Here's why: %s: %s",
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise
            raise

    def terminate(self):
        """
        Terminates an instance and waits for it to be in a terminated state.
        """
        if self.instance is None:
            logger.info("No instance to terminate.")
            return
        logger.info("Terminating instance %s.", self.instance.id)
        instance_id = self.instance.id
        try:
            self.instance.terminate()
            logger.info("Waiting for termination of instance %s.", instance_id)
            self.instance.wait_until_terminated()
            logger.info("Terminated instance %s.", instance_id)
            self.instance = None
        except ClientError as err:
            logger.error(
                "Couldn't terminate instance %s. Here's why: %s: %s",
                instance_id,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise

    def start(self):
        """
        Starts an instance and waits for it to be in a running state.

        :return: The response to the start request.
        """
        if self.instance is None:
            logger.info("No instance to start.")
            return

        logger.info("Starting instance %s.", self.instance.id)
        try:
            response = self.instance.start()
            logger.info("Waiting for start of instance %s.", self.instance.id)
            self.instance.wait_until_running()
            logger.info("Started instance %s.", self.instance.id)
            return response
        except ClientError as err:
            logger.error(
                "Couldn't start instance %s. Here's why: %s: %s",
                self.instance.id,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise

    def stop(self):
        """
        Stops an instance and waits for it to be in a stopped state.

        :return: The response to the stop request.
        """
        if self.instance is None:
            logger.info("No instance to stop.")
            return

        logger.info("Stopping instance %s.", self.instance.id)
        try:
            response = self.instance.stop()
            logger.info("Waiting for stop of instance %s.", self.instance.id)
            self.instance.wait_until_stopped()
            logger.info("Stopped instance %s.", self.instance.id)
            return response
        except ClientError as err:
            logger.error(
                "Couldn't stop instance %s. Here's why: %s: %s",
                self.instance.id,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise

    def get_images(self, image_ids):
        """
        Gets information about Amazon Machine Images (AMIs) from a list of AMI IDs.

        :param image_ids: The list of AMIs to look up.
        :return: A list of Boto3 Image objects that represent the requested AMIs.
        """
        logger.info("Getting images %s.", image_ids)
        try:
            images = list(self.ec2_resource.images.filter(ImageIds=image_ids))
            return images
        except ClientError as err:
            logger.error(
                "Couldn't get images. Here's why: %s: %s",
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise

    def get_instance_types(self, architecture):
        """
        Gets instance types that support the specified architecture and are designated
        as either 'micro' or 'small'. When an instance is created, the instance type
        you specify must support the architecture of the AMI you use.

        :param architecture: The kind of architecture the instance types must support,
                             such as 'x86_64'.
        :return: A list of instance types that support the specified architecture
                 and are either 'micro' or 'small'.
        """
        try:
            inst_types = []
            it_paginator = self.ec2_resource.meta.client.get_paginator(
                "describe_instance_types"
            )
            for page in it_paginator.paginate(
                Filters=[
                    {
                        "Name": "processor-info.supported-architecture",
                        "Values": [architecture],
                    },
                    {"Name": "instance-type", "Values": ["*.micro", "*.small"]},
                ]
            ):
                inst_types += page["InstanceTypes"]
        except ClientError as err:
            logger.error(
                "Couldn't get instance types. Here's why: %s: %s",
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise
        else:
            return inst_types
