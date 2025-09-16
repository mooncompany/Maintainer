package com.maintainer.app.ui.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maintainer.app.data.database.entity.MaintenanceRecord
import com.maintainer.app.data.database.entity.MaintenanceType
import com.maintainer.app.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceListScreen(
    vehicleId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddMaintenance: () -> Unit,
    onNavigateToEditMaintenance: (String) -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val maintenanceRecords by viewModel.maintenanceRecords.collectAsState()

    // Sort maintenance records by date (newest first)
    val sortedRecords = remember(maintenanceRecords) {
        maintenanceRecords.sortedByDescending { it.serviceDate }
    }

    LaunchedEffect(vehicleId) {
        viewModel.selectVehicle(vehicleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Maintenance Timeline",
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
                onClick = onNavigateToAddMaintenance,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Maintenance",
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
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // Timeline Header
            item {
                Spacer(modifier = Modifier.height(Spacing.medium))
                MaintenanceTimelineHeader(
                    totalRecords = sortedRecords.size,
                    totalCost = sortedRecords.sumOf { it.cost }
                )
            }

            if (sortedRecords.isEmpty()) {
                item {
                    MaintenanceEmptyState(onAddMaintenance = onNavigateToAddMaintenance)
                }
            } else {
                // Timeline subtitle
                item {
                    Text(
                        text = "Service History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = Spacing.small)
                    )
                }

                // Timeline items
                itemsIndexed(sortedRecords) { index, record ->
                    MaintenanceTimelineItem(
                        record = record,
                        isFirst = index == 0,
                        isLast = index == sortedRecords.lastIndex,
                        onEdit = { onNavigateToEditMaintenance(record.id) }
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
fun MaintenanceTimelineHeader(
    totalRecords: Int,
    totalCost: Double
) {
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
                text = "Service Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Total services stat
                MaintenanceTimelineStat(
                    title = "Services",
                    value = totalRecords.toString(),
                    icon = Icons.Default.Build,
                    color = MaterialTheme.colorScheme.primary
                )

                // Total cost stat
                MaintenanceTimelineStat(
                    title = "Total Cost",
                    value = if (totalCost > 0) NumberFormat.getCurrencyInstance(Locale.US).format(totalCost) else "--",
                    icon = Icons.Default.LocalGasStation,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Recent services (placeholder)
                MaintenanceTimelineStat(
                    title = "Recent",
                    value = if (totalRecords > 0) "1" else "0", // Future: Calculate recent services (last 30 days)
                    icon = Icons.Default.Schedule,
                    color = MaintenanceGreen
                )
            }
        }
    }
}

@Composable
fun MaintenanceTimelineStat(
    title: String,
    value: String,
    icon: ImageVector,
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
            style = MaterialTheme.typography.titleMedium,
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
fun MaintenanceEmptyState(onAddMaintenance: () -> Unit) {
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
                    Icons.Default.Build,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Text(
                text = "No service history yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = "Start tracking maintenance by adding your first service record",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            Button(
                onClick = onAddMaintenance,
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
                    text = "Add Service Record",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceTimelineItem(
    record: MaintenanceRecord,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline indicator column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // Line above (if not first)
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(Spacing.medium)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }

            // Timeline dot with maintenance type icon
            TimelineIndicator(
                maintenanceType = record.type,
                isRecent = isRecentMaintenance(record.serviceDate)
            )

            // Line below (if not last)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(Spacing.medium)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        // Card content
        Card(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
                // Header row with type and date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMaintenanceType(record.type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Maintenance",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Description
                if (record.description.isNotBlank()) {
                    Text(
                        text = record.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.small)
                    )
                }

                // Metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Column {
                        Text(
                            text = dateFormat.format(record.serviceDate),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Service Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Mileage
                    if (record.mileage > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${NumberFormat.getNumberInstance(Locale.US).format(record.mileage)} mi",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Mileage",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Cost
                    if (record.cost > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currencyFormat.format(record.cost),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Cost",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineIndicator(
    maintenanceType: MaintenanceType,
    isRecent: Boolean
) {
    val icon = getMaintenanceTypeIcon(maintenanceType)
    val color = if (isRecent) MaintenanceGreen else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.2f),
                        color.copy(alpha = 0.1f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun getMaintenanceTypeIcon(type: MaintenanceType): ImageVector {
    return when (type) {
        MaintenanceType.OIL_CHANGE -> Icons.Default.LocalGasStation
        MaintenanceType.FILTER_CHANGE -> Icons.Default.FilterAlt
        MaintenanceType.BRAKE_SERVICE -> Icons.Default.Settings
        MaintenanceType.TIRE_SERVICE -> Icons.Default.DirectionsCar
        MaintenanceType.BATTERY_SERVICE -> Icons.Default.Settings
        MaintenanceType.ENGINE_SERVICE -> Icons.Default.CarRepair
        MaintenanceType.TRANSMISSION_SERVICE -> Icons.Default.CarRepair
        MaintenanceType.COOLANT_SERVICE -> Icons.Default.LocalGasStation
        MaintenanceType.INSPECTION -> Icons.Default.Schedule
        MaintenanceType.REPAIR -> Icons.Default.CarRepair
        MaintenanceType.OTHER -> Icons.Default.Build
    }
}

fun formatMaintenanceType(type: MaintenanceType): String {
    return type.name.replace('_', ' ').lowercase()
        .split(' ')
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

fun isRecentMaintenance(serviceDate: Date): Boolean {
    val thirtyDaysAgo = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -30)
    }.time
    return serviceDate.after(thirtyDaysAgo)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceRecordCard(
    record: MaintenanceRecord,
    onEdit: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.type.name.replace('_', ' ').lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = dateFormat.format(record.serviceDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${record.mileage.toString().replace(Regex("(\\d)(?=(\\d{3})+\$)"), "$1,")} mi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (record.cost > 0) {
                        Text(
                            text = currencyFormat.format(record.cost),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Maintenance",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}