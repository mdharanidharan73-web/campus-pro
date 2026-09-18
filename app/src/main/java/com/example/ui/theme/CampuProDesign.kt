package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object CampuProDesign {
    // Brand & Semantic Surfaces
    val AppBackground @Composable get() = MaterialTheme.colorScheme.background
    val CardBackground @Composable get() = MaterialTheme.colorScheme.surface
    val SurfaceSecondary @Composable get() = if (isSystemInDarkTheme()) ios_dark_surfaceSecondary else ios_light_surfaceSecondary
    val CardBorder @Composable get() = MaterialTheme.colorScheme.outline
    
    // Semantic Text Colors - Strictly theme-aware
    val TextPrimary @Composable get() = if (isSystemInDarkTheme()) ios_dark_label else ios_light_label
    val TextSecondary @Composable get() = if (isSystemInDarkTheme()) ios_dark_secondaryLabel else ios_light_secondaryLabel
    val TextTertiary @Composable get() = if (isSystemInDarkTheme()) ios_dark_tertiaryLabel else ios_light_tertiaryLabel

    // Semantic Icon Colors
    val IconPrimary @Composable get() = if (isSystemInDarkTheme()) ios_dark_label else ios_light_label
    val IconSecondary @Composable get() = if (isSystemInDarkTheme()) ios_dark_secondaryLabel else ios_light_secondaryLabel

    // Status Colors
    val SuccessGreen = Color(0xFF34C759) // iOS System Green
    val SuccessBg @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1B3B22) else Color(0xFFDCFCE7)
    val SuccessText @Composable get() = if (isSystemInDarkTheme()) Color(0xFF34C759) else Color(0xFF16A34A)
    
    val ErrorRed @Composable get() = if (isSystemInDarkTheme()) ios_dark_error else ios_light_error
    val ErrorBg @Composable get() = if (isSystemInDarkTheme()) Color(0xFF381515) else Color(0xFFFEE2E2)
    val ErrorText @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFF6961) else Color(0xFFDC2626)

    // Gradients
    val PrimaryGradient @Composable get() = Brush.linearGradient(
        colors = if (isSystemInDarkTheme()) listOf(Color(0xFF5E5CE6), Color(0xFFBF5AF2))
                 else listOf(Color(0xFF4A6CF7), Color(0xFF8B5CF6))
    )
    val NavyGradient @Composable get() = Brush.linearGradient(
        colors = if (isSystemInDarkTheme()) listOf(Color(0xFF232838), Color(0xFF161A24))
                 else listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
    )
    val NextClassGradient @Composable get() = Brush.linearGradient(
        colors = if (isSystemInDarkTheme()) listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                 else listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
    )
    
    // Quick Action System Tiles
    val TilePurpleBg @Composable get() = CardBackground
    val TilePurpleIcon @Composable get() = AccentViolet
    
    val TileTealBg @Composable get() = CardBackground
    val TileTealIcon @Composable get() = AccentBlue
    
    val TileIndigoBg @Composable get() = CardBackground
    val TileIndigoIcon @Composable get() = AccentBlue
    
    val TileGreenBg @Composable get() = CardBackground
    val TileGreenIcon @Composable get() = AccentGreen
    
    // Accents
    val AccentBlue @Composable get() = if (isSystemInDarkTheme()) ios_dark_primary else ios_light_primary
    val AccentBlueHighlightFill @Composable get() = AccentBlue.copy(alpha = if (isSystemInDarkTheme()) 0.20f else 0.12f)
    val AccentViolet @Composable get() = if (isSystemInDarkTheme()) Color(0xFFBF5AF2) else Color(0xFFAF52DE)
    val AccentGreen @Composable get() = if (isSystemInDarkTheme()) Color(0xFF32D74B) else Color(0xFF16A34A)
    val AccentMagenta @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFF375F) else Color(0xFFFF2D55)
    val AccentOrange @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFF9F0A) else Color(0xFFF97316)

    // Liquid Glass Navigation Bar & Floating Bubble Tokens
    val NavBackground @Composable get() = if (isSystemInDarkTheme()) Color(0xD0161A23) else Color(0xF2FFFFFF)
    val NavBorder @Composable get() = if (isSystemInDarkTheme()) Color(0x35FFFFFF) else Color(0x25000000)

    val NavBubbleBackground @Composable get() = if (isSystemInDarkTheme()) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x502C2C2E),
                Color(0x703A3A3C),
                Color(0x400A84FF)
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, 1000f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0x28007AFF),
                Color(0x18007AFF),
                Color(0x0CFFFFFF)
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, 1000f)
        )
    }

    val NavBubbleBorder @Composable get() = if (isSystemInDarkTheme()) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x70FFFFFF),
                Color(0x20FFFFFF),
                Color(0x400A84FF)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0x60007AFF),
                Color(0x30007AFF),
                Color(0x20FFFFFF)
            )
        )
    }

    val NavTextSelected @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF007AFF)
    val NavTextUnselected @Composable get() = if (isSystemInDarkTheme()) Color(0x99FFFFFF) else Color(0xFF6B7280)
    val NavIconSelected @Composable get() = if (isSystemInDarkTheme()) Color(0xFF0A84FF) else Color(0xFF007AFF)
    val NavIconUnselected @Composable get() = if (isSystemInDarkTheme()) Color(0x99FFFFFF) else Color(0xFF6B7280)
}
