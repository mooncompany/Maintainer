package com.maintainer.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maintainer.app.ui.theme.*

/**
 * Home screen widgets for quick actions and information display
 * Features automotive-inspired design with smooth animations and micro-interactions
 */

@Composable
fun QuickStatsWidget(
    totalVehicles: Int,
    upcomingMaintenance: Int,
    totalSpent: Double,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTap() },
        variant = CardVariant.Elevated
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Fleet Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    value = totalVehicles.toString(),
                    label = "Vehicles",
                    icon = Icons.Default.DirectionsCar,
                    color = MechanicBlue40
                )

                StatItem(
                    value = upcomingMaintenance.toString(),
                    label = "Due Soon",
                    icon = Icons.Default.Schedule,
                    color = if (upcomingMaintenance > 0) WarningAmber40 else MaintenanceGreen
                )

                StatItem(
                    value = "$${String.format("%.0f", totalSpent)}",
                    label = "Total Spent",
                    icon = Icons.Default.AttachMoney,
                    color = SteelGray40
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
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

        Spacer(modifier = Modifier.height(Spacing.small))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActionsWidget(
    onAddVehicle: () -> Unit,
    onLogMaintenance: () -> Unit,
    onViewAnalytics: () -> Unit,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickAction(
            icon = Icons.Default.Add,
            label = "Add Vehicle",
            color = MaintenanceGreen,
            onClick = onAddVehicle
        ),
        QuickAction(
            icon = Icons.Default.Build,
            label = "Log Service",
            color = MechanicBlue40,
            onClick = onLogMaintenance
        ),
        QuickAction(
            icon = Icons.Default.Analytics,
            label = "Analytics",
            color = WarningAmber40,
            onClick = onViewAnalytics
        ),
        QuickAction(
            icon = Icons.Default.CameraAlt,
            label = "Photo",
            color = SteelGray40,
            onClick = onTakePhoto
        )
    )

    ModernCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Glass
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                items(actions) { action ->
                    QuickActionButton(
                        action = action,
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = AutomotiveAnimations.BounceSpring,
        label = "QuickActionScale"
    )

    val animatedBackgroundAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.2f else 0.1f,
        animationSpec = AutomotiveAnimations.FastOutSlowIn,
        label = "QuickActionBackground"
    )

    Column(
        modifier = modifier
            .scale(animatedScale)
            .clip(RoundedCornerShape(CornerRadius.medium))
            .background(action.color.copy(alpha = animatedBackgroundAlpha))
            .clickable {
                isPressed = true
                action.onClick()
            }
            .padding(Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.label,
            tint = action.color,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        Text(
            text = action.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun MaintenanceStatusWidget(
    overdueMaintenance: Int,
    dueSoonMaintenance: Int,
    recentlyCompleted: Int,
    modifier: Modifier = Modifier,
    onViewDetails: () -> Unit = {}
) {
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        variant = CardVariant.Default
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Maintenance Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                MaintenanceStatusRow(
                    count = overdueMaintenance,
                    label = "Overdue",
                    color = AlertRed,
                    icon = Icons.Default.Warning
                )

                MaintenanceStatusRow(
                    count = dueSoonMaintenance,
                    label = "Due Soon",
                    color = WarningAmber40,
                    icon = Icons.Default.Schedule
                )

                MaintenanceStatusRow(
                    count = recentlyCompleted,
                    label = "Recently Completed",
                    color = MaintenanceGreen,
                    icon = Icons.Default.CheckCircle
                )
            }
        }
    }
}

@Composable
private fun MaintenanceStatusRow(
    count: Int,
    label: String,
    color: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun WeatherWidget(
    temperature: String = "72°F",
    condition: String = "Perfect driving weather",
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Gradient
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = temperature,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = condition,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.WbSunny,
                contentDescription = "Weather",
                tint = WarningAmber40,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun TipOfTheDayWidget(
    tip: String = "Regular oil changes extend your engine's life significantly",
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.Glass
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = "Tip",
                tint = WarningAmber40,
                modifier = Modifier.size(24.dp)
            )

            Column {
                Text(
                    text = "Tip of the Day",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)