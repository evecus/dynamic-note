package com.note.dynamic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3E6FF),
    onPrimaryContainer = Color(0xFF1B2466),
    secondary = CoralAccent,
    onSecondary = Color.White,
    tertiary = AmberAccent,
    background = SurfaceLight,
    onBackground = OnSurfaceStrong,
    surface = CardWhite,
    onSurface = OnSurfaceStrong,
    surfaceVariant = Color(0xFFF0F1F7),
    onSurfaceVariant = OnSurfaceMedium,
    outline = OutlineSoft
)

private val DarkColors = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color(0xFF1B2466),
    primaryContainer = Color(0xFF2A3680),
    onPrimaryContainer = Color(0xFFE3E6FF),
    secondary = CoralAccent,
    onSecondary = Color.White,
    background = Color(0xFF12131F),
    onBackground = Color(0xFFE6E7F0),
    surface = Color(0xFF1C1E2C),
    onSurface = Color(0xFFE6E7F0),
    surfaceVariant = Color(0xFF272A3D),
    onSurfaceVariant = Color(0xFFB6B8CE),
    outline = Color(0xFF3A3D54)
)

@Composable
fun NotesAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
