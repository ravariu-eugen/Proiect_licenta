from flask import Flask, request
import logging
from endpoints.auth import auth_blueprint
from endpoints.buckets import buckets_blueprint
from endpoints.files import files_blueprint
import os


def check_credentials():
    
    if request.path == "/auth/keys":
        return
    """Check for credentials before every request."""
    if not (
        os.environ.get("AWS_ACCESS_KEY_ID") and os.environ.get("AWS_SECRET_ACCESS_KEY")
    ):
        return {"error": "AWS credentials not set"}, 401

    # check if credentials are valid
    import boto3

    sts = boto3.client("sts")
    try:
        sts.get_caller_identity()
    except Exception as e:
        return {"error": str(e)}, 401


def create_app():
    """Create Flask application."""
    app = Flask(__name__, instance_relative_config=False)
    app.config["LOG_LEVEL"] = logging.DEBUG

    # Register handlers
    handler = logging.StreamHandler()
    handler.setLevel(app.config["LOG_LEVEL"])
    app.logger.addHandler(handler)
    app.logger.setLevel(app.config["LOG_LEVEL"])

    # Check for credentials on every request

    1

    with app.app_context():
        # Import parts of our application
        app.before_request(check_credentials)
        # Register Blueprints
        app.register_blueprint(auth_blueprint)
        app.register_blueprint(buckets_blueprint)
        app.register_blueprint(files_blueprint)
        return app
