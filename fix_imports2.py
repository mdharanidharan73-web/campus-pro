with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

# Let's remove ALL duplicate rememberSaveable imports and messy ones.
text = text.replace("import androidx.compose.runtime.saveable.rememberSaveable\n", "")
text = text.replace("package com.example\n", "package com.example\n\nimport androidx.compose.runtime.saveable.rememberSaveable\n")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)
