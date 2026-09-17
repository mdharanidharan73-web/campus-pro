package com.example.ui.glass
import com.example.ui.theme.CampuProDesign

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.haptics.LocalIOSHaptics

/**
 * Section 13: NAVIGATION BAR
 *
 * Liquid Glass iOS-style top navigation bar.
 * - Floats above content with subtle lensing and adaptive lighting
 * - Back button: native-looking, small, clear, tactile, correct hierarchy
 * - Pops exactly one screen from the stack (preserves CampuPro navigation architecture)
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GlassNavigationBar(
    title: String,
    subtitle: String? = null,
    backTitle: String? = "Back",
    onBack: (() -> Unit)? = null,
    trailingActions: @Composable (RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.TopAppBar(
        modifier = modifier.liquidGlass(),
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = CampuProDesign.TextPrimary,
            navigationIconContentColor = CampuProDesign.AccentBlue,
            actionIconContentColor = CampuProDesign.AccentBlue
        ),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CampuProDesign.TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CampuProDesign.TextSecondary
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = CampuProDesign.AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    if (backTitle != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = backTitle,
                            fontSize = 17.sp,
                            color = CampuProDesign.AccentBlue
                        )
                    }
                }
            }
        },
        actions = {
            if (trailingActions != null) {
                trailingActions()
            }
        }
    )
}
