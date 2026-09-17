package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassHubRepository
import com.example.model.Assignment
import com.example.model.ResourceItem
import com.example.model.Subject
import com.example.model.User
import com.example.navigation.IOSNavigationBar
import com.example.navigation.iosSwipeBack
import com.example.ui.components.IOSSectionHeader
import com.example.ui.glass.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance,
    onNavigateToSubject: (String) -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    backTitle: String = "Today",
    onBack: (() -> Unit)? = null
) {
    val subjects by repository.subjects.collectAsState()
    val assignments by repository.assignments.collectAsState()
    val resources by repository.resources.collectAsState()
    val timetableSlots by repository.timetableSlots.collectAsState()

    var selectedSubjectForDetails by remember { mutableStateOf<Subject?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) subjects
        else subjects.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.facultyName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                IOSNavigationBar(
                    title = "Classes",
                    backTitle = backTitle,
                    onBack = onBack
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = if (onBack != null) Modifier.iosSwipeBack(enabled = true, onPop = onBack) else Modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = if (onBack != null) 8.dp else 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Title (only show if no top bar)
            if (onBack == null) {
                item {
                    Column {
                        Text(
                            text = "Classes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Semester 5 • 6 Enrolled Subjects",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Apple Liquid Glass Search Bar
            item {
                GlassSearch(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search subjects, professors, codes..."
                )
            }

            // Subject List Section Header
            item {
                IOSSectionHeader(title = "Enrolled Courses")
            }

            // Subject Cards
            items(filteredSubjects) { subject ->
                val attendanceSummaries = remember(user, subject) {
                    repository.getStudentAttendanceSummaries(user.id).find { it.subject.id == subject.id }
                }
                val pct = attendanceSummaries?.percentage ?: 85
                val safeSkips = attendanceSummaries?.safeSkips ?: 3

                val subjectSlots = timetableSlots.filter { it.subjectId == subject.id }
                val nextSlot = subjectSlots.firstOrNull()

                val subjectIcon = when {
                    subject.name.contains("Data", ignoreCase = true) -> Icons.Default.Storage
                    subject.name.contains("Python", ignoreCase = true) -> Icons.Default.Code
                    subject.name.contains("Web", ignoreCase = true) -> Icons.Default.Language
                    subject.name.contains("Literature", ignoreCase = true) || subject.name.contains("Kannada", ignoreCase = true) || subject.name.contains("Tamil", ignoreCase = true) -> Icons.Default.MenuBook
                    else -> Icons.Default.School
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSubject(subject.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Semantic Icon Box
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF007AFF).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = subjectIcon,
                                contentDescription = null,
                                tint = Color(0xFF007AFF),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Subject Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = subject.code,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF007AFF)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${subject.room}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subject.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subject.facultyName.ifBlank { "Faculty Member" },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Attendance Indicator & Chevron
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (pct >= 75) Color(0xFF34C759).copy(alpha = 0.15f)
                                        else Color(0xFFFF3B30).copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$pct%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pct >= 75) Color(0xFF34C759) else Color(0xFFFF3B30)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "View Details",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Class Detail Sheet
    selectedSubjectForDetails?.let { currentSubject ->
        ModalBottomSheet(
            onDismissRequest = { selectedSubjectForDetails = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ClassDetailSheetContent(
                user = user,
                subject = currentSubject,
                repository = repository,
                onDismiss = { selectedSubjectForDetails = null },
                onNavigateToAssignments = {
                    selectedSubjectForDetails = null
                    onNavigateToAssignments()
                }
            )
        }
    }
}

@Composable
fun ClassDetailSheetContent(
    user: User,
    subject: Subject,
    repository: ClassHubRepository,
    onDismiss: () -> Unit,
    onNavigateToAssignments: () -> Unit
) {
    val assignments by repository.assignments.collectAsState()
    val resources by repository.resources.collectAsState()
    val timetableSlots by repository.timetableSlots.collectAsState()

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

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF007AFF).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = subject.code,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF007AFF)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Instructor: ${subject.facultyName.ifBlank { "Department Faculty" }} • ${subject.room}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Attendance Quick Metric Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ATTENDANCE STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$pct% ($attended/$held classes)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (pct >= 75) Color(0xFF34C759) else Color(0xFFFF3B30)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (pct >= 75) Color(0xFF34C759) else Color(0xFFFF3B30),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (safeSkips > 0) "$safeSkips classes can be safely missed while staying >= 75%"
                               else "Warning: Next miss drops below mandatory 75% threshold",
                        fontSize = 12.sp,
                        color = if (safeSkips > 0) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFFF3B30)
                    )
                }
            }
        }

        // Weekly Class Schedule
        item {
            IOSSectionHeader(title = "Weekly Schedule")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (subjectSlots.isEmpty()) {
                        Text("Class schedule synced via timetable.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        // Course Assignments
        item {
            IOSSectionHeader(
                title = "Assignments (${subjectAssignments.size})",
                actionText = "View All",
                onActionClick = onNavigateToAssignments
            )
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (subjectAssignments.isEmpty()) {
                        Text("No active assignments for this subject.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        subjectAssignments.forEach { asgn ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = asgn.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Due ${asgn.dueDate} • Priority: ${asgn.priority}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Course Materials & Handouts
        item {
            IOSSectionHeader(title = "Study Materials & Notes")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
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
