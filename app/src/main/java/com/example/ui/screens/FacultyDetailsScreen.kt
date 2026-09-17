package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
fun FacultyDetailsScreen(
    facultyName: String,
    subjectCode: String,
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance,
    backTitle: String = "Back",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val subjects by repository.subjects.collectAsState()
    val matchingSubjects = subjects.filter { it.facultyName.equals(facultyName, ignoreCase = true) || it.code.equals(subjectCode, ignoreCase = true) }

    val email = "${facultyName.lowercase().replace(" ", ".").replace("dr.", "")}@university.edu"

    Scaffold(
        topBar = {
            IOSNavigationBar(
                title = "Faculty Profile",
                subtitle = "Department of Computer Science",
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
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF007AFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = facultyName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = facultyName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Senior Professor & Course Chair",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { Toast.makeText(context, "Email copied: $email", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email", fontSize = 13.sp)
                            }

                            FilledTonalButton(
                                onClick = { Toast.makeText(context, "Office location: Academic Block B, Room 302", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Office", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Office Hours & Availability
            item {
                IOSSectionHeader(title = "Office Hours & Consultation")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Monday & Wednesday", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("2:00 PM – 4:00 PM (Walk-in)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 0.5.dp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Friday by Appointment", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text("10:00 AM – 12:00 PM (Prior email required)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Courses Taught
            item {
                IOSSectionHeader(title = "Courses Taught This Semester")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (matchingSubjects.isEmpty()) {
                            Text("Primary Course: $subjectCode", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        } else {
                            matchingSubjects.forEach { sub ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = sub.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${sub.code} • Room ${sub.room}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF007AFF).copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(text = "Enrolled", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007AFF))
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
