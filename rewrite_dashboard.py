with open("app/src/main/java/com/example/ui/screens/StudentDashboardScreen.kt", "w") as f:
    f.write("""package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassHubRepository
import com.example.model.Assignment
import com.example.model.Subject
import com.example.model.User
import com.example.ui.haptics.LocalIOSHaptics
import com.example.ui.theme.CampuProDesign
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StudentDashboardScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance,
    onNavigateToClasses: () -> Unit = {},
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToExams: () -> Unit = {},
    onNavigateToAssistant: () -> Unit = {}
) {
    val haptics = LocalIOSHaptics.current
    val todayDateFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))
    }
    val firstName = user.fullName.split(" ").firstOrNull() ?: "Student"
    
    val assignments by repository.assignments.collectAsState()
    val statuses by repository.assignmentStatuses.collectAsState()
    val enrollments by repository.enrollments.collectAsState()
    val subjects by repository.subjects.collectAsState()
    
    val todayStr = remember { LocalDate.now().toString() }
    
    // Upcoming deadlines: not done, due date >= today
    val upcomingDeadlines = remember(assignments, statuses, user) {
        val userStatuses = statuses.filter { it.studentId == user.id }
        assignments.filter { asgn ->
            val isDone = userStatuses.find { it.assignmentId == asgn.id }?.isDone ?: false
            !isDone && asgn.dueDate >= todayStr
        }.sortedBy { it.dueDate }.take(3)
    }
    
    // Enrolled courses
    val enrolledCourses = remember(enrollments, subjects, user) {
        val subjectIds = enrollments.filter { it.studentId == user.id }.map { it.subjectId }.toSet()
        subjects.filter { it.id in subjectIds }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CampuProDesign.AppBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CampuPro",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CampuProDesign.TextPrimary
                    )
                    Text(
                        text = user.role.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = CampuProDesign.AccentBlue
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CampuProDesign.CardBackground)
                        .clickable { haptics?.selection() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstName.take(1).uppercase(),
                        color = CampuProDesign.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Greeting Block
        item {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Good morning, ",
                        fontSize = 22.sp,
                        color = CampuProDesign.TextSecondary
                    )
                    Text(
                        text = "$firstName.",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = CampuProDesign.TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = todayDateFormatted,
                        fontSize = 14.sp,
                        color = CampuProDesign.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "☀️ A great day to learn!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CampuProDesign.AccentBlue
                    )
                }
            }
        }
        
        // 3. Enrolled Courses Horizontal List
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enrolled Courses",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CampuProDesign.TextPrimary
                    )
                    Text(
                        text = "See All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CampuProDesign.AccentBlue,
                        modifier = Modifier.clickable { haptics?.lightImpact(); onNavigateToClasses() }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (enrolledCourses.isEmpty()) {
                    Text(
                        text = "Not enrolled in any courses.",
                        color = CampuProDesign.TextSecondary,
                        fontSize = 14.sp
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        // We use a custom trick to allow overscroll on the right edge.
                    ) {
                        items(enrolledCourses) { course ->
                            CourseCard(
                                subject = course,
                                onClick = { 
                                    haptics?.lightImpact()
                                    onNavigateToClasses()
                                }
                            )
                        }
                    }
                }
            }
        }

        // 4. Upcoming Deadlines Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Deadlines",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CampuProDesign.TextPrimary
                    )
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = CampuProDesign.AccentBlue,
                        modifier = Modifier.clickable { haptics?.lightImpact(); onNavigateToAssignments() }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (upcomingDeadlines.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                        border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = CampuProDesign.SuccessText,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "All caught up! No upcoming deadlines.",
                                color = CampuProDesign.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        upcomingDeadlines.forEach { assignment ->
                            DeadlineCard(
                                assignment = assignment,
                                onClick = {
                                    haptics?.lightImpact()
                                    onNavigateToAssignments()
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Quick Links Section
        item {
            Column {
                Text(
                    text = "Quick Links",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionTile(
                        title = "AI Assist",
                        icon = Icons.Default.AutoAwesome,
                        bgColor = CampuProDesign.TilePurpleBg,
                        iconColor = CampuProDesign.TilePurpleIcon,
                        onClick = { haptics?.lightImpact(); onNavigateToAssistant() }
                    )
                    QuickActionTile(
                        title = "Attendance",
                        icon = Icons.Default.CheckCircleOutline,
                        bgColor = CampuProDesign.TileTealBg,
                        iconColor = CampuProDesign.TileTealIcon,
                        onClick = { haptics?.lightImpact(); onNavigateToAttendance() }
                    )
                    QuickActionTile(
                        title = "Exams",
                        icon = Icons.Default.Description,
                        bgColor = CampuProDesign.TileIndigoBg,
                        iconColor = CampuProDesign.TileIndigoIcon,
                        onClick = { haptics?.lightImpact(); onNavigateToExams() }
                    )
                    QuickActionTile(
                        title = "Timetable",
                        icon = Icons.Default.CalendarMonth,
                        bgColor = CampuProDesign.TileGreenBg,
                        iconColor = CampuProDesign.TileGreenIcon,
                        onClick = { haptics?.lightImpact(); onNavigateToTimetable() }
                    )
                }
            }
        }
    }
}

@Composable
fun CourseCard(
    subject: Subject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
        border = BorderStroke(1.dp, CampuProDesign.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CampuProDesign.TileIndigoBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = CampuProDesign.TileIndigoIcon,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subject.code,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = CampuProDesign.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subject.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CampuProDesign.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CampuProDesign.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = subject.facultyName.ifEmpty { "TBA" },
                    fontSize = 13.sp,
                    color = CampuProDesign.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DeadlineCard(
    assignment: Assignment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
        border = BorderStroke(1.dp, CampuProDesign.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CampuProDesign.WarningBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = CampuProDesign.WarningText,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assignment.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = assignment.subjectName,
                        fontSize = 13.sp,
                        color = CampuProDesign.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " • ",
                        fontSize = 13.sp,
                        color = CampuProDesign.TextSecondary
                    )
                    Text(
                        text = "Due: ${assignment.dueDate}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = CampuProDesign.ErrorText
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = CampuProDesign.TextSecondary
            )
        }
    }
}

@Composable
fun QuickActionTile(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = CampuProDesign.TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}
""")
