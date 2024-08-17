from keras.models import Sequential
from keras.layers import Dense, Flatten
from joblib import dump, load

input_dir = '/run/input'
shared_dir = '/run/shared'
output_dir = '/run/output'



# Load the MNIST dataset
(X_train, y_train) = load('mnist_data_train.joblib')
(X_test, y_test) = load('mnist_data_test.joblib')

# Normalize pixel values to be between 0 and 1
X_train = X_train.astype('float32') / 255
X_test = X_test.astype('float32') / 255

# Reshape to be a 4D tensor
X_train = X_train.reshape((X_train.shape[0], 28, 28, 1))
X_test = X_test.reshape((X_test.shape[0], 28, 28, 1))

configuration = [32, 16]



# Define the model
model = Sequential()
model.add(Flatten(input_shape=(28, 28, 1)))
for c in configuration:
    model.add(Dense(c, activation='relu'))
model.add(Dense(10, activation='softmax'))

# Compile the model
model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

# Train the model
model.fit(X_train, y_train, epochs=5, batch_size=128)

# Evaluate the model
loss, accuracy = model.evaluate(X_test, y_test)
print('Test accuracy:', accuracy)



# Write to output folder
with open('result.txt', 'w') as file:
    file.write('Test accuracy: ' + str(accuracy))

