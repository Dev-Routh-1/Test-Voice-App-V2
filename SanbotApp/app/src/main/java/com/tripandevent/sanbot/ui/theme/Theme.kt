package com.tripandevent.sanbot.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = White,
    secondary = BrandOrange,
    onSecondary = White,
    secondaryContainer = BrandOrange,
    onSecondaryContainer = White,
    tertiary = SuccessGreen,
    onTertiary = White,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = White,
    onSurface = OnSurfaceLight,
    surfaceVariant = LightGray,
    onSurfaceVariant = OnSurfaceVariant,
    error = ErrorRed,
    onError = White,
    outline = LightGray
)

@Composable
fun SanbotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
