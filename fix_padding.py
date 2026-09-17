with open('app/src/main/java/com/example/ui/screens/MoreMenuScreen.kt', 'r') as f:
    text = f.read()

text = text.replace("contentPadding = PaddingValues(top = 60.dp, bottom = 120.dp)", 
                    "contentPadding = PaddingValues(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 20.dp, bottom = 120.dp)")

with open('app/src/main/java/com/example/ui/screens/MoreMenuScreen.kt', 'w') as f:
    f.write(text)
