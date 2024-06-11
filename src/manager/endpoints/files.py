from flask import Blueprint, request, current_app
import manager.data.files


# Blueprint Configuration
files_blueprint = Blueprint("files_blueprint", __name__, url_prefix="/files")



@files_blueprint.route("/<bucket_name>", methods=["GET"])
def get_bucket_files(bucket_name):
    """Get a list of files in a specified bucket.

    Args:
        None

    Returns:
        A tuple containing:
            - A list of file names in the bucket, or None if the bucket does not exist.
            - A HTTP status code.
    """
    files = manager.data.files.list_bucket_files(bucket_name)
    if files is None:
        return {"error": "No files found"}, 404
    return {"files": files}, 200


@files_blueprint.route("/<bucket_name>/<filename>", methods=["GET"])
def download_file(bucket_name, filename):
    """Download a file from a specified bucket.

    Args:
        None

    Returns:
        A tuple containing:
            - A dictionary containing the file as a stream, or None if the file is not found.
            - A HTTP status code.
    """
    file = manager.data.files.download_file_from_bucket(bucket_name, filename)
    if file is None:
        return {"error": "File not found"}, 404
    return file, 200



@files_blueprint.route("/<bucket_name>", methods=["POST"])
def upload_file_to_bucket(bucket_name):
    """Upload a file to a specified bucket.

    Args:
        None

    Returns:
        A tuple containing:
            - A dictionary containing the status of the operation.
            - A HTTP status code.
    """
    
    if "file" not in request.files:
        return {"error": "No file was sent"}, 400
    file = request.files["file"]
    current_app.logger.info(file)
    current_app.logger.info(type(file))
    
    current_app.logger.info(file.file_name)
    

    if not file:
        return {"error": "No file was sent"}, 400

    if not manager.data.files.upload_file_to_bucket(bucket_name=bucket_name, file=file, file_name=file.file_name):
        return {"error": "Failed to upload file"}, 500

    return {"message": "File uploaded"}, 200




