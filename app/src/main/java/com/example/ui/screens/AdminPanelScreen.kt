package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassHubRepository
import com.example.model.*
import com.example.ui.haptics.LocalIOSHaptics
import com.example.ui.theme.CampuProDesign

enum class AdminCategory(val label: String, val icon: ImageVector) {
    CLASSES("Classes", Icons.Default.School),
    SCHEDULE("Schedule", Icons.Default.Schedule),
    FACULTY("Faculty", Icons.Default.Badge),
    STUDENTS("Students", Icons.Default.Groups),
    ASSIGNMENTS("Assignments", Icons.Default.Assignment),
    EXAMS("Exams", Icons.Default.EventNote),
    ANNOUNCEMENTS("Announcements", Icons.Default.Campaign)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance,
    onBack: () -> Unit
) {
    val haptics = LocalIOSHaptics.current
    val context = LocalContext.current

    // Strict Backend-Aligned Authorization Guard
    if (user.role != "admin") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CampuProDesign.AppBackground)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFFFF3B30),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Access Denied",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CampuProDesign.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Administrator credentials are required to access this console.",
                fontSize = 14.sp,
                color = CampuProDesign.TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue)
            ) {
                Text("Return to More Menu", color = Color.White)
            }
        }
        return
    }

    var selectedCategory by remember { mutableStateOf(AdminCategory.CLASSES) }
    var searchQuery by remember { mutableStateOf("") }

    val subjects by repository.subjects.collectAsState()
    val timetableSlots by repository.timetableSlots.collectAsState()
    val allUsers by repository.allUsers.collectAsState()
    val assignments by repository.assignments.collectAsState()
    val exams by repository.exams.collectAsState()
    val events by repository.events.collectAsState()

    val facultyList = remember(allUsers) { allUsers.filter { it.role == "teacher" } }
    val studentList = remember(allUsers) { allUsers.filter { it.role == "student" || it.role == "cr" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CampuProDesign.AppBackground)
    ) {
        // 1. Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = CampuProDesign.TextPrimary,
                        modifier = Modifier.size(20.dp).offset(x = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Admin Console",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CampuProDesign.TextPrimary
                    )
                    Text(
                        text = "Campus Data Management",
                        fontSize = 12.sp,
                        color = CampuProDesign.TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CampuProDesign.AccentBlueHighlightFill)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ADMIN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.AccentBlue
                )
            }
        }

        // 2. Category Selector Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AdminCategory.values()) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .then(
                            if (isSelected) Modifier.background(CampuProDesign.PrimaryGradient)
                            else Modifier.background(CampuProDesign.CardBackground)
                        )
                        .clickable {
                            haptics?.selection()
                            selectedCategory = category
                            searchQuery = ""
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else CampuProDesign.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else CampuProDesign.TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Category Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedCategory) {
                AdminCategory.CLASSES -> ClassesAdminView(user, subjects, repository)
                AdminCategory.SCHEDULE -> ScheduleAdminView(user, timetableSlots, subjects, repository)
                AdminCategory.FACULTY -> FacultyAdminView(user, facultyList, repository)
                AdminCategory.STUDENTS -> StudentsAdminView(user, studentList, repository)
                AdminCategory.ASSIGNMENTS -> AssignmentsAdminView(user, assignments, subjects, repository)
                AdminCategory.EXAMS -> ExamsAdminView(user, exams, subjects, repository)
                AdminCategory.ANNOUNCEMENTS -> AnnouncementsAdminView(user, events, repository)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 1. CLASSES / SUBJECTS ADMIN VIEW
// -----------------------------------------------------------------------------
@Composable
fun ClassesAdminView(
    user: User,
    subjects: List<Subject>,
    repository: ClassHubRepository
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var deletingSubject by remember { mutableStateOf<Subject?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All Classes (${subjects.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CampuProDesign.TextPrimary
            )
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Class", fontSize = 13.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (subjects.isEmpty()) {
            EmptyStateCard(message = "No classes registered yet.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(subjects) { subject ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                        border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = subject.code,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CampuProDesign.AccentBlue
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = subject.room,
                                        fontSize = 12.sp,
                                        color = CampuProDesign.TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = subject.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CampuProDesign.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Faculty: ${subject.facultyName}",
                                    fontSize = 13.sp,
                                    color = CampuProDesign.TextSecondary
                                )
                            }
                            Row {
                                IconButton(onClick = { editingSubject = subject }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CampuProDesign.AccentBlue, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { deletingSubject = subject }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SubjectFormDialog(
            title = "Add Class",
            initialName = "",
            initialCode = "",
            initialFaculty = "",
            initialRoom = "",
            onDismiss = { showAddDialog = false },
            onSave = { name, code, fac, rm ->
                val result = repository.addSubject(user, name, code, fac, rm)
                if (result.isSuccess) {
                    Toast.makeText(context, "Class added successfully!", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                } else {
                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    editingSubject?.let { sub ->
        SubjectFormDialog(
            title = "Edit Class",
            initialName = sub.name,
            initialCode = sub.code,
            initialFaculty = sub.facultyName,
            initialRoom = sub.room,
            onDismiss = { editingSubject = null },
            onSave = { name, code, fac, rm ->
                val result = repository.updateSubject(user, sub.id, name, code, fac, rm)
                if (result.isSuccess) {
                    Toast.makeText(context, "Class updated successfully!", Toast.LENGTH_SHORT).show()
                    editingSubject = null
                } else {
                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    deletingSubject?.let { sub ->
        ConfirmDeleteDialog(
            title = "Delete Class",
            message = "Are you sure you want to delete '${sub.name}' (${sub.code})? Associated schedules, assignments, and exams will also be removed.",
            onDismiss = { deletingSubject = null },
            onConfirm = {
                val res = repository.deleteSubject(user, sub.id)
                if (res.isSuccess) {
                    Toast.makeText(context, "Class deleted", Toast.LENGTH_SHORT).show()
                    deletingSubject = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// 2. SCHEDULE / TIMETABLE SLOTS ADMIN VIEW
// -----------------------------------------------------------------------------
@Composable
fun ScheduleAdminView(
    user: User,
    slots: List<TimetableSlot>,
    subjects: List<Subject>,
    repository: ClassHubRepository
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<TimetableSlot?>(null) }
    var deletingSlot by remember { mutableStateOf<TimetableSlot?>(null) }

    val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timetable Slots (${slots.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CampuProDesign.TextPrimary
            )
            Button(
                onClick = {
                    if (subjects.isEmpty()) {
                        Toast.makeText(context, "Please add a class before adding a schedule slot.", Toast.LENGTH_SHORT).show()
                    } else {
                        showAddDialog = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Slot", fontSize = 13.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (slots.isEmpty()) {
            EmptyStateCard(message = "No timetable slots created yet.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(slots.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))) { slot ->
                    val subject = subjects.find { it.id == slot.subjectId }
                    val dayName = if (slot.dayOfWeek in 1..6) dayNames[slot.dayOfWeek - 1] else "Day ${slot.dayOfWeek}"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                        border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dayName.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CampuProDesign.AccentBlue
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${slot.startTime} – ${slot.endTime}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CampuProDesign.TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = subject?.name ?: "Unknown Subject",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CampuProDesign.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Room: ${slot.room} • ${subject?.code ?: ""}",
                                    fontSize = 13.sp,
                                    color = CampuProDesign.TextSecondary
                                )
                            }
                            Row {
                                IconButton(onClick = { editingSlot = slot }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CampuProDesign.AccentBlue, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { deletingSlot = slot }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SlotFormDialog(
            title = "Add Timetable Slot",
            subjects = subjects,
            initialSubjectId = subjects.firstOrNull()?.id ?: "",
            initialDay = 1,
            initialStartTime = "09:00",
            initialEndTime = "10:00",
            initialRoom = "Room 204",
            onDismiss = { showAddDialog = false },
            onSave = { subId, day, st, et, rm ->
                val res = repository.addTimetableSlot(user, subId, day, st, et, rm)
                if (res.isSuccess) {
                    Toast.makeText(context, "Slot added successfully!", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    editingSlot?.let { sl ->
        SlotFormDialog(
            title = "Edit Timetable Slot",
            subjects = subjects,
            initialSubjectId = sl.subjectId,
            initialDay = sl.dayOfWeek,
            initialStartTime = sl.startTime,
            initialEndTime = sl.endTime,
            initialRoom = sl.room,
            onDismiss = { editingSlot = null },
            onSave = { subId, day, st, et, rm ->
                val res = repository.updateTimetableSlot(user, sl.id, subId, day, st, et, rm)
                if (res.isSuccess) {
                    Toast.makeText(context, "Slot updated successfully!", Toast.LENGTH_SHORT).show()
                    editingSlot = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    deletingSlot?.let { sl ->
        ConfirmDeleteDialog(
            title = "Delete Schedule Slot",
            message = "Are you sure you want to remove this timetable slot?",
            onDismiss = { deletingSlot = null },
            onConfirm = {
                val res = repository.deleteTimetableSlot(user, sl.id)
                if (res.isSuccess) {
                    Toast.makeText(context, "Slot deleted", Toast.LENGTH_SHORT).show()
                    deletingSlot = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// 3. FACULTY ADMIN VIEW
// -----------------------------------------------------------------------------
@Composable
fun FacultyAdminView(
    user: User,
    facultyList: List<User>,
    repository: ClassHubRepository
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingFaculty by remember { mutableStateOf<User?>(null) }
    var deletingFaculty by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Faculty Members (${facultyList.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CampuProDesign.TextPrimary
            )
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Faculty", fontSize = 13.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(facultyList) { faculty ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                    border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CampuProDesign.PrimaryGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = faculty.fullName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = faculty.fullName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CampuProDesign.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = faculty.email,
                                    fontSize = 13.sp,
                                    color = CampuProDesign.TextSecondary
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = { editingFaculty = faculty }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CampuProDesign.AccentBlue, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { deletingFaculty = faculty }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        UserFormDialog(
            title = "Add Faculty Member",
            showRollNo = false,
            initialName = "",
            initialRoll = "",
            initialEmail = "",
            onDismiss = { showAddDialog = false },
            onSave = { name, _, email ->
                val res = repository.addFaculty(user, name, email)
                if (res.isSuccess) {
                    Toast.makeText(context, "Faculty added!", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    editingFaculty?.let { fac ->
        UserFormDialog(
            title = "Edit Faculty Member",
            showRollNo = false,
            initialName = fac.fullName,
            initialRoll = "",
            initialEmail = fac.email,
            onDismiss = { editingFaculty = null },
            onSave = { name, _, email ->
                val res = repository.updateFaculty(user, fac.id, name, email)
                if (res.isSuccess) {
                    Toast.makeText(context, "Faculty updated!", Toast.LENGTH_SHORT).show()
                    editingFaculty = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    deletingFaculty?.let { fac ->
        ConfirmDeleteDialog(
            title = "Delete Faculty",
            message = "Are you sure you want to remove ${fac.fullName} from the faculty directory?",
            onDismiss = { deletingFaculty = null },
            onConfirm = {
                val res = repository.deleteFaculty(user, fac.id)
                if (res.isSuccess) {
                    Toast.makeText(context, "Faculty removed", Toast.LENGTH_SHORT).show()
                    deletingFaculty = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// 4. STUDENTS ADMIN VIEW
// -----------------------------------------------------------------------------
@Composable
fun StudentsAdminView(
    user: User,
    students: List<User>,
    repository: ClassHubRepository
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<User?>(null) }
    var deletingStudent by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Enrolled Students (${students.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CampuProDesign.TextPrimary
            )
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Student", fontSize = 13.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(students) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                    border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CampuProDesign.NavyGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.rollNo?.takeLast(2) ?: "ST",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = student.fullName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CampuProDesign.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${student.rollNo ?: ""} • ${student.email}",
                                    fontSize = 13.sp,
                                    color = CampuProDesign.TextSecondary
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = { editingStudent = student }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CampuProDesign.AccentBlue, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { deletingStudent = student }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        UserFormDialog(
            title = "Add Student",
            showRollNo = true,
            initialName = "",
            initialRoll = "",
            initialEmail = "",
            onDismiss = { showAddDialog = false },
            onSave = { name, roll, email ->
                val res = repository.addStudent(user, name, roll, email)
                if (res.isSuccess) {
                    Toast.makeText(context, "Student added!", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    editingStudent?.let { st ->
        UserFormDialog(
            title = "Edit Student",
            showRollNo = true,
            initialName = st.fullName,
            initialRoll = st.rollNo ?: "",
            initialEmail = st.email,
            onDismiss = { editingStudent = null },
            onSave = { name, roll, email ->
                val res = repository.updateStudent(user, st.id, name, roll, email)
                if (res.isSuccess) {
                    Toast.makeText(context, "Student updated!", Toast.LENGTH_SHORT).show()
                    editingStudent = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    deletingStudent?.let { st ->
        ConfirmDeleteDialog(
            title = "Delete Student",
            message = "Are you sure you want to remove ${st.fullName} (${st.rollNo})?",
            onDismiss = { deletingStudent = null },
            onConfirm = {
                val res = repository.deleteStudent(user, st.id)
                if (res.isSuccess) {
                    Toast.makeText(context, "Student removed", Toast.LENGTH_SHORT).show()
                    deletingStudent = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// 5. ASSIGNMENTS ADMIN VIEW
// -----------------------------------------------------------------------------
@Composable
fun AssignmentsAdminView(
    user: User,
    assignments: List<Assignment>,
    subjects: List<Subject>,
    repository: ClassHubRepository
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAssignment by remember { mutableStateOf<Assignment?>(null) }
    var deletingAssignment by remember { mutableStateOf<Assignment?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Assignments (${assignments.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CampuProDesign.TextPrimary
            )
            Button(
                onClick = {
                    if (subjects.isEmpty()) {
                        Toast.makeText(context, "Please add a class first.", Toast.LENGTH_SHORT).show()
                    } else {
                        showAddDialog = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 13.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (assignments.isEmpty()) {
            EmptyStateCard(message = "No assignments currently created.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(assignments) { asgn ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                        border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = asgn.subjectName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CampuProDesign.AccentBlue
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Due: ${asgn.dueDate}",
                                        fontSize = 12.sp,
                                        color = CampuProDesign.TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = asgn.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CampuProDesign.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = asgn.description,
                                    fontSize = 13.sp,
                                    color = CampuProDesign.TextSecondary,
                                    maxLines = 2
                                )
                            }
                            Row {
                                IconButton(onClick = { editingAssignment = asgn }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CampuProDesign.AccentBlue, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { deletingAssignment = asgn }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AssignmentFormDialog(
            title = "Add Assignment",
            subjects = subjects,
            initialSubjectId = subjects.firstOrNull()?.id ?: "",
            initialTitle = "",
            initialDesc = "",
            initialDueDate = "2026-10-15",
            initialPriority = "Medium",
            onDismiss = { showAddDialog = false },
            onSave = { subId, title, desc, due, prio ->
                val res = repository.addAssignmentAdmin(user, title, subId, desc, due, prio)
                if (res.isSuccess) {
                    Toast.makeText(context, "Assignment created!", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    editingAssignment?.let { asgn ->
        AssignmentFormDialog(
            title = "Edit Assignment",
            subjects = subjects,
            initialSubjectId = asgn.subjectId,
            initialTitle = asgn.title,
            initialDesc = asgn.description,
            initialDueDate = asgn.dueDate,
            initialPriority = asgn.priority,
            onDismiss = { editingAssignment = null },
            onSave = { subId, title, desc, due, prio ->
                val res = repository.updateAssignmentAdmin(user, asgn.id, title, subId, desc, due, prio)
                if (res.isSuccess) {
                    Toast.makeText(context, "Assignment updated!", Toast.LENGTH_SHORT).show()
                    editingAssignment = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    deletingAssignment?.let { asgn ->
        ConfirmDeleteDialog(
            title = "Delete Assignment",
            message = "Are you sure you want to delete '${asgn.title}'?",
            onDismiss = { deletingAssignment = null },
            onConfirm = {
                val res = repository.deleteAssignmentAdmin(user, asgn.id)
                if (res.isSuccess) {
                    Toast.makeText(context, "Assignment deleted", Toast.LENGTH_SHORT).show()
                    deletingAssignment = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// 6. EXAMS ADMIN VIEW
// -----------------------------------------------------------------------------
@Composable
fun ExamsAdminView(
    user: User,
    exams: List<Exam>,
    subjects: List<Subject>,
    repository: ClassHubRepository
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<Exam?>(null) }
    var deletingExam by remember { mutableStateOf<Exam?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exams (${exams.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CampuProDesign.TextPrimary
            )
            Button(
                onClick = {
                    if (subjects.isEmpty()) {
                        Toast.makeText(context, "Please add a class first.", Toast.LENGTH_SHORT).show()
                    } else {
                        showAddDialog = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Exam", fontSize = 13.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (exams.isEmpty()) {
            EmptyStateCard(message = "No examinations scheduled.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(exams) { exam ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                        border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = exam.subjectName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CampuProDesign.AccentBlue
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${exam.date} • ${exam.time}",
                                        fontSize = 12.sp,
                                        color = CampuProDesign.TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = exam.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CampuProDesign.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Venue: ${exam.room} • Seat: ${exam.seatNo}",
                                    fontSize = 13.sp,
                                    color = CampuProDesign.TextSecondary
                                )
                            }
                            Row {
                                IconButton(onClick = { editingExam = exam }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CampuProDesign.AccentBlue, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { deletingExam = exam }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ExamFormDialog(
            title = "Add Exam",
            subjects = subjects,
            initialSubjectId = subjects.firstOrNull()?.id ?: "",
            initialTitle = "",
            initialDate = "2026-10-20",
            initialTime = "10:00 AM – 01:00 PM",
            initialRoom = "Auditorium Hall 2",
            initialSeat = "A-01",
            initialSyllabus = "Units 1–4",
            onDismiss = { showAddDialog = false },
            onSave = { subId, title, date, time, rm, seat, syl ->
                val res = repository.addExamAdmin(user, title, subId, date, time, rm, seat, syl)
                if (res.isSuccess) {
                    Toast.makeText(context, "Exam scheduled!", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    editingExam?.let { ex ->
        ExamFormDialog(
            title = "Edit Exam",
            subjects = subjects,
            initialSubjectId = ex.subjectId,
            initialTitle = ex.title,
            initialDate = ex.date,
            initialTime = ex.time,
            initialRoom = ex.room,
            initialSeat = ex.seatNo,
            initialSyllabus = ex.syllabusTopics,
            onDismiss = { editingExam = null },
            onSave = { subId, title, date, time, rm, seat, syl ->
                val res = repository.updateExamAdmin(user, ex.id, title, subId, date, time, rm, seat, syl)
                if (res.isSuccess) {
                    Toast.makeText(context, "Exam updated!", Toast.LENGTH_SHORT).show()
                    editingExam = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    deletingExam?.let { ex ->
        ConfirmDeleteDialog(
            title = "Delete Exam",
            message = "Are you sure you want to remove '${ex.title}'?",
            onDismiss = { deletingExam = null },
            onConfirm = {
                val res = repository.deleteExamAdmin(user, ex.id)
                if (res.isSuccess) {
                    Toast.makeText(context, "Exam deleted", Toast.LENGTH_SHORT).show()
                    deletingExam = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// 7. ANNOUNCEMENTS ADMIN VIEW
// -----------------------------------------------------------------------------
@Composable
fun AnnouncementsAdminView(
    user: User,
    events: List<EventPost>,
    repository: ClassHubRepository
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<EventPost?>(null) }
    var deletingEvent by remember { mutableStateOf<EventPost?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Announcements (${events.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CampuProDesign.TextPrimary
            )
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Broadcast", fontSize = 13.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (events.isEmpty()) {
            EmptyStateCard(message = "No announcements posted.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(events) { ev ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                        border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Posted by ${ev.postedBy} • ${ev.date}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CampuProDesign.AccentBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ev.caption,
                                    fontSize = 15.sp,
                                    color = CampuProDesign.TextPrimary
                                )
                            }
                            Row {
                                IconButton(onClick = { editingEvent = ev }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CampuProDesign.AccentBlue, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { deletingEvent = ev }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AnnouncementFormDialog(
            title = "New Campus Announcement",
            initialCaption = "",
            initialPostedBy = user.fullName,
            onDismiss = { showAddDialog = false },
            onSave = { caption, postedBy ->
                val res = repository.addAnnouncementAdmin(user, caption, postedBy)
                if (res.isSuccess) {
                    Toast.makeText(context, "Announcement posted!", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    editingEvent?.let { ev ->
        AnnouncementFormDialog(
            title = "Edit Announcement",
            initialCaption = ev.caption,
            initialPostedBy = ev.postedBy,
            onDismiss = { editingEvent = null },
            onSave = { caption, _ ->
                val res = repository.updateAnnouncementAdmin(user, ev.id, caption)
                if (res.isSuccess) {
                    Toast.makeText(context, "Announcement updated!", Toast.LENGTH_SHORT).show()
                    editingEvent = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    deletingEvent?.let { ev ->
        ConfirmDeleteDialog(
            title = "Delete Announcement",
            message = "Are you sure you want to delete this announcement?",
            onDismiss = { deletingEvent = null },
            onConfirm = {
                val res = repository.deleteAnnouncementAdmin(user, ev.id)
                if (res.isSuccess) {
                    Toast.makeText(context, "Announcement deleted", Toast.LENGTH_SHORT).show()
                    deletingEvent = null
                } else {
                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// DIALOGS & SHARED COMPONENTS
// -----------------------------------------------------------------------------
@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
        border = BorderStroke(1.dp, CampuProDesign.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = CampuProDesign.TextSecondary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = CampuProDesign.TextSecondary
            )
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = CampuProDesign.TextPrimary) },
        text = { Text(message, color = CampuProDesign.TextSecondary) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CampuProDesign.TextPrimary)
            }
        },
        containerColor = CampuProDesign.CardBackground
    )
}

@Composable
fun SubjectFormDialog(
    title: String,
    initialName: String,
    initialCode: String,
    initialFaculty: String,
    initialRoom: String,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, faculty: String, room: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var code by remember { mutableStateOf(initialCode) }
    var faculty by remember { mutableStateOf(initialFaculty) }
    var room by remember { mutableStateOf(initialRoom) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = CampuProDesign.TextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Class Name (e.g. Data Structures)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code (e.g. BCA301)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = faculty,
                    onValueChange = { faculty = it },
                    label = { Text("Faculty Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room (e.g. Room 204)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, code, faculty, room) },
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CampuProDesign.TextPrimary)
            }
        },
        containerColor = CampuProDesign.CardBackground
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotFormDialog(
    title: String,
    subjects: List<Subject>,
    initialSubjectId: String,
    initialDay: Int,
    initialStartTime: String,
    initialEndTime: String,
    initialRoom: String,
    onDismiss: () -> Unit,
    onSave: (subId: String, day: Int, start: String, end: String, room: String) -> Unit
) {
    var subjectId by remember { mutableStateOf(initialSubjectId) }
    var dayOfWeek by remember { mutableIntStateOf(initialDay) }
    var startTime by remember { mutableStateOf(initialStartTime) }
    var endTime by remember { mutableStateOf(initialEndTime) }
    var room by remember { mutableStateOf(initialRoom) }

    val days = listOf("Monday" to 1, "Tuesday" to 2, "Wednesday" to 3, "Thursday" to 4, "Friday" to 5, "Saturday" to 6)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = CampuProDesign.TextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Class:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CampuProDesign.TextPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { sub ->
                        val isSel = sub.id == subjectId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CampuProDesign.AccentBlue else CampuProDesign.CardBorder)
                                .clickable { subjectId = sub.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(sub.code, color = if (isSel) Color.White else CampuProDesign.TextPrimary, fontSize = 12.sp)
                        }
                    }
                }

                Text("Day of Week:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CampuProDesign.TextPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(days) { (dName, dNum) ->
                        val isSel = dNum == dayOfWeek
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CampuProDesign.AccentBlue else CampuProDesign.CardBorder)
                                .clickable { dayOfWeek = dNum }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(dName.take(3), color = if (isSel) Color.White else CampuProDesign.TextPrimary, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time (e.g. 09:00)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End Time (e.g. 10:00)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room (e.g. Room 204)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(subjectId, dayOfWeek, startTime, endTime, room) },
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CampuProDesign.TextPrimary)
            }
        },
        containerColor = CampuProDesign.CardBackground
    )
}

@Composable
fun UserFormDialog(
    title: String,
    showRollNo: Boolean,
    initialName: String,
    initialRoll: String,
    initialEmail: String,
    onDismiss: () -> Unit,
    onSave: (name: String, roll: String, email: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var roll by remember { mutableStateOf(initialRoll) }
    var email by remember { mutableStateOf(initialEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = CampuProDesign.TextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (showRollNo) {
                    OutlinedTextField(
                        value = roll,
                        onValueChange = { roll = it },
                        label = { Text("Roll Number (e.g. BCA045)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, roll, email) },
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CampuProDesign.TextPrimary)
            }
        },
        containerColor = CampuProDesign.CardBackground
    )
}

@Composable
fun AssignmentFormDialog(
    title: String,
    subjects: List<Subject>,
    initialSubjectId: String,
    initialTitle: String,
    initialDesc: String,
    initialDueDate: String,
    initialPriority: String,
    onDismiss: () -> Unit,
    onSave: (subId: String, title: String, desc: String, due: String, priority: String) -> Unit
) {
    var subjectId by remember { mutableStateOf(initialSubjectId) }
    var assignTitle by remember { mutableStateOf(initialTitle) }
    var desc by remember { mutableStateOf(initialDesc) }
    var dueDate by remember { mutableStateOf(initialDueDate) }
    var priority by remember { mutableStateOf(initialPriority) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = CampuProDesign.TextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Subject:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CampuProDesign.TextPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { sub ->
                        val isSel = sub.id == subjectId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CampuProDesign.AccentBlue else CampuProDesign.CardBorder)
                                .clickable { subjectId = sub.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(sub.code, color = if (isSel) Color.White else CampuProDesign.TextPrimary, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = assignTitle,
                    onValueChange = { assignTitle = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(subjectId, assignTitle, desc, dueDate, priority) },
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CampuProDesign.TextPrimary)
            }
        },
        containerColor = CampuProDesign.CardBackground
    )
}

@Composable
fun ExamFormDialog(
    title: String,
    subjects: List<Subject>,
    initialSubjectId: String,
    initialTitle: String,
    initialDate: String,
    initialTime: String,
    initialRoom: String,
    initialSeat: String,
    initialSyllabus: String,
    onDismiss: () -> Unit,
    onSave: (subId: String, title: String, date: String, time: String, room: String, seat: String, syllabus: String) -> Unit
) {
    var subjectId by remember { mutableStateOf(initialSubjectId) }
    var examTitle by remember { mutableStateOf(initialTitle) }
    var date by remember { mutableStateOf(initialDate) }
    var time by remember { mutableStateOf(initialTime) }
    var room by remember { mutableStateOf(initialRoom) }
    var seat by remember { mutableStateOf(initialSeat) }
    var syllabus by remember { mutableStateOf(initialSyllabus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = CampuProDesign.TextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Subject:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CampuProDesign.TextPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(subjects) { sub ->
                        val isSel = sub.id == subjectId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CampuProDesign.AccentBlue else CampuProDesign.CardBorder)
                                .clickable { subjectId = sub.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(sub.code, color = if (isSel) Color.White else CampuProDesign.TextPrimary, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("Exam Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 10:00 AM – 01:00 PM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Exam Room") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = seat,
                    onValueChange = { seat = it },
                    label = { Text("Seat Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = syllabus,
                    onValueChange = { syllabus = it },
                    label = { Text("Syllabus / Topics") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(subjectId, examTitle, date, time, room, seat, syllabus) },
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CampuProDesign.TextPrimary)
            }
        },
        containerColor = CampuProDesign.CardBackground
    )
}

@Composable
fun AnnouncementFormDialog(
    title: String,
    initialCaption: String,
    initialPostedBy: String,
    onDismiss: () -> Unit,
    onSave: (caption: String, postedBy: String) -> Unit
) {
    var caption by remember { mutableStateOf(initialCaption) }
    var postedBy by remember { mutableStateOf(initialPostedBy) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = CampuProDesign.TextPrimary) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Announcement Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = postedBy,
                    onValueChange = { postedBy = it },
                    label = { Text("Sender / Authority") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(caption, postedBy) },
                colors = ButtonDefaults.buttonColors(containerColor = CampuProDesign.AccentBlue)
            ) {
                Text("Broadcast", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CampuProDesign.TextPrimary)
            }
        },
        containerColor = CampuProDesign.CardBackground
    )
}
