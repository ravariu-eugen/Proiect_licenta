"""Routes for logged-in profile."""
from flask import Blueprint, current_app
from elasticIP import ElasticIpWrapper
from instance import InstanceWrapper
from keypair import KeyPairWrapper
from securitygroup import SecurityGroupWrapper

# Blueprint Configuration
ec2_blueprint = Blueprint("ec2_blueprint", __name__, url_prefix="/ec2")






