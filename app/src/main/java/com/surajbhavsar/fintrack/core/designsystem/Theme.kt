package com.surajbhavsar.fintrack.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Brand40,
    onPrimary = Neutral0,
    primaryContainer = Brand90,
    onPrimaryContainer = Brand10,
    secondary = Neutral60,
    onSecondary = Neutral0,
    secondaryContainer = Neutral20,
    onSecondaryContainer = Neutral90,
    tertiary = Warning,
    onTertiary = Neutral0,
    error = Danger,
    onError = Neutral0,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral0,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral60,
    outline = Neutral30,
    outlineVariant = Neutral20,
)

private val DarkColors = darkColorScheme(
    primary = Brand60,
    onPrimary = Brand10,
    primaryContainer = Brand30,
    onPrimaryContainer = Brand90,
    secondary = Neutral40,
    onSecondary = Neutral90,
    secondaryContainer = Neutral80,
    onSecondaryContainer = Neutral20,
    tertiary = Warning,
    onTertiary = Neutral90,
    error = Danger,
    onError = Neutral0,
    background = Neutral90,
    onBackground = Neutral10,
    surface = Neutral80,
    onSurface = Neutral10,
    surfaceVariant = Neutral80,
    onSurfaceVariant = Neutral40,
    outline = Neutral60,
    outlineVariant = Neutral80,
)

@Composable
fun FinTrackrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = FinTrackrTypography, content = content)
}
