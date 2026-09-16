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
    val tardyCount = studentStatuses.values.count { it == "tardy" }
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
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Tardy: $tardyCount", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                val currentStatus = studentStatuses[student.id] ?: "present"
                val containerColor = when(currentStatus) { "present" -> Color(0xFFF1F8E9); "absent" -> Color(0xFFFFEBEE); else -> Color(0xFFFFF3E0) }
                val iconColor = when(currentStatus) { "present" -> Color(0xFF81C784); "absent" -> Color(0xFFE57373); else -> Color(0xFFFFB74D) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = containerColor),

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
                                    .background(iconColor),
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

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = currentStatus == "present",
                                onClick = { studentStatuses[student.id] = "present" },
                                label = { Text("P", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFC8E6C9), selectedLabelColor = Color(0xFF2E7D32))
                            )
                            FilterChip(
                                selected = currentStatus == "tardy",
                                onClick = { studentStatuses[student.id] = "tardy" },
                                label = { Text("T", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFE0B2), selectedLabelColor = Color(0xFFE65100))
                            )
                            FilterChip(
                                selected = currentStatus == "absent",
                                onClick = { studentStatuses[student.id] = "absent" },
                                label = { Text("A", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFCDD2), selectedLabelColor = Color(0xFFC62828))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val summaryText = buildString {
            append("$presentCount Present")
            if (tardyCount > 0) append(", $tardyCount Tardy")
            append(", $absentCount Absent")
        }

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Attendance ($summaryText)")
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Attendance Submission") },
            text = {
                val detailStr = buildString {
                    append("Total Present: $presentCount")
                    if (tardyCount > 0) append(", Tardy: $tardyCount")
                    append(", Absent: $absentCount")
                }
                Text("Are you sure you want to finalize official attendance for ${selectedSubject.name}? $detailStr.")
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

data class StudentAttendanceHistoryItem(
    val sessionId: String,
    val date: String,
    val subjectName: String,
    val status: String,
    val createdAt: String
)

@Composable
fun StudentAttendanceView(
    user: User,
    repository: ClassHubRepository
) {
    val sessions by repository.attendanceSessions.collectAsState()
    val records by repository.attendanceRecords.collectAsState()
    val subjects by repository.subjects.collectAsState()
    val settings by repository.settings.collectAsState()

    val attendanceSummaries = remember(user, sessions, records) {
        repository.getStudentAttendanceSummaries(user.id)
    }

    var studentTab by remember { mutableStateOf(0) }
    var selectedSubjectFilter by remember { mutableStateOf("all") }
    var selectedStatusFilter by remember { mutableStateOf("all") }

    // Prepare chronological session history for this student
    val historyItems = remember(user, sessions, records, subjects) {
        val studentRecords = records.filter { it.studentId == user.id }
        studentRecords.mapNotNull { rec ->
            val sess = sessions.find { it.id == rec.sessionId } ?: return@mapNotNull null
            val sub = subjects.find { it.id == sess.subjectId }
            StudentAttendanceHistoryItem(
                sessionId = sess.id,
                date = sess.date,
                subjectName = sub?.name ?: "Unknown Subject",
                status = rec.status,
                createdAt = sess.createdAt
            )
        }.sortedByDescending { it.date }
    }

    val filteredHistory = remember(historyItems, selectedSubjectFilter, selectedStatusFilter) {
        historyItems.filter { item ->
            val matchSubject = selectedSubjectFilter == "all" || item.subjectName == selectedSubjectFilter
            val matchStatus = selectedStatusFilter == "all" || item.status.equals(selectedStatusFilter, ignoreCase = true)
            matchSubject && matchStatus
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = studentTab) {
            Tab(
                selected = studentTab == 0,
                onClick = { studentTab = 0 },
                text = { Text("Overview & Calculator") }
            )
            Tab(
                selected = studentTab == 1,
                onClick = { studentTab = 1 },
                text = { Text("Attendance History (${historyItems.size})") }
            )
        }

        if (studentTab == 0) {
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Safe-to-Bunk Calculator", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Formula: Keeps your attendance above ${settings.minAttendancePercent}%. Shows how many classes you can skip based on current schedule.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
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
                                        text = "${item.attendedCount} / ${item.heldCount} classes attended" + (if (item.tardyCount > 0) " (${item.tardyCount} tardy)" else ""),
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
        } else {
            // Tab 1: Attendance History Log
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Status Filter Chips
                Text("Filter by Status:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("all" to "All", "present" to "Present", "tardy" to "Tardy", "absent" to "Absent").forEach { (key, label) ->
                        FilterChip(
                            selected = selectedStatusFilter == key,
                            onClick = { selectedStatusFilter = key },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subject Filter Chips
                val enrolledSubjectNames = remember(attendanceSummaries) {
                    attendanceSummaries.map { it.subject.name }
                }
                if (enrolledSubjectNames.isNotEmpty()) {
                    Text("Filter by Subject:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedSubjectFilter == "all",
                            onClick = { selectedSubjectFilter = "all" },
                            label = { Text("All Subjects", fontSize = 12.sp) }
                        )
                        enrolledSubjectNames.forEach { subName ->
                            FilterChip(
                                selected = selectedSubjectFilter == subName,
                                onClick = { selectedSubjectFilter = subName },
                                label = { Text(subName.take(15), fontSize = 12.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No attendance records found matching filters.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredHistory) { hist ->
                            val statusBg = when (hist.status.lowercase()) {
                                "present" -> Color(0xFFE8F5E9)
                                "tardy" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFFFEBEE)
                            }
                            val statusColor = when (hist.status.lowercase()) {
                                "present" -> Color(0xFF2E7D32)
                                "tardy" -> Color(0xFFE65100)
                                else -> Color(0xFFC62828)
                            }
                            val statusIcon = when (hist.status.lowercase()) {
                                "present" -> Icons.Default.CheckCircle
                                "tardy" -> Icons.Default.Schedule
                                else -> Icons.Default.Cancel
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(statusBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                statusIcon,
                                                contentDescription = hist.status,
                                                tint = statusColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = hist.subjectName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Session Date: ${hist.date}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(statusBg)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = hist.status.replaceFirstChar { it.uppercase() },
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
