import os
import random

def create_folders_with_numbers(num_folders):
    for i in range(num_folders):
        folder_name = f'folder_{i}'
        os.makedirs(folder_name)
        file_path = os.path.join(folder_name, 'numbers.txt')
        with open(file_path, 'w') as f:
            f.write(f'{random.randint(0, 10000)} {random.randint(0, 10000)}')


def __main__():
    num_folders = 10
    create_folders_with_numbers(num_folders)

if __name__ == '__main__':
    __main__()