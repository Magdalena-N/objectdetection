import tensorflow as tf
import tensorflow_hub as hub
import os

saved_model_dir = os.path.join(os.getcwd(), "enet")

model = tf.keras.Sequential([
    hub.KerasLayer("https://tfhub.dev/tensorflow/efficientnet/b3/classification/1")
])
model.build([None, 320, 320, 3])

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]

lite_model = converter.convert()

with open('enet.tflite', 'wb') as f:
  f.write(lite_model)
