package com.maintainer.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Dark-optimized spacing system for Maintainer app
 * Based on 8dp grid system with automotive-focused spacing
 */
object Spacing {
    // Base unit - 8dp grid system
    val baseline = 8.dp

    // Micro spacing - For fine adjustments
    val micro = 2.dp
    val tiny = 4.dp

    // Small spacing - For tight layouts
    val small = 8.dp
    val smallMedium = 12.dp

    // Medium spacing - Standard component spacing
    val medium = 16.dp
    val mediumLarge = 20.dp

    // Large spacing - Section separation
    val large = 24.dp
    val largeMedium = 28.dp

    // Extra large spacing - Page level separation
    val extraLarge = 32.dp
    val huge = 40.dp
    val massive = 48.dp

    // Card and container spacing
    val cardPadding = medium
    val cardMargin = medium
    val cardRadius = 12.dp
    val cardElevation = 4.dp

    // Screen margins - Optimized for dark mode visibility
    val screenHorizontal = large
    val screenVertical = medium
    val screenTop = large
    val screenBottom = medium

    // List item spacing
    val listItemPadding = medium
    val listItemSpacing = small
    val listItemRadius = 8.dp

    // Button spacing
    val buttonPadding = medium
    val buttonSpacing = medium
    val buttonRadius = 8.dp

    // Form spacing - Optimized for maintenance data entry
    val formFieldSpacing = medium
    val formSectionSpacing = large
    val formPadding = medium

    // Touch targets - Minimum 44dp for accessibility
    val minTouchTarget = 44.dp
    val recommendedTouchTarget = 48.dp

    // Status indicators and badges
    val statusIndicatorSize = 8.dp
    val badgePadding = small
    val badgeRadius = 16.dp

    // Camera and scanning interface spacing
    val cameraOverlayPadding = large
    val scanningIndicatorSize = 56.dp
    val scanningGuideMargin = extraLarge
}

/**
 * Elevation system for dark mode depth perception
 * Dark themes need higher elevation values for proper depth perception
 */
object Elevation {
    val none = 0.dp
    val small = 2.dp
    val medium = 4.dp
    val large = 8.dp
    val extraLarge = 12.dp
    val maximum = 16.dp

    // Specific component elevations
    val card = medium
    val dialog = extraLarge
    val fab = large
    val bottomSheet = maximum
    val appBar = small
    val navigationBar = small
}

/**
 * Border radius system for consistent rounded corners
 */
object CornerRadius {
    val none = 0.dp
    val small = 4.dp
    val medium = 8.dp
    val large = 12.dp
    val extraLarge = 16.dp
    val circle = 50.dp

    // Component-specific radii
    val card = large
    val button = medium
    val textField = medium
    val chip = circle
    val dialog = extraLarge
}