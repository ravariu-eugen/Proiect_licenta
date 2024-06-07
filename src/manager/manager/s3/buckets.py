from typing import List, Dict, Tuple
import os
import logging
from boto3.session import Session

from flask import current_app


def get_buckets():
    """Retrieve all S3 buckets.

    Args:
        None

    Returns:
        A tuple containing:
            - A list of dictionaries, each containing the name and region of an S3 bucket.
    """
    session = Session(
        aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID"),
        aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY"),
    )
    s3 = session.resource("s3")
    current_app.logger.info(s3.buckets.all())
    
    s3_client = session.client('s3')
    current_app.logger.info(s3_client.list_buckets())
    
    for bucket in s3.buckets.all():
        current_app.logger.info(bucket.name)
        current_app.logger.info(s3_client.get_bucket_location(Bucket=bucket.name).get('LocationConstraint'))
    bucket_names = [bucket.name for bucket in s3.buckets.all()]
    return bucket_names