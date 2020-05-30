package com.george.face_verification.camera

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
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
    private var originalBitmap: Bitmap? = null
    private var rotatedBitmap: Bitmap? = null
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
        Log.e("PATH_TO_URI", path)

        val preLongStr: String = path.substring(0, 5)
        Log.e("PRE", preLongStr)
        val postLongStr: String = path.substring(5, path.length)
        Log.e("AFTER",postLongStr)

        //rotatedBitmap = modifyOrientation(originalBitmap, preLongStr + "//" + postLongStr)

        viewModelScope.launch {
            detectFaces(originalBitmap)
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


        // Create the Face Detector
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
        val faces: SparseArray<Face>? = faceDetector?.detect(frame)

        Log.e("NUMBER_FACES", faces!!.size().toString())

        // For every face draw rectangle

        var x1 = 0
        var x2 = 0
        var y1 = 0
        var y2 = 0
        var width = 0
        var height = 0
        for (i in 0 until faces.size()) {
            val thisFace = faces.valueAt(i)
            x1 = thisFace.position.x.toInt()
            y1 = thisFace.position.y.toInt()
            Log.e("POSITION_X", x1.toString())
            Log.e("POSITION_Y", y1.toString())
            x2 = (x1 + thisFace.width).toInt()
            y2 = (y1 + thisFace.height).toInt()
            width = thisFace.width.toInt()
            height = thisFace.height.toInt()
            /*tempCanvas.drawRoundRect(
                RectF(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat()),
                2f,
                2f,
                myRectPaint
            )*/
        }
        val croppedFaceBitmap = Bitmap.createBitmap(tempBitmap, x1, y1, width, height)
        //val cropp = Bitmap.createBitmap()

        // myImageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));

        // Create file to save picture
        // Create output file to hold the image
        val photoFile =
            CameraFragment.createFile(
                outputDirectory,
                CameraFragment.FILENAME,
                CameraFragment.PHOTO_EXTENSION
            )

        val mypath = File(outputDirectory, "molvedo.jpg")
        val fos: FileOutputStream?
        try {
            fos = FileOutputStream(mypath, false)
            croppedFaceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            Log.e("SAVE_IMAGE", e.message, e)
        }


    }

    private fun modifyOrientation(bitmap: Bitmap?, image_absolute_path: String): Bitmap? {
        val ei = ExifInterface(image_absolute_path)
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipImage(bitmap, true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipImage(bitmap, false, true)
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap?, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flipImage(bitmap: Bitmap?, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale((if (horizontal) -1 else 1).toFloat(), (if (vertical) -1 else 1).toFloat())
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Get real path for exifInterface
    private fun getRealPathFromURI(contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        val columnindex: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnindex)
    }
}

