package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassHubRepository
import com.example.model.User
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance
) {
    val subjects by repository.subjects.collectAsState()
    val allSlots by repository.timetableSlots.collectAsState()
    val enrollments by repository.enrollments.collectAsState()
    val overrides by repository.timetableOverrides.collectAsState()

    var selectedDay by remember {
        val currentDay = LocalDate.now().dayOfWeek.value // 1..7
        mutableStateOf(if (currentDay in 1..6) currentDay else 1)
    }

    var showOverrideDialog by remember { mutableStateOf(false) }

    // Filter slots based on user role and enrollment
    val visibleSlots = remember(selectedDay, allSlots, enrollments, user) {
        val daySlots = allSlots.filter { it.dayOfWeek == selectedDay }
        if (user.role == "teacher") {
            daySlots
        } else {
            // Student / CR: only show enrolled subjects! Language filter enforced!
            val enrolledSubjectIds = enrollments.filter { it.studentId == user.id }.map { it.subjectId }
            daySlots.filter { it.subjectId in enrolledSubjectIds }
        }.sortedBy { it.startTime }
    }

    val daysOfWeek = listOf(
        1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Day selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { (dayNum, label) ->
                FilterChip(
                    selected = selectedDay == dayNum,
                    onClick = { selectedDay = dayNum },
                    label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Header with active day info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fullDayName = when (selectedDay) {
                1 -> "Monday"; 2 -> "Tuesday"; 3 -> "Wednesday"; 4 -> "Thursday"; 5 -> "Friday"; 6 -> "Saturday"; else -> "Sunday"
            }
            Text(
                text = "$fullDayName's Schedule",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (user.role == "teacher") {
                Button(
                    onClick = { showOverrideDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Override", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (visibleSlots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No classes scheduled for this day.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibleSlots) { slot ->
                    val sub = subjects.find { it.id == slot.subjectId }
                    val override = overrides.find { it.slotId == slot.id }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
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
                                    fontSize = 16.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${slot.startTime} - ${slot.endTime}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.MeetingRoom,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (override?.status == "room_changed") "Room 302" else slot.room,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (!sub?.facultyName.isNullOrEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = sub?.facultyName ?: "",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Active Override Badge
                            if (override != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val badgeBg = when (override.status) {
                                    "cancelled" -> Color(0xFFFFEBEE)
                                    "room_changed" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFE3F2FD)
                                }
                                val badgeColor = when (override.status) {
                                    "cancelled" -> Color(0xFFC62828)
                                    "room_changed" -> Color(0xFFE65100)
                                    else -> Color(0xFF1565C0)
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "⚡ OVERRIDE: ${override.status.replace("_", " ").uppercase()} - ${override.note}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOverrideDialog) {
        AddOverrideDialog(
            slots = allSlots,
            subjects = subjects,
            onDismiss = { showOverrideDialog = false },
            onSave = { slotId, status, note ->
                repository.addTimetableOverride(slotId, LocalDate.now().toString(), status, note)
                showOverrideDialog = false
            }
        )
    }
}
