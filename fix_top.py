with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

# Remove the import from the top if it's there
if text.startswith("import androidx.compose.runtime.saveable.rememberSaveable\n"):
    text = text.replace("import androidx.compose.runtime.saveable.rememberSaveable\n", "")

# Add it after the package statement
text = text.replace("package com.example\n", "package com.example\nimport androidx.compose.runtime.saveable.rememberSaveable\n")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)
