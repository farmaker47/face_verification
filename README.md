# Under construction
____________________________________________________________

# face_verification
Kotlin android project of face verification using Keras/Tensorflow model.

# This is a project of face verification where it will include python files for training the model
and implement this model with Tensorflow Lite inside android application.
Android app will use CameraX library for images.

Check this repo if you want to get an idea of CameraX API usage, MVVM, KOIN_DI. Also example of Tensorflow lite model inside android.

This project is in __Kotlin__ language and has:

1) CameraX API usage.
2) Tensorflow Lite usage with custom model
3) Tensorflow model generated from Siamese netwotk with Triplet loss function.

also:

4) Databinding
5) Navigation Component usage
6) MVVM with Coroutines
7) Koin DI

**What is face verification?**

It can be thought of as a classification problem where the identity of the person is verified using a matching score. So, if two images are of the same person, they should have a high matching score and if the two images are of two different persons, the match should be low.
The first thing that may come to your mind is, why not match the captured picture with another picture pixel-wise? If the distance(mean-squared or absolute) between pixel values of the captured image and that of the other are small, they must correspond to the same person. But since the pixel values in an image change dramatically even with a slight change of light, position or orientation, this method might not, and in fact does not, work well.
So, what do we do now? This is where convolutional neural networks, better known as CNNs, help us. Such networks can help us better represent images by embedding each image into a d-dimensional vector space. The image embeddings are then evaluated for similarity.

The on-device model comes with several benefits. It is:

- Faster: The model resides on the device and does not require internet connectivity. Thus, the inference is very fast and has an average latency of only a few milliseconds.
- Resource efficient: The model has a small memory footprint on the device.
- Privacy-friendly: The user data never leaves the device and this eliminates any privacy restrictions.
