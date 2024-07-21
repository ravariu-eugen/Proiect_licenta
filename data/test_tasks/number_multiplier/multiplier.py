import os

# define the folder where the files are located
input_folder = '/input'
# define the folder where the files will be written
output_folder = '/output'

# create the output folder if it does not exist
if not os.path.exists(output_folder):
    os.makedirs(output_folder)

# get a list of files in the input folder
files = os.listdir(input_folder)

# iterate over each file in the input folder
for file in files:
    # open the file and read the content
    with open(os.path.join(input_folder, file), 'r') as f:
        content = f.read().split()
        # extract the two numbers from the file content
        num1 = int(content[0])
        num2 = int(content[1])
        # calculate the product of the two numbers
        product = num1 * num2
        # write the product to a file in the output folder
        with open(os.path.join(output_folder, file), 'w') as g:
            g.write(str(product))
