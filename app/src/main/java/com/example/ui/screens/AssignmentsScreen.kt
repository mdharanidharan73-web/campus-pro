package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassHubRepository
import com.example.model.Assignment
import com.example.model.User
import com.example.navigation.IOSNavigationBar
import com.example.navigation.iosSwipeBack
import com.example.ui.components.IOSSegmentedControl
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance,
    onNavigateToAssignmentDetail: (String) -> Unit = {},
    backTitle: String = "Back",
    onBack: (() -> Unit)? = null
) {
    val assignments by repository.assignments.collectAsState()
    val statuses by repository.assignmentStatuses.collectAsState()
    val subjects by repository.subjects.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Due Today", "Overdue", "Completed")

    var expandedAssignmentId by remember { mutableStateOf<String?>(null) }

    // Grouping logic
    val todayStr = remember { LocalDate.now().toString() } // "2026-09-17"

    val displayedAssignments = remember(assignments, statuses, selectedTabIndex, user) {
        val userStatuses = statuses.filter { it.studentId == user.id }
        assignments.filter { asgn ->
            val isDone = userStatuses.find { it.assignmentId == asgn.id }?.isDone ?: false
            when (selectedTabIndex) {
                0 -> !isDone && asgn.dueDate >= todayStr // Upcoming
                1 -> !isDone && asgn.dueDate == todayStr // Due today
                2 -> !isDone && asgn.dueDate < todayStr  // Overdue
                3 -> isDone                              // Completed
                else -> true
            }
        }
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                IOSNavigationBar(
                    title = "Assignments",
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
            contentPadding = PaddingValues(top = if (onBack != null) 8.dp else 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header (only show when not shown in top bar)
            if (onBack == null) {
                item {
                    Column {
                        Text(
                            text = "Assignments",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Track homework, lab reports & project deadlines",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // iOS Segmented Control
            item {
                IOSSegmentedControl(
                    items = tabs,
                    selectedIndex = selectedTabIndex,
                    onItemSelected = { selectedTabIndex = it }
                )
            }

            // Summary Pill
            item {
                val count = displayedAssignments.size
                Text(
                    text = "${tabs[selectedTabIndex].uppercase()} ($count)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // Empty state
            if (displayedAssignments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTabIndex == 3) Icons.Default.CheckCircle else Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = Color(0xFF34C759),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedTabIndex == 3) "No completed assignments yet"
                                else "All caught up in ${tabs[selectedTabIndex]}!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Great job staying ahead of your coursework deadlines.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Assignment Items
            items(displayedAssignments) { asgn ->
                val isDone = statuses.find { it.assignmentId == asgn.id && it.studentId == user.id }?.isDone ?: false
                val isExpanded = expandedAssignmentId == asgn.id

                val (priorityColor, priorityBg) = when (asgn.priority.lowercase()) {
                    "high" -> Pair(Color(0xFFFF3B30), Color(0xFFFF3B30).copy(alpha = 0.12f))
                    "low" -> Pair(Color(0xFF34C759), Color(0xFF34C759).copy(alpha = 0.12f))
                    else -> Pair(Color(0xFFFF9500), Color(0xFFFF9500).copy(alpha = 0.12f))
                }

                val subject = subjects.find { it.id == asgn.subjectId }
                val code = subject?.code ?: "BCA"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedAssignmentId = if (isExpanded) null else asgn.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular iOS Checkbox
                            IconButton(
                                onClick = { repository.toggleAssignmentStatus(asgn.id, user.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                if (isDone) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF34C759)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Details
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToAssignmentDetail(asgn.id) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = code,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF007AFF)
                                    )
                                    Text(
                                        text = "• ${asgn.subjectName}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = asgn.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = if (asgn.dueDate == todayStr) Color(0xFFFF9500) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (asgn.dueDate == todayStr) "Due Today"
                                            else "Due ${asgn.dueDate}",
                                            fontSize = 12.sp,
                                            fontWeight = if (asgn.dueDate == todayStr) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (asgn.dueDate == todayStr) Color(0xFFFF9500) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(priorityBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = asgn.priority.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = priorityColor
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Description",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Expandable instructions
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, start = 42.dp)
                            ) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    thickness = 0.5.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Instructions & Requirements:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = asgn.description,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    FilledTonalButton(
                                        onClick = { repository.toggleAssignmentStatus(asgn.id, user.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (isDone) "Mark Incomplete" else "Mark Complete",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
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
