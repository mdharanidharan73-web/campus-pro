package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.components.ClassHubHeader
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ClassHubRoot()
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
    var selectedTab by remember(currentUser.id) { mutableStateOf(0) }
    var moreSubScreen by remember { mutableStateOf<String?>(null) }

    val isTeacher = currentUser.role == "teacher"

    Scaffold(
        topBar = {
            ClassHubHeader(
                currentUser = currentUser,
                onSwitchRole = onSwitchRole,
                onLogout = onLogout
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0 && moreSubScreen == null,
                    onClick = {
                        selectedTab = 0
                        moreSubScreen = null
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1 && moreSubScreen == null,
                    onClick = {
                        selectedTab = 1
                        moreSubScreen = null
                    },
                    icon = {
                        Icon(
                            if (isTeacher) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = null
                        )
                    },
                    label = { Text(if (isTeacher) "Attendance" else "Timetable") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2 && moreSubScreen == null,
                    onClick = {
                        selectedTab = 2
                        moreSubScreen = null
                    },
                    icon = {
                        Icon(
                            if (isTeacher) Icons.Default.Schedule else Icons.Default.CheckCircle,
                            contentDescription = null
                        )
                    },
                    label = { Text(if (isTeacher) "Timetable" else "Attendance") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3 && moreSubScreen == null,
                    onClick = {
                        selectedTab = 3
                        moreSubScreen = null
                    },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Rooms") },
                    label = { Text("Rooms") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4 || moreSubScreen != null,
                    onClick = {
                        selectedTab = 4
                        moreSubScreen = null
                    },
                    icon = {
                        Icon(
                            if (isTeacher) Icons.Default.AdminPanelSettings else Icons.Default.MoreHoriz,
                            contentDescription = "More"
                        )
                    },
                    label = { Text(if (isTeacher) "Admin" else "More") }
                )
            }
        },
        floatingActionButton = {
            var showAssistant by remember { mutableStateOf(false) }
            FloatingActionButton(
                onClick = { showAssistant = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = "AI Assistant")
            }
            if (showAssistant) {
                ChatAssistantDialog(onDismiss = { showAssistant = false })
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                moreSubScreen == "roster" -> {
                    RosterScreen(user = currentUser, repository = repository)
                }
                moreSubScreen == "cr_permissions" -> {
                    CRPermissionsScreen(user = currentUser, repository = repository)
                }
                moreSubScreen == "stretch" -> {
                    StretchFeaturesScreen(user = currentUser, repository = repository)
                }
                moreSubScreen == "settings" -> {
                    SettingsScreen(user = currentUser, repository = repository)
                }
                selectedTab == 0 -> {
                    if (isTeacher) {
                        TeacherDashboardScreen(
                            user = currentUser,
                            repository = repository,
                            onNavigateToAttendance = { selectedTab = 1 },
                            onNavigateToTimetable = { selectedTab = 2 },
                            onNavigateToRoster = { moreSubScreen = "roster" },
                            onNavigateToRooms = { selectedTab = 3 },
                            onNavigateToCRPermissions = { moreSubScreen = "cr_permissions" }
                        )
                    } else {
                        StudentDashboardScreen(
                            user = currentUser,
                            repository = repository,
                            onNavigateToTimetable = { selectedTab = 1 },
                            onNavigateToAttendance = { selectedTab = 2 },
                            onNavigateToRooms = { selectedTab = 3 }
                        )
                    }
                }
                selectedTab == 1 -> {
                    if (isTeacher) {
                        AttendanceScreen(user = currentUser, repository = repository)
                    } else {
                        TimetableScreen(user = currentUser, repository = repository)
                    }
                }
                selectedTab == 2 -> {
                    if (isTeacher) {
                        TimetableScreen(user = currentUser, repository = repository)
                    } else {
                        AttendanceScreen(user = currentUser, repository = repository)
                    }
                }
                selectedTab == 3 -> {
                    RoomsScreen(user = currentUser, repository = repository)
                }
                selectedTab == 4 -> {
                    MoreNavMenu(
                        isTeacher = isTeacher,
                        onOpen = { moreSubScreen = it }
                    )
                }
            }
        }
    }
}

@Composable
fun MoreNavMenu(
    isTeacher: Boolean,
    onOpen: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isTeacher) "Faculty Administration & Features" else "Additional Features",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "ClassHub college class management tools",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        MoreMenuItem(
            icon = Icons.Default.FormatListNumbered,
            title = "Class Roster",
            subtitle = "View all 33 students, roll numbers & enrollments",
            onClick = { onOpen("roster") }
        )

        if (isTeacher) {
            MoreMenuItem(
                icon = Icons.Default.Star,
                title = "CR Permissions Delegation",
                subtitle = "Manage scoped subject attendance & moderation for CRs",
                onClick = { onOpen("cr_permissions") }
            )
        }

        MoreMenuItem(
            icon = Icons.Default.Assignment,
            title = "Assignments & Resource Shelf",
            subtitle = "Coursework tracker and downloadable study handouts (Phase 5)",
            onClick = { onOpen("stretch") }
        )

        MoreMenuItem(
            icon = Icons.Default.Settings,
            title = "App Settings & Thresholds",
            subtitle = "Minimum 75% attendance rule and semester calendar",
            onClick = { onOpen("settings") }
        )
    }
}

@Composable
fun MoreMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LoginScreen(
    repository: ClassHubRepository,
    onNavigateToSignup: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("rahul.verma@classhub.edu") }
    var password by remember { mutableStateOf("student123") }
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
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ClassHub",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Everything your class needs, in one place.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Demo Account Quick Selector (Page 31 of prompt!)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Demo Accounts Quick-Switch:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { repository.switchDemoUser("student") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("🎓 Rahul", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { repository.switchDemoUser("cr") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("⭐ Priya (CR)", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { repository.switchDemoUser("teacher") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("👨‍🏫 Sharma", fontSize = 11.sp)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("College Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val res = repository.login(email, password)
                    if (res.isFailure) {
                        errorMessage = res.exceptionOrNull()?.message
                    } else {
                        Toast.makeText(context, "Logged in as ${res.getOrNull()?.fullName}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Sign In to ClassHub", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToSignup) {
                Text("New student? Claim your Roll Number & Sign up")
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
            Text(
                text = "Student Registration",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Claim your pre-approved BCA class roll number",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = rollNumber,
                onValueChange = { rollNumber = it },
                label = { Text("Roll Number (e.g. BCA015)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (rollNumber.isBlank() || email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all registration fields."
                        return@Button
                    }
                    val res = repository.signup(fullName, rollNumber, email, password)
                    if (res.isFailure) {
                        errorMessage = res.exceptionOrNull()?.message
                    } else {
                        Toast.makeText(context, "Welcome to ClassHub, ${res.getOrNull()?.fullName}!", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Verify Roll & Register", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Sign In")
            }
        }
    }
}
