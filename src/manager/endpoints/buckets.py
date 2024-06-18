from flask import Blueprint, request
from manager.helper import is_valid_region
import manager.data.buckets as buckets


# Blueprint Configuration
buckets_blueprint = Blueprint("buckets_blueprint", __name__, url_prefix="/buckets")


@buckets_blueprint.route("/", methods=["GET"])
def get_buckets():
    """Retrieve all S3 buckets.

    Args:
        None

    Returns:
        A tuple containing:
            - A list of dictionaries, each containing the name and region of an S3 bucket.
            - A HTTP status code.
    """
    r = buckets.list_buckets()
    if r is None:
        return {"error": "No buckets found"}, 404
    return r, 200
    

@buckets_blueprint.route("/", methods=["POST"])
def create_bucket():
    """Create an S3 bucket.

    Args:
        None

    Returns:
        A tuple containing:
            - A dictionary containing the status of the operation.
            - A HTTP status code.
    """
    bucket_name = request.args.get("bucket_name")
    region = request.args.get("region")
    
    if not bucket_name:
        return {"error": "Bucket name is required"}, 400
    
    if not region:
        return {"error": "Region is required"}, 400
    
    if not is_valid_region(region):
        return {"error": "Invalid region"}, 400

    
    if not buckets.create_bucket(bucket_name, region):
        return {"error": "Failed to create bucket"}, 500
    return {"message": f"Bucket {bucket_name} created"}, 200


@buckets_blueprint.route("/<bucket_name>", methods=["DELETE"])
def delete_bucket(bucket_name):
    """Delete an S3 bucket.

    Args:
        bucket_name (str): The name of the bucket to delete.

    Returns:
        A tuple containing:
            - A dictionary containing the status of the operation.
            - A HTTP status code.
    """
    if not buckets.delete_bucket(bucket_name):
        return {"error": "Failed to delete bucket"}, 500
    return {"message": f"Bucket {bucket_name} deleted"}, 200




