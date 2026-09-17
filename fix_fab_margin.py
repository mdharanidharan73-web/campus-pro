with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

text = text.replace(
    ".padding(end = 20.dp, bottom = 84.dp)",
    ".padding(end = 20.dp, bottom = 124.dp)"
)
# And let's fix the content padding so it doesn't get covered by the higher tab bar
# The tab bar is 112dp total.
text = text.replace(
    ".padding(bottom = if (activeModal == null) 72.dp else 0.dp)",
    ".padding(bottom = if (activeModal == null) 112.dp else 0.dp)"
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)
