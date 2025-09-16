package com.maintainer.app.ui.vehicles

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.maintainer.app.ui.theme.*
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VinScannerScreen(
    onVinScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasFoundVin by remember { mutableStateOf(false) }
    var detectedText by remember { mutableStateOf("Point camera at VIN barcode") }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var scanningState by remember { mutableStateOf(ScanningState.SEARCHING) }

    // Camera permission handling
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            onClose() // Close scanner if permission denied
        }
    }

    // Request permission if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Dark-themed top bar
        TopAppBar(
            title = {
                Text(
                    text = "VIN Scanner",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                // Flash toggle button
                IconButton(
                    onClick = { isFlashEnabled = !isFlashEnabled }
                ) {
                    Icon(
                        imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (isFlashEnabled) "Turn off flash" else "Turn on flash",
                        tint = if (isFlashEnabled) WarningAmber80 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        )

        // Camera content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp), // Account for top bar
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                // Camera preview
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val executor = ContextCompat.getMainExecutor(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                if (!hasFoundVin) {
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )

                                        val scanner = BarcodeScanning.getClient()
                                        scanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                if (barcodes.isEmpty()) {
                                                    scanningState = ScanningState.SEARCHING
                                                    detectedText = "Searching for VIN barcode..."
                                                } else {
                                                    scanningState = ScanningState.DETECTING
                                                    detectedText = "Found ${barcodes.size} barcode(s)"
                                                }

                                                for (barcode in barcodes) {
                                                    val rawValue = barcode.rawValue
                                                    // Log all detected barcodes for debugging
                                                    android.util.Log.d("VinScanner", "Detected barcode: $rawValue")

                                                    if (rawValue != null) {
                                                        // Show what we detected in UI
                                                        detectedText = "Detected: ${rawValue.take(20)}${if (rawValue.length > 20) "..." else ""}"

                                                        // Clean the scanned value (remove spaces, special chars)
                                                        val cleanValue = rawValue.replace(Regex("[^A-HJ-NPR-Z0-9]"), "").uppercase()

                                                        // Check if it's a valid VIN format (17 alphanumeric chars, no I,O,Q)
                                                        if (isValidVin(cleanValue)) {
                                                            hasFoundVin = true
                                                            scanningState = ScanningState.SUCCESS
                                                            detectedText = "✅ Valid VIN found!"
                                                            android.util.Log.d("VinScanner", "Valid VIN found: $cleanValue")
                                                            onVinScanned(cleanValue)
                                                            break
                                                        } else if (cleanValue.length >= 10) {
                                                            // Show potential VINs
                                                            scanningState = ScanningState.POTENTIAL
                                                            detectedText = "Potential VIN (${cleanValue.length} chars): $cleanValue"
                                                            android.util.Log.d("VinScanner", "Potential VIN (${cleanValue.length} chars): $cleanValue")
                                                        }
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                android.util.Log.e("VinScanner", "Barcode scanning failed", e)
                                                scanningState = ScanningState.ERROR
                                                detectedText = "Scanning error occurred"
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (exc: Exception) {
                                // Handle camera binding errors
                                scanningState = ScanningState.ERROR
                            }

                        }, executor)
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Bright scan overlay with guides
                VinScanningOverlay(
                    scanningState = scanningState,
                    modifier = Modifier.fillMaxSize()
                )

                // Enhanced dark-themed feedback card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(CornerRadius.large),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = Elevation.large
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Scanning status indicator
                        ScanningStatusIndicator(
                            scanningState = scanningState,
                            modifier = Modifier.padding(bottom = Spacing.medium)
                        )

                        Text(
                            text = "Position VIN barcode in the frame above",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(Spacing.small))

                        Text(
                            text = detectedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (scanningState) {
                                ScanningState.SUCCESS -> MaintenanceGreen
                                ScanningState.ERROR -> AlertRed
                                ScanningState.POTENTIAL -> WarningAmber80
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(Spacing.medium))

                        // Manual entry button for testing/fallback
                        OutlinedButton(
                            onClick = {
                                // For testing - simulate a VIN scan
                                onVinScanned("1HGBH41JXMN109186")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Manual Entry (Testing)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            } else {
                // Enhanced permission request UI
                VinScannerPermissionRequest(
                    onRequestPermission = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        }
    }
}

enum class ScanningState {
    SEARCHING,
    DETECTING,
    POTENTIAL,
    SUCCESS,
    ERROR
}

@Composable
fun VinScanningOverlay(
    scanningState: ScanningState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val overlayColor = Color.Black.copy(alpha = 0.5f)
        val frameColor = when (scanningState) {
            ScanningState.SUCCESS -> Color.Green
            ScanningState.ERROR -> Color.Red
            ScanningState.POTENTIAL -> Color.Yellow
            ScanningState.DETECTING -> Color.Blue
            else -> Color.White
        }

        // Draw dark overlay
        drawRect(color = overlayColor)

        // Calculate scanning frame dimensions (centered, landscape rectangle for VIN)
        val frameWidth = size.width * 0.85f
        val frameHeight = size.height * 0.25f
        val frameLeft = (size.width - frameWidth) / 2
        val frameTop = (size.height - frameHeight) / 2

        // Clear the scanning area
        drawRect(
            color = Color.Transparent,
            topLeft = androidx.compose.ui.geometry.Offset(frameLeft, frameTop),
            size = androidx.compose.ui.geometry.Size(frameWidth, frameHeight)
        )

        // Draw bright scanning frame
        val strokeWidth = 6f
        val cornerLength = 40f

        // Top-left corner
        drawLine(
            color = frameColor,
            start = androidx.compose.ui.geometry.Offset(frameLeft, frameTop),
            end = androidx.compose.ui.geometry.Offset(frameLeft + cornerLength, frameTop),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = androidx.compose.ui.geometry.Offset(frameLeft, frameTop),
            end = androidx.compose.ui.geometry.Offset(frameLeft, frameTop + cornerLength),
            strokeWidth = strokeWidth
        )

        // Top-right corner
        drawLine(
            color = frameColor,
            start = androidx.compose.ui.geometry.Offset(frameLeft + frameWidth - cornerLength, frameTop),
            end = androidx.compose.ui.geometry.Offset(frameLeft + frameWidth, frameTop),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = androidx.compose.ui.geometry.Offset(frameLeft + frameWidth, frameTop),
            end = androidx.compose.ui.geometry.Offset(frameLeft + frameWidth, frameTop + cornerLength),
            strokeWidth = strokeWidth
        )

        // Bottom-left corner
        drawLine(
            color = frameColor,
            start = androidx.compose.ui.geometry.Offset(frameLeft, frameTop + frameHeight - cornerLength),
            end = androidx.compose.ui.geometry.Offset(frameLeft, frameTop + frameHeight),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = androidx.compose.ui.geometry.Offset(frameLeft, frameTop + frameHeight),
            end = androidx.compose.ui.geometry.Offset(frameLeft + cornerLength, frameTop + frameHeight),
            strokeWidth = strokeWidth
        )

        // Bottom-right corner
        drawLine(
            color = frameColor,
            start = androidx.compose.ui.geometry.Offset(frameLeft + frameWidth, frameTop + frameHeight - cornerLength),
            end = androidx.compose.ui.geometry.Offset(frameLeft + frameWidth, frameTop + frameHeight),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = frameColor,
            start = androidx.compose.ui.geometry.Offset(frameLeft + frameWidth - cornerLength, frameTop + frameHeight),
            end = androidx.compose.ui.geometry.Offset(frameLeft + frameWidth, frameTop + frameHeight),
            strokeWidth = strokeWidth
        )

        // Animated scanning line for active scanning
        if (scanningState == ScanningState.DETECTING || scanningState == ScanningState.SEARCHING) {
            val linePosition = frameTop + frameHeight * 0.5f // Center line
            drawLine(
                color = frameColor.copy(alpha = 0.7f),
                start = androidx.compose.ui.geometry.Offset(frameLeft + 20, linePosition),
                end = androidx.compose.ui.geometry.Offset(frameLeft + frameWidth - 20, linePosition),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
            )
        }
    }
}

@Composable
fun ScanningStatusIndicator(
    scanningState: ScanningState,
    modifier: Modifier = Modifier
) {
    val color = when (scanningState) {
        ScanningState.SUCCESS -> MaintenanceGreen
        ScanningState.ERROR -> AlertRed
        ScanningState.POTENTIAL -> WarningAmber80
        ScanningState.DETECTING -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val text = when (scanningState) {
        ScanningState.SEARCHING -> "Searching..."
        ScanningState.DETECTING -> "Detecting..."
        ScanningState.POTENTIAL -> "Potential VIN"
        ScanningState.SUCCESS -> "VIN Found!"
        ScanningState.ERROR -> "Error"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(Spacing.small))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VinScannerPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera icon with gradient background
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Text(
                text = "Camera Access Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = "We need camera access to scan VIN barcodes for your vehicles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(0.7f),
                shape = RoundedCornerShape(CornerRadius.medium)
            ) {
                Text(
                    text = "Grant Camera Permission",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun isValidVin(vin: String): Boolean {
    if (vin.length != 17) return false

    // Check for invalid characters (I, O, Q are not allowed in VINs)
    val invalidChars = setOf('I', 'O', 'Q')
    for (char in vin) {
        if (char in invalidChars) return false
    }

    // Check that it contains both letters and numbers (basic VIN pattern)
    val hasLetters = vin.any { it.isLetter() }
    val hasNumbers = vin.any { it.isDigit() }

    return hasLetters && hasNumbers
}