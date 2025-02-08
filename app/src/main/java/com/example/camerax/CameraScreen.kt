package com.example.camerax

import android.content.ContentValues
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraScreen(){

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val mainExecuter = ContextCompat.getMainExecutor(context)

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    cameraController.takePicture(
                        mainExecuter,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                super.onCaptureSuccess(image)
                                val contentValues = ContentValues().apply{
                                    put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

                                }
                                val uri : Uri? = context.contentResolver
                                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                                uri?.let {
                                    context.contentResolver.openOutputStream(it)
                                        ?.use { outputStream ->
                                            val buffer = image.planes[0].buffer
                                            val bytes = ByteArray(buffer.remaining())
                                            buffer.get(bytes)
                                            outputStream.write(bytes)
                                        }
                                    Toast.makeText(
                                        context,
                                        "Image saved to gallery",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } ?: run {
                                    Toast.makeText(
                                        context,
                                        "Failed to save image",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                image.close()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                super.onError(exception)
                                Toast.makeText(
                                    context,
                                    "Error Occurred in CameraX",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = "Capture")
            }

        }

        ){paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = {context ->
            PreviewView(context).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setBackgroundColor(Color.BLACK)
                scaleType = PreviewView.ScaleType.FIT_START
            }.also {previewView ->
                previewView.controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)
            }
        })
    }
}












