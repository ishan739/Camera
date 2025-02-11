package com.example.camerax.mlkit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val context: Context,
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val barcodeScanning = BarcodeScanning.getClient()
    private var lastScannerBarCode : String? = null

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        barcodeScanning.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let {

                        if(it != lastScannerBarCode){
                            lastScannerBarCode = it
                            Log.d("Barcode", "Scanned: $it")
                            onBarcodeDetected(it)
                        }

                        if(barcode.url != null){
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(barcode.url?.url))
                            context.startActivity(intent)
                        }
                    }
                }
            }
            .addOnFailureListener{e ->
                Log.e("MLKit", "Barcode scanning failed", e)
            }
            .addOnCompleteListener{
                imageProxy.close()
            }

    }

}