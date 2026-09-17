package com.example.ui.glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Liquid Glass Variants according to Apple WWDC25 Session 219 "Meet Liquid Glass":
 * - REGULAR: Primary material with adaptive translucency, subtle lensing, ambient shadow & contrast.
 * - CLEAR: Used sparingly over media-rich backgrounds.
 */
enum class GlassVariant {
    REGULAR,
    CLEAR
}

@Immutable
data class LiquidGlassAccessibility(
    val reducedTransparency: Boolean = false,
    val increasedContrast: Boolean = false,
    val reducedMotion: Boolean = false
)

val LocalLiquidGlassAccessibility = compositionLocalOf { LiquidGlassAccessibility() }

/**
 * Design tokens and optical parameters for Liquid Glass.
 */
object LiquidGlassTokens {
    // Brand Accent: Intelligent Deep Blue
    val BrandBlue = Color(0xFF007AFF)
    val BrandBlueDark = Color(0xFF0A84FF)

    // Corner Geometry (Concentric hierarchy)
    val CornerLarge = 28.dp      // Floating tab bar & modal sheets
    val CornerMedium = 20.dp     // Floating action controls & cards
    val CornerSmall = 14.dp      // Buttons, search fields, controls
    val CornerCapsule = 999.dp   // Segmented controls & pills

    // Lensing Rim Border Width
    val RimStrokeWidth = 1.dp
    val RimStrokeThick = 1.5.dp

    // Elevation & Shadows
    val AmbientElevation = 10.dp
    val SubtleElevation = 4.dp

    @Composable
    fun isAppDark(): Boolean {
        return MaterialTheme.colorScheme.surface.luminance() < 0.5f
    }

    @Composable
    fun getBackgroundColor(
        variant: GlassVariant = GlassVariant.REGULAR,
        isDark: Boolean = isAppDark(),
        reducedTransparency: Boolean = false
    ): Color {
        if (reducedTransparency) {
            return if (isDark) Color(0xFF1C1E24) else Color(0xFFF2F4F8)
        }
        return when (variant) {
            GlassVariant.REGULAR -> {
                if (isDark) Color(0x381E2330) // Translucent deep navy/slate
                else Color(0xE8FFFFFF)        // Translucent white milky frost
            }
            GlassVariant.CLEAR -> {
                if (isDark) Color(0x1810141E)
                else Color(0x90FFFFFF)
            }
        }
    }

    @Composable
    fun getBackgroundBrush(
        variant: GlassVariant = GlassVariant.REGULAR,
        isDark: Boolean = isAppDark(),
        reducedTransparency: Boolean = false
    ): Brush {
        if (reducedTransparency) {
            val solidColor = if (isDark) Color(0xFF1C1E24) else Color(0xFFF2F4F8)
            return Brush.verticalGradient(listOf(solidColor, solidColor))
        }

        return if (isDark) {
            when (variant) {
                GlassVariant.REGULAR -> Brush.verticalGradient(
                    colors = listOf(
                        Color(0x45252E40),
                        Color(0x3519202D),
                        Color(0x28121622)
                    )
                )
                GlassVariant.CLEAR -> Brush.verticalGradient(
                    colors = listOf(
                        Color(0x25252E40),
                        Color(0x15121622)
                    )
                )
            }
        } else {
            when (variant) {
                GlassVariant.REGULAR -> Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF5FFFFFF), // High specular top white reflection
                        Color(0xEBFAFBFD), // Translucent light glass
                        Color(0xDEEEF2F8)  // Subtle optical base
                    )
                )
                GlassVariant.CLEAR -> Brush.verticalGradient(
                    colors = listOf(
                        Color(0xC0FFFFFF),
                        Color(0x99F0F4FA)
                    )
                )
            }
        }
    }

    /**
     * Concentric specular rim highlight that replicates Apple Liquid Glass light bending.
     * Light hits top-left with high concentration and gently refracts around the geometry.
     */
    @Composable
    fun getRimBrush(
        isDark: Boolean = isAppDark(),
        increasedContrast: Boolean = false
    ): Brush {
        return if (isDark) {
            if (increasedContrast) {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x99FFFFFF),
                        Color(0x4DFFFFFF),
                        Color(0x20FFFFFF),
                        Color(0x400A84FF)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x66FFFFFF),
                        Color(0x33FFFFFF),
                        Color(0x12FFFFFF),
                        Color(0x250A84FF) // Gentle dispersion tint
                    )
                )
            }
        } else {
            if (increasedContrast) {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0x50000000),
                        Color(0x25000000)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF), // Specular rim reflection
                        Color(0x90FFFFFF),
                        Color(0x18000000), // Clean optical edge separation
                        Color(0x14007AFF)  // Subtle Apple dispersion tint
                    )
                )
            }
        }
    }

    @Composable
    fun getTouchIlluminationColor(isDark: Boolean = isAppDark()): Color {
        return if (isDark) Color(0x350A84FF) else Color(0x1E007AFF)
    }

    @Composable
    fun getActivePillBrush(isDark: Boolean = isAppDark()): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x500A84FF),
                    Color(0x350A84FF)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x25007AFF),
                    Color(0x12007AFF)
                )
            )
        }
    }
}
