package com.george.face_verification.camera

import android.app.Application
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.SparseArray
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.george.face_verification.MainActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragmentViewModel(app: Application) : AndroidViewModel(app) {

    private var _selectedPhotoPath = MutableLiveData<String>()

    // The external LiveData for the __
    private val selectedPhotoPath: LiveData<String>
        get() = _selectedPhotoPath

    var _trueOrFalsePhoto = MutableLiveData<Boolean>()

    // The external LiveData for the __
    val trueOrFalsePhoto: LiveData<Boolean>
        get() = _trueOrFalsePhoto

    private var pathOfPhoto: String?
    private lateinit var originalBitmap: Bitmap
    private var rotatedBitmap: Bitmap? = null
    private var rotatedBitmapSecondOutput: Bitmap? = null
        //Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_8888)
    private val context = getApplication<Application>().applicationContext
    private var faceDetector: FaceDetector? = null
    private var outputDirectory: File
    private var outputDirectoryTenPhotos: File? = null
    private var listOfPhotos: Array<File>? = null
    private lateinit var interpreter: Interpreter

    /** Executor to run inference task in the background. */
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    private var inputImageWidth: Int = 0 // will be inferred from TF Lite model.
    private var inputImageHeight: Int = 0 // will be inferred from TF Lite model.
    private var modelInputSize: Int = 0 // will be inferred from TF Lite model.
    private var outputShape: IntArray = intArrayOf(0) // will be inferred from TF Lite model.
    var isInitialized = false
        private set

    // Initialize the __ MutableLiveData
    init {
        pathOfPhoto = ""

        viewModelScope.launch {
            initializeInterpreter(app)
        }

        faceDetector = FaceDetector.Builder(context)
            .setTrackingEnabled(false)
            .build()

        outputDirectory =
            MainActivity.getOutputDirectory(
                context
            )


        //_trueOrFalsePhoto.value = false
    }

    // Initialize interpreter
    @Throws(IOException::class)
    private suspend fun initializeInterpreter(app: Application) = withContext(Dispatchers.IO) {
        // Load the TF Lite model from asset folder and initialize TF Lite Interpreter without NNAPI enabled.
        val assetManager = app.assets
        val model = loadModelFile(assetManager, "face_recog_model_layer.tflite")
        val options = Interpreter.Options()
        options.setUseNNAPI(false)
        interpreter = Interpreter(model, options)
        // Reads type and shape of input and output tensors, respectively.
        val imageTensorIndex = 0
        val inputShape: IntArray =
            interpreter.getInputTensor(imageTensorIndex).shape() // {1, length}
        Log.e("INPUT_TENSOR_WHOLE", Arrays.toString(inputShape))
        val imageDataType: DataType =
            interpreter.getInputTensor(imageTensorIndex).dataType()
        Log.e("INPUT_DATA_TYPE", imageDataType.toString())

        //modelInputSize indicates how many bytes of memory we should allocate to store the input for our TensorFlow Lite model.
        //FLOAT_TYPE_SIZE indicates how many bytes our input data type will require. We use float32, so it is 4 bytes.
        //PIXEL_SIZE indicates how many color channels there are in each pixel. Our input image is a colored image, so we have 3 color channel.
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth *
                inputImageHeight * PIXEL_SIZE

        val probabilityTensorIndex = 0
        outputShape =
            interpreter.getOutputTensor(probabilityTensorIndex).shape()// {1, NUM_CLASSES}
        Log.e("OUTPUT_TENSOR_SHAPE", outputShape.contentToString())
        val probabilityDataType: DataType =
            interpreter.getOutputTensor(probabilityTensorIndex).dataType()
        Log.e("OUTPUT_DATA_TYPE", probabilityDataType.toString())
        isInitialized = true
        Log.e(TAG, "Initialized TFLite interpreter.")


        // Inputs outputs
        /*val inputTensorModel: Int = interpreter.getInputIndex("input_1")
        Log.e("INPUT_TENSOR", inputTensorModel.toString())*/

    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, filename: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    //originalBitmap = getBitmapFromAsset(app, "george_black.jpg")
    private fun getBitmapFromAsset(context: Context, path: String): Bitmap =
        context.assets.open(path).use { BitmapFactory.decodeStream(it) }

    // Uri to bitmap
    private fun uriToBitmap(selectedFileUri: Uri): Bitmap {
        lateinit var image: Bitmap
        try {
            val parcelFileDescriptor: ParcelFileDescriptor? =
                context.contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    private suspend fun getBitmap() = withContext(Dispatchers.IO) {

        /*Log.e("IMPORTANT", "IMPORTANT")
        Thread(Runnable {
            //run on UI
        }).start()*/

        try {

            // status loading
            withContext(Dispatchers.Main) {
                // call to UI thread

            }


        } catch (e: IOException) {
            Log.e("EXCEPTION", e.toString())

            withContext(Dispatchers.Main) {
                // call to UI thread
            }
        }

        //_statusProgress.value = NewsApiStatus.DONE

    }

    // This is called from Camera fragment to set the path of latest photo taken
    fun setPathOfLatestPhoto(path: String) {

        outputDirectoryTenPhotos =
            MainActivity.getOutputDirectoryContentTenPictures(
                context
            )
        listOfPhotos = outputDirectoryTenPhotos?.listFiles()
        Log.e("LIST_OF_PHOTOS", listOfPhotos?.contentToString())

        // If you use Koin injection
        //pathOfPhoto = getKoin().getProperty("pathInfo")
        pathOfPhoto = path
        // Retrieve original bitmap
        originalBitmap = uriToBitmap(path.toUri())

        Log.i("WIDTH_ORIGINAL", originalBitmap.width.toString())
        Log.i("HEIGHT_ORIGINAL", originalBitmap.height.toString())
        Log.e("PATH_TO_URI", path)

        // Get specific path for EXIFINTERFACE
        val postLongStr: String = path.substring(7, path.length)
        Log.i("AFTER", postLongStr)
        // ExifInterface wants uri starting with /storage.. so we truncate string path
        // Also we modify orientation so Face detector gets faces
        rotatedBitmap = modifyOrientation(originalBitmap, postLongStr)

        // Everything in background thread because it does heavy computation
        viewModelScope.launch {
            detectFaces(rotatedBitmap)
        }

    }

    private suspend fun detectFaces(bitmap: Bitmap?) = withContext(Dispatchers.IO) {

        // Create a Paint object for drawing with
        /*val myRectPaint = Paint()
        myRectPaint.strokeWidth = 5F
        myRectPaint.color = Color.RED
        myRectPaint.style = Paint.Style.STROKE*/

        // Create a Canvas object for drawing on
        val tempBitmap =
            Bitmap.createBitmap(bitmap!!.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0F, 0F, null)

        // Create the Face Detector with parameters
        /*val detector: FaceDetector = FaceDetector.Builder(context)
            .setTrackingEnabled(true)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setMode(FaceDetector.ACCURATE_MODE)
            .build()*/


        if (!faceDetector!!.isOperational) {
            AlertDialog.Builder(context).setMessage("Could not set up the face detector!").show()
        }

        // Detect the Faces
        val frame: Frame = Frame.Builder().setBitmap(bitmap).build()
        // Use facedetector or detector object
        val faces: SparseArray<Face>? = faceDetector?.detect(frame)
        Log.e("NUMBER_FACES", faces!!.size().toString())

        // For every face get coordinates and draw red rectangle if desired
        var x1 = 0
        //var x2 = 0
        var y1 = 0
        //var y2 = 0
        var width = 0
        var height = 0
        if (faces.size() > 0) {
            for (i in 0 until faces.size()) {
                val thisFace = faces.valueAt(i)
                x1 = thisFace.position.x.toInt()
                y1 = thisFace.position.y.toInt()
                Log.i("POSITION_X", x1.toString())
                Log.i("POSITION_Y", y1.toString())
                //x2 = (x1 + thisFace.width).toInt()
                //y2 = (y1 + thisFace.height).toInt()
                width = thisFace.width.toInt()
                height = thisFace.height.toInt()
                /*tempCanvas.drawRoundRect(
                    RectF(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat()),
                    2f,
                    2f,
                    myRectPaint
                )*/
            }

            // Use this to widen picture on top or bottom
            val croppedFaceBitmap =
                Bitmap.createBitmap(tempBitmap, x1, y1 - 200, width, height + 300)

            // Create file to save picture
            // Create output file to hold the image
            val photoFile =
                CameraFragment.createFile(
                    outputDirectory,
                    CameraFragment.FILENAME,
                    CameraFragment.PHOTO_EXTENSION
                )

            // Use this specific name for created face bitmap
            // saveBitmapToPhone(croppedFaceBitmap)

            // After all this procedure we pass our bitmap inside interpreter
            classify(croppedFaceBitmap)

        }

    }

    private fun saveBitmapToPhone(croppedFaceBitmap: Bitmap) {
        val mypath = File(outputDirectory, "molvedo_2.jpg")
        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(mypath, false)
            croppedFaceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            Log.i("SAVE_IMAGE", e.message, e)
        }
    }

    // Modify orientation to help face detector
    private fun modifyOrientation(bitmap: Bitmap, image_absolute_path: String): Bitmap? {
        val ei =
            ExifInterface(image_absolute_path)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        Log.e("ORIENTATION", orientation.toString())
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270F)

            // My phone Xiaomi RedMi Note
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                rotateImageTransverse(bitmap, 270F, false, true)
                //flipImage(bitmap, true, false)
            }

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipImage(bitmap, true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipImage(bitmap, false, true)
            else -> bitmap
        }
    }

    // Transverse orientation aka int = 7
    private fun rotateImageTransverse(
        img: Bitmap,
        degree: Float,
        horizontal: Boolean,
        vertical: Boolean
    ): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
        val rotatedImg =
            Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg =
            Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    private fun flipImage(bitmap: Bitmap?, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun classifyAsync(bitmap: Bitmap): Task<String> {
        return Tasks.call(executorService, Callable { classify(bitmap) })
    }

    private fun classify(bitmap: Bitmap): String {
        check(isInitialized) { "TF Lite Interpreter is not initialized yet." }

        // Pre-processing: resize the input image to match the model input shape.
        val resizedImage = Bitmap.createScaledBitmap(
            bitmap,
            inputImageWidth,
            inputImageHeight,
            true
        )
        saveResizedBitmapToPhone(resizedImage, "molvedo_first.jpg")
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        // Define an array to store the model output.
        // Outputshape[1] = 64
        val output = Array(1) { FloatArray(outputShape[1]) }

        // Run inference with the input data.
        interpreter.run(byteBuffer, output)

        // Post-processing: find the digit that has the highest probability
        // and return it a human-readable string.
        val result = output[0]
        Log.e("RESULT", result.contentToString())

        // Max value of array
        val maxIndex = result.indices.maxBy { result[it] } ?: -1
        val maxValue = result[maxIndex]
        Log.e("MAX_VALUE", maxValue.toString())

        // Divide generated array with max number to avoid large numbers
        val arrayOfDividedNumbers: FloatArray = FloatArray(outputShape[1]) { 0F }
        for ((index, number) in result.withIndex()) {
            arrayOfDividedNumbers[index] = number // maxValue
        }
        Log.e("DIVIDED_ARRAY", arrayOfDividedNumbers.contentToString())


        /////////////////////////////////////////
        ////////////////////////////////////////
        // Second image
        // file:///storage/emulated/0/Android/media/com.george.face_verification/face_verification/molvedo.jpg

        val photo = listOfPhotos!![(0..9).random()]
        Log.e("PHOTO_RANDOM", photo.toString())

        val outPutSecondBitmap =
        //uriToBitmap("file:///storage/emulated/0/Android/media/com.george.face_verification/face_verification/molvedo.jpg".toUri())
            //uriToBitmap(("file://" + listOfPhotos[0].toString()).toUri())
            uriToBitmap(photo.toUri())

        // ExifInterface wants uri starting with /storage.. so we truncate string path
        // Also we modify orientation so Face detector gets faces
        rotatedBitmapSecondOutput = modifyOrientation(outPutSecondBitmap, photo.toString())

        val resizedSecondImage = rotatedBitmapSecondOutput?.let {
            Bitmap.createScaledBitmap(
                it,
                inputImageWidth,
                inputImageHeight,
                true
            )
        }
        saveResizedBitmapToPhoneNull(resizedSecondImage, "molvedo_second.jpg")
        val byteBufferSecond = convertBitmapToByteBufferNull(resizedSecondImage)

        // Define an array to store the model output.
        // Outputshape[1] = 64
        val outputSecond = Array(1) { FloatArray(outputShape[1]) }

        // Run inference with the input data.
        interpreter.run(byteBufferSecond, outputSecond)

        /*interpreter.getOutputTensor(-1)
        interpreter.*/

        // Post-processing: find the digit that has the highest probability
        // and return it a human-readable string.
        val resultSecond = outputSecond[0]
        Log.e("RESULT_Second", resultSecond.contentToString())

        // Divide generated array with max number to avoid large numbers
        val arrayOfDividedNumbersSecond = FloatArray(outputShape[1]) { 0F }
        for ((index, number) in resultSecond.withIndex()) {
            arrayOfDividedNumbersSecond[index] = number // maxValue
        }
        Log.e("DIVIDED_ARRAY_Second", arrayOfDividedNumbersSecond.contentToString())

        /////////////////////////////////////////
        ////////////////////////////////////////
        // Third image
        // file:///storage/emulated/0/Android/media/com.george.face_verification/face_verification/molvedo.jpg

        val outPutThirdBitmap = getBitmapFromAsset(context, "generated_fake.JPG")
        //uriToBitmap("file:///storage/emulated/0/Android/media/com.george.face_verification/face_verification/molvedo_3.jpg".toUri())
        val resizedThirdImage = Bitmap.createScaledBitmap(
            outPutThirdBitmap,
            inputImageWidth,
            inputImageHeight,
            true
        )
        saveResizedBitmapToPhone(resizedThirdImage, "molvedo_third.jpg")
        val byteBufferThird = convertBitmapToByteBuffer(resizedThirdImage)

        // Define an array to store the model output.
        // Outputshape[1] = 64
        val outputThird = Array(1) { FloatArray(outputShape[1]) }

        // Run inference with the input data.
        interpreter.run(byteBufferThird, outputThird)

        val resultThird = outputThird[0]
        Log.e("RESULT_Third", resultThird.contentToString())

        // Divide generated array with max number to avoid large numbers
        val arrayOfDividedNumbersThird = FloatArray(outputShape[1]) { 0F }
        for ((index, number) in resultThird.withIndex()) {
            arrayOfDividedNumbersThird[index] = number // maxValue
        }
        Log.e("DIVIDED_ARRAY_Third", arrayOfDividedNumbersThird.contentToString())


        // Find MSE of two arrays to find the loss between two images
        var sum_Pos = 0.0
        for (i in 0 until arrayOfDividedNumbers.size) {
            val diff = arrayOfDividedNumbers[i] - arrayOfDividedNumbersSecond[i]
            sum_Pos += diff * diff
        }
        val mse_Pos = sum_Pos / arrayOfDividedNumbers.size

        Log.e("MSE_POSITIVE", mse_Pos.toString())

        // Find MSE of two arrays to find the loss between two images
        var sum_Neg = 0.0
        for (i in 0 until arrayOfDividedNumbers.size) {
            val diff = arrayOfDividedNumbers[i] - arrayOfDividedNumbersThird[i]
            sum_Neg += diff * diff
        }
        val mse_Neg = sum_Neg / arrayOfDividedNumbers.size

        Log.e("MSE_NEGATIVE", mse_Neg.toString())

        //AlertDialog.Builder(context).setMessage("Hi George!").show()
        //Toast.makeText(context, "Hi George!", Toast.LENGTH_LONG).show()
        //_trueOrFalsePhoto.postValue(true)
        if (mse_Pos < mse_Neg) {
            Log.e("VIEW_MODEL", "George_Yes")
            //Toast.makeText(context, "Hi George!", Toast.LENGTH_LONG).show()
            //AlertDialog.Builder(context).setMessage("Hi George!").show()
            _trueOrFalsePhoto.postValue(true)
            //_trueOrFalsePhoto.value = true

        } else {
            Log.e("VIEW_MODEL", "George_No")
            //Toast.makeText(context, "Who are you??", Toast.LENGTH_LONG).show()
            //AlertDialog.Builder(context).setMessage("Who are you??").show()
            _trueOrFalsePhoto.postValue(false)
            //_trueOrFalsePhoto.value = false

        }

        // Delete taken picture
        val file = File(pathOfPhoto!!.substring(7, pathOfPhoto!!.length))
        file.delete()


        return "Prediction Result: %d\nConfidence: %2f"
            .format(maxIndex, result[maxIndex])
    }

    private fun saveResizedBitmapToPhone(croppedFaceBitmap: Bitmap, name: String) {
        val mypath = File(outputDirectory, name)
        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(mypath, false)
            croppedFaceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            Log.i("SAVE_IMAGE", e.message, e)
        }
    }

    private fun saveResizedBitmapToPhoneNull(croppedFaceBitmap: Bitmap?, name: String) {
        val mypath = File(outputDirectory, name)
        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(mypath, false)
            croppedFaceBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            Log.i("SAVE_IMAGE", e.message, e)
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // Pre-process the input: convert a Bitmap instance to a ByteBuffer instance
        // containing the pixel values of all pixels in the input image.
        // We use ByteBuffer because it is faster than a Kotlin native float multidimensional array.
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            // Normalize pixel value to [0..1].
            val normalizedPixelValue = (r + g + b) / 255.0F
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }

    private fun convertBitmapToByteBufferNull(bitmap: Bitmap?): ByteBuffer {
        // Pre-process the input: convert a Bitmap instance to a ByteBuffer instance
        // containing the pixel values of all pixels in the input image.
        // We use ByteBuffer because it is faster than a Kotlin native float multidimensional array.
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap?.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            // Normalize pixel value to [0..1].
            val normalizedPixelValue = (r + g + b) / 255.0F
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }

    companion object {
        private const val TAG = "MainActivityViewModel"
        private const val OUTPUT_CLASSES_COUNT = 64
        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 3
    }
}
