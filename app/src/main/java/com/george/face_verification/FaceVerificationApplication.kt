package com.george.face_verification

import android.app.Application
import com.george.face_verification.di.cameraFragmentViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FaceVerificationApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            //androidContext(applicationContext)
            androidContext(this@FaceVerificationApplication)
            modules(cameraFragmentViewModelModule)
        }
    }
}