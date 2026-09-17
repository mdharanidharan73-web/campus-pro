with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'r') as f:
    text = f.read()

text = text.replace("import androidx.compose.material3.NavigationBarItem\n", "import androidx.compose.material3.NavigationBarItem\nimport androidx.compose.material3.NavigationBarItemDefaults\n")

with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'w') as f:
    f.write(text)

