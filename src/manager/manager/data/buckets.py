import boto3
from botocore.exceptions import ClientError
from flask import current_app

def get_bucket_location(bucket_name):
    location = (
        boto3.client("s3")
        .get_bucket_location(Bucket=bucket_name)
        .get("LocationConstraint")
    )
    return location or "us-east-1"  # None is 'us-east-1'

def list_buckets():
    """Retrieve all S3 buckets.

    Args:
        None

    Returns:
        - A list of names 
    """
    buckets = []
    
    try:
        s3 = boto3.resource("s3")
        buckets = [
            bucket.name for bucket in s3.buckets.all()
        ]
    except Exception as err:
        current_app.logger.error(
            "Couldn't list buckets. Here's why: %s: %s",
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
    
    return buckets

def create_bucket(bucket_name, region=None):
    """Create an S3 bucket.

    Args:
        bucket_name (str): The name of the bucket to create.
        region (str): The region in which to create the bucket.

    Returns:
        True if the bucket is created, False otherwise.
    """
    try:
        if region == 'us-east-1' or region is None:
            boto3.client("s3").create_bucket(Bucket=bucket_name)
        else:
            location = {"LocationConstraint": region}
            boto3.client("s3", region_name=region).create_bucket(
                Bucket=bucket_name, CreateBucketConfiguration=location
            )
    except ClientError as err:
        current_app.logger.error(
            "Couldn't create bucket %s. Here's why: %s: %s",
            bucket_name,
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
        return False

    return True

def delete_bucket(bucket_name):
    """Delete an S3 bucket.

    Args:
        bucket_name (str): The name of the bucket to delete.

    Returns:
        True if the bucket is deleted, False otherwise.
    """
    try:
        boto3.client("s3").delete_bucket(Bucket=bucket_name)
    except ClientError as err:
        current_app.logger.error(
            "Couldn't delete bucket %s. Here's why: %s: %s",
            bucket_name,
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
        return False
    return True