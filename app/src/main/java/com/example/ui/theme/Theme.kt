package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ios_dark_primary,
    onPrimary = Color.White,
    primaryContainer = ios_dark_primary,
    onPrimaryContainer = Color.White,
    secondary = ios_dark_primary,
    onSecondary = Color.White,
    secondaryContainer = ios_dark_secondarySystemBackground,
    onSecondaryContainer = ios_dark_label,
    error = ios_dark_error,
    onError = Color.White,
    background = ios_dark_systemBackground,
    onBackground = ios_dark_label,
    surface = ios_dark_secondarySystemBackground,
    onSurface = ios_dark_label,
    surfaceVariant = ios_dark_systemBackground,
    onSurfaceVariant = ios_dark_secondaryLabel,
    outline = ios_dark_separator
)

private val LightColorScheme = lightColorScheme(
    primary = ios_light_primary,
    onPrimary = Color.White,
    primaryContainer = ios_light_primary,
    onPrimaryContainer = Color.White,
    secondary = ios_light_primary,
    onSecondary = Color.White,
    secondaryContainer = ios_light_secondarySystemBackground,
    onSecondaryContainer = ios_light_label,
    error = ios_light_error,
    onError = Color.White,
    background = ios_light_systemBackground,
    onBackground = ios_light_label,
    surface = ios_light_secondarySystemBackground,
    onSurface = ios_light_label,
    surfaceVariant = ios_light_systemBackground,
    onSurfaceVariant = ios_light_secondaryLabel,
    outline = ios_light_separator
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
