package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Section 3 & 26: GLASS SHEET
 *
 * Liquid Glass bottom sheet container with increased material depth,
 * concentric geometry, subtle lensing, and standard grab handle.
 */
@Composable
fun GlassSheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LiquidGlassTokens.isAppDark()
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = shape,
                variant = GlassVariant.REGULAR,
                elevation = 20.dp,
                borderWidth = 1.dp
            )
            .padding(top = 10.dp, bottom = 24.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .size(width = 36.dp, height = 5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(if (isDark) Color(0x50FFFFFF) else Color(0x35000000))
            )
            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}
