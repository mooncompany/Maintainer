package com.maintainer.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.maintainer.app.ui.theme.*

/**
 * Modern card components with enhanced visual design language
 * Features glassmorphism, elevation effects, and automotive-inspired styling
 */

@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: CardVariant = CardVariant.Default,
    elevation: Float = 4f,
    enableInteractions: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val animatedElevation by animateFloatAsState(
        targetValue = when {
            isPressed -> elevation * 0.5f
            isHovered -> elevation * 1.5f
            else -> elevation
        },
        animationSpec = AutomotiveAnimations.BounceSpring,
        label = "CardElevation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = when {
            isPressed && enableInteractions -> 0.98f
            isHovered && enableInteractions -> 1.02f
            else -> 1f
        },
        animationSpec = spring(),
        label = "CardScale"
    )

    val containerColor by animateColorAsState(
        targetValue = when (variant) {
            CardVariant.Default -> MaterialTheme.colorScheme.surfaceContainer
            CardVariant.Elevated -> MaterialTheme.colorScheme.surfaceContainerHigh
            CardVariant.Glass -> MaterialTheme.colorScheme.surfaceContainer.withGlassmorphism(0.7f)
            CardVariant.Gradient -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "CardColor"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .shadow(
                elevation = animatedElevation.dp,
                shape = RoundedCornerShape(CornerRadius.large),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else Modifier
            ),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // We handle elevation manually for better control
        )
    ) {
        if (variant == CardVariant.Gradient) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    content = content
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    blurRadius: Float = 16f,
    borderAlpha: Float = 0.2f,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedBorderAlpha by animateFloatAsState(
        targetValue = if (isPressed) borderAlpha * 1.5f else borderAlpha,
        animationSpec = tween(300),
        label = "BorderAlpha"
    )

    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = animatedBorderAlpha),
                shape = RoundedCornerShape(CornerRadius.large)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else Modifier
            ),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                ),
            content = content
        )
    }
}

@Composable
fun AutomotiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: AutomotiveVariant = AutomotiveVariant.Chrome,
    content: @Composable ColumnScope.() -> Unit
) {
    val gradient = when (variant) {
        AutomotiveVariant.Chrome -> Brush.linearGradient(AutomotiveColors.ChromeReflection)
        AutomotiveVariant.BrushedMetal -> Brush.linearGradient(AutomotiveColors.BrushedMetal)
        AutomotiveVariant.CarbonFiber -> Brush.linearGradient(AutomotiveColors.CarbonFiber)
    }

    val containerColor = when (variant) {
        AutomotiveVariant.Chrome -> MaterialTheme.colorScheme.surfaceContainerHighest
        AutomotiveVariant.BrushedMetal -> MaterialTheme.colorScheme.surfaceContainer
        AutomotiveVariant.CarbonFiber -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadius.large))
            .background(gradient)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = containerColor.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.large)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    status: StatusType,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val statusColor = when (status) {
        StatusType.Success -> MaintenanceGreen
        StatusType.Warning -> WarningAmber40
        StatusType.Error -> AlertRed
        StatusType.Info -> MechanicBlue40
        StatusType.Neutral -> MaterialTheme.colorScheme.surfaceVariant
    }

    val containerColor by animateColorAsState(
        targetValue = statusColor.copy(alpha = 0.1f),
        animationSpec = spring(),
        label = "StatusColor"
    )

    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = statusColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(CornerRadius.large)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.medium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(containerColor),
            content = content
        )
    }
}

@Composable
fun InteractiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    rippleColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedRippleAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.12f else 0f,
        animationSpec = tween(300),
        label = "RippleAlpha"
    )

    Card(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick?.invoke() },
                onClickLabel = "Card Action"
            ),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(rippleColor.copy(alpha = animatedRippleAlpha)),
            content = content
        )
    }
}

enum class CardVariant {
    Default,
    Elevated,
    Glass,
    Gradient
}

enum class AutomotiveVariant {
    Chrome,
    BrushedMetal,
    CarbonFiber
}

enum class StatusType {
    Success,
    Warning,
    Error,
    Info,
    Neutral
}