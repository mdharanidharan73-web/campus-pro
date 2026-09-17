with open('app/src/main/java/com/example/ui/glass/GlassNavigationBar.kt', 'r') as f:
    text = f.read()

if "import com.example.ui.theme.CampuProDesign" not in text:
    text = text.replace("package com.example.ui.glass\n", "package com.example.ui.glass\nimport com.example.ui.theme.CampuProDesign\n")

with open('app/src/main/java/com/example/ui/glass/GlassNavigationBar.kt', 'w') as f:
    f.write(text)

with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'r') as f:
    text = f.read()

text = text.replace("androidx.compose.material3.NavigationBarItem(", "androidx.compose.material3.NavigationBarItem(") # Wait, that was already there. Let's just add the import and remove the prefix.
text = text.replace("androidx.compose.material3.NavigationBarItem", "NavigationBarItem")
text = text.replace("package com.example.ui.glass\n", "package com.example.ui.glass\nimport androidx.compose.material3.NavigationBarItem\n")

with open('app/src/main/java/com/example/ui/glass/GlassTabBar.kt', 'w') as f:
    f.write(text)

