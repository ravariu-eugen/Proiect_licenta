from typing import List, Dict, Tuple
import os
import logging
from boto3.session import Session
import boto3

from flask import current_app
from botocore.exceptions import ClientError


def get_buckets():
    """Retrieve all S3 buckets.

    Args:
        None

    Returns:
        A tuple containing:
            - A list of dictionaries, each containing the name and region of an S3 bucket.
    """

    s3 = boto3.resource("s3")
    current_app.logger.info(s3.buckets.all())
    
    s3_client = boto3.client('s3')
    current_app.logger.info(s3_client.list_buckets())
    
    for bucket in s3.buckets.all():
        current_app.logger.info(bucket.name)
        current_app.logger.info(s3_client.get_bucket_location(Bucket=bucket.name).get('LocationConstraint'))
    bucket_names = [bucket.name for bucket in s3.buckets.all()]
    return bucket_names

def create_bucket(bucket_name, region=None):
    """Create an S3 bucket.

    Args:
        bucket_name (str): The name of the bucket to create.
        region (str): The region in which to create the bucket.

    Returns:
        True if the bucket is created, False otherwise.
    """

    try:
        if region is None:
            s3_client = boto3.client('s3')
            s3_client.create_bucket(Bucket=bucket_name)
        else:
            s3_client = boto3.client('s3', region_name=region)
            location = {'LocationConstraint': region}
            s3_client.create_bucket(Bucket=bucket_name,
                                    CreateBucketConfiguration=location)
    except ClientError as e:
        current_app.logger.error(e)
        return False
    
    return True


def upload_file_to_bucket(file, bucket_name):
    """Upload a file to a specified bucket.

    Args:
        file (werkzeug.datastructures.FileStorage): The file to upload.
        bucket_name (str): The name of the bucket to upload the file to.

    Returns:
        True if the file is uploaded, False otherwise.
    """
    try:

        s3_client = boto3.client('s3')
        s3_client.upload_fileobj(file, bucket_name, file.filename)
    except ClientError as e:
        current_app.logger.error(e)
        return False
    
    return True


def list_bucket_files(bucket_name):
    """List the files in a specified bucket.

    Args:
        bucket_name (str): The name of the bucket.

    Returns:
        A list of file names in the bucket, or None if the bucket does not exist.
    """
    try:
        s3_client = boto3.client('s3')
        response = s3_client.list_objects_v2(Bucket=bucket_name)
        file_list = [content['Key'] for content in response['Contents']]
        return file_list
    except ClientError as e:
        current_app.logger.error(e)
        return None
