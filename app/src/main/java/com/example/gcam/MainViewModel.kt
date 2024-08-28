package com.example.gcam

import android.app.Application
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val barcodeScannerModel = BarcodeScannerModel(application)
    private val _cameraProvider = MutableLiveData<ProcessCameraProvider>()
    val cameraProvider: LiveData<ProcessCameraProvider> = _cameraProvider

    fun initializeCamera() {
        viewModelScope.launch {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
            cameraProviderFuture.addListener({
                _cameraProvider.postValue(cameraProviderFuture.get())
            }, ContextCompat.getMainExecutor(getApplication()))
        }
    }

    fun processImageProxy(imageProxy: ImageProxy) {
        barcodeScannerModel.processImageProxy(imageProxy)
    }

    fun isProcessing(): Boolean = barcodeScannerModel.isProcessing
}
