import boto3
from botocore.exceptions import ClientError
from flask import current_app


def list_bucket_files(bucket_name):
    """List the files in a specified bucket.

    Args:
        bucket_name (str): The name of the bucket.

    Returns:
        A list of file names in the bucket, or None if the bucket does not exist.
    """
    try:
        response = boto3.client("s3").list_objects_v2(Bucket=bucket_name)
        file_list = [content["Key"] for content in response["Contents"]]
        return file_list
    except ClientError as err:
        current_app.logger.error(
            "Couldn't list bucket %s. Here's why: %s: %s",
            bucket_name,
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
        return None

def upload_file_to_bucket(bucket_name, file, file_name):
    """Upload a file to a specified bucket.

    Args:
        bucket_name (str): The name of the bucket.
        file_name (str): The path to the file to upload.
        object_name (str): The name of the object in the bucket.

    Returns:
        True if the file is uploaded, False otherwise.
    """
    try:
        boto3.client("s3").upload_fileobj(file, bucket_name, file_name)
    except ClientError as err:
        current_app.logger.error(
            "Couldn't upload file %s. Here's why: %s: %s",
            file_name,
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
        return False

    return True

def delete_file_from_bucket(bucket_name, file_name):
    """Delete a file from a specified bucket.

    Args:
        bucket_name (str): The name of the bucket.
        file_name (str): The name of the file to delete.

    Returns:
        True if the file is deleted, False otherwise.
    """
    try:
        boto3.client("s3").delete_object(Bucket=bucket_name, Key=file_name)
    except ClientError as err:
        current_app.logger.error(
            "Couldn't delete file %s. Here's why: %s: %s",
            file_name,
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
        return False

    return True

def download_file_from_bucket(bucket_name, file_name):
    """Download a file from a specified bucket.

    Args:
        bucket_name (str): The name of the bucket.
        file_name (str): The name of the file to download.

    Returns:
        The contents of the file, or None if the file does not exist.
    """
    try:
        response = boto3.client("s3").get_object(
            Bucket=bucket_name, Key=file_name
        )
        file_content = response["Body"].read().decode("utf-8")
        return file_content
    except ClientError as err:
        current_app.logger.error(
            "Couldn't download file %s. Here's why: %s: %s",
            file_name,
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
        return None

def rename_file_in_bucket(bucket_name, old_file_name, new_file_name):
    """Rename a file in a specified bucket.

    Args:
        bucket_name (str): The name of the bucket.
        old_file_name (str): The name of the file to rename.
        new_file_name (str): The new name of the file.

    Returns:
        True if the file is renamed, False otherwise.
    """
    current_app.logger.info("Renaming file %s to %s", old_file_name, new_file_name)
    try:
        boto3.client("s3").copy_object(
            CopySource={"Bucket": bucket_name, "Key": old_file_name},
            Bucket=bucket_name,
            Key=new_file_name,
        )
        boto3.client("s3").delete_object(
            Bucket=bucket_name, Key=old_file_name
        )
    except ClientError as err:
        current_app.logger.error(
            "Couldn't rename file %s. Here's why: %s: %s",
            old_file_name,
            err.response["Error"]["Code"],
            err.response["Error"]["Message"],
        )
        return False

    return True