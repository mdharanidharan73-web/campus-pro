package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.model.User
import com.example.ui.glass.GlassSegmentedControl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassHubHeader(
    currentUser: User?,
    onSwitchRole: (String) -> Unit,
    onLogout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0xFF007AFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "CampuPro Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CampuPro",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.3).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Role Badge
                if (currentUser != null) {
                    val (badgeText, badgeBg, badgeTextColor) = when (currentUser.role) {
                        "teacher" -> Triple("FACULTY", Color(0xFF34C759).copy(alpha = 0.15f), Color(0xFF34C759))
                        "cr" -> Triple("CR", Color(0xFFFF9500).copy(alpha = 0.15f), Color(0xFFFF9500))
                        else -> Triple(currentUser.rollNo ?: "STUDENT", Color(0xFF007AFF).copy(alpha = 0.15f), Color(0xFF007AFF))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeTextColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Profile / Switcher Dropdown
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF007AFF).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.fullName?.firstOrNull() ?: 'U').toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF007AFF)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("🎓 Student (Dharanidharan)") },
                            onClick = {
                                onSwitchRole("student")
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF007AFF)) }
                        )
                        DropdownMenuItem(
                            text = { Text("⭐ CR (Priya)") },
                            onClick = {
                                onSwitchRole("cr")
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF9500)) }
                        )
                        DropdownMenuItem(
                            text = { Text("👨‍🏫 Teacher (Prof. Sharma)") },
                            onClick = {
                                onSwitchRole("teacher")
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = Color(0xFF34C759)) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 0.5.dp)
                        DropdownMenuItem(
                            text = { Text("Sign Out", color = Color(0xFFFF3B30)) },
                            onClick = {
                                onLogout()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFFF3B30)) }
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 0.5.dp)
    }
}

@Composable
fun IOSSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassSegmentedControl(
        items = items,
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier
    )
}

@Composable
fun IOSSectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF007AFF),
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
fun IOSGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun AttendanceTrendGraph(trend: List<Int>, isWarning: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        trend.forEachIndexed { index, pct ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$pct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (pct < 75) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (pct < 75) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(pct / 100f)
                            .background(
                                if (pct < 75) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Wk ${index + 1}",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EventPostCard(event: com.example.model.EventPost) {
    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
            if (event.imageUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = event.imageUrl,
                    contentDescription = "Event Image",
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.Default.Event,
                        contentDescription = "Event Icon",
                        modifier = androidx.compose.ui.Modifier.size(48.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.padding(14.dp)) {
                androidx.compose.material3.Text(
                    text = event.caption,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    fontSize = 14.sp
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    androidx.compose.material3.Text(
                        text = "Posted by: ${event.postedBy}",
                        fontSize = 11.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.material3.Text(
                        text = event.date,
                        fontSize = 11.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
