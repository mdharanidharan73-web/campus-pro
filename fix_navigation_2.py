import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

start_idx = content.find('@Composable\nfun MainAppShell')
end_idx = content.find('@Composable\nfun LoginScreen')

new_main_app_shell = """@Composable
fun MainAppShell(
    currentUser: User,
    repository: ClassHubRepository,
    onLogout: () -> Unit,
    onSwitchRole: () -> Unit
) {
    val navController = androidx.navigation.compose.rememberNavController()
    val isTeacher = currentUser.role == "teacher"
    var activeModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.navigation.CampuProModal?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var currentTab by androidx.compose.runtime.rememberSaveable { androidx.compose.runtime.mutableStateOf(com.example.navigation.CampuProTab.TODAY) }

    Scaffold(
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
                    .padding(bottom = if (activeModal == null) 72.dp else 0.dp)
            ) {
                androidx.navigation.compose.NavHost(
                    navController = navController,
                    startDestination = "today",
                    enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it }) },
                    popEnterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }) },
                    popExitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }) }
                ) {
                    androidx.navigation.compose.composable("today") {
                        val canPop = false
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() }) else Modifier) {
                            if (isTeacher) {
                                TeacherDashboardScreen(
                                    user = currentUser,
                                    repository = repository,
                                    onNavigateToAttendance = { currentTab = com.example.navigation.CampuProTab.CLASSES; navController.navigate("attendance") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } },
                                    onNavigateToTimetable = { currentTab = com.example.navigation.CampuProTab.SCHEDULE; navController.navigate("schedule") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } },
                                    onNavigateToRoster = { currentTab = com.example.navigation.CampuProTab.MORE; navController.navigate("roster") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } },
                                    onNavigateToRooms = { currentTab = com.example.navigation.CampuProTab.MORE; navController.navigate("rooms") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } },
                                    onNavigateToCRPermissions = { currentTab = com.example.navigation.CampuProTab.MORE; navController.navigate("cr_permissions") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } }
                                )
                            } else {
                                StudentDashboardScreen(
                                    user = currentUser,
                                    repository = repository,
                                    onNavigateToClasses = { currentTab = com.example.navigation.CampuProTab.CLASSES; navController.navigate("classes") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } },
                                    onNavigateToTimetable = { currentTab = com.example.navigation.CampuProTab.SCHEDULE; navController.navigate("schedule") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } },
                                    onNavigateToAttendance = { currentTab = com.example.navigation.CampuProTab.ATTENDANCE; navController.navigate("attendance") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } },
                                    onNavigateToAssignments = { currentTab = com.example.navigation.CampuProTab.MORE; navController.navigate("assignments") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } },
                                    onNavigateToExams = { currentTab = com.example.navigation.CampuProTab.MORE; navController.navigate("exams") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } },
                                    onNavigateToAssistant = { activeModal = com.example.navigation.CampuProModal.AIAssistant }
                                )
                            }
                        }
                    }
                    androidx.navigation.compose.composable("classes") {
                        val canPop = false
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() }) else Modifier) {
                            ClassesScreen(
                                user = currentUser,
                                repository = repository,
                                onNavigateToSubject = { subjectId -> navController.navigate("subject/$subjectId") },
                                onNavigateToAssignments = { currentTab = com.example.navigation.CampuProTab.MORE; navController.navigate("assignments") { popUpTo("today") { saveState = true }; launchSingleTop = true; restoreState = true } }
                            )
                        }
                    }
                    androidx.navigation.compose.composable("schedule") {
                        val canPop = false
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() }) else Modifier) {
                            TimetableScreen(
                                user = currentUser,
                                repository = repository
                            )
                        }
                    }
                    androidx.navigation.compose.composable("attendance") {
                        val canPop = false
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() }) else Modifier) {
                            AttendanceScreen(
                                user = currentUser,
                                repository = repository
                            )
                        }
                    }
                    androidx.navigation.compose.composable("rooms") {
                        val canPop = false
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() }) else Modifier) {
                            RoomsScreen(user = currentUser, repository = repository)
                        }
                    }
                    androidx.navigation.compose.composable("more") {
                        val canPop = false
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() }) else Modifier) {
                            MoreMenuScreen(
                                user = currentUser,
                                onNavigate = { dest ->
                                    when (dest) {
                                        "assignments" -> navController.navigate("assignments")
                                        "exams" -> navController.navigate("exams")
                                        "profile" -> navController.navigate("profile")
                                        "rooms" -> navController.navigate("rooms")
                                        "roster" -> navController.navigate("roster")
                                        "cr_permissions" -> navController.navigate("cr_permissions")
                                        "stretch" -> navController.navigate("stretch")
                                        "settings" -> navController.navigate("settings")
                                    }
                                }
                            )
                        }
                    }
                    androidx.navigation.compose.composable("subject/{subjectId}") { backStackEntry ->
                        val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            SubjectDetailsScreen(
                                user = currentUser,
                                subjectId = subjectId,
                                repository = repository,
                                onBack = { navController.popBackStack() },
                                onNavigateToAssignmentDetail = { assignmentId -> navController.navigate("assignment/$assignmentId") },
                                onNavigateToFacultyDetail = { facultyName, subjectCode -> navController.navigate("faculty/$facultyName/$subjectCode") }
                            )
                        }
                    }
                    androidx.navigation.compose.composable("assignment/{assignmentId}") { backStackEntry ->
                        val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: ""
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            AssignmentDetailsScreen(
                                user = currentUser,
                                assignmentId = assignmentId,
                                repository = repository,
                                onBack = { navController.popBackStack() },
                                onNavigateToFacultyDetail = { facultyName, subjectCode -> navController.navigate("faculty/$facultyName/$subjectCode") }
                            )
                        }
                    }
                    androidx.navigation.compose.composable("faculty/{facultyName}/{subjectCode}") { backStackEntry ->
                        val facultyName = backStackEntry.arguments?.getString("facultyName") ?: ""
                        val subjectCode = backStackEntry.arguments?.getString("subjectCode") ?: ""
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            FacultyDetailsScreen(
                                user = currentUser,
                                facultyName = facultyName,
                                subjectCode = subjectCode,
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    androidx.navigation.compose.composable("assignments") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            AssignmentsScreen(
                                user = currentUser,
                                repository = repository,
                                onNavigateToAssignment = { assignmentId -> navController.navigate("assignment/$assignmentId") }
                            )
                        }
                    }
                    androidx.navigation.compose.composable("exams") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            ExamsScreen(user = currentUser, repository = repository)
                        }
                    }
                    androidx.navigation.compose.composable("profile") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            ProfileScreen(
                                user = currentUser,
                                repository = repository,
                                onLogout = onLogout,
                                onSwitchRole = onSwitchRole,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    androidx.navigation.compose.composable("settings") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            SettingsScreen(user = currentUser, repository = repository)
                        }
                    }
                    androidx.navigation.compose.composable("roster") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            RosterScreen(user = currentUser, repository = repository)
                        }
                    }
                    androidx.navigation.compose.composable("cr_permissions") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            CRPermissionsScreen(user = currentUser, repository = repository)
                        }
                    }
                    androidx.navigation.compose.composable("stretch") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            StretchFeaturesScreen(user = currentUser, repository = repository)
                        }
                    }
                }
            }

            // Floating Liquid Glass Tab Bar
            if (activeModal == null) {
                GlassTabBar(
                    currentTab = currentTab,
                    isTeacher = isTeacher,
                    onTabSelected = { tab ->
                        if (currentTab == tab) {
                            navController.popBackStack(navController.graph.findStartDestination().id, false)
                        } else {
                            currentTab = tab
                            val route = when (tab) {
                                com.example.navigation.CampuProTab.TODAY -> "today"
                                com.example.navigation.CampuProTab.CLASSES -> if (isTeacher) "attendance" else "classes"
                                com.example.navigation.CampuProTab.SCHEDULE -> "schedule"
                                com.example.navigation.CampuProTab.ATTENDANCE -> if (isTeacher) "rooms" else "attendance"
                                com.example.navigation.CampuProTab.MORE -> "more"
                            }
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                // Floating Liquid Glass AI Assistant FAB
                GlassFloatingControl(
                    onClick = { activeModal = com.example.navigation.CampuProModal.AIAssistant },
                    icon = Icons.Default.SmartToy,
                    contentDescription = "CampuPro AI",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 84.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }

        // Active Modal Presentation
        if (activeModal is com.example.navigation.CampuProModal.AIAssistant) {
            AIAssistantScreen(
                user = currentUser,
                repository = repository,
                onBack = { activeModal = null },
                onNavigateToAttendance = {
                    activeModal = null
                    currentTab = if (isTeacher) com.example.navigation.CampuProTab.CLASSES else com.example.navigation.CampuProTab.ATTENDANCE
                    navController.navigate("attendance") { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToTimetable = {
                    activeModal = null
                    currentTab = com.example.navigation.CampuProTab.SCHEDULE
                    navController.navigate("schedule") { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToAssignments = {
                    activeModal = null
                    currentTab = com.example.navigation.CampuProTab.MORE
                    navController.navigate("assignments") { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
                },
                onNavigateToExams = {
                    activeModal = null
                    currentTab = com.example.navigation.CampuProTab.MORE
                    navController.navigate("exams") { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
                }
            )
        }
    }
}
"""

imports = """
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
"""

content = content[:start_idx] + new_main_app_shell + "\n\n" + content[end_idx:]
if "import androidx.navigation.compose.composable" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\n" + imports)

# Remove the prefix androidx.navigation.compose. from composable in content just to be sure
content = content.replace("androidx.navigation.compose.composable", "composable")
content = content.replace("androidx.navigation.compose.NavHost", "NavHost")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

