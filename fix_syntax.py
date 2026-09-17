with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

text = text.replace("navController.graph.androidx.navigation.NavGraph.Companion.findStartDestination().id", "navController.graph.findStartDestination().id")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)
