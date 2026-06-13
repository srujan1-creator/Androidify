package com.androidify.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.androidify.app.ui.components.CameraControls
import com.androidify.app.viewmodel.CameraViewModel
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Camera screen that manages CameraX preview and capture.
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onImageCaptured: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            CameraPreviewContent(
                viewModel = viewModel,
                onImageCaptured = onImageCaptured,
                context = context,
                lifecycleOwner = lifecycleOwner
            )
        } else {
            PermissionDeniedContent(
                onRequestPermission = { launcher.launch(Manifest.permission.CAMERA) },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun CameraPreviewContent(
    viewModel: CameraViewModel,
    onImageCaptured: () -> Unit,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val lensFacing by viewModel.lensFacing.collectAsState()
    val flashEnabled by viewModel.flashEnabled.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()

    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(lensFacing, flashEnabled, zoomLevel) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(
                if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
            )
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            
            // Set zoom
            camera?.cameraControl?.setLinearZoom(zoomLevel)
        } catch (exc: Exception) {
            // Log/handle binding errors
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        CameraControls(
            flashEnabled = flashEnabled,
            onFlashToggle = { viewModel.toggleFlash() },
            onFlipCamera = { viewModel.toggleLensFacing() },
            onCapture = {
                val captureCase = imageCapture ?: return@CameraControls
                captureCase.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = image.toBitmap()
                            val rotationDegrees = image.imageInfo.rotationDegrees
                            image.close()

                            // Apply rotation
                            val matrix = Matrix().apply {
                                postRotate(rotationDegrees.toFloat())
                                // If using front camera, mirror the image for natural selfie look
                                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                    postScale(-1f, 1f)
                                }
                            }
                            
                            val rotatedBitmap = Bitmap.createBitmap(
                                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                            )
                            
                            // Callback to main thread
                            ContextCompat.getMainExecutor(context).execute {
                                viewModel.onImageCaptured(rotatedBitmap)
                                onImageCaptured()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            // Capture error handling
                        }
                    }
                )
            },
            zoomLevel = zoomLevel,
            onZoomChange = { viewModel.setZoomLevel(it) },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermission: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Androidify needs access to your camera to take a selfie and generate your robot avatar.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DDC84)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Grant Permission", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack) {
            Text("Go Back", color = Color.White)
        }
    }
}
