package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassHubRepository
import com.example.model.Subject
import com.example.model.User
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance
) {
    val context = LocalContext.current
    val subjects by repository.subjects.collectAsState()
    val allUsers by repository.allUsers.collectAsState()
    val enrollments by repository.enrollments.collectAsState()
    val crPermissions by repository.crPermissions.collectAsState()
    val settings by repository.settings.collectAsState()

    val isTeacher = user.role == "teacher"
    val isCR = user.role == "cr"

    // If teacher or CR, determine which subjects they can mark
    val markableSubjects = remember(user, subjects, crPermissions) {
        if (isTeacher) {
            subjects
        } else if (isCR) {
            val allowedSubIds = crPermissions.filter { it.userId == user.id && it.canMarkAttendance }.map { it.subjectId }
            subjects.filter { it.id in allowedSubIds }
        } else {
            emptyList()
        }
    }

    var selectedTab by remember { mutableStateOf(if (markableSubjects.isNotEmpty()) 0 else 1) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (markableSubjects.isNotEmpty()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Mark Attendance") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Attendance Calculator") }
                )
            }
        }

        if (selectedTab == 0 && markableSubjects.isNotEmpty()) {
            TeacherAttendanceView(
                currentUser = user,
                markableSubjects = markableSubjects,
                allUsers = allUsers,
                enrollments = enrollments,
                onSave = { subId, date, statuses ->
                    val res = repository.markAttendance(subId, date, user.id, statuses)
                    if (res.isSuccess) {
                        Toast.makeText(context, "Attendance saved successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        } else {
            StudentAttendanceView(user = user, repository = repository)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAttendanceView(
    currentUser: User,
    markableSubjects: List<Subject>,
    allUsers: List<User>,
    enrollments: List<com.example.model.SubjectEnrollment>,
    onSave: (subjectId: String, date: String, statuses: Map<String, String>) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(markableSubjects.first()) }
    val todayDate = remember { LocalDate.now().toString() }

    // Enrolled students ONLY for the chosen subject!
    val enrolledStudents = remember(selectedSubject, allUsers, enrollments) {
        val enrolledStudentIds = enrollments.filter { it.subjectId == selectedSubject.id }.map { it.studentId }
        allUsers.filter { it.id in enrolledStudentIds }.sortedBy { it.rollNo }
    }

    // Default: Everyone = PRESENT as required by prompt!
    val studentStatuses = remember(enrolledStudents) {
        mutableStateMapOf<String, String>().apply {
            enrolledStudents.forEach { put(it.id, "present") }
        }
    }

    val presentCount = studentStatuses.values.count { it == "present" }
    val absentCount = studentStatuses.values.count { it == "absent" }

    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Scoped CR message notice if CR
        if (currentUser.role == "cr") {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "CR Mode: You have delegated permission to mark attendance for ${selectedSubject.name}.",
                        fontSize = 11.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Subject selector chips
        Text("Select Subject:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            markableSubjects.forEach { sub ->
                FilterChip(
                    selected = selectedSubject.id == sub.id,
                    onClick = { selectedSubject = sub },
                    label = { Text(sub.name, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Header with live counts
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Session Date: $todayDate",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Enrolled: ${enrolledStudents.size} students",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Present: $presentCount", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFEBEE))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Absent: $absentCount", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Student Roster List for marking
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(enrolledStudents) { student ->
                val isPresent = studentStatuses[student.id] == "present"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            studentStatuses[student.id] = if (isPresent) "absent" else "present"
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPresent) Color(0xFFF1F8E9) else Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isPresent) Color(0xFF81C784) else Color(0xFFE57373)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.rollNo?.takeLast(2) ?: "ST",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = student.fullName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = student.rollNo ?: "",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Toggle status indicator button
                        Button(
                            onClick = {
                                studentStatuses[student.id] = if (isPresent) "absent" else "present"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPresent) Color(0xFF2E7D32) else Color(0xFFC62828)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isPresent) "PRESENT" else "ABSENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Attendance ($presentCount Present, $absentCount Absent)")
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Attendance Submission") },
            text = {
                Text("Are you sure you want to finalize official attendance for ${selectedSubject.name}? Total Present: $presentCount, Absent: $absentCount.")
            },
            confirmButton = {
                Button(onClick = {
                    showConfirmDialog = false
                    onSave(selectedSubject.id, todayDate, studentStatuses.toMap())
                }) {
                    Text("Confirm & Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Review") }
            }
        )
    }
}

@Composable
fun StudentAttendanceView(
    user: User,
    repository: ClassHubRepository
) {
    val attendanceSummaries = remember(user) {
        repository.getStudentAttendanceSummaries(user.id)
    }
    val settings by repository.settings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Safe-to-Bunk Formula Explained Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Safe-to-Bunk Calculator Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Calculation: floor( A + R - threshold * (H + R) )",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• A: Classes attended so far\n• H: Total classes held so far\n• R: Classes remaining before semester end (${settings.semesterEndDate})\n• Threshold: ${settings.minAttendancePercent}% minimum requirement",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Subject Breakdown Cards
        items(attendanceSummaries) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.subject.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "${item.attendedCount} / ${item.heldCount} classes attended",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Percentage & Status
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${item.percentage}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.percentage >= 75) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Text(
                                text = if (item.percentage >= 75) "Healthy" else "At Risk",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.percentage >= 75) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Safe Skips Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (item.safeSkips <= 1) Color(0xFFFFEBEE) else Color(0xFFE8F5E9))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (item.safeSkips <= 1) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (item.safeSkips <= 1) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Safe to miss:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "${item.safeSkips} more class(es)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.safeSkips <= 1) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        )
                    }

                    if (item.safeSkips <= 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Warning: One more absence will put your semester exam eligibility at risk!",
                            fontSize = 11.sp,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
