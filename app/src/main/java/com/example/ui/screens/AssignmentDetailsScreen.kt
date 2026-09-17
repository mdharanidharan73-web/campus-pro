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
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailsScreen(
    assignmentId: String,
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance,
    backTitle: String = "Subject",
    onBack: () -> Unit,
    onNavigateToFacultyDetail: (facultyName: String, subjectCode: String) -> Unit = { _, _ -> }
) {
    val assignments by repository.assignments.collectAsState()
    val statuses by repository.assignmentStatuses.collectAsState()
    val subjects by repository.subjects.collectAsState()

    val asgn = assignments.find { it.id == assignmentId }

    if (asgn == null) {
        Scaffold(
            topBar = {
                IOSNavigationBar(
                    title = "Assignment Details",
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
                Text("Assignment not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val matchingSubject = subjects.find { it.id == asgn.subjectId }
    val isDone = statuses.find { it.studentId == user.id && it.assignmentId == asgn.id }?.isDone ?: false
    val todayStr = remember { LocalDate.now().toString() }

    val (priorityBg, priorityColor) = when (asgn.priority.lowercase()) {
        "high" -> Pair(Color(0xFFFF3B30).copy(alpha = 0.12f), Color(0xFFFF3B30))
        "medium" -> Pair(Color(0xFFFF9500).copy(alpha = 0.12f), Color(0xFFFF9500))
        else -> Pair(Color(0xFF34C759).copy(alpha = 0.12f), Color(0xFF34C759))
    }

    Scaffold(
        topBar = {
            IOSNavigationBar(
                title = "Assignment",
                subtitle = matchingSubject?.code ?: asgn.subjectName,
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
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Assignment Card
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
                                    .background(priorityBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${asgn.priority.uppercase()} PRIORITY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = priorityColor
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isDone) Color(0xFF34C759) else Color(0xFFFF9500)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDone) "Completed" else "Due ${asgn.dueDate}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDone) Color(0xFF34C759) else Color(0xFFFF9500)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = asgn.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${matchingSubject?.code ?: "SUB"} • ${matchingSubject?.name ?: asgn.subjectName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { repository.toggleAssignmentStatus(asgn.id, user.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF007AFF),
                                contentColor = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.Replay else Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDone) "Mark as Incomplete" else "Mark as Completed",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Instructions & Rubric
            item {
                IOSSectionHeader(title = "Instructions & Deliverables")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = asgn.description.ifBlank { "Submit the complete laboratory report or assignment document as instructed in lecture." },
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            thickness = 0.5.dp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Submission Mode:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Digital Portal / Turnitin",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Weightage:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "15% Continuous Assessment",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Faculty Information Link Card
            item {
                IOSSectionHeader(title = "Course Instructor")
                val profName = matchingSubject?.facultyName?.ifBlank { "Dr. Academic Faculty" } ?: "Dr. Academic Faculty"
                val subjCode = matchingSubject?.code ?: "GEN101"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToFacultyDetail(profName, subjCode) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF007AFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap to view Office Hours & Faculty Details",
                                fontSize = 12.sp,
                                color = Color(0xFF007AFF)
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "View Faculty Details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
