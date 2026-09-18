package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.haptics.LocalIOSHaptics
import com.example.ui.theme.CampuProDesign

@Composable
fun TimetableScreen(user: com.example.model.User, repository: com.example.data.ClassHubRepository = com.example.data.ClassHubRepository.instance) {
    val haptics = LocalIOSHaptics.current
    val days = listOf("Mon 27", "Tue 28", "Wed 29", "Thu 30", "Fri 31")
    var selectedDay by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CampuProDesign.AppBackground),
        contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp)
    ) {
        // 1. Header
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Today's Schedule",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your classes for today",
                    fontSize = 14.sp,
                    color = CampuProDesign.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. Day Selector
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days.size) { index ->
                    val isSelected = index == selectedDay
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .then(if (isSelected) Modifier.background(CampuProDesign.PrimaryGradient) else Modifier.background(Color.Transparent))
                            .clickable {
                                haptics?.selection()
                                selectedDay = index
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = days[index],
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else CampuProDesign.TextSecondary
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Transparent)
                            .clickable { haptics?.lightImpact() }
                            .padding(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = CampuProDesign.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. Class Cards
        item {
            val timetableSlots by repository.timetableSlots.collectAsState()
            val subjects by repository.subjects.collectAsState()
            val dayOfWeekNum = selectedDay + 1
            val daySlots = timetableSlots.filter { it.dayOfWeek == dayOfWeekNum }.sortedBy { it.startTime }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (daySlots.isEmpty()) {
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
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = CampuProDesign.TextSecondary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No classes scheduled for this day",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = CampuProDesign.TextSecondary
                            )
                        }
                    }
                } else {
                    daySlots.forEachIndexed { idx, slot ->
                        val subject = subjects.find { it.id == slot.subjectId }
                        val accentColor = when (idx % 4) {
                            0 -> CampuProDesign.AccentBlue
                            1 -> CampuProDesign.AccentViolet
                            2 -> CampuProDesign.AccentGreen
                            else -> CampuProDesign.AccentMagenta
                        }
                        ClassCard(
                            startTime = slot.startTime,
                            endTime = slot.endTime,
                            title = subject?.name ?: "Scheduled Class",
                            code = subject?.code ?: "CSE",
                            room = slot.room,
                            accentColor = accentColor,
                            icon = Icons.Default.School,
                            isHighlighted = idx == 0
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. Promo Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CampuProDesign.NavyGradient)
                    .clickable { haptics?.lightImpact() }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Small steps greater futures.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Keep learning",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ClassCard(
    startTime: String,
    endTime: String,
    title: String,
    code: String,
    room: String,
    accentColor: Color,
    icon: ImageVector,
    isHighlighted: Boolean
) {
    val haptics = LocalIOSHaptics.current
    val bgColor = if (isHighlighted) CampuProDesign.AccentBlueHighlightFill else CampuProDesign.CardBackground
    val titleColor = if (isHighlighted) CampuProDesign.AccentBlue else CampuProDesign.TextPrimary
    val borderColor = if (isHighlighted) CampuProDesign.AccentBlue.copy(alpha = 0.3f) else CampuProDesign.CardBorder

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { haptics?.lightImpact() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            // Left Accent Bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(accentColor)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Stack
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(50.dp)
                ) {
                    Text(
                        text = startTime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CampuProDesign.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = endTime,
                        fontSize = 11.sp,
                        color = CampuProDesign.TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$code • $room",
                        fontSize = 12.sp,
                        color = CampuProDesign.TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
