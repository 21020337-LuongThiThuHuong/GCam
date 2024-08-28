package com.example.gcam

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class BarcodeScannerModel(
    private val context: Context,
) {
    private val barcodeScanner: BarcodeScanner =
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
        )

    var isProcessing = false
        private set

    fun processImageProxy(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }

        isProcessing = true

        @androidx.camera.core.ExperimentalGetImage val image = imageProxy.image ?: return
        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner
            .process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (barcode.valueType == Barcode.TYPE_URL && rawValue != null) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawValue))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        if (rawValue != null) {
                            Toast
                                .makeText(context, "Scanned text: $rawValue", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                // Delay for 1 second before allowing next scan
                Executors.newSingleThreadScheduledExecutor().schedule({
                    isProcessing = false
                }, 1, java.util.concurrent.TimeUnit.SECONDS)

                imageProxy.close()
            }.addOnFailureListener {
                imageProxy.close()
                isProcessing = false
            }
    }
}
