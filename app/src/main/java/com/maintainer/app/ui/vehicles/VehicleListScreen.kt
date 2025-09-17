package com.maintainer.app.ui.vehicles

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.abs
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.maintainer.app.ui.theme.*
import com.maintainer.app.ui.components.VehicleDeletionDialog
import java.io.File
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToEditVehicle: (String) -> Unit,
    onNavigateToMaintenance: (String) -> Unit,
    viewModel: VehicleViewModel = hiltViewModel()
) {
    val vehicles by viewModel.vehicles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Fleet",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddVehicle,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Vehicle",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        if (vehicles.isEmpty()) {
            VehicleListEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Spacing.screenHorizontal),
                onAddVehicle = onNavigateToAddVehicle
            )
        } else {
            SwipeableVehicleCards(
                vehicles = vehicles,
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onEdit = onNavigateToEditVehicle,
                onViewMaintenance = onNavigateToMaintenance,
                onDelete = { viewModel.deleteVehicle(it) }
            )
        }
    }
}

@Composable
fun VehicleListEmptyState(
    modifier: Modifier = Modifier,
    onAddVehicle: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
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
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                Text(
                    text = "No vehicles in your fleet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                Text(
                    text = "Add vehicles to track their maintenance history and costs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.large))

                Button(
                    onClick = onAddVehicle,
                    modifier = Modifier.fillMaxWidth(0.7f),
                    shape = RoundedCornerShape(CornerRadius.medium)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "Add Vehicle",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeableVehicleCards(
    vehicles: List<com.maintainer.app.data.database.entity.Vehicle>,
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier,
    onEdit: (String) -> Unit,
    onViewMaintenance: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.medium))
        }

        item {
            VehicleFleetStats(vehicles = vehicles, viewModel = viewModel)
        }

        item {
            Text(
                text = "Your Vehicles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = Spacing.medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(vehicles) { vehicle ->
            SwipeableVehicleCard(
                vehicle = vehicle,
                viewModel = viewModel,
                onEdit = { onEdit(vehicle.id) },
                onViewMaintenance = { onViewMaintenance(vehicle.id) },
                onDelete = { onDelete(vehicle.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.massive))
        }
    }
}

@Composable
fun VehicleFleetStats(
    vehicles: List<com.maintainer.app.data.database.entity.Vehicle>,
    viewModel: VehicleViewModel
) {
    var totalFleetValue by remember { mutableStateOf(0.0) }
    var averageHealthScore by remember { mutableStateOf(0) }

    LaunchedEffect(vehicles) {
        // Calculate fleet statistics
        totalFleetValue = vehicles.sumOf { it.currentMileage * 0.01 } // Mock calculation
        averageHealthScore = if (vehicles.isNotEmpty()) {
            vehicles.map { calculateHealthScore(it) }.average().toInt()
        } else 0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = SteelGray40.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fleet Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${vehicles.size} Vehicle${if (vehicles.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FleetStatItem(
                    label = "Avg Health",
                    value = "$averageHealthScore%",
                    color = getHealthColor(averageHealthScore)
                )

                FleetStatItem(
                    label = "Active",
                    value = "${vehicles.count { it.isActive }}",
                    color = MaintenanceGreen
                )

                FleetStatItem(
                    label = "Needs Service",
                    value = "${vehicles.count { calculateHealthScore(it) < 70 }}",
                    color = WarningAmber40
                )
            }
        }
    }
}

@Composable
fun FleetStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableVehicleCard(
    vehicle: com.maintainer.app.data.database.entity.Vehicle,
    viewModel: VehicleViewModel,
    onEdit: () -> Unit,
    onViewMaintenance: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var totalMaintenanceCost by remember { mutableStateOf(0.0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_offset"
    )

    val healthScore = calculateHealthScore(vehicle)

    LaunchedEffect(vehicle.id) {
        viewModel.getTotalMaintenanceCostForVehicle(vehicle.id) { cost ->
            totalMaintenanceCost = cost
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Background action indicators
        if (abs(animatedOffset) > 50) {
            SwipeActionBackground(
                offset = animatedOffset,
                isLeft = animatedOffset < 0
            )
        }

        // Main card
        Card(
            onClick = onViewMaintenance,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = animatedOffset
                    rotationZ = animatedOffset * 0.02f
                    scaleX = 1f - (abs(animatedOffset) * 0.0005f)
                    scaleY = 1f - (abs(animatedOffset) * 0.0005f)
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            when {
                                offsetX < -200 -> {
                                    // Swipe left for edit
                                    onEdit()
                                    offsetX = 0f
                                }
                                offsetX > 200 -> {
                                    // Swipe right for delete
                                    showDeleteDialog = true
                                    offsetX = 0f
                                }
                                else -> {
                                    offsetX = 0f
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        offsetX += dragAmount.x
                        offsetX = offsetX.coerceIn(-300f, 300f)
                    }
                },
            shape = RoundedCornerShape(CornerRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Elevation.card + (abs(animatedOffset) * 0.01f).dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.cardPadding)
            ) {
                // Vehicle header with health score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VehicleCardImage(vehicle = vehicle)

                        Spacer(modifier = Modifier.width(Spacing.medium))

                        Column {
                            Text(
                                text = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    VehicleHealthBadge(healthScore = healthScore)
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Vehicle stats row
                VehicleStatsRow(
                    vehicle = vehicle,
                    totalMaintenanceCost = totalMaintenanceCost
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Action buttons row
                VehicleCardActions(
                    onEdit = onEdit,
                    onViewMaintenance = onViewMaintenance,
                    onDelete = { showDeleteDialog = true }
                )
            }
        }
    }

    // Deletion confirmation dialog
    VehicleDeletionDialog(
        isVisible = showDeleteDialog,
        vehicleName = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}",
        onConfirm = onDelete,
        onDismiss = { showDeleteDialog = false }
    )
}

@Composable
fun SwipeActionBackground(
    offset: Float,
    isLeft: Boolean
) {
    val backgroundColor = if (isLeft) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val icon = if (isLeft) Icons.Default.Edit else Icons.Default.Delete
    val text = if (isLeft) "Edit" else "Delete"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                color = backgroundColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(CornerRadius.large)
            ),
        contentAlignment = if (isLeft) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.large)
        ) {
            if (!isLeft) {
                Icon(
                    icon,
                    contentDescription = text,
                    tint = backgroundColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = backgroundColor,
                fontWeight = FontWeight.Bold
            )

            if (isLeft) {
                Spacer(modifier = Modifier.width(Spacing.small))
                Icon(
                    icon,
                    contentDescription = text,
                    tint = backgroundColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun VehicleCardImage(vehicle: com.maintainer.app.data.database.entity.Vehicle) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(CornerRadius.medium)),
        contentAlignment = Alignment.Center
    ) {
        if (!vehicle.photoPath.isNullOrEmpty()) {
            val photoFile = File(vehicle.photoPath)
            if (photoFile.exists() && photoFile.canRead()) {
                Image(
                    painter = rememberAsyncImagePainter(photoFile),
                    contentDescription = "Vehicle Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                VehicleCardIconBackground()
            }
        } else {
            VehicleCardIconBackground()
        }
    }
}

@Composable
fun VehicleCardIconBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MechanicBlue40.copy(alpha = 0.3f),
                        MechanicBlue40.copy(alpha = 0.1f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MechanicBlue40
        )
    }
}

@Composable
fun VehicleHealthBadge(healthScore: Int) {
    val color = getHealthColor(healthScore)
    val backgroundColor = color.copy(alpha = 0.15f)

    Surface(
        shape = RoundedCornerShape(CornerRadius.small),
        color = backgroundColor,
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.tiny),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )

            Spacer(modifier = Modifier.width(Spacing.tiny))

            Text(
                text = "$healthScore%",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VehicleStatsRow(
    vehicle: com.maintainer.app.data.database.entity.Vehicle,
    totalMaintenanceCost: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        VehicleStatItem(
            label = "Miles",
            value = if (vehicle.currentMileage > 0) {
                NumberFormat.getNumberInstance(Locale.US).format(vehicle.currentMileage)
            } else "--",
            icon = Icons.Default.Speed
        )

        VehicleStatItem(
            label = "Status",
            value = if (vehicle.isActive) "Active" else "Inactive",
            icon = Icons.Default.Circle,
            valueColor = if (vehicle.isActive) MaintenanceGreen else MaterialTheme.colorScheme.error
        )

        VehicleStatItem(
            label = "Cost",
            value = if (totalMaintenanceCost > 0) {
                NumberFormat.getCurrencyInstance(Locale.US).format(totalMaintenanceCost)
            } else "$0",
            icon = Icons.Default.AttachMoney
        )
    }
}

@Composable
fun VehicleStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.tiny))

        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VehicleCardActions(
    onEdit: () -> Unit,
    onViewMaintenance: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(CornerRadius.medium)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.tiny))
            Text("Edit")
        }

        Spacer(modifier = Modifier.width(Spacing.small))

        Button(
            onClick = onViewMaintenance,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(CornerRadius.medium)
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = "Maintenance",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.tiny))
            Text("Service")
        }

        Spacer(modifier = Modifier.width(Spacing.small))

        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(CornerRadius.medium),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.tiny))
            Text("Delete")
        }
    }
}

fun calculateHealthScore(vehicle: com.maintainer.app.data.database.entity.Vehicle): Int {
    // Mock health score calculation based on various factors
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

fun getHealthColor(healthScore: Int): Color {
    return when {
        healthScore >= 80 -> MaintenanceGreen
        healthScore >= 60 -> WarningAmber40
        else -> Color(0xFFE53E3E) // Red
    }
}

// Legacy components for backward compatibility
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedVehicleListItem(
    vehicle: com.maintainer.app.data.database.entity.Vehicle,
    viewModel: VehicleViewModel,
    onEdit: () -> Unit,
    onViewMaintenance: () -> Unit,
    onDelete: () -> Unit
) {
    var totalMaintenanceCost by remember { mutableStateOf(0.0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(vehicle.id) {
        viewModel.getTotalMaintenanceCostForVehicle(vehicle.id) { cost ->
            totalMaintenanceCost = cost
        }
    }

    Card(
        onClick = onViewMaintenance,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.card
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VehicleListItemImage(vehicle = vehicle)

            Spacer(modifier = Modifier.width(Spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VehicleListStatusIndicator(isActive = vehicle.isActive)

                    if (vehicle.currentMileage > 0) {
                        Text(
                            text = "${NumberFormat.getNumberInstance(Locale.US).format(vehicle.currentMileage)} mi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (totalMaintenanceCost > 0 || vehicle.licensePlate?.isNotEmpty() == true) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.tiny),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (vehicle.licensePlate?.isNotEmpty() == true) {
                            Text(
                                text = vehicle.licensePlate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (totalMaintenanceCost > 0) {
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US).format(totalMaintenanceCost),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            VehicleListItemActions(
                onEdit = onEdit,
                onViewMaintenance = onViewMaintenance,
                onDelete = { showDeleteDialog = true }
            )
        }
    }

    VehicleDeletionDialog(
        isVisible = showDeleteDialog,
        vehicleName = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}",
        onConfirm = onDelete,
        onDismiss = { showDeleteDialog = false }
    )
}

@Composable
fun VehicleListItemImage(vehicle: com.maintainer.app.data.database.entity.Vehicle) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(CornerRadius.medium)),
        contentAlignment = Alignment.Center
    ) {
        if (!vehicle.photoPath.isNullOrEmpty()) {
            val photoFile = File(vehicle.photoPath)
            if (photoFile.exists() && photoFile.canRead()) {
                Image(
                    painter = rememberAsyncImagePainter(photoFile),
                    contentDescription = "Vehicle Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                VehicleListItemIconBackground()
            }
        } else {
            VehicleListItemIconBackground()
        }
    }
}

@Composable
fun VehicleListItemIconBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun VehicleListStatusIndicator(isActive: Boolean) {
    val backgroundColor = if (isActive) MaintenanceGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
    val contentColor = if (isActive) MaintenanceGreen else MaterialTheme.colorScheme.error

    Surface(
        shape = RoundedCornerShape(CornerRadius.small),
        color = backgroundColor
    ) {
        Text(
            text = if (isActive) "Active" else "Inactive",
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.tiny),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VehicleListItemActions(
    onEdit: () -> Unit,
    onViewMaintenance: () -> Unit,
    onDelete: () -> Unit
) {
    Column {
        IconButton(onClick = onEdit) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit Vehicle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onViewMaintenance) {
            Icon(
                Icons.Default.Build,
                contentDescription = "View Maintenance",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Vehicle",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}