with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

import re
# find exact substring and replace
bad_str = """                    is CampuProScreen.MoreMenu -> {
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
                    }) { Text("Assignments") }
                            Button(onClick = { navController.push(CampuProScreen.ExamsList) }) { Text("Exams") }
                            Button(onClick = { navController.push(CampuProScreen.Profile) }) { Text("Profile") }
                            Button(onClick = { navController.push(CampuProScreen.Settings) }) { Text("Settings") }
                        }
                    }"""

good_str = """                    is CampuProScreen.MoreMenu -> {
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

if bad_str in text:
    text = text.replace(bad_str, good_str)
else:
    print("Could not find bad string")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(text)

