package com.devtalk.messenger.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.devtalk.messenger.ui.components.*
import com.devtalk.messenger.ui.theme.IdeColors
import com.devtalk.messenger.ui.theme.IdeTypography
import com.devtalk.messenger.util.QrCodeUtils
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
fun QrScannerScreen(
    onBack: () -> Unit,
    onUsernameScanned: (String) -> Unit,
    onManualAdd: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var manualUsername by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(IdeColors.bgToolbar)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IdeIconButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            Text(
                text = "DevTalk — Scan QR Code",
                style = IdeTypography.codeSmall.copy(color = IdeColors.textSecondary),
                modifier = Modifier.weight(1f)
            )
        }
        Divider(color = IdeColors.border, thickness = 1.dp)

        // Tabs
        IdeTabBar(
            tabs = listOf(
                TabItem("scanner.cam", icon = Icons.Default.QrCodeScanner),
                TabItem("manual_add.kt", icon = Icons.Default.PersonAdd)
            ),
            selectedIndex = 0,
            onTabSelected = {}
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "// Point camera at a DevTalk QR code",
                style = IdeTypography.comment
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (hasCameraPermission) {
                // Camera preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .border(2.dp, IdeColors.border, RoundedCornerShape(4.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                @androidx.annotation.OptIn(ExperimentalGetImage::class)
                                val imageAnalyzer = ImageAnalysis.Builder()
                                    .setTargetResolution(Size(1280, 720))
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null && !isProcessing) {
                                                val image = InputImage.fromMediaImage(
                                                    mediaImage,
                                                    imageProxy.imageInfo.rotationDegrees
                                                )
                                                val scanner = BarcodeScanning.getClient()
                                                scanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        for (barcode in barcodes) {
                                                            if (barcode.valueType == Barcode.TYPE_TEXT ||
                                                                barcode.valueType == Barcode.TYPE_URL
                                                            ) {
                                                                val raw = barcode.rawValue ?: continue
                                                                val username = QrCodeUtils.parseProfileLink(raw)
                                                                if (username != null) {
                                                                    isProcessing = true
                                                                    scannedResult = username
                                                                    onUsernameScanned(username)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .addOnCompleteListener {
                                                        imageProxy.close()
                                                    }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }
                                    }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalyzer
                                    )
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay scan frame
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .border(
                                    2.dp,
                                    IdeColors.accentBlue.copy(alpha = 0.7f),
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
            } else {
                // No camera permission
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(IdeColors.bgSecondary)
                        .border(2.dp, IdeColors.border, RoundedCornerShape(4.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = IdeColors.textComment,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "// Camera permission required",
                        style = IdeTypography.comment
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IdeButton(
                        text = "Grant Permission",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        icon = Icons.Default.Lock
                    )
                }
            }

            // Scanned result
            scannedResult?.let { username ->
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(IdeColors.accentGreen.copy(alpha = 0.1f))
                        .border(1.dp, IdeColors.accentGreen.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "// ✓ Contact found!",
                        style = IdeTypography.comment.copy(color = IdeColors.accentGreen)
                    )
                    Text(
                        text = "addContact(\"$username\")",
                        style = IdeTypography.code.copy(color = IdeColors.textFunction)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Manual add section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(IdeColors.bgSecondary)
                    .border(1.dp, IdeColors.border, RoundedCornerShape(3.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "// Or add manually by username:",
                    style = IdeTypography.comment
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IdeTextField(
                        value = manualUsername,
                        onValueChange = { manualUsername = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' || c == '-' } },
                        placeholder = "username",
                        prefix = "find ",
                        modifier = Modifier.weight(1f)
                    )
                    IdeButton(
                        text = "Add",
                        onClick = { onManualAdd(manualUsername) },
                        icon = Icons.Default.PersonAdd,
                        enabled = manualUsername.length >= 3
                    )
                }
            }
        }

        // Status bar
        IdeStatusBar(
            items = listOf(
                StatusBarItem(
                    text = "scanner",
                    icon = Icons.Default.QrCodeScanner,
                    color = IdeColors.accentBlue
                ),
                StatusBarItem(text = "", fillWeight = true),
                StatusBarItem(
                    text = if (hasCameraPermission) "Camera: ACTIVE" else "Camera: DENIED",
                    color = if (hasCameraPermission) IdeColors.accentGreen else IdeColors.accentRed
                )
            )
        )
    }
}
