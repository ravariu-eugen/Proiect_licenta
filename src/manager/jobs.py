import json

class Job:
    def __init__(self, name, resources, input_data, duration, interruptible=False, penalty=0):
        self.name = name
        self.resources = resources
        self.input_data = input_data
        self.duration = duration
        self.interruptible = interruptible
        self.penalty = penalty

    @classmethod
    def from_file(cls, file_path):
        with open(file_path, 'r') as file:
            job_data = json.load(file)
            
            
            
        return cls(**job_data)
    
    def __str__(self):
        return self.name