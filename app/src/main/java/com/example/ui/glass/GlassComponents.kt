package com.example.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.haptics.LocalIOSHaptics

/**
 * Reusable Glass Button with subtle lensing, touch illumination, and spring flex.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color? = null,
    enabled: Boolean = true
) {
    val isDark = LiquidGlassTokens.isAppDark()
    val defaultTextTint = tint ?: if (isDark) LiquidGlassTokens.BrandBlueDark else LiquidGlassTokens.BrandBlue

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(LiquidGlassTokens.CornerSmall))
            .liquidGlass(
                shape = RoundedCornerShape(LiquidGlassTokens.CornerSmall),
                variant = GlassVariant.REGULAR,
                elevation = 4.dp,
                interactive = enabled,
                onClick = if (enabled) onClick else null
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = defaultTextTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = defaultTextTint
            )
        }
    }
}

/**
 * Reusable Glass Icon Button with concentric boundary and touch illumination.
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color? = null,
    enabled: Boolean = true
) {
    val isDark = LiquidGlassTokens.isAppDark()
    val iconColor = tint ?: if (isDark) Color.White else Color.Black

    Box(
        modifier = modifier
            .size(size)
            .liquidGlass(
                shape = CircleShape,
                variant = GlassVariant.REGULAR,
                elevation = 3.dp,
                interactive = enabled,
                onClick = if (enabled) onClick else null
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * Section 11 & 14: GLASS FLOATING CONTROL (e.g. Floating AI Assistant FAB)
 */
@Composable
fun GlassFloatingControl(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified, // We'll set this below if unspecified
    backgroundColor: Color = Color.Transparent
) {
    val isDark = LiquidGlassTokens.isAppDark()
    val brandColor = if (isDark) LiquidGlassTokens.BrandBlueDark else LiquidGlassTokens.BrandBlue
    val resolvedTint = if (tint == Color.Unspecified) brandColor else tint

    Box(
        modifier = modifier
            .size(56.dp)
            .liquidGlass(
                shape = RoundedCornerShape(18.dp),
                variant = GlassVariant.REGULAR,
                tint = backgroundColor, // transparent
                elevation = 12.dp,
                borderWidth = 1.2.dp,
                interactive = true,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = resolvedTint,
            modifier = Modifier.size(26.dp)
        )
    }
}

/**
 * Glass Capsule for status tags, filters, and indicators.
 */
@Composable
fun GlassCapsule(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    tintColor: Color? = null
) {
    val isDark = LiquidGlassTokens.isAppDark()
    val activeTint = tintColor ?: if (isDark) LiquidGlassTokens.BrandBlueDark else LiquidGlassTokens.BrandBlue

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(LiquidGlassTokens.CornerCapsule))
            .liquidGlass(
                shape = RoundedCornerShape(LiquidGlassTokens.CornerCapsule),
                variant = GlassVariant.REGULAR,
                elevation = if (isSelected) 4.dp else 1.dp,
                tint = if (isSelected) activeTint.copy(alpha = 0.22f) else null,
                interactive = onClick != null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeTint else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Section 17: GLASS CONTROL (Segmented Day Selector / Filter)
 */
@Composable
fun GlassSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LiquidGlassTokens.isAppDark()
    val shape = RoundedCornerShape(LiquidGlassTokens.CornerCapsule)
    val haptics = LocalIOSHaptics.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = shape,
                variant = GlassVariant.REGULAR,
                elevation = 2.dp,
                borderWidth = 0.5.dp
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val activeBlue = if (isDark) LiquidGlassTokens.BrandBlueDark else LiquidGlassTokens.BrandBlue

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    if (isDark) Color(0x400A84FF) else Color(0x25007AFF),
                                    shape = shape
                                )
                            } else Modifier
                        )
                        .clickable {
                            if (selectedIndex != index) {
                                haptics?.selection()
                            }
                            onItemSelected(index)
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) activeBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Section 6: GLASS TOOLBAR
 * Reusable Liquid Glass toolbar container for contextual controls and filter clusters.
 */
@Composable
fun GlassToolbar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(LiquidGlassTokens.CornerCapsule)
    Box(
        modifier = modifier
            .clip(shape)
            .liquidGlass(
                shape = shape,
                variant = GlassVariant.REGULAR,
                elevation = 4.dp,
                borderWidth = 0.5.dp
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

/**
 * Section 6: GLASS MENU
 * Reusable Liquid Glass popover menu surface.
 */
@Composable
fun GlassMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    if (expanded) {
        val shape = RoundedCornerShape(LiquidGlassTokens.CornerMedium)
        Box(
            modifier = modifier
                .widthIn(min = 200.dp)
                .clip(shape)
                .liquidGlass(
                    shape = shape,
                    variant = GlassVariant.REGULAR,
                    elevation = 16.dp,
                    borderWidth = 0.5.dp
                )
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

/**
 * Section 3: GLASS SEARCH
 */
@Composable
fun GlassSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    val isDark = LiquidGlassTokens.isAppDark()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(12.dp),
                variant = GlassVariant.REGULAR,
                elevation = 2.dp,
                borderWidth = 0.5.dp
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onQueryChange("") }
                )
            }
        }
    }
}
