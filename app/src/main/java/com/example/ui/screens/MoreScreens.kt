package com.example.ui.screens

import android.widget.Toast
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ReminderHelper
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassHubRepository
import com.example.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance
) {
    val allUsers by repository.allUsers.collectAsState()
    
    val allowlist by repository.rosterAllowlist.collectAsState()
    val enrollments by repository.enrollments.collectAsState()
    val subjects by repository.subjects.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val students = remember(allUsers, searchQuery) {
        allUsers.filter { it.role in listOf("student", "cr") }
            .filter {
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                (it.rollNo ?: "").contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.rollNo }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text("Official Class Roster", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(6.dp))
        Text("BCA 2026 Batch (${allUsers.count { it.role in listOf("student", "cr") }} registered)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name or roll number...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(students) { st ->
                    val enrolledSubIds = enrollments.filter { it.studentId == st.id }.map { it.subjectId }
                    val language = subjects.find { it.id in enrolledSubIds && !it.isCommon }?.name ?: "Elective"

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(st.fullName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    if (st.role == "cr") {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFF9500).copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("CR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9500))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Roll No: ${st.rollNo} • Lang: $language", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Text("Active", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF34C759))
                        }
                        
                        if (st != students.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CRPermissionsScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance
) {
    val context = LocalContext.current
    val allUsers by repository.allUsers.collectAsState()
    
    val subjects by repository.subjects.collectAsState()
    val crPermissions by repository.crPermissions.collectAsState()

    val crStudents = allUsers.filter { it.role == "cr" }
    var selectedStudent by remember { mutableStateOf(crStudents.firstOrNull()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text("CR Scoped Delegations", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Delegate subject-specific attendance & moderation permissions to CRs", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        if (crStudents.isEmpty()) {
            Text("No CR students designated.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                crStudents.forEach { cr ->
                    item {
                        Text(
                            text = "${cr.fullName} (${cr.rollNo})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column {
                                subjects.forEachIndexed { index, sub ->
                                    val perm = crPermissions.find { it.userId == cr.id && it.subjectId == sub.id }
                                    val canMark = perm?.canMarkAttendance == true
                                    val canMod = perm?.canModerateRoom == true

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(sub.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Mark Attendance", style = MaterialTheme.typography.bodyMedium)
                                            Switch(
                                                checked = canMark,
                                                onCheckedChange = {
                                                    if (user.role == "teacher") {
                                                        repository.updateCRPermission(cr.id, sub.id, it, canMod)
                                                        Toast.makeText(context, "Attendance permission updated", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF34C759))
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Moderate Room", style = MaterialTheme.typography.bodyMedium)
                                            Switch(
                                                checked = canMod,
                                                onCheckedChange = {
                                                    if (user.role == "teacher") {
                                                        repository.updateCRPermission(cr.id, sub.id, canMark, it)
                                                        Toast.makeText(context, "Mod permission updated", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF34C759))
                                            )
                                        }
                                    }

                                    if (index < subjects.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 16.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
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

@Composable
fun StretchFeaturesScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance
) {
    val context = LocalContext.current
    val assignments by repository.assignments.collectAsState()
    val assignmentStatuses by repository.assignmentStatuses.collectAsState()
    val resources by repository.resources.collectAsState()
    val subjects by repository.subjects.collectAsState()
    val allUsers by repository.allUsers.collectAsState()
    
    var assignmentToRemind by remember { mutableStateOf<com.example.model.Assignment?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted && assignmentToRemind != null) {
            ReminderHelper.scheduleAssignmentReminder(
                context,
                assignmentToRemind!!.id,
                assignmentToRemind!!.title,
                assignmentToRemind!!.subjectName,
                assignmentToRemind!!.dueDate
            )
            Toast.makeText(context, "Reminder Set!", Toast.LENGTH_SHORT).show()
            assignmentToRemind = null
        } else if (!isGranted) {
            Toast.makeText(context, "Permission denied for reminders.", Toast.LENGTH_SHORT).show()
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    var showAddAssignment by remember { mutableStateOf(false) }
    var showAddResource by remember { mutableStateOf(false) }
    var reviewAssignmentId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            if (user.role == "teacher") {
                FloatingActionButton(
                    onClick = {
                        if (selectedTab == 0) showAddAssignment = true else showAddResource = true
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Assignments") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Resource Shelf") })
            }

            if (selectedTab == 0) {
                LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Phase 5: Assignment Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Track pending subject assignments. Students have private Done/Not Done status.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                items(assignments) { asgn ->
                    val status = assignmentStatuses.find { it.assignmentId == asgn.id && it.studentId == user.id }
                    val isDone = status?.isDone == true

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(asgn.subjectName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Due: ${asgn.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(asgn.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(asgn.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            if (user.role in listOf("student", "cr")) {
                                if (isDone) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Column {
                                            Text("Submitted on ${status?.submittedAt ?: "N/A"}", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                            Text("File: ${status?.submissionFile ?: "No file"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (status?.grade != null) {
                                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE8F5E9)).padding(6.dp)) {
                                                Text("Grade: ${status.grade}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                    if (status?.feedback != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Feedback: ${status.feedback}", fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                } else {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        OutlinedButton(
                                            onClick = {
                                                assignmentToRemind = asgn
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    ReminderHelper.scheduleAssignmentReminder(context, asgn.id, asgn.title, asgn.subjectName, asgn.dueDate)
                                                    Toast.makeText(context, "Reminder Set!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Remind", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { 
                                                repository.submitAssignment(asgn.id, user.id, "submission_${user.rollNo}.pdf")
                                                Toast.makeText(context, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Upload File", fontSize = 12.sp)
                                        }
                                    }
                                }
                            } else if (user.role == "teacher") {
                                val submissionCount = assignmentStatuses.count { it.assignmentId == asgn.id && it.isDone }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("$submissionCount students submitted", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    OutlinedButton(
                                        onClick = { reviewAssignmentId = asgn.id },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Review Submissions", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Phase 5: Resource Shelf", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Subject notes, PPT handouts, and lab manuals.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                items(resources) { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (res.fileType) {
                                                "PDF" -> Color(0xFFFFEBEE)
                                                "PPT" -> Color(0xFFFFF3E0)
                                                else -> Color(0xFFE3F2FD)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        res.fileType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = when (res.fileType) {
                                            "PDF" -> Color(0xFFC62828)
                                            "PPT" -> Color(0xFFE65100)
                                            else -> Color(0xFF1565C0)
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(res.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("${res.subjectName} • Added on ${res.dateAdded}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            IconButton(onClick = {
                                Toast.makeText(context, "Opening ${res.title}...", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Download")
                            }
                        }
                    }
                }
            }
        }
    }
    } // Close Scaffold

    if (showAddAssignment) {
        AddAssignmentDialog(
            subjects = subjects,
            onDismiss = { showAddAssignment = false },
            onSave = { title, subId, desc, due ->
                repository.addAssignment(title, subId, desc, due)
                showAddAssignment = false
                Toast.makeText(context, "Assignment Added", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddResource) {
        AddResourceDialog(
            subjects = subjects,
            onDismiss = { showAddResource = false },
            onSave = { title, subId, type ->
                repository.addResource(title, subId, type)
                showAddResource = false
                Toast.makeText(context, "Resource Uploaded", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (reviewAssignmentId != null) {
        ReviewSubmissionsDialog(
            assignment = assignments.find { it.id == reviewAssignmentId },
            allStatuses = assignmentStatuses.filter { it.assignmentId == reviewAssignmentId },
            allUsers = allUsers,
            onDismiss = { reviewAssignmentId = null },
            onGrade = { studentId, grade, feedback ->
                repository.gradeAssignment(reviewAssignmentId!!, studentId, grade, feedback)
                Toast.makeText(context, "Grade saved", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentDialog(
    subjects: List<com.example.model.Subject>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Assignment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = subjects.find { it.id == selectedSubjectId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        subjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name) },
                                onClick = {
                                    selectedSubjectId = sub.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (e.g. 2026-10-15)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && selectedSubjectId.isNotBlank()) onSave(title, selectedSubjectId, desc, dueDate) }
            ) { Text("Publish") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResourceDialog(
    subjects: List<com.example.model.Subject>,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
    var fileType by remember { mutableStateOf("PDF") }
    var expandedSub by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    val fileTypes = listOf("PDF", "PPT", "DOC")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Resource") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Resource Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = expandedSub,
                    onExpandedChange = { expandedSub = !expandedSub }
                ) {
                    OutlinedTextField(
                        value = subjects.find { it.id == selectedSubjectId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSub) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSub,
                        onDismissRequest = { expandedSub = false }
                    ) {
                        subjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name) },
                                onClick = {
                                    selectedSubjectId = sub.id
                                    expandedSub = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = fileType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("File Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        fileTypes.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    fileType = t
                                    expandedType = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && selectedSubjectId.isNotBlank()) onSave(title, selectedSubjectId, fileType) }
            ) { Text("Upload") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SettingsScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance
) {
    val context = LocalContext.current
    val settings by repository.settings.collectAsState()
    var minAttendanceText by remember(settings) { mutableStateOf(settings.minAttendancePercent.toString()) }
    var semesterEndText by remember(settings) { mutableStateOf(settings.semesterEndDate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("App Settings & Policies", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("College Class Configuration", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Attendance Threshold Policy", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                OutlinedTextField(
                    value = minAttendanceText,
                    onValueChange = { minAttendanceText = it },
                    label = { Text("Minimum Attendance %") },
                    enabled = user.role == "teacher",
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = semesterEndText,
                    onValueChange = { semesterEndText = it },
                    label = { Text("Semester End Date (YYYY-MM-DD)") },
                    enabled = user.role == "teacher",
                    modifier = Modifier.fillMaxWidth()
                )

                if (user.role == "teacher") {
                    Button(
                        onClick = {
                            val newPct = minAttendanceText.toIntOrNull() ?: 75
                            repository.updateSettings(newPct, semesterEndText)
                            Toast.makeText(context, "Settings updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Configuration")
                    }
                } else {
                    Text(
                        "Note: Settings can only be modified by faculty members.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewSubmissionsDialog(
    assignment: com.example.model.Assignment?,
    allStatuses: List<com.example.model.AssignmentStatus>,
    allUsers: List<com.example.model.User>,
    onDismiss: () -> Unit,
    onGrade: (String, String, String) -> Unit
) {
    if (assignment == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submissions: ${assignment.title}") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val submittedUsers = allUsers.filter { user -> allStatuses.any { it.studentId == user.id && it.isDone } }
                if (submittedUsers.isEmpty()) {
                    item { Text("No submissions yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                items(submittedUsers) { user ->
                    val status = allStatuses.find { it.studentId == user.id }
                    var grade by remember { mutableStateOf(status?.grade ?: "") }
                    var feedback by remember { mutableStateOf(status?.feedback ?: "") }
                    var isEditing by remember { mutableStateOf(status?.grade == null) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(user.fullName, fontWeight = FontWeight.Bold)
                                Text(user.rollNo ?: "N/A", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("File: ${status?.submissionFile ?: "Unknown"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Date: ${status?.submittedAt ?: "Unknown"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            if (isEditing) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = grade,
                                        onValueChange = { grade = it },
                                        label = { Text("Grade") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = feedback,
                                        onValueChange = { feedback = it },
                                        label = { Text("Feedback") },
                                        modifier = Modifier.weight(2f)
                                    )
                                }
                                Button(
                                    onClick = { 
                                        onGrade(user.id, grade, feedback)
                                        isEditing = false
                                    },
                                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                                ) {
                                    Text("Save Grade")
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        Text("Grade: ${status?.grade}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        if (!status?.feedback.isNullOrEmpty()) {
                                            Text("Feedback: ${status?.feedback}", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 12.sp)
                                        }
                                    }
                                    TextButton(onClick = { isEditing = true }) { Text("Edit") }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
