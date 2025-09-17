package com.maintainer.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maintainer.app.ui.theme.*
import kotlinx.coroutines.delay
import java.util.*
import kotlin.math.*

/**
 * Micro-interaction components for enhanced user experience
 * Features automotive-inspired feedback, haptics, and delightful animations
 */

@Composable
fun AnimatedLikeButton(
    isLiked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Float = 24f,
    animationDuration: Int = 300
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.8f
            isLiked -> 1.2f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "LikeButtonScale"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (isLiked) AlertRed else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(animationDuration),
        label = "LikeButtonColor"
    )

    Icon(
        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
        contentDescription = if (isLiked) "Unlike" else "Like",
        tint = animatedColor,
        modifier = modifier
            .size(size.dp)
            .scale(animatedScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle(!isLiked)
            }
    )
}

@Composable
fun SwipeToRefreshIndicator(
    isRefreshing: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val rotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else progress * 180f,
        animationSpec = if (isRefreshing) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        },
        label = "RefreshRotation"
    )

    val scale by animateFloatAsState(
        targetValue = if (progress > 0.8f || isRefreshing) 1f else progress,
        animationSpec = AutomotiveAnimations.BounceSpring,
        label = "RefreshScale"
    )

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        } else {
            Canvas(modifier = Modifier.size(24.dp)) {
                val strokeWidth = 3.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2

                drawArc(
                    color = MechanicBlue40,
                    startAngle = -90f,
                    sweepAngle = progress * 270f,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

@Composable
fun PulsatingDot(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Float = 8f,
    maxScale: Float = 1.5f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulsatingDot")

    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) maxScale else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotScale"
    )

    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotAlpha"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .scale(animatedScale)
            .background(
                color = color.copy(alpha = animatedAlpha),
                shape = CircleShape
            )
    )
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 4f,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    showPercentage: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = AutomotiveAnimations.EngineRevEasing),
        label = "RingProgress"
    )

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.dp.toPx()
            val radius = (size.minDimension - strokeWidthPx) / 2

            // Background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                style = Stroke(width = strokeWidthPx)
            )

            // Progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            )
        }

        if (showPercentage) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WaveLoader(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    waveCount: Int = 3
) {
    if (!isLoading) return

    val infiniteTransition = rememberInfiniteTransition(label = "WaveLoader")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(waveCount) { index ->
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = 16f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "WaveHeight$index"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun ElasticButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ElasticScale"
    )

    Box(
        modifier = modifier
            .scale(animatedScale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false }
                ) { _, _ -> }
            }
    ) {
        content()
    }
}

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    shape: SkeletonShape = SkeletonShape.Rectangle,
    isLoading: Boolean = true
) {
    if (!isLoading) return

    val infiniteTransition = rememberInfiniteTransition(label = "Skeleton")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerProgress"
    )

    val shimmerBrush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        startX = shimmerProgress * 300f - 150f,
        endX = shimmerProgress * 300f + 150f
    )

    val shapeModifier = when (shape) {
        SkeletonShape.Rectangle -> Modifier.clip(RoundedCornerShape(4.dp))
        SkeletonShape.Circle -> Modifier.clip(CircleShape)
        SkeletonShape.RoundedRectangle -> Modifier.clip(RoundedCornerShape(8.dp))
    }

    Box(
        modifier = modifier
            .then(shapeModifier)
            .background(shimmerBrush)
    )
}

@Composable
fun CounterAnimation(
    targetCount: Int,
    modifier: Modifier = Modifier,
    duration: Int = 1000,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    var currentCount by remember { mutableStateOf(0) }

    LaunchedEffect(targetCount) {
        val step = if (targetCount > currentCount) 1 else -1
        val delay = duration / abs(targetCount - currentCount).coerceAtLeast(1)

        while (currentCount != targetCount) {
            delay(delay.toLong())
            currentCount += step
        }
    }

    Text(
        text = currentCount.toString(),
        style = textStyle,
        modifier = modifier
    )
}

@Composable
fun StatusIndicator(
    status: StatusType,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    size: Float = 12f
) {
    val color = when (status) {
        StatusType.Success -> MaintenanceGreen
        StatusType.Warning -> WarningAmber40
        StatusType.Error -> AlertRed
        StatusType.Info -> MechanicBlue40
        StatusType.Neutral -> MaterialTheme.colorScheme.surfaceVariant
    }

    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = if (animated) spring() else snap(),
        label = "StatusColor"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .background(animatedColor, CircleShape)
            .then(
                if (animated && status == StatusType.Warning) {
                    Modifier.pulseAnimation()
                } else Modifier
            )
    )
}

@Composable
fun FloatingParticles(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 20,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
) {
    if (!isActive) return

    val particles = remember {
        (0 until particleCount).map {
            Particle(
                x = Random().nextFloat(),
                y = Random().nextFloat(),
                velocity = Random().nextFloat() * 0.02f + 0.01f,
                size = Random().nextFloat() * 4f + 2f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "FloatingParticles")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ParticleMovement"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val currentY = (particle.y - animationProgress * particle.velocity) % 1f
            val currentX = particle.x + sin(animationProgress * 2 * PI + particle.x * 10) * 0.1f

            drawCircle(
                color = color,
                radius = particle.size,
                center = Offset(
                    x = currentX.toFloat() * size.width,
                    y = currentY * size.height
                )
            )
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val velocity: Float,
    val size: Float
)

enum class SkeletonShape {
    Rectangle,
    Circle,
    RoundedRectangle
}