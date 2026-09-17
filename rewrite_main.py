import re

content = """package com.example

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
                            TimetableScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.Attendance -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            AttendanceScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.Rooms -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            RoomsScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.MoreMenu -> {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Button(onClick = { navController.push(CampuProScreen.AssignmentsList) }) { Text("Assignments") }
                            Button(onClick = { navController.push(CampuProScreen.ExamsList) }) { Text("Exams") }
                            Button(onClick = { navController.push(CampuProScreen.Profile) }) { Text("Profile") }
                            Button(onClick = { navController.push(CampuProScreen.Settings) }) { Text("Settings") }
                        }
                    }
                    is CampuProScreen.Profile -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
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
                            RosterScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.CRPermissions -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            CRPermissionsScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.StretchFeatures -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
                            StretchFeaturesScreen(user = currentUser, repository = repository)
                        }
                    }
                    is CampuProScreen.Settings -> {
                        Column(modifier = if (canPop) Modifier.iosSwipeBack(enabled = true, onPop = { navController.pop() }) else Modifier) {
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

        // Active Modal Presentation
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
"""

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

