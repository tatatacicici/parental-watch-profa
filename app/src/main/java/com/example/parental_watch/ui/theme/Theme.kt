package com.example.parental_watch.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SoftSkyBlue,
    onPrimary = SurfaceLight,
    primaryContainer = SoftSkyBlueContainer,
    onPrimaryContainer = TextPrimary,
    secondary = SoftCyan,
    onSecondary = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary,
    outlineVariant = SoftSkyBlueContainer,
    error = DangerRed,
    onError = SurfaceLight
)

// Dark scheme defined but we prioritize Light by default
private val DarkColorScheme = darkColorScheme(
    primary = SoftSkyBlue,
    onPrimary = TextPrimary,
    primaryContainer = Color(0xFF1E293B), // Darker slate for dark mode
    secondary = SoftCyan,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B)
)

@Composable
fun ParentalWatchTheme(
    darkTheme: Boolean = false, // Default to light theme as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
