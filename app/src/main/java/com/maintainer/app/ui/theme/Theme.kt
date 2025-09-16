package com.maintainer.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    // Primary colors - Automotive Blue theme
    primary = MechanicBlue80,
    onPrimary = Color(0xFF001D36),
    primaryContainer = MechanicBlue40,
    onPrimaryContainer = Color(0xFFD0E4FF),

    // Secondary colors - Steel Gray theme
    secondary = SteelGray80,
    onSecondary = Color(0xFF1A1C1E),
    secondaryContainer = SteelGray40,
    onSecondaryContainer = Color(0xFFE0E3E6),

    // Tertiary colors - Warning Amber theme
    tertiary = WarningAmber80,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = WarningAmber40,
    onTertiaryContainer = Color(0xFFFFE1B3),

    // Error colors - Alert Red
    error = AlertRedLight,
    onError = Color(0xFF680003),
    errorContainer = AlertRed,
    onErrorContainer = Color(0xFFFFDAD6),

    // Surface colors - Deep automotive dark theme
    surface = DarkSurface,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC4C7C5),

    // Outline colors
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    // Container colors
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF242424),
    surfaceContainerHighest = Color(0xFF2F2F2F)
)

private val LightColorScheme = lightColorScheme(
    // Primary colors - Automotive Blue theme
    primary = MechanicBlue40,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),

    // Secondary colors - Steel Gray theme
    secondary = SteelGray40,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E3E6),
    onSecondaryContainer = Color(0xFF1A1C1E),

    // Tertiary colors - Warning Amber theme
    tertiary = WarningAmber40,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE1B3),
    onTertiaryContainer = Color(0xFF452B00),

    // Error colors - Alert Red
    error = AlertRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF680003),

    // Surface colors - Clean light theme
    surface = LightSurface,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF43474E),

    // Outline colors
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,

    // Container colors
    surfaceContainer = Color(0xFFF3F4F6),
    surfaceContainerHigh = Color(0xFFEDEFE1),
    surfaceContainerHighest = Color(0xFFE7E9EB)
)

@Composable
fun MaintainerTheme(
    darkTheme: Boolean = true, // Can be overridden by user preference
    dynamicColor: Boolean = false, // Disable dynamic colors to use our automotive theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}