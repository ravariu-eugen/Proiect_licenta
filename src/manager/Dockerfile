# Use an official Python runtime as a parent image
FROM python:alpine

# Set the working directory in the container
WORKDIR /app

# Copy the requirements file into the container
COPY requirements.txt .

# Install any needed packages specified in requirements.txt
RUN pip install -r requirements.txt

# Copy the rest of the application code into the container
COPY . .

# Expose port 5000 for the application
EXPOSE 8080

# Run the application when the container launches
CMD ["python", "app.py"]
