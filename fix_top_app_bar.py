with open('app/src/main/java/com/example/ui/glass/GlassNavigationBar.kt', 'r') as f:
    text = f.read()

start_idx = text.find('@Composable\nfun GlassNavigationBar')

new_nav_bar = """@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
"""

text = text[:start_idx] + new_nav_bar

with open('app/src/main/java/com/example/ui/glass/GlassNavigationBar.kt', 'w') as f:
    f.write(text)
