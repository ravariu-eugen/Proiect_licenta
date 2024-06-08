from flask import Blueprint, request
import manager.s3.buckets as buckets
import manager.region.region as region

from typing import List, Dict, Tuple

# Blueprint Configuration
s3_blueprint = Blueprint("s3_blueprint", __name__, url_prefix="/s3")


@s3_blueprint.route("/buckets", methods=["GET"])
def get_buckets() -> Tuple[List[Dict[str, str]], int]:
    """Retrieve all S3 buckets.

    Args:
        None

    Returns:
        A tuple containing:
            - A list of dictionaries, each containing the name and region of an S3 bucket.
            - A HTTP status code.
    """
    r = buckets.get_buckets()
    if r is None:
        return {"error": "No buckets found"}, 404
    return r, 200
    

@s3_blueprint.route("/buckets", methods=["POST"])
def create_bucket() -> Tuple[Dict[str, str], int]:
    """Create an S3 bucket.

    Args:
        None

    Returns:
        A tuple containing:
            - A dictionary containing the status of the operation.
            - A HTTP status code.
    """
    bucket_name = request.form["bucket"]
    region = request.form["region"]
    
    if not buckets.is_valid_region(region):
        return {"error": "Invalid region"}, 400

    
    if not buckets.create_bucket(bucket_name, region):
        return {"error": "Failed to create bucket"}, 500
    return {"message": "Bucket created"}, 200



@s3_blueprint.route("/buckets/<bucket_name>", methods=["POST"])
def upload_file_to_bucket(bucket_name) -> Tuple[Dict[str, str], int]:
    """Upload a file to a specified bucket.

    Args:
        None

    Returns:
        A tuple containing:
            - A dictionary containing the status of the operation.
            - A HTTP status code.
    """
    file = request.files["file"]


    if not file:
        return {"error": "No file was sent"}, 400

    if not buckets.upload_file_to_bucket(file, bucket_name):
        return {"error": "Failed to upload file"}, 500

    return {"message": "File uploaded"}, 200




