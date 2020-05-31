package com.george.face_verification.camera

import android.app.Application
import android.content.Context
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
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException


class CameraFragmentViewModel(app: Application) : AndroidViewModel(app) {

    private var _selectedPhotoPath = MutableLiveData<String>()

    // The external LiveData for the __
    private val selectedPhotoPath: LiveData<String>
        get() = _selectedPhotoPath

    private var pathOfPhoto: String?
    private lateinit var originalBitmap: Bitmap
    private var rotatedBitmap: Bitmap? = null
    private val context = getApplication<Application>().applicationContext
    private var faceDetector: FaceDetector? = null
    private var outputDirectory: File

    // Initialize the __ MutableLiveData
    init {
        pathOfPhoto = ""

        viewModelScope.launch {

        }

        faceDetector = FaceDetector.Builder(context)
            .setTrackingEnabled(false)
            .build()

        outputDirectory =
            MainActivity.getOutputDirectory(
                context
            )
    }

    /*//originalBitmap = getBitmapFromAsset(app, "george_black.jpg")
    private fun getBitmapFromAsset(context: Context, path: String): Bitmap =
        context.assets.open(path).use { BitmapFactory.decodeStream(it) }*/

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

        // If you use Koin injection
        pathOfPhoto = getKoin().getProperty("pathInfo")
        // Retrieve original bitmap
        originalBitmap = uriToBitmap(path.toUri())

        Log.i("WIDTH_ORIGINAL", originalBitmap?.width.toString())
        Log.i("HEIGHT_ORIGINAL", originalBitmap?.height.toString())
        Log.i("PATH_TO_URI", path)

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
        val detector: FaceDetector = FaceDetector.Builder(context)
            .setTrackingEnabled(true)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setMode(FaceDetector.ACCURATE_MODE)
            .build()


        if (!faceDetector!!.isOperational) {
            AlertDialog.Builder(context).setMessage("Could not set up the face detector!").show()
        }

        // Detect the Faces
        val frame: Frame = Frame.Builder().setBitmap(bitmap).build()
        // Use facedetector or detector object
        val faces: SparseArray<Face>? = faceDetector?.detect(frame)
        Log.i("NUMBER_FACES", faces!!.size().toString())

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
            val mypath = File(outputDirectory, "molvedo.jpg")
            val fos: FileOutputStream?
            try {
                fos = FileOutputStream(mypath, false)
                croppedFaceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()
            } catch (e: Exception) {
                Log.i("SAVE_IMAGE", e.message, e)
            }
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
    private fun rotateImageTransverse(img: Bitmap, degree: Float, horizontal: Boolean, vertical: Boolean): Bitmap? {
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

}

