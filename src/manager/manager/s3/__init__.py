from flask import Blueprint
import manager.s3.buckets as buckets

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
    

