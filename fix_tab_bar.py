with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'r') as f:
    text = f.read()

start_idx = text.find('@Composable\nfun GlassTabBar')

new_tab_bar = """@Composable
fun GlassTabBar(
    currentTab: CampuProTab,
    onTabSelected: (CampuProTab) -> Unit,
    isTeacher: Boolean,
    modifier: Modifier = Modifier
) {
    val haptics = LocalIOSHaptics.current
    val tabs = CampuProTab.entries.toTypedArray()
    
    androidx.compose.material3.NavigationBar(
        modifier = modifier.liquidGlass(),
        containerColor = Color.Transparent,
        contentColor = CampuProDesign.TextSecondary,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { tab ->
            val isSelected = currentTab == tab
            val title = if (isTeacher) tab.titleTeacher else tab.titleStudent
            val icon = if (isTeacher) tab.iconTeacher else tab.iconStudent
            
            androidx.compose.material3.NavigationBarItem(
                selected = isSelected,
                onClick = { 
                    if (currentTab != tab) {
                        haptics?.selection()
                        onTabSelected(tab)
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                    )
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = CampuProDesign.AccentBlue,
                    selectedTextColor = CampuProDesign.AccentBlue,
                    unselectedIconColor = CampuProDesign.TextSecondary,
                    unselectedTextColor = CampuProDesign.TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
"""

text = text[:start_idx] + new_tab_bar

with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'w') as f:
    f.write(text)
