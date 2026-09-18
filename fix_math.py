with open("app/src/main/java/com/example/ui/glass/GlassTabBar.kt", "r") as f:
    text = f.read()

text = text.replace("val isMoving = (animatedRight - targetRight).value.kotlin.math.abs(0f) > 1f", "val isMoving = kotlin.math.abs((animatedRight - targetRight).value) > 1f")
text = text.replace("end = Offset(0f, Float.POSITIVE_INFINITY)", "end = Offset(0f, 1000f)")

with open("app/src/main/java/com/example/ui/glass/GlassTabBar.kt", "w") as f:
    f.write(text)
