"""Routes for logged-in profile."""
from flask import Blueprint, current_app
from manager.compute.elasticIP import ElasticIpWrapper
from manager.compute.instance import InstanceWrapper
from manager.compute.keypair import KeyPairWrapper
from manager.compute.securitygroup import SecurityGroupWrapper
import boto3
from flask import request


# Blueprint Configuration
ec2_blueprint = Blueprint("ec2_blueprint", __name__, url_prefix="/ec2")





@ec2_blueprint.route('/instances', methods=['GET'])
def get_all_instances():
    """
    Returns a list of all instances.

    :return: A JSON object containing a list of instances.
    """
    region = request.args.get('region')
    current_app.logger.info("Getting all instances in region %s.", region)
    ec2 = boto3.resource('ec2', region_name=region)
    instances = ec2.instances.all()
    return [instance.to_dict() for instance in instances]


@ec2_blueprint.route('/keypairs', methods=['GET'])
def list_all_keypairs():
    """
    Returns a list of all key pairs.

    :return: A JSON object containing a list of key pairs.
    """
    region = request.args.get('region')
    if not region:
        return 'Missing region', 400
    current_app.logger.info("Listing all key pairs in region %s.", region)
    ec2_resource = boto3.resource('ec2', region_name=region)
    keypair_wrapper = KeyPairWrapper.from_resource(ec2_resource)
    return keypair_wrapper.list(limit=1000), 200




