import re

with open("app/src/main/java/com/example/ui/glass/GlassTabBar.kt", "w") as f:
    f.write("""package com.example.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.navigation.CampuProTab
import com.example.ui.haptics.LocalIOSHaptics
import com.example.ui.theme.CampuProDesign

@Composable
fun GlassTabBar(
    currentTab: CampuProTab,
    onTabSelected: (CampuProTab) -> Unit,
    isTeacher: Boolean,
    modifier: Modifier = Modifier
) {
    val haptics = LocalIOSHaptics.current
    val tabs = CampuProTab.entries.toTypedArray()
    val selectedIndex = tabs.indexOf(currentTab)
    
    // Ambient liquid animation phase
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val lightPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightPhase"
    )

    // Outer Liquid Glass Container
    BoxWithConstraints(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .liquidGlass(
                shape = RoundedCornerShape(28.dp),
                variant = GlassVariant.REGULAR,
                elevation = 8.dp
            )
            .height(72.dp)
            .fillMaxWidth()
    ) {
        val totalWidth = maxWidth
        val tabWidth = totalWidth / tabs.size
        
        // Fluid spring animations for the edges to create stretching/morphing effect
        val targetLeft = tabWidth * selectedIndex
        val targetRight = targetLeft + tabWidth
        
        // Moving left and right edges independently gives a soft organic stretch
        val animatedLeft by animateDpAsState(
            targetValue = targetLeft,
            animationSpec = spring(
                dampingRatio = 0.65f, // More springy
                stiffness = 200f
            ),
            label = "BubbleLeft"
        )
        
        val animatedRight by animateDpAsState(
            targetValue = targetRight,
            animationSpec = spring(
                dampingRatio = 0.65f,
                stiffness = 200f
            ),
            label = "BubbleRight"
        )
        
        val bubbleWidth = animatedRight - animatedLeft
        val bubbleOffset = animatedLeft
        
        // The Floating Liquid Glass Bubble
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .offset(x = bubbleOffset)
                .width(bubbleWidth)
                .padding(horizontal = 4.dp, vertical = 6.dp)
                // We use standard compose modifiers to build the bespoke bubble 
                // so it can have its own animated internal lighting
                .graphicsLayer {
                    // Slight scale on the Y axis during fast movement to simulate fluid compression
                    val isMoving = (animatedRight - targetRight).value.kotlin.math.abs(0f) > 1f
                    // Just simple scaling for now, but keeping depth
                    shadowElevation = 4.dp.toPx()
                    shape = RoundedCornerShape(22.dp)
                    clip = true
                }
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x351C1C1E),
                            Color(0x602C2C2E),
                            Color(0x400A84FF) // Subtle blue optical tint
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    )
                )
                // Internal animated specular light (moving highlight)
                .drawWithCache {
                    val w = size.width
                    val h = size.height
                    
                    val movingLightBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x18FFFFFF), 
                            Color(0x35FFFFFF), // optical highlight
                            Color.Transparent
                        ),
                        start = Offset(w * lightPhase - w * 0.5f, 0f),
                        end = Offset(w * lightPhase + w * 0.5f, h)
                    )
                    
                    val innerGlowBrush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x250A84FF), // soft blue inner luminance
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.8f
                    )
                    
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush = movingLightBrush)
                        drawRect(brush = innerGlowBrush)
                    }
                }
                // Extremely subtle edge highlight
                .border(
                    width = 0.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x60FFFFFF),
                            Color(0x10FFFFFF),
                            Color(0x300A84FF)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = currentTab == tab
                val title = if (isTeacher) tab.titleTeacher else tab.titleStudent
                val icon = if (isTeacher) tab.iconTeacher else tab.iconStudent
                
                val interactionSource = remember { MutableInteractionSource() }
                
                // Animate icons/labels to pop more when selected
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) CampuProDesign.AccentBlue else Color(0x99FFFFFF), // Muted white/gray
                    animationSpec = tween(300),
                    label = "IconColor"
                )
                
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFFFFFFF) else Color(0x80FFFFFF),
                    animationSpec = tween(300),
                    label = "TextColor"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null // Bubble acts as the visual indication
                        ) {
                            if (currentTab != tab) {
                                haptics?.lightImpact() // smooth touch response
                                onTabSelected(tab)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}
""")
