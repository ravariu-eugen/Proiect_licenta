from flask import Flask
import logging
import manager.s3 as s3

def create_app():
    """Create Flask application."""
    app = Flask(__name__, instance_relative_config=False)
    app.config["LOG_LEVEL"] = logging.DEBUG


    # Register handlers
    handler = logging.StreamHandler()
    handler.setLevel(app.config["LOG_LEVEL"])
    app.logger.addHandler(handler)
    app.logger.setLevel(app.config["LOG_LEVEL"])
    
    
    
    with app.app_context():
        # Import parts of our application

        # Register Blueprints
        app.register_blueprint(s3.s3_blueprint)

        return app
