"""Routes for logged-in profile."""
from flask import Blueprint, current_app


# Blueprint Configuration
ec2_blueprint = Blueprint("ec2_blueprint", __name__, url_prefix="/ec2")

