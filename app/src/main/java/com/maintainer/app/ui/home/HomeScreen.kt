package com.maintainer.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.maintainer.app.ui.theme.*
import com.maintainer.app.ui.vehicles.VehicleViewModel
import java.io.File
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToVehicles: () -> Unit,
    onNavigateToMaintenance: (String) -> Unit,
    onNavigateToEditVehicle: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: VehicleViewModel = hiltViewModel()
) {
    val vehicles by viewModel.vehicles.collectAsState()

    Scaffold(
        topBar = {
            // Modern dark-themed top bar with glassmorphism effect
            TopAppBar(
                title = {
                    Text(
                        text = "Maintainer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            // Enhanced FAB with automotive styling
            FloatingActionButton(
                onClick = onNavigateToVehicles,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // Dashboard Header with overview stats
            item {
                Spacer(modifier = Modifier.height(Spacing.medium))
                DashboardHeader(vehicles = vehicles)
            }

            // Fleet overview section
            item {
                Text(
                    text = "Your Fleet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = Spacing.small)
                )
            }

            if (vehicles.isEmpty()) {
                item {
                    EmptyStateCard(onAddVehicle = onNavigateToVehicles)
                }
            } else {
                items(vehicles) { vehicle ->
                    EnhancedVehicleCard(
                        vehicle = vehicle,
                        viewModel = viewModel,
                        onClick = { onNavigateToMaintenance(vehicle.id) },
                        onEditClick = { onNavigateToEditVehicle(vehicle.id) }
                    )
                }
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(Spacing.massive))
            }
        }
    }
}

@Composable
fun DashboardHeader(vehicles: List<com.maintainer.app.data.database.entity.Vehicle>) {
    // Dashboard overview with glassmorphism effect
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Text(
                text = "Fleet Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Total vehicles stat
                DashboardStat(
                    title = "Vehicles",
                    value = vehicles.size.toString(),
                    icon = Icons.Default.DirectionsCar,
                    color = MaterialTheme.colorScheme.primary
                )

                // Active vehicles (placeholder logic)
                DashboardStat(
                    title = "Active",
                    value = vehicles.filter { it.isActive }.size.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = MaintenanceGreen
                )

                // Maintenance due (placeholder)
                DashboardStat(
                    title = "Due Soon",
                    value = "0", // Future: Calculate from maintenance schedule
                    icon = Icons.Default.Schedule,
                    color = WarningAmber80
                )
            }
        }
    }
}

@Composable
fun DashboardStat(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.small))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateCard(onAddVehicle: () -> Unit) {
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
            // Gradient background for icon
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
                text = "Your garage awaits",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = "Add your first vehicle to start tracking maintenance history, costs, and schedules",
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
                    text = "Add Your First Vehicle",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedVehicleCard(
    vehicle: com.maintainer.app.data.database.entity.Vehicle,
    viewModel: VehicleViewModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var totalMaintenanceCost by remember { mutableStateOf(0.0) }
    var latestServiceMileage by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(vehicle.id) {
        viewModel.getTotalMaintenanceCostForVehicle(vehicle.id) { cost ->
            totalMaintenanceCost = cost
        }
        viewModel.getLatestServiceMileageForVehicle(vehicle.id) { mileage ->
            latestServiceMileage = mileage
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.card
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.cardPadding)
        ) {
            // Top row with vehicle info and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vehicle photo or icon with enhanced styling
                VehicleImageOrIcon(vehicle = vehicle)

                Spacer(modifier = Modifier.width(Spacing.medium))

                // Vehicle details
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

                    // Vehicle status indicator
                    VehicleStatusChip(isActive = vehicle.isActive)
                }

                // Action buttons
                VehicleActions(
                    onEditClick = onEditClick,
                    onMaintenanceClick = onClick
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Bottom section with metrics
            VehicleMetrics(
                displayMileage = latestServiceMileage ?: vehicle.currentMileage,
                isLatestService = latestServiceMileage != null,
                totalMaintenanceCost = totalMaintenanceCost
            )
        }
    }
}

@Composable
fun VehicleImageOrIcon(vehicle: com.maintainer.app.data.database.entity.Vehicle) {
    Box(
        modifier = Modifier
            .size(72.dp)
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
                VehicleIconBackground()
            }
        } else {
            VehicleIconBackground()
        }
    }
}

@Composable
fun VehicleIconBackground() {
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
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun VehicleStatusChip(isActive: Boolean) {
    val backgroundColor = if (isActive) MaintenanceGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
    val contentColor = if (isActive) MaintenanceGreen else MaterialTheme.colorScheme.error

    Surface(
        shape = RoundedCornerShape(CornerRadius.small),
        color = backgroundColor,
        modifier = Modifier.padding(top = Spacing.tiny)
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
fun VehicleActions(
    onEditClick: () -> Unit,
    onMaintenanceClick: () -> Unit
) {
    Row {
        IconButton(onClick = onEditClick) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit Vehicle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onMaintenanceClick) {
            Icon(
                Icons.Default.Build,
                contentDescription = "View Maintenance",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun VehicleMetrics(
    displayMileage: Int,
    isLatestService: Boolean,
    totalMaintenanceCost: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Mileage info
        if (displayMileage > 0) {
            Column {
                Text(
                    text = NumberFormat.getNumberInstance(Locale.US).format(displayMileage),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isLatestService) "miles (last service)" else "miles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cost info
        if (totalMaintenanceCost > 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale.US).format(totalMaintenanceCost),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "total maintenance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCard(
    vehicle: com.maintainer.app.data.database.entity.Vehicle,
    viewModel: VehicleViewModel,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var totalMaintenanceCost by remember { mutableStateOf(0.0) }
    var latestServiceMileage by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(vehicle.id) {
        viewModel.getTotalMaintenanceCostForVehicle(vehicle.id) { cost ->
            totalMaintenanceCost = cost
        }
        viewModel.getLatestServiceMileageForVehicle(vehicle.id) { mileage ->
            latestServiceMileage = mileage
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle photo or default icon
            if (!vehicle.photoPath.isNullOrEmpty()) {
                val photoFile = File(vehicle.photoPath)
                if (photoFile.exists() && photoFile.canRead()) {
                    Image(
                        painter = rememberAsyncImagePainter(photoFile),
                        contentDescription = "Vehicle Photo",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Show latest service mileage if available, otherwise show vehicle's original mileage
                val displayMileage = latestServiceMileage ?: vehicle.currentMileage
                if (displayMileage > 0) {
                    Text(
                        text = "${NumberFormat.getNumberInstance(Locale.US).format(displayMileage)} miles" +
                               if (latestServiceMileage != null) " (last service)" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (totalMaintenanceCost > 0) {
                    Text(
                        text = "Total maintenance: ${NumberFormat.getCurrencyInstance(Locale.US).format(totalMaintenanceCost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Vehicle",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onClick) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = "View Maintenance",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}