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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IdeColors.bgPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    onClick = onBack,
                    tint = IdeColors.accentGreen
                )
                Text(
                    text = "[ QR INTERCEPT :: SCANNER ]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentGreen),
                    modifier = Modifier.weight(1f)
                )
            }
            NeonDivider()

            IdeTabBar(
                tabs = listOf(
                    TabItem("scanner.cam", icon = Icons.Default.QrCodeScanner),
                    TabItem("manual_add.sh", icon = Icons.Default.PersonAdd)
                ),
                selectedIndex = 0,
                onTabSelected = {}
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "[POINT CAMERA AT TARGET QR]",
                    style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan, letterSpacing = 2.sp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .neonBorder(IdeColors.accentGreen)
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

                        // Scanning overlay with neon corners
                        Canvas(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                            val cornerLen = 30.dp.toPx()
                            val stroke = 2.dp.toPx()
                            val color = IdeColors.accentGreen

                            // Top-left corner
                            drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), stroke)
                            drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), stroke)
                            // Top-right
                            drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), stroke)
                            drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLen), stroke)
                            // Bottom-left
                            drawLine(color, Offset(0f, size.height), Offset(cornerLen, size.height), stroke)
                            drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLen), stroke)
                            // Bottom-right
                            drawLine(color, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), stroke)
                            drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), stroke)

                            // Dashed center crosshair
                            val center = Offset(size.width / 2, size.height / 2)
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            drawLine(
                                color.copy(alpha = 0.3f),
                                Offset(center.x - 20.dp.toPx(), center.y),
                                Offset(center.x + 20.dp.toPx(), center.y),
                                1.dp.toPx(),
                                pathEffect = dashEffect
                            )
                            drawLine(
                                color.copy(alpha = 0.3f),
                                Offset(center.x, center.y - 20.dp.toPx()),
                                Offset(center.x, center.y + 20.dp.toPx()),
                                1.dp.toPx(),
                                pathEffect = dashEffect
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(IdeColors.bgSecondary)
                            .neonBorder(IdeColors.accentRed)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("[ACCESS DENIED]", style = IdeTypography.code.copy(color = IdeColors.accentRed))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("[CAMERA PERMISSION REQUIRED]", style = IdeTypography.codeSmall.copy(color = IdeColors.textComment))
                        Spacer(modifier = Modifier.height(16.dp))
                        IdeButton(
                            text = "GRANT ACCESS",
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            icon = Icons.Default.Lock,
                            color = IdeColors.accentGreen
                        )
                    }
                }

                // Scan result
                scannedResult?.let { username ->
                    Spacer(modifier = Modifier.height(16.dp))
                    HackerPanel(borderColor = IdeColors.accentGreen) {
                        Text(
                            text = "[✓ AGENT IDENTIFIED]",
                            style = IdeTypography.code.copy(color = IdeColors.accentGreen)
                        )
                        Text(
                            text = "TARGET: $username",
                            style = IdeTypography.code.copy(color = IdeColors.accentCyan)
                        )
                        Text(
                            text = "ADDING TO NETWORK...",
                            style = IdeTypography.codeSmall.copy(color = IdeColors.textComment)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Manual add
                HackerPanel(borderColor = IdeColors.accentCyan) {
                    Text(
                        text = "[MANUAL AGENT LOOKUP]",
                        style = IdeTypography.codeSmall.copy(color = IdeColors.accentCyan)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IdeTextField(
                            value = manualUsername,
                            onValueChange = {
                                manualUsername = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' || c == '-' }
                            },
                            placeholder = "agent_handle",
                            prefix = "find ",
                            modifier = Modifier.weight(1f)
                        )
                        IdeButton(
                            text = "LOCATE",
                            onClick = { onManualAdd(manualUsername) },
                            icon = Icons.Default.PersonAdd,
                            enabled = manualUsername.length >= 3,
                            color = IdeColors.accentCyan
                        )
                    }
                }
            }

            // Status bar
            IdeStatusBar(
                items = listOf(
                    StatusBarItem(
                        text = "SCANNER",
                        icon = Icons.Default.QrCodeScanner,
                        color = IdeColors.accentGreen
                    ),
                    StatusBarItem(text = "", fillWeight = true),
                    StatusBarItem(
                        text = if (hasCameraPermission) "CAM: ACTIVE" else "CAM: BLOCKED",
                        color = if (hasCameraPermission) IdeColors.accentGreen else IdeColors.accentRed
                    )
                )
            )
        }

        CrtOverlay()
    }
}

private val sp = androidx.compose.ui.unit.sp
