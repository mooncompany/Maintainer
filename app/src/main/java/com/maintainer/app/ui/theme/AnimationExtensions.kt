package com.maintainer.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * Animation extensions for enhanced micro-interactions and visual feedback
 * Provides automotive-inspired animations with proper spring physics and easing
 */

// Enhanced animation specifications for automotive feel
object AutomotiveAnimations {
    // Spring animations with automotive-inspired dampening
    val BounceSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val SoftSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    val CrispSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )

    // Tween animations with custom easing
    val FastOutSlowIn = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )

    val SlowOutFastIn = tween<Float>(
        durationMillis = 250,
        easing = LinearOutSlowInEasing
    )

    val Decelerate = tween<Float>(
        durationMillis = 400,
        easing = FastOutSlowInEasing
    )

    // Custom automotive easing curves
    val EngineRevEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
    val BrakeEasing = CubicBezierEasing(0.55f, 0.06f, 0.68f, 0.19f)
    val AccelerateEasing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
}

// Enhanced bounce animation for press feedback
fun Modifier.bounceClick(
    scale: Float = 0.95f,
    duration: Int = 150
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) scale else 1f,
        animationSpec = AutomotiveAnimations.BounceSpring,
        label = "BounceClick"
    )

    this
        .scale(animatedScale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

// Shimmer effect for loading states with automotive styling
fun Modifier.shimmerEffect(
    isVisible: Boolean = true,
    duration: Int = 1200
): Modifier = composed {
    if (!isVisible) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerProgress"
    )

    this.graphicsLayer {
        alpha = 0.6f + (sin(shimmerProgress * 2 * PI) * 0.4f).toFloat()
    }
}

// Parallax effect for depth perception
fun Modifier.parallaxEffect(
    scrollOffset: Float,
    rate: Float = 0.5f
): Modifier = composed {
    this.offset(y = (scrollOffset * rate).dp)
}

// Elastic scale animation for interactive elements
fun Modifier.elasticScale(
    targetScale: Float,
    isEnabled: Boolean = true
): Modifier = composed {
    val animatedScale by animateFloatAsState(
        targetValue = if (isEnabled) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ElasticScale"
    )

    this.scale(animatedScale)
}

// Pulsing animation for attention-grabbing elements
fun Modifier.pulseAnimation(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    duration: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    this.scale(scale)
}

// Floating animation for cards and elevated elements
fun Modifier.floatingAnimation(
    enabled: Boolean = true,
    distance: Float = 4f,
    duration: Int = 3000
): Modifier = composed {
    if (!enabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "Float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = distance,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatingOffset"
    )

    this.offset(y = offsetY.dp)
}

// Slide in from edge animations
@Composable
fun SlideInFromEdge(
    visible: Boolean,
    edge: Edge = Edge.Bottom,
    duration: Int = 300,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(duration, easing = AutomotiveAnimations.EngineRevEasing),
            initialOffsetY = { height ->
                when (edge) {
                    Edge.Top -> -height
                    Edge.Bottom -> height
                }
            }
        ) + fadeIn(animationSpec = tween(duration)),
        exit = slideOutVertically(
            animationSpec = tween(duration, easing = AutomotiveAnimations.BrakeEasing),
            targetOffsetY = { height ->
                when (edge) {
                    Edge.Top -> -height
                    Edge.Bottom -> height
                }
            }
        ) + fadeOut(animationSpec = tween(duration))
    ) {
        content()
    }
}

enum class Edge { Top, Bottom }

// Expand/collapse animation with smooth transitions
@Composable
fun ExpandableContent(
    expanded: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(
            animationSpec = spring(),
            expandFrom = androidx.compose.ui.Alignment.Top
        ) + fadeIn(animationSpec = tween(300)),
        exit = shrinkVertically(
            animationSpec = spring(),
            shrinkTowards = androidx.compose.ui.Alignment.Top
        ) + fadeOut(animationSpec = tween(200))
    ) {
        content()
    }
}

// Staggered animation for list items
@Composable
fun StaggeredAnimation(
    visible: Boolean,
    index: Int,
    staggerDelay: Int = 50,
    content: @Composable () -> Unit
) {
    var localVisible by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            delay((index * staggerDelay).toLong())
            localVisible = true
        } else {
            localVisible = false
        }
    }

    AnimatedVisibility(
        visible = localVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it / 2 },
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        content()
    }
}

// Rotation animation for loading indicators
fun Modifier.rotatingLoad(
    isLoading: Boolean,
    duration: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "Rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    if (isLoading) {
        this.graphicsLayer { rotationZ = rotation }
    } else {
        this
    }
}

// Progress animation with automotive-inspired easing
@Composable
fun animateProgressAsState(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = tween(300)
): State<Float> {
    return animateFloatAsState(
        targetValue = targetValue.coerceIn(0f, 1f),
        animationSpec = animationSpec,
        label = "ProgressAnimation"
    )
}

// Custom easing functions for automotive feel
object CustomEasing {
    val SpeedRampUp = Easing { fraction ->
        // Simulates engine acceleration curve
        1f - (1f - fraction).pow(3f)
    }

    val BrakeDeceleration = Easing { fraction ->
        // Simulates brake application curve
        fraction.pow(2f)
    }

    val SuspensionBounce = Easing { fraction ->
        // Simulates suspension dampening
        if (fraction < 0.5f) {
            2 * fraction * fraction
        } else {
            1 - 2 * (1 - fraction).pow(2f)
        }
    }

    val ElectricMotor = Easing { fraction ->
        // Smooth, linear acceleration like electric vehicles
        fraction
    }
}

// Combine multiple animations for complex effects
@Composable
fun ComplexAnimation(
    trigger: Boolean,
    scale: Float = 1.1f,
    rotation: Float = 5f,
    alpha: Float = 0.8f,
    duration: Int = 300,
    content: @Composable () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (trigger) scale else 1f,
        animationSpec = AutomotiveAnimations.BounceSpring,
        label = "ComplexScale"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = if (trigger) rotation else 0f,
        animationSpec = AutomotiveAnimations.SoftSpring,
        label = "ComplexRotation"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (trigger) alpha else 1f,
        animationSpec = tween(duration),
        label = "ComplexAlpha"
    )

    Box(
        modifier = Modifier
            .scale(animatedScale)
            .graphicsLayer {
                rotationZ = animatedRotation
                this.alpha = animatedAlpha
            }
    ) {
        content()
    }
}