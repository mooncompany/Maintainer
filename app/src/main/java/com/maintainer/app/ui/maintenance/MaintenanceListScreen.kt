package com.maintainer.app.ui.maintenance

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Sort and group maintenance records by month/year
    val groupedRecords = remember(maintenanceRecords) {
        maintenanceRecords
            .sortedByDescending { it.serviceDate }
            .groupBy { record ->
                val calendar = Calendar.getInstance().apply { time = record.serviceDate }
                "${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.YEAR)}"
            }
            .map { (monthYear, records) ->
                val sampleDate = records.first().serviceDate
                val calendar = Calendar.getInstance().apply { time = sampleDate }
                val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(sampleDate)
                monthName to records
            }
    }

    LaunchedEffect(vehicleId) {
        viewModel.selectVehicle(vehicleId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Service Timeline",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddMaintenance,
                    containerColor = MechanicBlue40,
                    contentColor = Color.White,
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(MechanicBlue40, MechanicBlue40.copy(alpha = 0.8f))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Service",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "Add Service",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Timeline Header with Statistics
                item {
                    Spacer(modifier = Modifier.height(Spacing.medium))
                    ModernTimelineHeader(
                        totalRecords = maintenanceRecords.size,
                        totalCost = maintenanceRecords.sumOf { it.cost },
                        avgCost = if (maintenanceRecords.isNotEmpty()) maintenanceRecords.sumOf { it.cost } / maintenanceRecords.size else 0.0
                    )
                }

                if (groupedRecords.isEmpty()) {
                    item {
                        ModernEmptyState(onAddMaintenance = onNavigateToAddMaintenance)
                    }
                } else {
                    // Timeline Groups by Month
                    groupedRecords.forEach { (monthYear, records) ->
                        item {
                            TimelineMonthHeader(monthName = monthYear)
                        }

                        itemsIndexed(records) { index, record ->
                            val animationDelay = index * 100
                            ModernTimelineItem(
                                record = record,
                                isFirst = index == 0,
                                isLast = index == records.lastIndex,
                                animationDelay = animationDelay,
                                onEdit = { onNavigateToEditMaintenance(record.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(Spacing.large))
                        }
                    }
                }

                // Bottom spacing for FAB
                item {
                    Spacer(modifier = Modifier.height(Spacing.massive))
                }
            }
        }
    }
}

@Composable
fun ModernTimelineHeader(
    totalRecords: Int,
    totalCost: Double,
    avgCost: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.extraLarge),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.extraLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Service Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SteelGray40
                )

                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MechanicBlue40.copy(alpha = 0.2f),
                                    MechanicBlue40.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MechanicBlue40,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = "Total Services",
                    value = totalRecords.toString(),
                    subtitle = "lifetime",
                    color = MechanicBlue40,
                    icon = Icons.Default.Build
                )

                StatisticCard(
                    title = "Total Cost",
                    value = if (totalCost > 0) NumberFormat.getCurrencyInstance(Locale.US).format(totalCost) else "$0",
                    subtitle = "spent",
                    color = WarningAmber40,
                    icon = Icons.Default.AttachMoney
                )

                StatisticCard(
                    title = "Average Cost",
                    value = if (avgCost > 0) NumberFormat.getCurrencyInstance(Locale.US).format(avgCost) else "$0",
                    subtitle = "per service",
                    color = MaintenanceGreen,
                    icon = Icons.Default.TrendingUp
                )
            }
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
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
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = SteelGray40,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TimelineMonthHeader(monthName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MechanicBlue40.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.width(Spacing.medium))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MechanicBlue40.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = Spacing.small)
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MechanicBlue40,
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MechanicBlue40.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTimelineItem(
    record: MaintenanceRecord,
    isFirst: Boolean,
    isLast: Boolean,
    animationDelay: Int,
    onEdit: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance() }

    // Animation state with delayed trigger
    var animationTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        animationTriggered = true
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = EaseOutCubic
        ),
        label = "alpha_animation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animatedAlpha)
    ) {
        // Enhanced Timeline Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp)
        ) {
            // Line above (if not first)
            if (!isFirst) {
                Canvas(
                    modifier = Modifier
                        .width(3.dp)
                        .height(Spacing.large)
                ) {
                    drawTimelineLine(
                        color = MechanicBlue40.copy(alpha = 0.3f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }

            // Enhanced Timeline Indicator
            EnhancedTimelineIndicator(
                maintenanceType = record.type,
                isRecent = isRecentMaintenance(record.serviceDate),
                scale = animatedScale
            )

            // Line below (if not last)
            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(3.dp)
                        .height(Spacing.large)
                ) {
                    drawTimelineLine(
                        color = MechanicBlue40.copy(alpha = 0.3f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        // Enhanced Card Content
        Card(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                },
            shape = RoundedCornerShape(CornerRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.large)
            ) {
                // Header with type and priority indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            getMaintenanceTypeColor(record.type).copy(alpha = 0.2f),
                                            getMaintenanceTypeColor(record.type).copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                getMaintenanceTypeIcon(record.type),
                                contentDescription = null,
                                tint = getMaintenanceTypeColor(record.type),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(Spacing.medium))

                        Column {
                            Text(
                                text = formatMaintenanceType(record.type),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SteelGray40
                            )

                            if (isRecentMaintenance(record.serviceDate)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaintenanceGreen.copy(alpha = 0.1f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "RECENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaintenanceGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .background(
                                MechanicBlue40.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MechanicBlue40,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Description with better typography
                if (record.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.medium))

                    Text(
                        text = record.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Enhanced Metrics Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricChip(
                        label = "Date",
                        value = dateFormat.format(record.serviceDate),
                        icon = Icons.Default.CalendarToday,
                        color = MechanicBlue40
                    )

                    if (record.mileage > 0) {
                        MetricChip(
                            label = "Mileage",
                            value = "${NumberFormat.getNumberInstance(Locale.US).format(record.mileage)} mi",
                            icon = Icons.Default.Speed,
                            color = WarningAmber40
                        )
                    }

                    if (record.cost > 0) {
                        MetricChip(
                            label = "Cost",
                            value = currencyFormat.format(record.cost),
                            icon = Icons.Default.AttachMoney,
                            color = MaintenanceGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EnhancedTimelineIndicator(
    maintenanceType: MaintenanceType,
    isRecent: Boolean,
    scale: Float
) {
    val icon = getMaintenanceTypeIcon(maintenanceType)
    val primaryColor = getMaintenanceTypeColor(maintenanceType)
    val pulseColor = if (isRecent) MaintenanceGreen else primaryColor

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer pulse ring for recent items
        if (isRecent) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaintenanceGreen.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Main indicator
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f),
                            primaryColor.copy(alpha = 0.1f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ModernEmptyState(onAddMaintenance: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.extraLarge),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
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
                    .size(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MechanicBlue40.copy(alpha = 0.2f),
                                MechanicBlue40.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Timeline,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MechanicBlue40
                )
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Text(
                text = "No Service History Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SteelGray40
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = "Start building your vehicle's service timeline by adding your first maintenance record",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            Button(
                onClick = onAddMaintenance,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MechanicBlue40
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "Add First Service",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helper functions with enhanced styling
fun DrawScope.drawTimelineLine(color: Color, strokeWidth: Float) {
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.5f),
                color
            )
        ),
        start = Offset(size.width / 2, 0f),
        end = Offset(size.width / 2, size.height),
        strokeWidth = strokeWidth
    )
}

@Composable
fun getMaintenanceTypeIcon(type: MaintenanceType): ImageVector {
    return when (type) {
        MaintenanceType.OIL_CHANGE -> Icons.Default.Opacity
        MaintenanceType.FILTER_CHANGE -> Icons.Default.FilterAlt
        MaintenanceType.BRAKE_SERVICE -> Icons.Default.Settings
        MaintenanceType.TIRE_SERVICE -> Icons.Default.Circle
        MaintenanceType.BATTERY_SERVICE -> Icons.Default.Battery6Bar
        MaintenanceType.ENGINE_SERVICE -> Icons.Default.PrecisionManufacturing
        MaintenanceType.TRANSMISSION_SERVICE -> Icons.Default.Settings
        MaintenanceType.COOLANT_SERVICE -> Icons.Default.Waves
        MaintenanceType.INSPECTION -> Icons.Default.Visibility
        MaintenanceType.REPAIR -> Icons.Default.Build
        MaintenanceType.OTHER -> Icons.Default.MoreHoriz
    }
}

@Composable
fun getMaintenanceTypeColor(type: MaintenanceType): Color {
    return when (type) {
        MaintenanceType.OIL_CHANGE -> Color(0xFF2196F3) // Blue
        MaintenanceType.FILTER_CHANGE -> Color(0xFF4CAF50) // Green
        MaintenanceType.BRAKE_SERVICE -> Color(0xFFFF5722) // Red-Orange
        MaintenanceType.TIRE_SERVICE -> Color(0xFF607D8B) // Blue Grey
        MaintenanceType.BATTERY_SERVICE -> Color(0xFFFFEB3B) // Yellow
        MaintenanceType.ENGINE_SERVICE -> Color(0xFFE91E63) // Pink
        MaintenanceType.TRANSMISSION_SERVICE -> Color(0xFF9C27B0) // Purple
        MaintenanceType.COOLANT_SERVICE -> Color(0xFF00BCD4) // Cyan
        MaintenanceType.INSPECTION -> Color(0xFF795548) // Brown
        MaintenanceType.REPAIR -> Color(0xFFFF9800) // Orange
        MaintenanceType.OTHER -> Color(0xFF9E9E9E) // Grey
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