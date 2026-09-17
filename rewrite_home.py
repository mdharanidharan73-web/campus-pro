import re
import os

with open('app/src/main/java/com/example/ui/screens/StudentDashboardScreen.kt', 'w') as f:
    f.write("""package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassHubRepository
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
    val firstName = user.fullName.split(" ").firstOrNull() ?: "Dharanidharan"

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
                        text = "BCA001",
                        fontSize = 14.sp,
                        color = CampuProDesign.TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CampuProDesign.PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstName.take(1).uppercase(),
                        color = Color.White,
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
                        color = CampuProDesign.TextSecondary
                    )
                }
            }
        }

        // 3. White Rounded Card (5 Classes today)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        haptics?.lightImpact()
                        onNavigateToTimetable() 
                    },
                shape = RoundedCornerShape(20.dp),
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
                            .background(CampuProDesign.AccentBlueHighlightFill),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = CampuProDesign.AccentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "5 classes today",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CampuProDesign.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Stay consistent. Keep going!",
                            fontSize = 13.sp,
                            color = CampuProDesign.TextSecondary
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = CampuProDesign.TextSecondary
                    )
                }
            }
        }

        // 4. Featured Dark-Navy Gradient Card ("Next Class")
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CampuProDesign.NavyGradient)
                    .clickable {
                        haptics?.lightImpact()
                        onNavigateToTimetable()
                    }
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Next Class",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "In 1h 20m",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Web Technologies",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "BCA303",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            Icons.Default.LaptopMac,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Prof. Rajesh Sharma",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "10:15 – 11:15 AM",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Room 204",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Quick Actions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.TextPrimary
                )
                Text(
                    text = "See All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = CampuProDesign.AccentBlue,
                    modifier = Modifier.clickable { haptics?.lightImpact() }
                )
            }
        }

        // 6. Row of 4 Icon Tiles
        item {
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

        // 7. Attendance Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptics?.lightImpact()
                        onNavigateToAttendance()
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attendance",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CampuProDesign.TextPrimary
                        )
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = CampuProDesign.TextSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Circular Progress Ring representation
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                color = CampuProDesign.CardBorder,
                                strokeWidth = 8.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                            CircularProgressIndicator(
                                progress = { 0.83f },
                                color = CampuProDesign.SuccessGreen,
                                strokeWidth = 8.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = "83%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = CampuProDesign.TextPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(20.dp))
                        
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "20 of 24 attended",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CampuProDesign.TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(CampuProDesign.SuccessBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Eligible",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CampuProDesign.SuccessText
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "5 safe skips remaining",
                                    fontSize = 12.sp,
                                    color = CampuProDesign.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
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
"""
)
