import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'is CampuProScreen\.MoreMenu -> \{.*?\}', re.DOTALL)

replacement = """is CampuProScreen.MoreMenu -> {
                        MoreMenuScreen(
                            user = currentUser,
                            onNavigate = { dest ->
                                when (dest) {
                                    "assignments" -> navController.push(CampuProScreen.AssignmentsList)
                                    "exams" -> navController.push(CampuProScreen.ExamsList)
                                    "profile" -> navController.push(CampuProScreen.Profile)
                                    "rooms" -> navController.push(CampuProScreen.Rooms)
                                    "roster" -> navController.push(CampuProScreen.Roster)
                                    "cr_permissions" -> navController.push(CampuProScreen.CRPermissions)
                                    "stretch" -> navController.push(CampuProScreen.StretchFeatures)
                                    "settings" -> navController.push(CampuProScreen.Settings)
                                }
                            }
                        )
                    }"""

new_content = pattern.sub(replacement, content, count=1)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(new_content)

