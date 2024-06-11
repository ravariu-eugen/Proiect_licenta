import boto3
from botocore.exceptions import ClientError
from flask import current_app 

def list_all_ec2_instances():
    """
    List all EC2 instances for the current account.

    :return: A list of EC2 instances.
    """
    try:
        ec2_client = boto3.client('ec2')
        response = ec2_client.describe_instances()
        instances = response['Reservations']
        return instances
    except ClientError as err:
        current_app.logger.error(
            "Couldn't list EC2 instances. Here's why: %s: %s",
            err.response['Error']['Code'],
            err.response['Error']['Message'],
        )
        raise


