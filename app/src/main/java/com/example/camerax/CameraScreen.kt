package com.example.camerax

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(){

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val mainExecuter = ContextCompat.getMainExecutor(context)

    var isFrontCamera by remember { mutableStateOf(false) }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ){

                FloatingActionButton(
                    onClick = {
                        isFrontCamera = !isFrontCamera
                        var cameraSelector = if (isFrontCamera){
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }

                        cameraController.cameraSelector = cameraSelector
                        cameraController.bindToLifecycle(lifecycleOwner)

                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_switch),
                        contentDescription = "Switch Camera"
                    )
                }

                FloatingActionButton(
                    onClick = {
                        cameraController.takePicture(
                            mainExecuter,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    super.onCaptureSuccess(image)

                                    CoroutineScope(Dispatchers.IO).launch {
                                        saveImageToGallery(context, image)

                                    }
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
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Capture Image"
                    )
                }
            }



        }

        ){paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        )
        {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    PreviewView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        setBackgroundColor(Color.BLACK)
                        scaleType = PreviewView.ScaleType.FIT_CENTER
                    }.also { previewView ->
                        previewView.controller = cameraController
                        cameraController.bindToLifecycle(lifecycleOwner)
                    }
                })
        }
    }
}

private fun saveImageToGallery(context: Context, image: ImageProxy){

    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

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
                outputStream.write(bytes)
                outputStream.flush()
            }
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Image Saved!", Toast.LENGTH_SHORT).show()
        }
    } ?: run {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "Failed To Save!", Toast.LENGTH_SHORT).show()
        }
    }
    image.close()
}












