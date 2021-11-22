import tensorflow as tf
import os

TFLITE_FILE_PATH = os.path.join(os.getcwd(), "enet.tflite")

interpreter = tf.lite.Interpreter(TFLITE_FILE_PATH)

interpreter.resize_tensor_input(0, [20, 320, 320, 3])

interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

print(input_details)
print(output_details)
