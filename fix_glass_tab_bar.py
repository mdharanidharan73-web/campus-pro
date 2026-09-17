with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'r') as f:
    text = f.read()

bad_tab = """                        Box(
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
                        }"""

good_tab = """                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) CampuProDesign.AccentBlue else CampuProDesign.TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }"""

text = text.replace(bad_tab, good_tab)

with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'w') as f:
    f.write(text)

