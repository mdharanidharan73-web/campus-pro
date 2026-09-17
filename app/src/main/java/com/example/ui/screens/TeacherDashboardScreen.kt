package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.model.User
import com.example.ui.components.EventPostCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TeacherDashboardScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance,
    onNavigateToAttendance: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToRooms: () -> Unit,
    onNavigateToCRPermissions: () -> Unit
) {
    val allUsers by repository.allUsers.collectAsState()
    val subjects by repository.subjects.collectAsState()
    val slots by repository.timetableSlots.collectAsState()
    val overrides by repository.timetableOverrides.collectAsState()
    val events by repository.events.collectAsState()

    val totalStudents = allUsers.count { it.role in listOf("student", "cr") }
    val todayDayOfWeek = LocalDate.now().dayOfWeek.value
    val todaySlots = slots.filter { it.dayOfWeek == todayDayOfWeek }.sortedBy { it.startTime }

    var showOverrideDialog by remember { mutableStateOf(false) }
    var showEventDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Welcome banner
        item {
            Column {
                Text(
                    text = "Welcome, ${user.fullName}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Faculty Administrative Control Center",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Class Overview Stat Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TeacherStatCard(
                    title = "Total Students",
                    value = "$totalStudents",
                    subtitle = "BCA 2026 Batch",
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                TeacherStatCard(
                    title = "Subjects",
                    value = "${subjects.size}",
                    subtitle = "3 Core + 3 Lang",
                    icon = Icons.Default.Book,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TeacherStatCard(
                    title = "Today's Classes",
                    value = "${todaySlots.size}",
                    subtitle = "Scheduled slots",
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
                TeacherStatCard(
                    title = "Attendance Alert",
                    value = "3",
                    subtitle = "Students <75%",
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFFEBEE)
                )
            }
        }

        // 3. Quick Action Buttons
        item {
            Text(
                text = "Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigateToAttendance() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Attendance", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { showOverrideDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Override", fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToCRPermissions() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CR Permissions", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { onNavigateToRoster() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Class Roster", fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Today's Class Schedule
        item {
            Text(
                text = "Today's Schedule",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (todaySlots.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = "No classes scheduled for today.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(todaySlots) { slot ->
                val sub = subjects.find { it.id == slot.subjectId }
                val override = overrides.find { it.slotId == slot.id && it.date == LocalDate.now().toString() }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub?.name ?: "Subject",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${slot.startTime} - ${slot.endTime}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Room: ${slot.room}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (sub?.isCommon == false) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Language Elective", fontSize = 10.sp)
                                }
                            }
                        }

                        if (override != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFF3E0))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "OVERRIDE: ${override.status.uppercase()} (${override.note})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Recent Events
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Event Highlights",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showEventDialog = true }) {
                    Text("+ Post Event")
                }
            }
        }
        
        items(events) { event ->
            EventPostCard(event)
        }
    }

    // Modal Dialog to Add Override
    if (showOverrideDialog) {
        AddOverrideDialog(
            slots = slots,
            subjects = subjects,
            onDismiss = { showOverrideDialog = false },
            onSave = { slotId, status, note ->
                repository.addTimetableOverride(slotId, LocalDate.now().toString(), status, note)
                showOverrideDialog = false
            }
        )
    }

    if (showEventDialog) {
        var caption by remember { mutableStateOf("") }
        var imageUrl by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEventDialog = false },
            title = { Text("Post New Event") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Caption") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    repository.addEventPost(caption, imageUrl, user.fullName)
                    showEventDialog = false
                }) {
                    Text("Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEventDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TeacherStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOverrideDialog(
    slots: List<com.example.model.TimetableSlot>,
    subjects: List<com.example.model.Subject>,
    onDismiss: () -> Unit,
    onSave: (slotId: String, status: String, note: String) -> Unit
) {
    var selectedSlotId by remember { mutableStateOf(slots.firstOrNull()?.id ?: "") }
    var selectedStatus by remember { mutableStateOf("room_changed") }
    var note by remember { mutableStateOf("Shifted to Room 302") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Timetable Override") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Slot to override for today:", fontSize = 12.sp)

                // Status Radio Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("room_changed" to "Room Change", "cancelled" to "Cancel", "rescheduled" to "Reschedule").forEach { (st, label) ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Reason / Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedSlotId, selectedStatus, note) }) {
                Text("Publish Override")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
