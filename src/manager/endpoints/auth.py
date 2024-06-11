from flask import Blueprint, request
from typing import Dict, Tuple
from flask import current_app
import boto3

auth_blueprint = Blueprint("auth", __name__, url_prefix="/auth")
@auth_blueprint.route("/keys", methods=["POST"])
def set_aws_keys() -> Tuple[Dict[str, str], int]:
    """
    Set the AWS access key ID and secret access key in the environment variables.

    Returns:
        A tuple containing:
            - A dictionary with a message indicating the success of the operation.
            - A HTTP status code.
    """
    
    current_app.logger.info(request.form)
    try:
        import os

        os.environ["AWS_ACCESS_KEY_ID"] = request.form["aws_access_key_id"]
        os.environ["AWS_SECRET_ACCESS_KEY"] = request.form["aws_secret_access_key"]

        return {"message": "Successfully set AWS keys"}, 200

    except Exception as e:
        return {"error": str(e)}, 500



@auth_blueprint.route("/id", methods=["GET"])
def get_id() -> Tuple[Dict[str, str], int]:
    """
    Get the user aws id 
    Returns:
        A tuple containing:
            - A dictionary with the id.
            - A HTTP status code.
    """
    
    try:
        id = boto3.client('sts').get_caller_identity().get('Account')

        return {"id": id}, 200

    except Exception as e:
        return {"error": str(e)}, 500



