def is_valid_region(region: str) -> bool:
    """
    Check if the given region is a valid AWS region.

    Parameters:
        region (str): The AWS region to check.

    Returns:
        bool: True if the region is valid, False otherwise.
    """
    valid_regions = [
        "us-east-1",
        "us-east-2",
        "us-west-1",
        "us-west-2",
        "ap-south-1",
        "ap-northeast-1",
        "ap-northeast-2",
        "ap-northeast-3",
        "ap-southeast-1",
        "ap-southeast-2",
        "ca-central-1",
        "eu-central-1",
        "eu-west-1",
        "eu-west-2",
        "eu-west-3",
        "eu-north-1",
        "sa-east-1",
    ]
    return region in valid_regions
