package com.maintainer.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maintainer.app.ui.theme.*

/**
 * Modern button components with enhanced visual design and micro-interactions
 * Features automotive-inspired styling, haptic feedback, and smooth animations
 */

@Composable
fun ModernButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Leading,
    isLoading: Boolean = false
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = AutomotiveAnimations.BounceSpring,
        label = "ButtonScale"
    )

    val (containerColor, contentColor, disabledContainerColor, disabledContentColor) = when (variant) {
        ButtonVariant.Primary -> listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        ButtonVariant.Secondary -> listOf(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        ButtonVariant.Outline -> listOf(
            Color.Transparent,
            MaterialTheme.colorScheme.primary,
            Color.Transparent,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        ButtonVariant.Ghost -> listOf(
            Color.Transparent,
            MaterialTheme.colorScheme.primary,
            Color.Transparent,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        ButtonVariant.Destructive -> listOf(
            AlertRed,
            Color.White,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        ButtonVariant.Success -> listOf(
            MaintenanceGreen,
            Color.White,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = if (enabled) containerColor else disabledContainerColor,
        animationSpec = tween(300),
        label = "ButtonContainerColor"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (enabled) contentColor else disabledContentColor,
        animationSpec = tween(300),
        label = "ButtonContentColor"
    )

    val paddingValues = when (size) {
        ButtonSize.Small -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ButtonSize.Medium -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ButtonSize.Large -> PaddingValues(horizontal = 32.dp, vertical = 16.dp)
    }

    val buttonModifier = modifier
        .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
        .let { mod ->
            when (variant) {
                ButtonVariant.Outline -> mod.border(
                    width = 1.dp,
                    color = animatedContentColor,
                    shape = RoundedCornerShape(CornerRadius.button)
                )
                else -> mod
            }
        }
        .clip(RoundedCornerShape(CornerRadius.button))
        .background(animatedContainerColor)
        .clickable(
            interactionSource = interactionSource,
            indication = rememberRipple(color = animatedContentColor),
            enabled = enabled
        ) {
            if (enabled) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
        }
        .padding(paddingValues)

    Row(
        modifier = buttonModifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = animatedContentColor,
                strokeWidth = 2.dp
            )
            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        } else if (icon != null && iconPosition == IconPosition.Leading) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = animatedContentColor,
                modifier = Modifier.size(18.dp)
            )
            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        if (text.isNotEmpty()) {
            Text(
                text = text,
                color = animatedContentColor,
                style = when (size) {
                    ButtonSize.Small -> MaterialTheme.typography.labelMedium
                    ButtonSize.Medium -> MaterialTheme.typography.labelLarge
                    ButtonSize.Large -> MaterialTheme.typography.titleMedium
                },
                fontWeight = FontWeight.SemiBold
            )
        }

        if (icon != null && iconPosition == IconPosition.Trailing && !isLoading) {
            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = animatedContentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    enabled: Boolean = true,
    variant: FABVariant = FABVariant.Primary,
    size: FABSize = FABSize.Regular
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.9f else 1f,
        animationSpec = AutomotiveAnimations.BounceSpring,
        label = "FABScale"
    )

    val animatedElevation by animateFloatAsState(
        targetValue = if (isPressed) 6f else 12f,
        animationSpec = spring(),
        label = "FABElevation"
    )

    val (containerColor, contentColor) = when (variant) {
        FABVariant.Primary -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        FABVariant.Secondary -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        FABVariant.Surface -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurface
    }

    val fabSize = when (size) {
        FABSize.Small -> 40.dp
        FABSize.Regular -> 56.dp
        FABSize.Large -> 72.dp
    }

    val iconSize = when (size) {
        FABSize.Small -> 18.dp
        FABSize.Regular -> 24.dp
        FABSize.Large -> 32.dp
    }

    Box(
        modifier = modifier
            .size(fabSize)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .shadow(
                elevation = animatedElevation.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            )
            .background(containerColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = contentColor, bounded = false),
                enabled = enabled
            ) {
                if (enabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(MechanicBlue40, MechanicBlue80)
    ),
    contentColor: Color = Color.White,
    icon: ImageVector? = null
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = AutomotiveAnimations.BounceSpring,
        label = "GradientButtonScale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.6f,
        animationSpec = tween(300),
        label = "GradientButtonAlpha"
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
            }
            .clip(RoundedCornerShape(CornerRadius.button))
            .background(gradient)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = contentColor),
                enabled = enabled
            ) {
                if (enabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ToggleButton(
    isToggled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    toggledText: String = "ON",
    untoggledText: String = "OFF",
    enabled: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isToggled) {
            MaintenanceGreen
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = spring(),
        label = "ToggleContainerColor"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (isToggled) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(300),
        label = "ToggleContentColor"
    )

    val displayText = if (isToggled) toggledText else untoggledText

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadius.button))
            .background(animatedContainerColor)
            .clickable(enabled = enabled) {
                if (enabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle(!isToggled)
                }
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayText,
            color = animatedContentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

enum class ButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Destructive,
    Success
}

enum class ButtonSize {
    Small,
    Medium,
    Large
}

enum class IconPosition {
    Leading,
    Trailing
}

enum class FABVariant {
    Primary,
    Secondary,
    Surface
}

enum class FABSize {
    Small,
    Regular,
    Large
}