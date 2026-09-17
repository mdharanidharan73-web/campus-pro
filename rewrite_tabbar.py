import re

with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'w') as f:
    f.write("""package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                spotColor = Color(0xFF141E46).copy(alpha = 0.06f),
                ambientColor = Color(0xFF141E46).copy(alpha = 0.06f)
            )
            .background(Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    val title = if (isTeacher) tab.titleTeacher else tab.titleStudent
                    val icon = if (isTeacher) tab.iconTeacher else tab.iconStudent
                    
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (currentTab != tab) {
                                    haptics?.selection()
                                    onTabSelected(tab)
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) CampuProDesign.AccentBlueHighlightFill else Color.Transparent)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) CampuProDesign.AccentBlue else CampuProDesign.TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                            color = if (isSelected) CampuProDesign.AccentBlue else CampuProDesign.TextSecondary
                        )
                    }
                }
            }
            
            // Thin home-indicator bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(134.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.Black)
                )
            }
        }
    }
}
"""
)
