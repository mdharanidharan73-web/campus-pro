with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'w') as f:
    f.write("""package com.example.ui.glass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    BoxWithConstraints(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .liquidGlass(shape = RoundedCornerShape(28.dp))
            .height(72.dp)
            .fillMaxWidth()
    ) {
        val tabWidth = maxWidth / tabs.size
        
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "IndicatorOffset"
        )

        Box(
            modifier = Modifier
                .width(tabWidth)
                .fillMaxHeight()
                .offset(x = indicatorOffset)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .liquidGlass(
                    shape = RoundedCornerShape(20.dp),
                    tint = CampuProDesign.AccentBlue.copy(alpha = 0.15f),
                    borderWidth = 0.5.dp,
                    elevation = 2.dp
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
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (currentTab != tab) {
                                haptics?.selection()
                                onTabSelected(tab)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val color by animateColorAsState(
                        targetValue = if (isSelected) CampuProDesign.AccentBlue else CampuProDesign.TextSecondary,
                        animationSpec = tween(200),
                        label = "Color"
                    )
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}
""")
