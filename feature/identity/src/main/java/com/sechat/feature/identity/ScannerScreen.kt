package com.sechat.feature.identity

import android.Manifest
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.sechat.core.crypto.IdentityManager
import com.sechat.core.crypto.QrCodeDecoder
import com.sechat.core.data.ContactRepository
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onPeerScanned: (contactId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val contactRepo = remember { get<ContactRepository>(ContactRepository::class.java) }
    val identityManager = remember { get<IdentityManager>(IdentityManager::class.java) }
    var scanning by remember { mutableStateOf(true) }

    var cameraGranted by remember {
        mutableStateOf(
            PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PermissionChecker.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> cameraGranted = granted }

    DisposableEffect(Unit) {
        if (!cameraGranted) launcher.launch(Manifest.permission.CAMERA)
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (cameraGranted) {
                CameraPreview(
                    onQrDetected = { rawText ->
                        if (!scanning) return@CameraPreview
                        scanning = false

                        val decoded = QrCodeDecoder.decode(rawText)
                        if (decoded != null) {
                            scope.launch {
                                val displayName = decoded.fingerprint.take(8)
                                val hexBytes = decoded.publicKeyHex.chunked(2)
                                    .map { it.toInt(16).toByte() }.toByteArray()
                                val id = contactRepo.addContact(displayName, hexBytes)
                                onPeerScanned(id)
                            }
                        } else {
                            scanning = true
                        }
                    }
                )
            } else {
                Text(
                    text = "Camera permission required to scan QR codes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(onQrDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as? androidx.lifecycle.LifecycleOwner ?: return
    val executor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                val analysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(executor) { image ->
                    scanQrCode(image)?.let(onQrDetected)
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA,
                        preview, analysis
                    )
                } catch (_: Exception) { }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) { onDispose { executor.shutdown() } }
}

private fun scanQrCode(image: ImageProxy): String? {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
    val rgb = IntArray(image.width * image.height)
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val gray = bytes[y * image.width + x].toInt() and 0xFF
            rgb[y * image.width + x] = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
        }
    }
    return try {
        val result = MultiFormatReader().decode(
            BinaryBitmap(HybridBinarizer(RGBLuminanceSource(image.width, image.height, rgb)))
        )
        image.close(); result.text
    } catch (_: Exception) {
        image.close(); null
    }
}
