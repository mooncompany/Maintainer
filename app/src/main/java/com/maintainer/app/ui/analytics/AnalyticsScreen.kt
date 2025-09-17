package com.maintainer.app.ui.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maintainer.app.ui.analytics.components.BarChart
import com.maintainer.app.ui.analytics.components.DonutChart
import com.maintainer.app.ui.analytics.components.LineChart
import com.maintainer.app.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val analyticsData by viewModel.analyticsData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    if (isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshData()
            isRefreshing = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (isLoading) {
                LoadingState()
            } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        // Overview Cards
                        item {
                            Text(
                                text = "Overview",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = Spacing.small)
                            )
                        }

                        item {
                            OverviewCards(analyticsData)
                        }

                        // Monthly Spending Chart
                        if (analyticsData.monthlySpending.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Spending Trends",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = Spacing.small)
                                )
                            }

                            item {
                                LineChart(
                                    data = analyticsData.monthlySpending,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Maintenance Type Breakdown
                        if (analyticsData.maintenanceTypeBreakdown.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Maintenance Breakdown",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = Spacing.small)
                                )
                            }

                            item {
                                DonutChart(
                                    data = analyticsData.maintenanceTypeBreakdown,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Vehicle Cost Analysis
                        if (analyticsData.vehicleCostBreakdown.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Vehicle Cost Analysis",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = Spacing.small)
                                )
                            }

                            item {
                                val vehicleData = analyticsData.vehicleCostBreakdown.map { vehicle ->
                                    "${vehicle.vehicle.make} ${vehicle.vehicle.model}" to vehicle.totalCost
                                }
                                BarChart(
                                    data = vehicleData,
                                    title = "Total Cost by Vehicle",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Insights Section
                        item {
                            Text(
                                text = "Insights",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = Spacing.small)
                            )
                        }

                        item {
                            InsightsSection(analyticsData)
                        }

                        // Add some bottom padding
                        item {
                            Spacer(modifier = Modifier.height(Spacing.large))
                        }
                    }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading analytics...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OverviewCards(data: AnalyticsData) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        contentPadding = PaddingValues(horizontal = Spacing.tiny)
    ) {
        item {
            OverviewCard(
                title = "Total Spent",
                value = formatCurrency(data.totalMaintenanceCost),
                icon = Icons.Default.AttachMoney,
                color = MechanicBlue40,
                trend = data.recentTrends.monthOverMonthChange,
                isIncreasing = data.recentTrends.isSpendingIncreasing
            )
        }

        item {
            OverviewCard(
                title = "Vehicles",
                value = data.totalVehicles.toString(),
                icon = Icons.Default.DirectionsCar,
                color = MaintenanceGreen
            )
        }

        item {
            OverviewCard(
                title = "Avg/Vehicle",
                value = formatCurrency(data.averageCostPerVehicle),
                icon = Icons.Default.BarChart,
                color = WarningAmber40
            )
        }

        item {
            OverviewCard(
                title = "Cost/Mile",
                value = formatCurrency(data.costPerMile, showCents = true),
                icon = Icons.Default.Speed,
                color = SteelGray40
            )
        }
    }
}

@Composable
private fun OverviewCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    trend: Double? = null,
    isIncreasing: Boolean = false
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (trend != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)
                    ) {
                        Icon(
                            imageVector = if (isIncreasing) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isIncreasing) AlertRed else MaintenanceGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", kotlin.math.abs(trend))}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isIncreasing) AlertRed else MaintenanceGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsSection(data: AnalyticsData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Most expensive maintenance type
        data.mostExpensiveMaintenanceType?.let { type ->
            InsightCard(
                title = "Most Expensive Service",
                description = "Oil changes are your highest maintenance cost",
                icon = Icons.Default.TrendingUp,
                color = AlertRed
            )
        }

        // Monthly trend insight
        if (data.recentTrends.isSpendingIncreasing) {
            InsightCard(
                title = "Spending Trend",
                description = "Your maintenance spending has increased by ${String.format("%.1f", data.recentTrends.monthOverMonthChange)}% this month",
                icon = Icons.Default.Warning,
                color = WarningAmber40
            )
        } else {
            InsightCard(
                title = "Great Progress!",
                description = "Your maintenance spending has decreased by ${String.format("%.1f", kotlin.math.abs(data.recentTrends.monthOverMonthChange))}% this month",
                icon = Icons.Default.CheckCircle,
                color = MaintenanceGreen
            )
        }

        // Vehicle with highest cost per mile
        if (data.vehicleCostBreakdown.isNotEmpty()) {
            val highestCostVehicle = data.vehicleCostBreakdown.maxByOrNull { it.costPerMile }
            highestCostVehicle?.let { vehicle ->
                InsightCard(
                    title = "Monitor This Vehicle",
                    description = "${vehicle.vehicle.make} ${vehicle.vehicle.model} has the highest cost per mile at ${formatCurrency(vehicle.costPerMile, showCents = true)}",
                    icon = Icons.Default.DirectionsCar,
                    color = MechanicBlue40
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double, showCents: Boolean = false): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return if (showCents) {
        formatter.format(amount)
    } else {
        formatter.format(amount).replace(Regex("\\.\\d+"), "")
    }
}