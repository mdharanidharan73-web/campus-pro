import re

with open('app/src/main/java/com/example/ui/screens/TimetableScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'fun TimetableScreen() {',
    'fun TimetableScreen(user: com.example.model.User, repository: com.example.data.ClassHubRepository = com.example.data.ClassHubRepository.instance) {'
)

# Fix Modifier.background with Brush
content = content.replace(
    '.background(if (isSelected) CampuProDesign.PrimaryGradient else Color.Transparent)',
    '.then(if (isSelected) Modifier.background(CampuProDesign.PrimaryGradient) else Modifier.background(Color.Transparent))'
)

with open('app/src/main/java/com/example/ui/screens/TimetableScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content2 = f.read()

content2 = content2.replace(
    'fun ProfileScreen(\n    user: User,\n    onBack: () -> Unit = {}\n) {',
    'fun ProfileScreen(\n    user: User,\n    repository: com.example.data.ClassHubRepository = com.example.data.ClassHubRepository.instance,\n    onLogout: () -> Unit = {},\n    onSwitchRole: () -> Unit = {},\n    onBack: () -> Unit = {}\n) {'
)

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content2)
