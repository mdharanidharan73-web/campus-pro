with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'r') as f:
    text = f.read()

text = text.replace(
    ".padding(horizontal = 20.dp, vertical = 20.dp)",
    ".windowInsetsPadding(WindowInsets.navigationBars)\n            .padding(horizontal = 20.dp, vertical = 20.dp)"
)

with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'w') as f:
    f.write(text)
