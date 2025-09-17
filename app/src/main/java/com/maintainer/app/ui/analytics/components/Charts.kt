package com.maintainer.app.ui.analytics.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maintainer.app.ui.analytics.MonthlySpending
import com.maintainer.app.ui.analytics.MaintenanceTypeData
import com.maintainer.app.ui.theme.*
import kotlin.math.*

@Composable
fun LineChart(
    data: List<MonthlySpending>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartState(
            modifier = modifier,
            message = "No spending data available"
        )
        return
    }

    val maxAmount = data.maxOfOrNull { it.amount } ?: 1.0
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large)
        ) {
            Text(
                text = "Monthly Spending Trend",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawLineChart(
                    data = data,
                    maxAmount = maxAmount,
                    animationProgress = animationProgress.value,
                    canvasSize = size
                )
            }

            // Legend
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                contentPadding = PaddingValues(vertical = Spacing.small)
            ) {
                items(data.size) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = data[index].month.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.0f", data[index].amount)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    data: List<MaintenanceTypeData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartState(
            modifier = modifier,
            message = "No maintenance data available"
        )
        return
    }

    val totalCost = data.sumOf { it.totalCost }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large)
        ) {
            Text(
                text = "Maintenance Type Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawDonutChart(
                            data = data,
                            totalCost = totalCost,
                            animationProgress = animationProgress.value,
                            canvasSize = size
                        )
                    }

                    // Center text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.0f", totalCost)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Legend
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    data.take(6).forEachIndexed { index, typeData ->
                        val percentage = (typeData.totalCost / totalCost * 100)
                        val color = getChartColor(index)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, CircleShape)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = formatMaintenanceType(typeData.type),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format("%.1f", percentage)}%",
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
}

@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    title: String,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartState(
            modifier = modifier,
            message = "No data available"
        )
        return
    }

    val maxValue = data.maxOfOrNull { it.second } ?: 1.0
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                drawBarChart(
                    data = data,
                    maxValue = maxValue,
                    animationProgress = animationProgress.value,
                    canvasSize = size
                )
            }
        }
    }
}

@Composable
private fun EmptyChartState(
    modifier: Modifier,
    message: String
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(Spacing.large),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun DrawScope.drawLineChart(
    data: List<MonthlySpending>,
    maxAmount: Double,
    animationProgress: Float,
    canvasSize: Size
) {
    if (data.isEmpty()) return

    val strokeWidth = 4.dp.toPx()
    val pointRadius = 6.dp.toPx()

    val padding = 32.dp.toPx()
    val chartWidth = canvasSize.width - (padding * 2)
    val chartHeight = canvasSize.height - (padding * 2)

    val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)
    val scaleY = chartHeight / maxAmount.toFloat()

    // Create gradient for line
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MechanicBlue40,
            MechanicBlue80
        ),
        startY = 0f,
        endY = canvasSize.height
    )

    val path = Path()
    val gradientPath = Path()

    data.forEachIndexed { index, spending ->
        val x = padding + (index * stepX)
        val y = canvasSize.height - padding - (spending.amount.toFloat() * scaleY)

        val animatedX = x * animationProgress
        val animatedY = y + ((canvasSize.height - y) * (1 - animationProgress))

        if (index == 0) {
            path.moveTo(animatedX, animatedY)
            gradientPath.moveTo(animatedX, canvasSize.height)
            gradientPath.lineTo(animatedX, animatedY)
        } else {
            path.lineTo(animatedX, animatedY)
            gradientPath.lineTo(animatedX, animatedY)
        }
    }

    // Close gradient path
    gradientPath.lineTo(padding + ((data.size - 1) * stepX) * animationProgress, canvasSize.height)
    gradientPath.close()

    // Draw gradient fill
    drawPath(
        path = gradientPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                MechanicBlue40.copy(alpha = 0.3f),
                Color.Transparent
            )
        )
    )

    // Draw main line
    drawPath(
        path = path,
        color = MechanicBlue40,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Draw points
    data.forEachIndexed { index, spending ->
        val x = padding + (index * stepX)
        val y = canvasSize.height - padding - (spending.amount.toFloat() * scaleY)

        val animatedX = x * animationProgress
        val animatedY = y + ((canvasSize.height - y) * (1 - animationProgress))

        if (animationProgress > (index.toFloat() / data.size)) {
            drawCircle(
                color = MechanicBlue40,
                radius = pointRadius,
                center = Offset(animatedX, animatedY)
            )
            drawCircle(
                color = Color.White,
                radius = pointRadius * 0.5f,
                center = Offset(animatedX, animatedY)
            )
        }
    }
}

private fun DrawScope.drawDonutChart(
    data: List<MaintenanceTypeData>,
    totalCost: Double,
    animationProgress: Float,
    canvasSize: Size
) {
    val strokeWidth = 28.dp.toPx()
    val radius = (minOf(canvasSize.width, canvasSize.height) / 2) - strokeWidth
    val center = Offset(canvasSize.width / 2, canvasSize.height / 2)

    var startAngle = -90f

    data.forEachIndexed { index, typeData ->
        val percentage = typeData.totalCost / totalCost
        val sweepAngle = (360f * percentage * animationProgress).toFloat()
        val color = getChartColor(index)

        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        startAngle += sweepAngle
    }
}

private fun DrawScope.drawBarChart(
    data: List<Pair<String, Double>>,
    maxValue: Double,
    animationProgress: Float,
    canvasSize: Size
) {
    val barWidth = 24.dp.toPx()
    val spacing = 16.dp.toPx()
    val padding = 32.dp.toPx()

    val chartHeight = canvasSize.height - (padding * 2)
    val totalWidth = (data.size * barWidth) + ((data.size - 1) * spacing)
    val startX = (canvasSize.width - totalWidth) / 2

    data.forEachIndexed { index, (_, value) ->
        val barHeight = (value / maxValue * chartHeight * animationProgress).toFloat()
        val x = startX + (index * (barWidth + spacing))
        val y = canvasSize.height - padding - barHeight

        val gradient = Brush.verticalGradient(
            colors = listOf(
                getChartColor(index),
                getChartColor(index).copy(alpha = 0.7f)
            )
        )

        drawRoundRect(
            brush = gradient,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
        )
    }
}

private fun getChartColor(index: Int): Color {
    val colors = listOf(
        MechanicBlue40,
        MaintenanceGreen,
        WarningAmber40,
        AlertRed,
        SteelGray40,
        MechanicBlue80,
        MaintenanceGreenLight,
        WarningAmber80
    )
    return colors[index % colors.size]
}

private fun formatMaintenanceType(type: com.maintainer.app.data.database.entity.MaintenanceType): String {
    return when (type) {
        com.maintainer.app.data.database.entity.MaintenanceType.OIL_CHANGE -> "Oil Change"
        com.maintainer.app.data.database.entity.MaintenanceType.FILTER_CHANGE -> "Filter Change"
        com.maintainer.app.data.database.entity.MaintenanceType.BRAKE_SERVICE -> "Brake Service"
        com.maintainer.app.data.database.entity.MaintenanceType.TIRE_SERVICE -> "Tire Service"
        com.maintainer.app.data.database.entity.MaintenanceType.BATTERY_SERVICE -> "Battery Service"
        com.maintainer.app.data.database.entity.MaintenanceType.ENGINE_SERVICE -> "Engine Service"
        com.maintainer.app.data.database.entity.MaintenanceType.TRANSMISSION_SERVICE -> "Transmission"
        com.maintainer.app.data.database.entity.MaintenanceType.COOLANT_SERVICE -> "Coolant Service"
        com.maintainer.app.data.database.entity.MaintenanceType.INSPECTION -> "Inspection"
        com.maintainer.app.data.database.entity.MaintenanceType.REPAIR -> "Repair"
        com.maintainer.app.data.database.entity.MaintenanceType.OTHER -> "Other"
    }
}