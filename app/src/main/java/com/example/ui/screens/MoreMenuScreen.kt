package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import com.example.model.User
import com.example.ui.theme.CampuProDesign

@Composable
fun MoreMenuScreen(
    user: User,
    onNavigate: (String) -> Unit
) {
    val isTeacher = user.role == "teacher"
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CampuProDesign.AppBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "More",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.TextPrimary
                )
                Text(
                    text = "Everything you need in one place.",
                    fontSize = 15.sp,
                    color = CampuProDesign.TextSecondary
                )
            }
        }
        
        // Compact Profile Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("profile") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(CampuProDesign.PrimaryGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = CampuProDesign.TextPrimary
                        )
                        val subText = if (isTeacher) "Faculty" else "Student - ${user.rollNo ?: ""}"
                        Text(
                            text = subText,
                            fontSize = 14.sp,
                            color = CampuProDesign.TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Go to Profile",
                        tint = CampuProDesign.TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // QUICK ACCESS
        item {
            IOSSection(title = "QUICK ACCESS") {
                IOSRow(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    title = "Assignments",
                    onClick = { onNavigate("assignments") }
                )
                IOSRow(
                    icon = Icons.Filled.DateRange,
                    title = "Exams",
                    onClick = { onNavigate("exams") }
                )
                IOSRow(
                    icon = Icons.Filled.Person,
                    title = "Profile",
                    onClick = { onNavigate("profile") }
                )
                IOSRow(
                    icon = Icons.Filled.Settings,
                    title = "Settings",
                    onClick = { onNavigate("settings") },
                    showDivider = false
                )
            }
        }
        
        item {
            IOSSection(title = "CAMPUS") {
                IOSRow(
                    icon = Icons.Filled.MeetingRoom,
                    title = "Rooms",
                    onClick = { onNavigate("rooms") }
                )
                if (isTeacher || user.role == "cr") {
                    IOSRow(
                        icon = Icons.Filled.PeopleAlt,
                        title = "Class Roster",
                        onClick = { onNavigate("roster") }
                    )
                    IOSRow(
                        icon = Icons.Filled.Key,
                        title = "CR Permissions",
                        onClick = { onNavigate("cr_permissions") }
                    )
                }
                IOSRow(
                    icon = Icons.Filled.Lightbulb,
                    title = "Smart Campus",
                    onClick = { onNavigate("stretch") },
                    showDivider = false
                )
            }
        }
    }
}

@Composable
fun IOSSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CampuProDesign.TextSecondary,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun IOSRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CampuProDesign.AccentBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 17.sp,
                color = CampuProDesign.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = CampuProDesign.TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 56.dp),
                thickness = 1.dp,
                color = CampuProDesign.CardBorder
            )
        }
    }
}
