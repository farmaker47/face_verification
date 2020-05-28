package com.george.face_verification.camera

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import org.koin.android.ext.android.getKoin
import org.koin.java.KoinJavaComponent.getKoin
import java.io.IOException

class CameraFragmentViewModel(app: Application) : AndroidViewModel(app) {

    private var _selectedPhotoPath = MutableLiveData<String>()

    // The external LiveData for the __
    private val selectedPhotoPath: LiveData<String>
        get() = _selectedPhotoPath

    private val _selectedVideo = MutableLiveData<String>()

    private var pathOfPhoto: String?

    // Initialize the __ MutableLiveData
    init {

       // _selectedPhotoPath.value = ""
        pathOfPhoto = ""

        viewModelScope.launch {

        }
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

    }
}

