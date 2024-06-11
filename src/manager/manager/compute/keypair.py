import boto3
from botocore.exceptions import ClientError
from flask import current_app
import tempfile
import os


class KeyPairWrapper:
    """Encapsulates Amazon Elastic Compute Cloud (Amazon EC2) key pair actions."""

    def __init__(self, ec2_resource, key_file_dir, key_pair=None):
        """
        :param ec2_resource: A Boto3 Amazon EC2 resource. This high-level resource
                             is used to create additional high-level objects
                             that wrap low-level Amazon EC2 service actions.
        :param key_file_dir: The folder where the private key information is stored.
                             This should be a secure folder.
        :param key_pair: A Boto3 KeyPair object. This is a high-level object that
                         wraps key pair actions.
        """
        self.ec2_resource = ec2_resource
        self.key_pair = key_pair
        self.key_file_path = None
        self.key_file_dir = key_file_dir

    @classmethod
    def from_resource(cls):
        ec2_resource = boto3.resource("ec2")
        return cls(ec2_resource, tempfile.TemporaryDirectory())


def create_keypair(self, key_name):
    """
    Creates a key pair that can be used to securely connect to an EC2 instance.
    The returned key pair contains private key information that cannot be retrieved
    again. The private key data is stored as a .pem file.

    :param key_name: The name of the key pair to create.
    :return: A Boto3 KeyPair object that represents the newly created key pair.
    """
    
    current_app.logger.info("Creating key %s.", key_name)
    try:
        self.key_pair = self.ec2_resource.create_key_pair(KeyName=key_name)
        current_app.logger.info("Created key %s.", self.key_pair.name)
        self.key_file_path = os.path.join(
            self.key_file_dir.name, f"{self.key_pair.name}.pem"
        )
        with open(self.key_file_path, "w") as key_file:
            key_file.write(self.key_pair.key_material)
            
        return self.key_pair
    except ClientError as err:
        current_app.logger.error(
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
    current_app.logger.info("Listing key pairs.")
    try:
        return list(self.ec2_resource.key_pairs.limit(limit))
    except ClientError as err:
        current_app.logger.error(
            "Couldn't list key pairs. Here's why: %s: %s",
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
        raise


def delete_keypair(key_name):
    """
    Deletes a key pair.
    """
    
    

    current_app.logger.info("Deleting key %s.", key_name)
    try:
        response = boto3.client("ec2").delete_key_pair(KeyName=key_name)
        current_app.logger.info("Deleted key %s.", key_name)
        return response['ResponseMetadata']['HTTPStatusCode']
    except ClientError as err:
        current_app.logger.error(
            "Couldn't delete key %s. Here's why: %s : %s",
            key_name,
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
        raise


