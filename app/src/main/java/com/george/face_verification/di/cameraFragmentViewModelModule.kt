package com.george.face_verification.di

import com.george.face_verification.camera.CameraFragmentViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val cameraFragmentViewModelModule = module {
    viewModel {
        CameraFragmentViewModel(get())
    }
}