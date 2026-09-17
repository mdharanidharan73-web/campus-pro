package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object CampuProDesign {
    // Brand Colors
    val AppBackground @Composable get() = MaterialTheme.colorScheme.background
    val CardBackground @Composable get() = MaterialTheme.colorScheme.surface
    val CardBorder @Composable get() = MaterialTheme.colorScheme.outline
    
    val TextPrimary @Composable get() = MaterialTheme.colorScheme.onBackground
    val TextSecondary @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    
    val SuccessGreen = Color(0xFF34C759) // iOS System Green
    val SuccessBg @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1B3B22) else Color(0xFFDCFCE7)
    val SuccessText @Composable get() = if (isSystemInDarkTheme()) Color(0xFF34C759) else Color(0xFF16A34A)
    
    // Gradients
    val PrimaryGradient @Composable get() = Brush.linearGradient(
        colors = if (isSystemInDarkTheme()) listOf(Color(0xFF5E5CE6), Color(0xFFBF5AF2))
                 else listOf(Color(0xFF4A6CF7), Color(0xFF8B5CF6))
    )
    val NavyGradient @Composable get() = Brush.linearGradient(
        colors = if (isSystemInDarkTheme()) listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E))
                 else listOf(Color(0xFF007AFF), Color(0xFF0056B3))
    )
    val NextClassGradient @Composable get() = Brush.linearGradient(
        colors = if (isSystemInDarkTheme()) listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E))
                 else listOf(Color(0xFF0A84FF), Color(0xFF0066CC))
    )
    
    // Quick Action Restrained System Tiles
    val TilePurpleBg @Composable get() = CardBackground
    val TilePurpleIcon @Composable get() = AccentViolet
    
    val TileTealBg @Composable get() = CardBackground
    val TileTealIcon @Composable get() = AccentBlue
    
    val TileIndigoBg @Composable get() = CardBackground
    val TileIndigoIcon @Composable get() = AccentBlue
    
    val TileGreenBg @Composable get() = CardBackground
    val TileGreenIcon @Composable get() = AccentGreen

    
    // Accent Bars
    val AccentBlue @Composable get() = if (isSystemInDarkTheme()) Color(0xFF0A84FF) else Color(0xFF007AFF) // iOS Blue
    val AccentBlueHighlightFill @Composable get() = if (isSystemInDarkTheme()) Color(0xFF0A84FF).copy(alpha = 0.15f) else Color(0xFF007AFF).copy(alpha = 0.1f)
    val AccentViolet @Composable get() = if (isSystemInDarkTheme()) Color(0xFFBF5AF2) else Color(0xFFAF52DE)
    val AccentGreen @Composable get() = if (isSystemInDarkTheme()) Color(0xFF32D74B) else Color(0xFF34C759)
    val AccentMagenta @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFF375F) else Color(0xFFFF2D55)
}
