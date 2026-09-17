with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

text = text.replace("import composable\nimport NavHost\n", "import androidx.navigation.compose.composable\nimport androidx.navigation.compose.NavHost\n")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

