import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    text = f.read()

# Replace everything from "@OptIn(ExperimentalMaterial3Api::class)" to right before "@Composable\nfun LoginScreen"
pattern = re.compile(r'@OptIn\(ExperimentalMaterial3Api::class\).*?(@Composable\s*fun LoginScreen)', re.DOTALL)

replacement = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    currentUser: User,
    repository: ClassHubRepository,
    onLogout: () -> Unit,
    onSwitchRole: (String) -> Unit
) {
    val isTeacher = currentUser.role == "teacher"
    val navController = rememberCampuProNavController(isTeacher = isTeacher)

    BackHandler(enabled = navController.activeModal != null || navController.canPop) {
        navController.pop()
    }

    val currentScreen = navController.currentScreen
    val canPop = navController.canPop
    val previousScreenTitle = navController.previousScreen?.title ?: "Back"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (navController.activeModal == null) 72.dp else 0.dp)
            ) {
                when (currentScreen) {
                    is CampuProScreen.Dashboard -> {
                        if (isTeacher) {
                            TeacherDashboardScreen(
                                user = currentUser,
                                repository = repository,
                                onNavigateToAttendance = { navController.selectTab(CampuProTab.CLASSES) },
                                onNavigateToTimetable = { navController.selectTab(CampuProTab.SCHEDULE) },
                                onNavigateToRoster = { navController.push(CampuProScreen.Roster) },
                                onNavigateToRooms = { navController.push(CampuProScreen.Rooms) },
                                onNavigateToCRPermissions = { navController.push(CampuProScreen.CRPermissions) }
                            )
                        } else {
                            StudentDashboardScreen(
                                user = currentUser,
                                repository = repository,
                                onNavigateToClasses = { navController.push(CampuProScreen.Classes) },
                                onNavigateToTimetable = { navController.push(CampuProScreen.Schedule) },
                                onNavigateToAttendance = { navController.push(CampuProScreen.Attendance) },
                                onNavigateToAssignments = { navController.push(CampuProScreen.AssignmentsList) },
                                onNavigateToExams = { navController.push(CampuProScreen.ExamsList) },
                                onNavigateToAssistant = { navController.presentModal(CampuProModal.AIAssistant) }
                            )
                        }
                    }
                    is CampuProScreen.Classes -> {
                        ClassesScreen(
                            user = currentUser,
                            repository = repository,
                            onNavigateToSubject = { navController.push(CampuProScreen.SubjectDetails(it)) },
                            onNavigateToAssignments = { navController.push(CampuProScreen.AssignmentsList) },
                            backTitle = previousScreenTitle,
                            onBack = if (canPop) { { navController.pop() } } else null
                        )
                    }
                    is CampuProScreen.SubjectDetails -> {
                        SubjectDetailsScreen(
                            subjectId = currentScreen.subjectId,
                            user = currentUser,
                            repository = repository,
                            backTitle = previousScreenTitle,
                            onBack = { navController.pop() },
                            onNavigateToAssignmentDetail = { navController.push(CampuProScreen.AssignmentDetails(it, currentScreen.subjectId)) },
                            onNavigateToFacultyDetail = { name, code -> navController.push(CampuProScreen.FacultyDetails(name, code)) }
                        )
                    }
                    is CampuProScreen.AssignmentDetails -> {
                        AssignmentDetailsScreen(
                            assignmentId = currentScreen.assignmentId,
                            user = currentUser,
                            repository = repository,
                            backTitle = previousScreenTitle,
                            onBack = { navController.pop() },
                            onNavigateToFacultyDetail = { name, code -> navController.push(CampuProScreen.FacultyDetails(name, code)) }
                        )
                    }
                    is CampuProScreen.FacultyDetails -> {
                        FacultyDetailsScreen(
                            facultyName = currentScreen.facultyName,
                            subjectCode = currentScreen.subjectCode,
                            user = currentUser,
                            repository = repository,
                            backTitle = previousScreenTitle,
                            onBack = { navController.pop() }
                        )
                    }
                    is CampuProScreen.AssignmentsList -> {
                        AssignmentsScreen(
                            user = currentUser,
                            repository = repository,
                            onNavigateToAssignmentDetail = { navController.push(CampuProScreen.AssignmentDetails(it)) },
                            backTitle = previousScreenTitle,
                            onBack = if (canPop) { { navController.pop() } } else null
                        )
                    }
                    is CampuProScreen.ExamsList -> {
                        ExamsScreen(
                            user = currentUser,
                            repository = repository,
                            onBack = { navController.pop() }
                        )
                    }
                    is CampuProScreen.Schedule -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            if (canPop) {
                                IOSNavigationBar(title = "Schedule", backTitle = previousScreenTitle, onBack = { navController.pop() })
                            }
                            TimetableScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.Attendance -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            if (canPop) {
                                IOSNavigationBar(title = "Attendance", backTitle = previousScreenTitle, onBack = { navController.pop() })
                            }
                            AttendanceScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.Rooms -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            if (canPop) {
                                IOSNavigationBar(title = "Rooms", backTitle = previousScreenTitle, onBack = { navController.pop() })
                            }
                            RoomsScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.MoreMenu -> {
                        MoreNavMenu(
                            isTeacher = isTeacher,
                            onOpen = { dest ->
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
                    }
                    is CampuProScreen.Profile -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            if (canPop) {
                                IOSNavigationBar(title = "Profile", backTitle = previousScreenTitle, onBack = { navController.pop() })
                            }
                            ProfileScreen(
                                user = currentUser,
                                repository = repository,
                                onLogout = onLogout,
                                onSwitchRole = onSwitchRole,
                                onBack = { navController.pop() }
                            )
                        }
                    }
                    is CampuProScreen.Roster -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            if (canPop) {
                                IOSNavigationBar(title = "Class Roster", backTitle = previousScreenTitle, onBack = { navController.pop() })
                            }
                            RosterScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.CRPermissions -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            if (canPop) {
                                IOSNavigationBar(title = "CR Permissions", backTitle = previousScreenTitle, onBack = { navController.pop() })
                            }
                            CRPermissionsScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.StretchFeatures -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            if (canPop) {
                                IOSNavigationBar(title = "Smart Campus", backTitle = previousScreenTitle, onBack = { navController.pop() })
                            }
                            StretchFeaturesScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.Settings -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            if (canPop) {
                                IOSNavigationBar(title = "Settings", backTitle = previousScreenTitle, onBack = { navController.pop() })
                            }
                            SettingsScreen(user = currentUser, repository = repository)
                        }
                    }
                }
            }

            // Floating Liquid Glass Tab Bar (floats above content)
            if (navController.activeModal == null) {
                GlassTabBar(
                    currentTab = navController.currentTab,
                    isTeacher = isTeacher,
                    onTabSelected = { tab ->
                        if (navController.currentTab == tab) {
                            while (navController.canPop) {
                                navController.pop()
                            }
                        } else {
                            navController.selectTab(tab)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // Floating Liquid Glass AI Assistant FAB
                GlassFloatingControl(
                    onClick = { navController.presentModal(CampuProModal.AIAssistant) },
                    icon = Icons.Default.SmartToy,
                    contentDescription = "CampuPro AI",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 84.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }

        // Active Modal Presentation (e.g. AI Assistant)
        if (navController.activeModal is CampuProModal.AIAssistant) {
            AIAssistantScreen(
                user = currentUser,
                repository = repository,
                onBack = { navController.dismissModal() },
                onNavigateToAttendance = {
                    navController.dismissModal()
                    navController.selectTab(if (isTeacher) CampuProTab.CLASSES else CampuProTab.ATTENDANCE)
                },
                onNavigateToTimetable = {
                    navController.dismissModal()
                    navController.selectTab(CampuProTab.SCHEDULE)
                },
                onNavigateToAssignments = {
                    navController.dismissModal()
                    navController.push(CampuProScreen.AssignmentsList)
                },
                onNavigateToExams = {
                    navController.dismissModal()
                    navController.push(CampuProScreen.ExamsList)
                }
            )
        }
    }
}

\1"""

new_text = pattern.sub(replacement, text)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(new_text)

