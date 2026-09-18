package com.example

import androidx.compose.runtime.saveable.rememberSaveable

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController


import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController


import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination

import com.example.data.ClassHubRepository
import com.example.model.User
import com.example.navigation.*
import com.example.ui.glass.*
import com.example.ui.haptics.LocalIOSHaptics
import com.example.ui.haptics.rememberIOSHaptics
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val haptics = rememberIOSHaptics()
                CompositionLocalProvider(LocalIOSHaptics provides haptics) {
                    ClassHubRoot()
                }
            }
        }
    }
}

@Composable
fun ClassHubRoot(repository: ClassHubRepository = ClassHubRepository.instance) {
    val currentUser by repository.currentUser.collectAsState()
    var isSignUpView by remember { mutableStateOf(false) }

    if (currentUser == null) {
        if (isSignUpView) {
            SignupScreen(
                repository = repository,
                onNavigateToLogin = { isSignUpView = false }
            )
        } else {
            LoginScreen(
                repository = repository,
                onNavigateToSignup = { isSignUpView = true }
            )
        }
    } else {
        MainAppShell(
            currentUser = currentUser!!,
            repository = repository,
            onLogout = { repository.logout() },
            onSwitchRole = { role -> repository.switchDemoUser(role) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    currentUser: User,
    repository: ClassHubRepository,
    onLogout: () -> Unit,
    onSwitchRole: (String) -> Unit
) {
    val navController = androidx.navigation.compose.rememberNavController()
    val isTeacher = currentUser.role == "teacher"
    var activeModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.navigation.CampuProModal?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var currentTab by rememberSaveable { androidx.compose.runtime.mutableStateOf(com.example.navigation.CampuProTab.TODAY) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (activeModal == null) 112.dp else 0.dp)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "today",
                    enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it }) },
                    popEnterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }) },
                    popExitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }) }
                ) {
                    composable("today") {
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
                    composable("classes") {
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
                    composable("schedule") {
                        val canPop = false
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() }) else Modifier) {
                            TimetableScreen(
                                user = currentUser,
                                repository = repository
                            )
                        }
                    }
                    composable("attendance") {
                        val canPop = false
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() }) else Modifier) {
                            AttendanceScreen(
                                user = currentUser,
                                repository = repository
                            )
                        }
                    }
                    composable("rooms") {
                        val canPop = false
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() }) else Modifier) {
                            RoomsScreen(user = currentUser, repository = repository)
                        }
                    }
                    composable("more") {
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
                                        "admin_panel" -> navController.navigate("admin_panel")
                                    }
                                }
                            )
                        }
                    }
                    composable("subject/{subjectId}") { backStackEntry ->
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
                    composable("assignment/{assignmentId}") { backStackEntry ->
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
                    composable("faculty/{facultyName}/{subjectCode}") { backStackEntry ->
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
                    composable("assignments") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            AssignmentsScreen(
                                user = currentUser,
                                repository = repository,
                                onNavigateToAssignmentDetail = { assignmentId -> navController.navigate("assignment/$assignmentId") }
                            )
                        }
                    }
                    composable("exams") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            ExamsScreen(user = currentUser, repository = repository)
                        }
                    }
                    composable("profile") {
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
                    composable("settings") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            SettingsScreen(user = currentUser, repository = repository)
                        }
                    }
                    composable("roster") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            RosterScreen(user = currentUser, repository = repository)
                        }
                    }
                    composable("cr_permissions") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            CRPermissionsScreen(user = currentUser, repository = repository)
                        }
                    }
                    composable("stretch") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            StretchFeaturesScreen(user = currentUser, repository = repository)
                        }
                    }
                    composable("admin_panel") {
                        Column(modifier = Modifier.iosSwipeBack(enabled = true, onPop = { navController.popBackStack() })) {
                            AdminPanelScreen(
                                user = currentUser,
                                repository = repository,
                                onBack = { navController.popBackStack() }
                            )
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
                        .padding(end = 20.dp, bottom = 124.dp)
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


@Composable
fun LoginScreen(
    repository: ClassHubRepository,
    onNavigateToSignup: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val res = repository.login(email, password)
                    if (res.isFailure) errorMessage = res.exceptionOrNull()?.message
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onNavigateToSignup) {
                Text("Sign up")
            }
            if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Button(onClick = { repository.switchDemoUser("student") }) { Text("Student") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { repository.switchDemoUser("teacher") }) { Text("Teacher") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { repository.switchDemoUser("admin") }) { Text("Admin") }
            }
        }
    }
}

@Composable
fun SignupScreen(
    repository: ClassHubRepository,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = rollNumber, onValueChange = { rollNumber = it }, label = { Text("Roll Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val res = repository.signup(fullName, rollNumber, email, password)
                    if (res.isFailure) errorMessage = res.exceptionOrNull()?.message
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Sign Up") }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onNavigateToLogin) { Text("Login") }
            if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red)
            }
        }
    }
}
