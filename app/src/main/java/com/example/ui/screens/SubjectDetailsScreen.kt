package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassHubRepository
import com.example.model.User
import com.example.navigation.IOSNavigationBar
import com.example.navigation.iosSwipeBack
import com.example.ui.components.IOSSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailsScreen(
    subjectId: String,
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance,
    backTitle: String = "Classes",
    onBack: () -> Unit,
    onNavigateToAssignmentDetail: (String) -> Unit = {},
    onNavigateToFacultyDetail: (facultyName: String, subjectCode: String) -> Unit = { _, _ -> }
) {
    val subjects by repository.subjects.collectAsState()
    val assignments by repository.assignments.collectAsState()
    val resources by repository.resources.collectAsState()
    val timetableSlots by repository.timetableSlots.collectAsState()

    val subject = subjects.find { it.id == subjectId }

    if (subject == null) {
        Scaffold(
            topBar = {
                IOSNavigationBar(
                    title = "Subject Details",
                    backTitle = backTitle,
                    onBack = onBack
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Subject not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val subjectAssignments = assignments.filter { it.subjectId == subject.id }
    val subjectResources = resources.filter { it.subjectId == subject.id }
    val subjectSlots = timetableSlots.filter { it.subjectId == subject.id }

    val attendanceSummaries = remember(user, subject) {
        repository.getStudentAttendanceSummaries(user.id).find { it.subject.id == subject.id }
    }
    val attended = attendanceSummaries?.attendedCount ?: 12
    val held = attendanceSummaries?.heldCount ?: 14
    val pct = attendanceSummaries?.percentage ?: 85
    val safeSkips = attendanceSummaries?.safeSkips ?: 2

    Scaffold(
        topBar = {
            IOSNavigationBar(
                title = subject.code,
                subtitle = subject.name,
                backTitle = backTitle,
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.iosSwipeBack(enabled = true, onPop = onBack)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF007AFF).copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = subject.code,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF)
                                )
                            }
                            Text(
                                text = "Room: ${subject.room}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tappable Faculty Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val profName = subject.facultyName.ifBlank { "Dr. Academic Faculty" }
                                    onNavigateToFacultyDetail(profName, subject.code)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF007AFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.facultyName.ifBlank { "Dr. Academic Faculty" },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Course Lead • Tap for Profile & Hours",
                                        fontSize = 11.sp,
                                        color = Color(0xFF007AFF)
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = "View Faculty",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Attendance Statistics Grid
            item {
                IOSSectionHeader(title = "Attendance Status")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Current Attendance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$pct%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pct >= 85) Color(0xFF34C759) else if (pct >= 75) Color(0xFFFF9500) else Color(0xFFFF3B30)
                            )
                            Text(text = "$attended / $held classes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Safe to Bunk", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$safeSkips Classes",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (safeSkips > 0) Color(0xFF007AFF) else Color(0xFFFF3B30)
                            )
                            Text(text = "Keeps > 75% goal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Weekly Timetable Slots
            item {
                IOSSectionHeader(title = "Scheduled Lectures")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (subjectSlots.isEmpty()) {
                            Text("No recurring lectures scheduled.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            subjectSlots.forEach { slot ->
                                val dayName = when (slot.dayOfWeek) {
                                    1 -> "Monday"
                                    2 -> "Tuesday"
                                    3 -> "Wednesday"
                                    4 -> "Thursday"
                                    5 -> "Friday"
                                    6 -> "Saturday"
                                    else -> "Week"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF007AFF))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = dayName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Text(
                                        text = "${slot.startTime} – ${slot.endTime} (${slot.room})",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Course Assignments (clickable to push AssignmentDetails)
            item {
                IOSSectionHeader(title = "Course Assignments (${subjectAssignments.size})")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (subjectAssignments.isEmpty()) {
                            Text("No active assignments for this subject.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            subjectAssignments.forEach { asgn ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onNavigateToAssignmentDetail(asgn.id) }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = asgn.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = "Due ${asgn.dueDate} • Priority: ${asgn.priority}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = "Open Assignment",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Study Materials
            item {
                IOSSectionHeader(title = "Study Materials & Handouts")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (subjectResources.isEmpty()) {
                            Text("Course slides and handouts will appear here.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            subjectResources.forEach { res ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF007AFF).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = res.fileType,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF007AFF)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = res.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "Added ${res.dateAdded}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Default.Download, contentDescription = "Download", tint = Color(0xFF007AFF), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
