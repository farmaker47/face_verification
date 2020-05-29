package com.george.face_verification.camera

import android.app.Application
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.SparseArray
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
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
    private var originalBitmap: Bitmap? = null
    private val context = getApplication<Application>().applicationContext
    private var faceDetector: FaceDetector? = null
    private var outputDirectory: File

    // Initialize the __ MutableLiveData
    init {

        //originalBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);
        // _selectedPhotoPath.value = ""
        pathOfPhoto = ""

        viewModelScope.launch {

        }
        originalBitmap = getBitmapFromAsset(app, "george_black.jpg")
        Log.e("HEIGHT", originalBitmap?.height.toString())
        Log.e("WIDTH", originalBitmap?.width.toString())

        faceDetector = FaceDetector.Builder(context)
            .setTrackingEnabled(false)
            .build()

        outputDirectory =
            MainActivity.getOutputDirectory(
                context
            )
    }

    private fun getBitmapFromAsset(context: Context, path: String): Bitmap =
        context.assets.open(path).use { BitmapFactory.decodeStream(it) }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        var image: Bitmap? = null
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

    private suspend fun getNews() = withContext(Dispatchers.IO) {

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

    fun setPathOfLatestPhoto(path: String) {
        /*withContext(Dispatchers.Main) {
            // call to UI thread
            _selectedPhotoPath.value = path
        }*/

        pathOfPhoto = getKoin().getProperty("pathInfo")
        Log.e("VIEW_MODEL", getKoin().getProperty("pathInfo"))

        originalBitmap = uriToBitmap(path.toUri())
        Log.e("WIDTH_ORIGINAL", originalBitmap?.width.toString())
        Log.e("HEIGHT_ORIGINAL", originalBitmap?.height.toString())

        detectFaces(originalBitmap)

    }

    private fun detectFaces(bitmap: Bitmap?) {

        // Create a Paint object for drawing with
        val myRectPaint = Paint()
        myRectPaint.strokeWidth = 5F
        myRectPaint.color = Color.RED
        myRectPaint.style = Paint.Style.STROKE

        // Create a Canvas object for drawing on
        val tempBitmap =
            Bitmap.createBitmap(bitmap!!.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(tempBitmap)
        tempCanvas.drawBitmap(bitmap, 0F, 0F, null)

        // Create the Face Detector
        val detector: FaceDetector = FaceDetector.Builder(context)
            .setTrackingEnabled(true)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setMode(FaceDetector.ACCURATE_MODE)
            .build()


        if (!faceDetector!!.isOperational) {
            AlertDialog.Builder(context).setMessage("Could not set up the face detector!").show();
            return
        }

        // Detect the Faces
        val frame: Frame = Frame.Builder().setBitmap(bitmap).build()
        val faces: SparseArray<Face>? = detector.detect(frame)

        Log.e("NUMBER_FACES", faces!!.size().toString())

        // For every face draw rectangle
        for (i in 0 until faces.size()) {
            val thisFace = faces.valueAt(i)
            val x1 = thisFace.position.x
            val y1 = thisFace.position.y
            Log.e("POSITION_X", x1.toString())
            Log.e("POSITION_Y", y1.toString())
            val x2 = x1 + thisFace.width
            val y2 = y1 + thisFace.height
            tempCanvas.drawRoundRect(RectF(x1, y1, x2, y2), 2f, 2f, myRectPaint)
        }

        // myImageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));

        // Create file to save picture
        // Create output file to hold the image
        val photoFile =
            CameraFragment.createFile(
                outputDirectory,
                CameraFragment.FILENAME,
                CameraFragment.PHOTO_EXTENSION
            )

        val mypath = File(outputDirectory, "soloupis.png")

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            Log.e("SAVE_IMAGE", e.message, e)
        }


    }
}

