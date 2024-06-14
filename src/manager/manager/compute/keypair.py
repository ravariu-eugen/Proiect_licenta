import boto3
from botocore.exceptions import ClientError
import logging
import tempfile
import os
logger = logging.getLogger("main")


class KeyPairWrapper:
    """Encapsulates Amazon Elastic Compute Cloud (Amazon EC2) key pair actions."""

    def __init__(self, region):
        """
        :param ec2_resource: A Boto3 Amazon EC2 resource. This high-level resource
                             is used to create additional high-level objects
                             that wrap low-level Amazon EC2 service actions.
        :param key_file_dir: The folder where the private key information is stored.
                             This should be a secure folder.
        :param key_pair: A Boto3 KeyPair object. This is a high-level object that
                         wraps key pair actions.
        """
        self.ec2_resource = boto3.resource("ec2", region_name=region)
        self.ec2_client = boto3.client("ec2", region_name=region)
        self.key_pair = None
        self.key_file_path = None
        self.key_file_dir = tempfile.TemporaryDirectory()
        self.key_name = None


    def create_keypair(self, key_name):
        """
        Creates a key pair that can be used to securely connect to an EC2 instance.
        The returned key pair contains private key information that cannot be retrieved
        again. The private key data is stored as a .pem file.

        :param key_name: The name of the key pair to create.
        :return: A Boto3 KeyPair object that represents the newly created key pair.
        """
        
        logger.info("Creating key %s.", key_name)
        
        try:
            # check if key already exists
            self.ec2_client.delete_key_pair(KeyName=key_name)
            
            self.key_pair = self.ec2_resource.create_key_pair(KeyName=key_name)
            logger.info("Created key %s.", self.key_pair.name)
            self.key_file_path = os.path.join(
                self.key_file_dir.name, f"{self.key_pair.name}.pem"
            )
            with open(self.key_file_path, "w") as key_file:
                key_file.write(self.key_pair.key_material)
            os.chmod(self.key_file_path, 0o600)
            self.key_name = key_name
            return self.key_pair
        except ClientError as err:
            logger.error(
                "Couldn't create key %s. Here's why: %s: %s",
                key_name,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise
            


    def list(self, limit):
        """
        Retrieves a list of key pairs for the current account.

        :param limit: The maximum number of key pairs to list.
        """
        logger.info("Listing key pairs.")
        try:
            return list(self.ec2_resource.key_pairs.limit(limit))
        except ClientError as err:
            logger.error(
                "Couldn't list key pairs. Here's why: %s: %s",
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise


    def delete_keypair(self):
        """
        Deletes a key pair.
        """
        
        

        logger.info("Deleting key %s.", self.key_name)
        try:
            response = self.ec2_client.delete_key_pair(KeyName=self.key_name)
            logger.info("Deleted key %s.", self.key_name)
            return response['ResponseMetadata']['HTTPStatusCode']
        except ClientError as err:
            logger.error(
                "Couldn't delete key %s. Here's why: %s : %s",
                self.key_name,
                err.response["Error"]["Code"],
                err.response["Error"]["Message"],
            )
            raise


