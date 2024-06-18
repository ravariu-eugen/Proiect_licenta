import os
import json
from manager.instancemanager import InstanceManager
import logging
import requests


# Create a logger
logger = logging.getLogger("main")

# Set the logging level to DEBUG
logger.setLevel(logging.DEBUG)

# Create a console handler
handler = logging.StreamHandler()

# Set the level of the console handler to DEBUG
handler.setLevel(logging.DEBUG)

# Create a formatter and add it to the console handler
formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
handler.setFormatter(formatter)

# Add the console handler to the logger
logger.addHandler(handler)

userData = """#!/bin/bash
mkdir /input
mkdir /output
mkdir /shared
mkdir /worker
mkdir /code
cd /worker
git clone https://github.com/ravariu-eugen/Proiect_licenta_worker
cd Proiect_licenta_worker
pwd > index.html
git help >> index.html
./worker
"""

valid_regions = [
    "us-east-1",
    "eu-north-1",
]


def try_connect(ip):
    
    try:
        response = requests.get("http://" + ip + ":" + "8080", timeout=15)
        logger.info(response.text)
        logger.info(f"{ip} is reachable")
        return True
    except Exception as e:
        logger.error(f"{ip} is not reachable")
        return False

def wait_for_connection(ip, tries=10):
    logger.info("try connecting to " + ip)
    for i in range(tries):
        ok = try_connect(ip)
        if ok:
            logger.info("done on attempt" + str(i))
            break

def s

def main():
    # read the jobs folder
    inst = InstanceManager("instance1", "us-east-1")
    inst.create(userData=userData, instance_type="t3.micro")
    # i.run_command("ls -l /home")

    ip = inst.getIP()
    wait_for_connection(ip)
    
    
    # send aws
    
    data = {
        "accessKeyID": os.getenv("AWS_ACCESS_KEY_ID"),
        "secretAccessKey": os.getenv("AWS_SECRET_ACCESS_KEY"),
    }
    
    try:
        response = requests.post("http://" + ip + ":" + "8080/aws_setup", json=data)
        logger.info(response.text)
    except Exception as e:
        logger.error(f"{ip} is not reachable")
    

    inst.cleanup()


if __name__ == "__main__":
    main()
