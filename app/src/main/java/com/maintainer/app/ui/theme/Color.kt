package com.maintainer.app.ui.theme

import androidx.compose.ui.graphics.Color

// Dark Mode Primary Colors (Automotive Blue Theme)
val MechanicBlue80 = Color(0xFF90CAF9)      // Light blue for dark backgrounds
val SteelGray80 = Color(0xFFB0BEC5)         // Light steel gray
val WarningAmber80 = Color(0xFFFFCC80)      // Light amber for warnings

// Light Mode Primary Colors (Darker variants)
val MechanicBlue40 = Color(0xFF1976D2)      // Deep mechanic blue
val SteelGray40 = Color(0xFF455A64)         // Dark steel gray
val WarningAmber40 = Color(0xFFE65100)      // Deep amber

// Automotive Accent Colors
val MaintenanceGreen = Color(0xFF4CAF50)    // Success/completed maintenance
val MaintenanceGreenLight = Color(0xFF81C784)
val AlertRed = Color(0xFFE53935)            // Urgent maintenance
val AlertRedLight = Color(0xFFEF5350)

// Dark Mode Surface Colors
val DarkSurface = Color(0xFF121212)         // Primary dark surface
val DarkSurfaceVariant = Color(0xFF1E1E1E)  // Elevated cards
val DarkOutline = Color(0xFF373737)         // Subtle borders
val DarkOutlineVariant = Color(0xFF2A2A2A)  // Very subtle dividers

// Light Mode Surface Colors
val LightSurface = Color(0xFFFEFEFE)        // Primary light surface
val LightSurfaceVariant = Color(0xFFF5F5F5) // Elevated cards
val LightOutline = Color(0xFFE0E0E0)        // Subtle borders
val LightOutlineVariant = Color(0xFFF0F0F0) // Very subtle dividers

// Semantic Colors for Maintenance Status
val MaintenanceDue = Color(0xFFFF9800)      // Orange for due maintenance
val MaintenanceOverdue = Color(0xFFD32F2F)  // Red for overdue
val MaintenanceRecent = Color(0xFF388E3C)   // Green for recently completed
val MaintenanceUpcoming = Color(0xFF1976D2) // Blue for upcoming

// Legacy colors (keeping for compatibility during transition)
val Purple80 = MechanicBlue80
val PurpleGrey80 = SteelGray80
val Pink80 = WarningAmber80

val Purple40 = MechanicBlue40
val PurpleGrey40 = SteelGray40
val Pink40 = WarningAmber40