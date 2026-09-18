package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.ui.haptics.LocalIOSHaptics
import com.example.ui.theme.CampuProDesign

@Composable
fun ProfileScreen(
    user: User,
    repository: com.example.data.ClassHubRepository = com.example.data.ClassHubRepository.instance,
    onLogout: () -> Unit = {},
    onSwitchRole: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val haptics = LocalIOSHaptics.current
    val firstName = user.fullName.split(" ").firstOrNull() ?: "Dharanidharan"
    var showPersonalInfoDialog by remember { mutableStateOf(false) }
    var showAcademicDialog by remember { mutableStateOf(false) }
    var showAttendanceOverviewDialog by remember { mutableStateOf(false) }
    var showExamPerformanceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CampuProDesign.AppBackground),
        contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            haptics?.lightImpact()
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = CampuProDesign.TextPrimary,
                        modifier = Modifier.size(20.dp).offset(x = 4.dp)
                    )
                }
                Text(
                    text = "Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.TextPrimary
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { haptics?.lightImpact() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CampuProDesign.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 2. Avatar & Name
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(CampuProDesign.PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = user.fullName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                val roleSubtitle = when (user.role) {
                    "admin" -> "Campus Administrator"
                    "teacher" -> "Faculty Member"
                    "cr" -> "Class Representative • ${user.rollNo ?: ""}"
                    else -> "Student • ${user.rollNo ?: ""}"
                }
                Text(
                    text = roleSubtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CampuProDesign.AccentBlue
                )
                Spacer(modifier = Modifier.height(2.dp))
                val deptSubtitle = when (user.role) {
                    "admin" -> "System & Campus Data Administration"
                    "teacher" -> "Department of Computer Applications"
                    else -> "Bachelor of Computer Applications"
                }
                Text(
                    text = deptSubtitle,
                    fontSize = 13.sp,
                    color = CampuProDesign.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 3. Info Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                when (user.role) {
                    "admin" -> {
                        InfoChip("ADMIN", "Access Level")
                        InfoChip("Active", "Security Status")
                        InfoChip("Root", "Scope")
                    }
                    "teacher" -> {
                        InfoChip("FACULTY", "Role")
                        InfoChip("Full-Time", "Tenure")
                        InfoChip("Dept A", "Department")
                    }
                    else -> {
                        InfoChip("BCA", "Programme")
                        InfoChip("2nd Year", "Academic Year")
                        InfoChip("Section A", "Section")
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 4. Role Switcher (for testing and demoing permissions)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SWITCH ACCOUNT / ROLE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CampuProDesign.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val roles = listOf("student" to "Student", "teacher" to "Teacher", "admin" to "Admin")
                        roles.forEach { (rKey, rLabel) ->
                            val isCurrent = user.role == rKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCurrent) CampuProDesign.AccentBlue else CampuProDesign.CardBorder)
                                    .clickable {
                                        haptics?.selection()
                                        onSwitchRole(rKey)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = rLabel,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) Color.White else CampuProDesign.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 5. Menu List
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    MenuRow(Icons.Default.PersonOutline, "Personal Information") {
                        showPersonalInfoDialog = true
                    }
                    HorizontalDivider(color = CampuProDesign.CardBorder, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    MenuRow(Icons.Default.School, "Academic Details") {
                        showAcademicDialog = true
                    }
                    HorizontalDivider(color = CampuProDesign.CardBorder, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    MenuRow(Icons.Default.CheckCircleOutline, "Attendance Overview") {
                        showAttendanceOverviewDialog = true
                    }
                    HorizontalDivider(color = CampuProDesign.CardBorder, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    MenuRow(Icons.Default.StarOutline, "Exam Performance") {
                        showExamPerformanceDialog = true
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 6. Logout Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Button(
                    onClick = {
                        haptics?.warning()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30).copy(alpha = 0.12f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF3B30)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. Quote Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CampuProDesign.AccentBlueHighlightFill)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = CampuProDesign.AccentBlue.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Discipline today creates freedom tomorrow.",
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        color = CampuProDesign.TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 30.dp, height = 3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(CampuProDesign.AccentBlue)
                    )
                }
            }
        }
    }

    // Dialogs for Profile Items
    if (showPersonalInfoDialog) {
        val studentIndex = (user.rollNo?.filter { it.isDigit() }?.toIntOrNull() ?: 1)
        val mobileNumber = "+91 98765 %05d".format(10000 + studentIndex)
        AlertDialog(
            onDismissRequest = { showPersonalInfoDialog = false },
            title = { Text("Personal & Contact Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Full Name: ${user.fullName}", fontSize = 14.sp)
                    Text("Roll Number: ${user.rollNo ?: "N/A"}", fontSize = 14.sp)
                    Text("Email: ${user.email.ifBlank { "student@classhub.edu" }}", fontSize = 14.sp)
                    Text("Mobile / Phone: $mobileNumber", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CampuProDesign.AccentBlue)
                    Text("Account Role: ${user.role.uppercase()}", fontSize = 14.sp)
                    Text("Registered: ${user.createdAt.ifBlank { "2026-08-05" }}", fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPersonalInfoDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAcademicDialog) {
        AlertDialog(
            onDismissRequest = { showAcademicDialog = false },
            title = { Text("Academic Information", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Programme: Bachelor of Computer Applications (BCA)", fontSize = 14.sp)
                    Text("Current Year: 2nd Year (Semester IV)", fontSize = 14.sp)
                    Text("Section: Section A", fontSize = 14.sp)
                    Text("Batch: 2024 - 2027", fontSize = 14.sp)
                    Text("Affiliated University: Bangalore Central University", fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAcademicDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAttendanceOverviewDialog) {
        AlertDialog(
            onDismissRequest = { showAttendanceOverviewDialog = false },
            title = { Text("Attendance Overview", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Overall Aggregate: 82.4%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34C759))
                    Text("Total Classes Held: 142", fontSize = 14.sp)
                    Text("Total Attended: 117", fontSize = 14.sp)
                    Text("Minimum Required: 75%", fontSize = 14.sp)
                    Text("Status: Safe & Eligible for Term-End Exams", fontSize = 13.sp, color = CampuProDesign.TextSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttendanceOverviewDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showExamPerformanceDialog) {
        AlertDialog(
            onDismissRequest = { showExamPerformanceDialog = false },
            title = { Text("Exam Performance", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current Cumulative GPA: 8.65 / 10.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CampuProDesign.AccentBlue)
                    Text("Semester I GPA: 8.40", fontSize = 14.sp)
                    Text("Semester II GPA: 8.75", fontSize = 14.sp)
                    Text("Semester III GPA: 8.80", fontSize = 14.sp)
                    Text("Backlogs: 0", fontSize = 14.sp, color = Color(0xFF34C759))
                }
            },
            confirmButton = {
                TextButton(onClick = { showExamPerformanceDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun RowScope.InfoChip(value: String, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CampuProDesign.CardBorder.copy(alpha = 0.5f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CampuProDesign.TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = CampuProDesign.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit = {}
) {
    val haptics = LocalIOSHaptics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptics?.lightImpact()
                onClick()
            }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CampuProDesign.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = CampuProDesign.TextPrimary
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = CampuProDesign.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
