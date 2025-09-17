package com.maintainer.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

/**
 * Extended color utilities for enhanced visual design language
 * Provides smooth color transitions, elevation-based variants, and automotive-inspired palettes
 */

// Enhanced Color Variants for Depth and Interaction
@Composable
fun Color.animatedVariant(
    isActive: Boolean,
    activeColor: Color = this,
    inactiveColor: Color = this.copy(alpha = 0.6f)
): Color {
    val targetColor = if (isActive) activeColor else inactiveColor
    return animateColorAsState(
        targetValue = targetColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ColorVariant"
    ).value
}

// Color with elevation-based brightness adjustment
fun Color.withElevation(elevation: Float): Color {
    val factor = (elevation / 16f).coerceIn(0f, 1f)
    return Color(
        red = (red + (1f - red) * factor * 0.1f).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor * 0.1f).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor * 0.1f).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// Color with glassmorphism effect
fun Color.withGlassmorphism(opacity: Float = 0.1f): Color {
    return this.copy(alpha = opacity)
}

// Color with subtle shimmer effect (for loading states)
fun Color.withShimmer(progress: Float): Color {
    val shimmerAmount = (sin(progress * 2 * Math.PI) * 0.1f + 0.1f).toFloat()
    return Color(
        red = (red + shimmerAmount).coerceIn(0f, 1f),
        green = (green + shimmerAmount).coerceIn(0f, 1f),
        blue = (blue + shimmerAmount).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// Automotive-inspired color palettes
object AutomotiveColors {
    // Electric Vehicle Colors
    val ElectricBlue = Color(0xFF00E5FF)
    val ElectricGreen = Color(0xFF00E676)
    val ElectricPurple = Color(0xFF7C4DFF)

    // Performance Colors
    val SpeedRed = Color(0xFFFF1744)
    val TurboOrange = Color(0xFFFF6D00)
    val RacingYellow = Color(0xFFFFD600)

    // Luxury Colors
    val PlatinumSilver = Color(0xFFE8EAF0)
    val ChampagneGold = Color(0xFFD4AF37)
    val DeepBlack = Color(0xFF0D1117)

    // Status Indicators
    val EngineWarning = Color(0xFFFF5722)
    val BatteryLow = Color(0xFFFF9800)
    val FuelLow = Color(0xFFFFB74D)
    val ServiceDue = Color(0xFF03DAC6)

    // Material finish effects
    val ChromeReflection = listOf(
        Color(0xFFE8EAF0),
        Color(0xFFB0BEC5),
        Color(0xFFE8EAF0)
    )

    val BrushedMetal = listOf(
        Color(0xFF90A4AE),
        Color(0xFF607D8B),
        Color(0xFF455A64)
    )

    val CarbonFiber = listOf(
        Color(0xFF2E2E2E),
        Color(0xFF1A1A1A),
        Color(0xFF000000)
    )
}

// Smart color selection based on maintenance type
fun getMaintenanceTypeColor(type: com.maintainer.app.data.database.entity.MaintenanceType): Color {
    return when (type) {
        com.maintainer.app.data.database.entity.MaintenanceType.OIL_CHANGE -> Color(0xFF4CAF50) // Green - routine
        com.maintainer.app.data.database.entity.MaintenanceType.BRAKE_SERVICE -> Color(0xFFE53935) // Red - safety critical
        com.maintainer.app.data.database.entity.MaintenanceType.TIRE_SERVICE -> Color(0xFF1976D2) // Blue - performance
        com.maintainer.app.data.database.entity.MaintenanceType.ENGINE_SERVICE -> Color(0xFFFF5722) // Deep orange - important
        com.maintainer.app.data.database.entity.MaintenanceType.TRANSMISSION_SERVICE -> Color(0xFF9C27B0) // Purple - complex
        com.maintainer.app.data.database.entity.MaintenanceType.BATTERY_SERVICE -> Color(0xFFFFEB3B) // Yellow - electrical
        com.maintainer.app.data.database.entity.MaintenanceType.COOLANT_SERVICE -> Color(0xFF00E5FF) // Cyan - cooling
        com.maintainer.app.data.database.entity.MaintenanceType.FILTER_CHANGE -> Color(0xFF81C784) // Light green - routine
        com.maintainer.app.data.database.entity.MaintenanceType.INSPECTION -> Color(0xFF607D8B) // Blue gray - standard
        com.maintainer.app.data.database.entity.MaintenanceType.REPAIR -> Color(0xFFFF9800) // Orange - attention needed
        com.maintainer.app.data.database.entity.MaintenanceType.OTHER -> Color(0xFF9E9E9E) // Gray - neutral
    }
}

// Vehicle health score color mapping
fun getHealthScoreColor(score: Float): Color {
    return when {
        score >= 0.9f -> AutomotiveColors.ElectricGreen
        score >= 0.8f -> MaintenanceGreen
        score >= 0.7f -> Color(0xFF8BC34A) // Light green
        score >= 0.6f -> Color(0xFFCDDC39) // Lime
        score >= 0.5f -> Color(0xFFFFEB3B) // Yellow
        score >= 0.4f -> Color(0xFFFF9800) // Orange
        score >= 0.3f -> Color(0xFFFF5722) // Deep orange
        score >= 0.2f -> Color(0xFFE53935) // Red
        else -> Color(0xFFD32F2F) // Dark red
    }
}

// Dynamic color interpolation for progress indicators
fun interpolateMaintenanceColor(progress: Float): Color {
    return when {
        progress < 0.5f -> lerp(MaintenanceGreen, WarningAmber40, progress * 2f)
        else -> lerp(WarningAmber40, AlertRed, (progress - 0.5f) * 2f)
    }
}

// Accessibility-friendly color pairs
object AccessibleColors {
    val HighContrast = listOf(
        Color(0xFF000000) to Color(0xFFFFFFFF),
        Color(0xFF1976D2) to Color(0xFFFFFFFF),
        Color(0xFF388E3C) to Color(0xFFFFFFFF),
        Color(0xFFD32F2F) to Color(0xFFFFFFFF)
    )

    val MediumContrast = listOf(
        Color(0xFF424242) to Color(0xFFFFFFFF),
        Color(0xFF1565C0) to Color(0xFFE3F2FD),
        Color(0xFF2E7D32) to Color(0xFFE8F5E8),
        Color(0xFFC62828) to Color(0xFFFFEBEE)
    )
}

// Color contrast utilities
fun Color.contrastRatio(other: Color): Float {
    val l1 = this.luminance() + 0.05f
    val l2 = other.luminance() + 0.05f
    return if (l1 > l2) l1 / l2 else l2 / l1
}

fun Color.luminance(): Float {
    fun componentToLinear(c: Float): Float {
        return if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    }

    val r = componentToLinear(red)
    val g = componentToLinear(green)
    val b = componentToLinear(blue)

    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

// Color with automatic contrast adjustment
fun Color.withAutoContrast(background: Color, minimumRatio: Float = 4.5f): Color {
    val currentRatio = this.contrastRatio(background)

    if (currentRatio >= minimumRatio) return this

    // Adjust brightness to meet contrast requirements
    val factor = if (background.luminance() > 0.5f) {
        // Light background - make color darker
        -0.1f
    } else {
        // Dark background - make color lighter
        0.1f
    }

    return Color(
        red = (red + factor).coerceIn(0f, 1f),
        green = (green + factor).coerceIn(0f, 1f),
        blue = (blue + factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}