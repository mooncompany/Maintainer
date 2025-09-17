package com.maintainer.app.ui.vehicles

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.maintainer.app.data.database.entity.PowertrainType
import com.maintainer.app.data.database.entity.Vehicle
import com.maintainer.app.ui.components.VehicleDeletionDialog
import com.maintainer.app.ui.theme.*
import org.json.JSONArray
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfileScreen(
    vehicleId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    viewModel: VehicleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicle = uiState.selectedVehicle
    var showDeleteDialog by remember { mutableStateOf(false) }
    var totalMaintenanceCost by remember { mutableStateOf(0.0) }
    var maintenanceCount by remember { mutableStateOf(0) }

    LaunchedEffect(vehicleId) {
        viewModel.loadVehicle(vehicleId)
    }

    LaunchedEffect(vehicle?.id) {
        vehicle?.id?.let { id ->
            viewModel.getTotalMaintenanceCostForVehicle(id) { cost ->
                totalMaintenanceCost = cost
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SteelGray40.copy(alpha = 0.95f),
                        Color.Black.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MechanicBlue40)
            }
        } else if (vehicle != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Custom header with glass morphism
                item {
                    AutomotiveHeader(
                        vehicle = vehicle,
                        onNavigateBack = onNavigateBack,
                        onEdit = onNavigateToEdit,
                        onDelete = { showDeleteDialog = true }
                    )
                }

                // Digital instrument cluster section
                item {
                    DigitalInstrumentCluster(
                        vehicle = vehicle,
                        totalMaintenanceCost = totalMaintenanceCost,
                        maintenanceCount = maintenanceCount
                    )
                }

                // Performance metrics grid
                item {
                    AutomotiveMetricsGrid(
                        vehicle = vehicle,
                        totalMaintenanceCost = totalMaintenanceCost
                    )
                }

                // Vehicle systems status
                item {
                    VehicleSystemsPanel(vehicle = vehicle)
                }

                // Photo gallery with automotive styling
                item {
                    AutomotivePhotoGallery(vehicle = vehicle)
                }

                // Action controls
                item {
                    AutomotiveControlPanel(
                        onEdit = onNavigateToEdit,
                        onMaintenance = onNavigateToMaintenance,
                        onDelete = { showDeleteDialog = true }
                    )
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(Spacing.massive))
                }
            }
        }
    }

    // Deletion confirmation dialog
    if (vehicle != null) {
        VehicleDeletionDialog(
            isVisible = showDeleteDialog,
            vehicleName = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}",
            onConfirm = {
                viewModel.deleteVehicle(vehicle.id)
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun AutomotiveHeader(
    vehicle: Vehicle,
    onNavigateBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Background with gradient and automotive styling
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Glass morphism background
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(32f, 32f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Header content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.large)
        ) {
            // Top row with navigation and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .background(
                                MechanicBlue40.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MechanicBlue40
                        )
                    }

                    Spacer(modifier = Modifier.width(Spacing.small))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .background(
                                Color.Red.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Vehicle name and info
            Column {
                Text(
                    text = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Status indicator with automotive styling
                AutomotiveStatusBadge(isActive = vehicle.isActive)
            }
        }
    }
}

@Composable
fun AutomotiveStatusBadge(isActive: Boolean) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (isActive) {
                        listOf(MaintenanceGreen.copy(alpha = 0.3f), MaintenanceGreen.copy(alpha = 0.1f))
                    } else {
                        listOf(Color.Red.copy(alpha = 0.3f), Color.Red.copy(alpha = 0.1f))
                    }
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isActive) MaintenanceGreen else Color.Red,
                        CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(Spacing.small))

            Text(
                text = if (isActive) "ACTIVE" else "INACTIVE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun DigitalInstrumentCluster(
    vehicle: Vehicle,
    totalMaintenanceCost: Double,
    maintenanceCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal),
        shape = RoundedCornerShape(CornerRadius.extraLarge),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Text(
                text = "DIGITAL INSTRUMENT CLUSTER",
                style = MaterialTheme.typography.labelLarge,
                color = MechanicBlue40,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mileage gauge
                AutomotiveGauge(
                    title = "MILEAGE",
                    value = vehicle.currentMileage.toFloat(),
                    maxValue = 200000f,
                    unit = "mi",
                    color = MechanicBlue40,
                    modifier = Modifier.weight(1f)
                )

                // Health score gauge
                val healthScore = calculateVehicleHealth(vehicle)
                AutomotiveGauge(
                    title = "HEALTH",
                    value = healthScore.toFloat(),
                    maxValue = 100f,
                    unit = "%",
                    color = getHealthColor(healthScore),
                    modifier = Modifier.weight(1f)
                )

                // Maintenance cost gauge
                AutomotiveGauge(
                    title = "MAINTENANCE",
                    value = totalMaintenanceCost.toFloat(),
                    maxValue = 10000f,
                    unit = "$",
                    color = WarningAmber40,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AutomotiveGauge(
    title: String,
    value: Float,
    maxValue: Float,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(1500, easing = EaseOutCubic),
        label = "gauge_animation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        // Gauge visualization
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawAutomotiveGauge(
                    value = animatedValue,
                    maxValue = maxValue,
                    color = color
                )
            }

            // Value text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (unit) {
                        "mi" -> NumberFormat.getNumberInstance(Locale.US).format(value.toInt())
                        "$" -> NumberFormat.getCurrencyInstance(Locale.US).format(value)
                        else -> "${value.toInt()}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (unit != "$") {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

fun DrawScope.drawAutomotiveGauge(
    value: Float,
    maxValue: Float,
    color: Color
) {
    val strokeWidth = 12.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = Offset(size.width / 2, size.height / 2)

    // Background arc
    drawArc(
        color = Color.White.copy(alpha = 0.1f),
        startAngle = 135f,
        sweepAngle = 270f,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        size = Size(radius * 2, radius * 2),
        topLeft = Offset(center.x - radius, center.y - radius)
    )

    // Progress arc
    val progress = (value / maxValue).coerceIn(0f, 1f)
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(
                color.copy(alpha = 0.3f),
                color,
                color.copy(alpha = 0.7f)
            )
        ),
        startAngle = 135f,
        sweepAngle = 270f * progress,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        size = Size(radius * 2, radius * 2),
        topLeft = Offset(center.x - radius, center.y - radius)
    )

    // Tick marks
    for (i in 0..10) {
        val angle = 135f + (270f * i / 10f)
        val tickRadius = radius + 8.dp.toPx()
        val tickStart = Offset(
            center.x + (radius - 4.dp.toPx()) * cos(Math.toRadians(angle.toDouble())).toFloat(),
            center.y + (radius - 4.dp.toPx()) * sin(Math.toRadians(angle.toDouble())).toFloat()
        )
        val tickEnd = Offset(
            center.x + tickRadius * cos(Math.toRadians(angle.toDouble())).toFloat(),
            center.y + tickRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
        )

        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = tickStart,
            end = tickEnd,
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun AutomotiveMetricsGrid(
    vehicle: Vehicle,
    totalMaintenanceCost: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Text(
                text = "VEHICLE METRICS",
                style = MaterialTheme.typography.labelLarge,
                color = MechanicBlue40,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            // Two-column grid
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    AutomotiveMetricItem(
                        title = "YEAR",
                        value = vehicle.year.toString(),
                        icon = Icons.Default.CalendarToday
                    )

                    Spacer(modifier = Modifier.height(Spacing.medium))

                    AutomotiveMetricItem(
                        title = "MAKE",
                        value = vehicle.make,
                        icon = Icons.Default.DirectionsCar
                    )

                    Spacer(modifier = Modifier.height(Spacing.medium))

                    AutomotiveMetricItem(
                        title = "MODEL",
                        value = vehicle.model,
                        icon = Icons.Default.Info
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.large))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    AutomotiveMetricItem(
                        title = "VIN",
                        value = vehicle.vin?.takeLast(8) ?: "N/A",
                        icon = Icons.Default.QrCode
                    )

                    Spacer(modifier = Modifier.height(Spacing.medium))

                    AutomotiveMetricItem(
                        title = "LICENSE",
                        value = vehicle.licensePlate ?: "N/A",
                        icon = Icons.Default.Badge
                    )

                    Spacer(modifier = Modifier.height(Spacing.medium))

                    AutomotiveMetricItem(
                        title = "COLOR",
                        value = vehicle.color ?: "N/A",
                        icon = Icons.Default.Palette
                    )
                }
            }
        }
    }
}

@Composable
fun AutomotiveMetricItem(
    title: String,
    value: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(CornerRadius.medium)
            )
            .padding(Spacing.medium)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MechanicBlue40,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(Spacing.medium))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun VehicleSystemsPanel(vehicle: Vehicle) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Text(
                text = "VEHICLE SYSTEMS",
                style = MaterialTheme.typography.labelLarge,
                color = MechanicBlue40,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SystemStatusIndicator(
                    title = "ENGINE",
                    status = vehicle.isActive,
                    icon = Icons.Default.Speed
                )

                SystemStatusIndicator(
                    title = "TRANSMISSION",
                    status = true, // Mock status
                    icon = Icons.Default.Settings
                )

                SystemStatusIndicator(
                    title = "POWERTRAIN",
                    status = vehicle.powertrainType != null,
                    icon = when (vehicle.powertrainType) {
                        PowertrainType.ELECTRIC -> Icons.Default.ElectricBolt
                        PowertrainType.HYBRID -> Icons.Default.Eco
                        else -> Icons.Default.LocalGasStation
                    }
                )

                SystemStatusIndicator(
                    title = "DIAGNOSTICS",
                    status = vehicle.vin != null,
                    icon = Icons.Default.Computer
                )
            }
        }
    }
}

@Composable
fun SystemStatusIndicator(
    title: String,
    status: Boolean,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (status) {
                            listOf(MaintenanceGreen.copy(alpha = 0.3f), MaintenanceGreen.copy(alpha = 0.1f))
                        } else {
                            listOf(Color.Red.copy(alpha = 0.3f), Color.Red.copy(alpha = 0.1f))
                        }
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (status) MaintenanceGreen else Color.Red,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Text(
            text = if (status) "ONLINE" else "OFFLINE",
            style = MaterialTheme.typography.labelSmall,
            color = if (status) MaintenanceGreen else Color.Red,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun AutomotivePhotoGallery(vehicle: Vehicle) {
    if (vehicle.photos?.isNotEmpty() == true) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal),
            shape = RoundedCornerShape(CornerRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.large)
            ) {
                Text(
                    text = "PHOTO GALLERY",
                    style = MaterialTheme.typography.labelLarge,
                    color = MechanicBlue40,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(Spacing.large))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    try {
                        val photosArray = JSONArray(vehicle.photos)
                        items(photosArray.length()) { index ->
                            val photoPath = photosArray.getString(index)
                            AutomotivePhotoItem(
                                photoPath = photoPath,
                                isMain = index == (vehicle.mainProfilePhotoIndex ?: 0)
                            )
                        }
                    } catch (e: Exception) {
                        // Handle JSON parsing error
                    }
                }
            }
        }
    }
}

@Composable
fun AutomotivePhotoItem(
    photoPath: String,
    isMain: Boolean
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(CornerRadius.medium))
    ) {
        val photoFile = File(photoPath)
        if (photoFile.exists() && photoFile.canRead()) {
            Image(
                painter = rememberAsyncImagePainter(photoFile),
                contentDescription = "Vehicle Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (isMain) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            MechanicBlue40.copy(alpha = 0.9f),
                            CircleShape
                        )
                        .padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Main Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Glass overlay effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun AutomotiveControlPanel(
    onEdit: () -> Unit,
    onMaintenance: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Text(
                text = "CONTROL PANEL",
                style = MaterialTheme.typography.labelLarge,
                color = MechanicBlue40,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AutomotiveControlButton(
                    title = "EDIT",
                    subtitle = "VEHICLE",
                    icon = Icons.Default.Edit,
                    color = MechanicBlue40,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(Spacing.medium))

                AutomotiveControlButton(
                    title = "SERVICE",
                    subtitle = "HISTORY",
                    icon = Icons.Default.Build,
                    color = WarningAmber40,
                    onClick = onMaintenance,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(Spacing.medium))

                AutomotiveControlButton(
                    title = "DELETE",
                    subtitle = "VEHICLE",
                    icon = Icons.Default.Delete,
                    color = Color.Red,
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AutomotiveControlButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(CornerRadius.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.2f),
            contentColor = color
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

// Helper functions
fun calculateVehicleHealth(vehicle: Vehicle): Int {
    var score = 100

    // Deduct points for high mileage
    if (vehicle.currentMileage > 150000) score -= 30
    else if (vehicle.currentMileage > 100000) score -= 20
    else if (vehicle.currentMileage > 50000) score -= 10

    // Deduct points for old vehicles
    val currentYear = 2024
    val age = currentYear - vehicle.year
    if (age > 15) score -= 25
    else if (age > 10) score -= 15
    else if (age > 5) score -= 5

    // Deduct points if inactive
    if (!vehicle.isActive) score -= 40

    return score.coerceIn(0, 100)
}

