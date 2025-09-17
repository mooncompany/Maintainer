package com.maintainer.app.ui.garage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Analytics
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.maintainer.app.ui.theme.*
import com.maintainer.app.ui.vehicles.VehicleViewModel
import com.maintainer.app.ui.components.VehicleDeletionDialog
import com.maintainer.app.data.service.ServiceTemplateEngine
import com.maintainer.app.data.service.ServicePriority
import java.io.File
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(
    onNavigateToVehicles: () -> Unit,
    onNavigateToMaintenance: (String) -> Unit,
    onNavigateToEditVehicle: (String) -> Unit = {},
    onNavigateToVehicleProfile: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
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
            // Attention Needed Alert Card
            item {
                Spacer(modifier = Modifier.height(Spacing.medium))
                AttentionNeededCard(vehicles = vehicles, onVehicleClick = onNavigateToVehicleProfile)
            }

            // Fleet Overview Card
            item {
                FleetOverviewCard(vehicles = vehicles, viewModel = viewModel)
            }

            // Recent Activity Card
            item {
                RecentActivityCard(vehicles = vehicles, viewModel = viewModel)
            }

            // Quick Actions Card
            item {
                ModernQuickActionsCard(
                    onLogService = { /* TODO: Navigate to quick maintenance */ },
                    onAddPhoto = { /* TODO: Navigate to camera */ },
                    onFindShop = { /* TODO: Navigate to map */ },
                    onTrackCost = onNavigateToAnalytics
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
                        onClick = { onNavigateToVehicleProfile(vehicle.id) },
                        onEditClick = { onNavigateToEditVehicle(vehicle.id) },
                        onDeleteClick = { viewModel.deleteVehicle(vehicle.id) }
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
fun IntelligentDashboardHeader(vehicles: List<com.maintainer.app.data.database.entity.Vehicle>, viewModel: VehicleViewModel) {
    var totalMaintenanceCost by remember { mutableStateOf(0.0) }
    var overdueServicesCount by remember { mutableStateOf(0) }

    LaunchedEffect(vehicles) {
        // Calculate total fleet maintenance cost
        var totalCost = 0.0
        var overdueCount = 0

        vehicles.forEach { vehicle ->
            viewModel.getTotalMaintenanceCostForVehicle(vehicle.id) { cost ->
                totalCost += cost
            }

            // Calculate overdue services for each vehicle
            val criticalServices = ServiceTemplateEngine.getCriticalOverdueServices(
                powertrainType = vehicle.powertrainType,
                currentMileage = vehicle.currentMileage,
                lastServiceMileage = 0, // TODO: Get actual last service mileage
                lastServiceDate = Date(0) // TODO: Get actual last service date
            )
            overdueCount += criticalServices.size
        }

        totalMaintenanceCost = totalCost
        overdueServicesCount = overdueCount
    }
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
                text = "Garage Dashboard",
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

                // Fleet maintenance cost
                DashboardStat(
                    title = "Maintenance",
                    value = if (totalMaintenanceCost > 0) NumberFormat.getCurrencyInstance(Locale.US).format(totalMaintenanceCost) else "$0",
                    icon = Icons.Default.Build,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Critical services
                DashboardStat(
                    title = if (overdueServicesCount > 0) "Overdue" else "Up to Date",
                    value = if (overdueServicesCount > 0) overdueServicesCount.toString() else "✓",
                    icon = if (overdueServicesCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                    color = if (overdueServicesCount > 0) MaterialTheme.colorScheme.error else MaintenanceGreen
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
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var totalMaintenanceCost by remember { mutableStateOf(0.0) }
    var latestServiceMileage by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                    onMaintenanceClick = onClick,
                    onDeleteClick = { showDeleteDialog = true }
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

    // Deletion confirmation dialog
    VehicleDeletionDialog(
        isVisible = showDeleteDialog,
        vehicleName = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}",
        onConfirm = onDeleteClick,
        onDismiss = { showDeleteDialog = false }
    )
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
    onMaintenanceClick: () -> Unit,
    onDeleteClick: () -> Unit
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
        IconButton(onClick = onDeleteClick) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete Vehicle",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MaintenanceAlertsSection(vehicles: List<com.maintainer.app.data.database.entity.Vehicle>) {
    // Calculate total overdue services across fleet
    var overdueAlerts by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    LaunchedEffect(vehicles) {
        val alerts = vehicles.mapNotNull { vehicle ->
            val criticalServices = ServiceTemplateEngine.getCriticalOverdueServices(
                powertrainType = vehicle.powertrainType,
                currentMileage = vehicle.currentMileage
            )
            if (criticalServices.isNotEmpty()) {
                val vehicleName = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}"
                vehicleName to criticalServices.size
            } else null
        }
        overdueAlerts = alerts
    }

    if (overdueAlerts.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.large)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Text(
                        text = "Maintenance Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                overdueAlerts.forEach { (vehicleName, count) ->
                    Text(
                        text = "$vehicleName has $count overdue service${if (count > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsHub(
    onAddVehicle: () -> Unit,
    onQuickMaintenance: () -> Unit,
    onViewFullFleet: () -> Unit
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
                .padding(Spacing.large)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionCard(
                    icon = Icons.Default.Add,
                    title = "Add Vehicle",
                    subtitle = "Register new vehicle",
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onAddVehicle,
                    isPrimary = true
                )

                QuickActionCard(
                    icon = Icons.Default.Build,
                    title = "Quick Service",
                    subtitle = "Log maintenance",
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = onQuickMaintenance
                )

                QuickActionCard(
                    icon = Icons.Default.DirectionsCar,
                    title = "View Fleet",
                    subtitle = "Manage vehicles",
                    color = MaterialTheme.colorScheme.tertiary,
                    onClick = onViewFullFleet
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    val cardWidth = if (isPrimary) 140.dp else 100.dp

    Card(
        modifier = Modifier
            .width(cardWidth)
            .clickable { onClick() },
        shape = RoundedCornerShape(CornerRadius.medium),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPrimary) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(if (isPrimary) 56.dp else 48.dp)
                    .background(
                        color.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(if (isPrimary) 28.dp else 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = title,
                style = if (isPrimary) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

@Composable
fun AttentionNeededCard(
    vehicles: List<com.maintainer.app.data.database.entity.Vehicle>,
    onVehicleClick: (String) -> Unit
) {
    // Find vehicles that need attention
    val urgentVehicles = vehicles.filter { vehicle ->
        val criticalServices = ServiceTemplateEngine.getCriticalOverdueServices(
            powertrainType = vehicle.powertrainType,
            currentMileage = vehicle.currentMileage
        )
        criticalServices.isNotEmpty()
    }

    if (urgentVehicles.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.large)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Text(
                        text = "🔴 ATTENTION NEEDED",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                urgentVehicles.forEach { vehicle ->
                    UrgentVehicleItem(
                        vehicle = vehicle,
                        onClick = { onVehicleClick(vehicle.id) }
                    )
                    if (vehicle != urgentVehicles.last()) {
                        Spacer(modifier = Modifier.height(Spacing.small))
                    }
                }
            }
        }
    }
}

@Composable
fun UrgentVehicleItem(
    vehicle: com.maintainer.app.data.database.entity.Vehicle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(CornerRadius.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = vehicle.nickname ?: "${vehicle.year} ${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = "Oil change 2,847 miles overdue", // TODO: Calculate actual overdue amount
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Details")
                }
                Button(
                    onClick = { /* TODO: Mark complete */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Mark Complete")
                }
            }
        }
    }
}

@Composable
fun FleetOverviewCard(
    vehicles: List<com.maintainer.app.data.database.entity.Vehicle>,
    viewModel: VehicleViewModel
) {
    var totalMaintenanceCost by remember { mutableStateOf(0.0) }

    LaunchedEffect(vehicles) {
        var totalCost = 0.0
        vehicles.forEach { vehicle ->
            viewModel.getTotalMaintenanceCostForVehicle(vehicle.id) { cost ->
                totalCost += cost
            }
        }
        totalMaintenanceCost = totalCost
    }

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
                .padding(Spacing.large)
        ) {
            Text(
                text = "📊 FLEET OVERVIEW",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Fleet status dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                repeat(8) { index ->
                    FleetStatusDot(
                        isActive = index < vehicles.size,
                        status = if (index < vehicles.size) {
                            vehicles[index].let { vehicle ->
                                val criticalServices = ServiceTemplateEngine.getCriticalOverdueServices(
                                    powertrainType = vehicle.powertrainType,
                                    currentMileage = vehicle.currentMileage
                                )
                                when {
                                    criticalServices.isNotEmpty() -> FleetStatus.CRITICAL
                                    vehicle.currentMileage % 5000 < 500 -> FleetStatus.WARNING
                                    else -> FleetStatus.HEALTHY
                                }
                            }
                        } else FleetStatus.EMPTY
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Text(
                text = "${vehicles.size} vehicles • ${NumberFormat.getCurrencyInstance(Locale.US).format(totalMaintenanceCost)} total costs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class FleetStatus {
    HEALTHY, WARNING, CRITICAL, EMPTY
}

@Composable
fun FleetStatusDot(isActive: Boolean, status: FleetStatus) {
    val color = when {
        !isActive -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        status == FleetStatus.HEALTHY -> Color(0xFF00D4AA)
        status == FleetStatus.WARNING -> Color(0xFFFF9500)
        status == FleetStatus.CRITICAL -> Color(0xFFFF3B30)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isActive && status != FleetStatus.EMPTY) {
            val icon = when (status) {
                FleetStatus.HEALTHY -> "●"
                FleetStatus.WARNING -> "●"
                FleetStatus.CRITICAL -> "●"
                else -> ""
            }
            Text(
                text = icon,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RecentActivityCard(
    vehicles: List<com.maintainer.app.data.database.entity.Vehicle>,
    viewModel: VehicleViewModel
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
                .padding(Spacing.large)
        ) {
            Text(
                text = "📋 RECENT ACTIVITY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // TODO: Get actual recent maintenance records
            RecentActivityItem(
                icon = "🛢️",
                title = "Oil change - Honda Civic",
                subtitle = "$47.99 • 3 days ago"
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            RecentActivityItem(
                icon = "🛞",
                title = "Tire rotation - Toyota",
                subtitle = "$35.00 • 1 week ago"
            )
        }
    }
}

@Composable
fun RecentActivityItem(icon: String, title: String, subtitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModernQuickActionsCard(
    onLogService: () -> Unit,
    onAddPhoto: () -> Unit,
    onFindShop: () -> Unit,
    onTrackCost: () -> Unit
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
                .padding(Spacing.large)
        ) {
            Text(
                text = "🎯 QUICK ACTIONS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Build,
                    label = "Log Service",
                    onClick = onLogService
                )
                QuickActionButton(
                    icon = Icons.Default.CameraAlt,
                    label = "Add Photo",
                    onClick = onAddPhoto
                )
                QuickActionButton(
                    icon = Icons.Default.LocationOn,
                    label = "Find Shop",
                    onClick = onFindShop
                )
                QuickActionButton(
                    icon = Icons.Default.Analytics,
                    label = "Track Cost",
                    onClick = onTrackCost
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}