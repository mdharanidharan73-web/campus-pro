package com.example.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.haptics.LocalIOSHaptics
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Reusable Liquid Glass modifier adhering to Apple WWDC25 Session 219.
 *
 * Implements:
 * 1. Multi-layer translucent diffusion (depth)
 * 2. Concentric specular lensing boundary rim (light bending)
 * 3. Soft ambient directional shadow
 * 4. Subtle inner refraction highlight
 * 5. Dynamic touch illumination and elastic spring flex
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.CornerMedium),
    variant: GlassVariant = GlassVariant.REGULAR,
    borderWidth: Dp = LiquidGlassTokens.RimStrokeWidth,
    elevation: Dp = LiquidGlassTokens.AmbientElevation,
    tint: Color? = null,
    interactive: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val isDark = LiquidGlassTokens.isAppDark()
    val accessibility = LocalLiquidGlassAccessibility.current
    val haptics = LocalIOSHaptics.current

    val bgBrush = LiquidGlassTokens.getBackgroundBrush(
        variant = variant,
        isDark = isDark,
        reducedTransparency = accessibility.reducedTransparency
    )

    val rimBrush = LiquidGlassTokens.getRimBrush(
        isDark = isDark,
        increasedContrast = accessibility.increasedContrast
    )

    // Touch interaction states
    val scale = remember { Animatable(1f) }
    val illuminationAlpha = remember { Animatable(0f) }
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    val coroutineScope = rememberCoroutineScope()

    val touchModifier = if (interactive || onClick != null) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                touchPosition = down.position
                haptics?.selection()

                if (!accessibility.reducedMotion) {
                    coroutineScope.launch {
                        scale.animateTo(
                            targetValue = 0.975f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                }
                coroutineScope.launch {
                    illuminationAlpha.animateTo(
                        targetValue = 0.85f,
                        animationSpec = tween(durationMillis = 100)
                    )
                }

                val up = waitForUpOrCancellation()
                if (!accessibility.reducedMotion) {
                    coroutineScope.launch {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }
                coroutineScope.launch {
                    illuminationAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 350)
                    )
                }

                if (up != null && onClick != null) {
                    haptics?.mediumImpact()
                    onClick()
                }
            }
        }
    } else {
        Modifier
    }

    this
        .then(
            if (elevation > 0.dp && !accessibility.reducedTransparency) {
                Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    clip = false,
                    ambientColor = if (isDark) Color(0x60000000) else Color(0x18000000),
                    spotColor = if (isDark) Color(0x80000000) else Color(0x28000000)
                )
            } else Modifier
        )
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .then(touchModifier)
        .clip(shape)
        .background(bgBrush)
        .then(
            if (tint != null) {
                Modifier.background(tint)
            } else Modifier
        )
        // Internal specular refraction gradient + touch illumination
        .drawWithCache {
            val width = size.width
            val height = size.height
            val innerGlowBrush = Brush.linearGradient(
                colors = listOf(
                    if (isDark) Color(0x20FFFFFF) else Color(0x35FFFFFF),
                    Color.Transparent
                ),
                start = Offset.Zero,
                end = Offset(width * 0.4f, height * 0.4f)
            )

            onDrawWithContent {
                drawContent()

                // Subtle inner top-left specular refraction
                drawRect(brush = innerGlowBrush)

                // Touch illumination radial flare
                if (illuminationAlpha.value > 0.01f) {
                    val flareRadius = width.coerceAtLeast(height) * 0.8f
                    val flareBrush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) Color(0x3A0A84FF) else Color(0x35007AFF),
                            Color.Transparent
                        ),
                        center = touchPosition,
                        radius = flareRadius
                    )
                    drawRect(
                        brush = flareBrush,
                        alpha = illuminationAlpha.value
                    )
                }
            }
        }
        // Concentric specular rim boundary (light bending lensing)
        .border(
            width = borderWidth,
            brush = rimBrush,
            shape = shape
        )
}
