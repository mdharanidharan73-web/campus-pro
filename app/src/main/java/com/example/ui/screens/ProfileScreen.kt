package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.ui.haptics.LocalIOSHaptics
import com.example.ui.theme.CampuProDesign

@Composable
fun ProfileScreen(
    user: User,
    repository: com.example.data.ClassHubRepository = com.example.data.ClassHubRepository.instance,
    onLogout: () -> Unit = {},
    onSwitchRole: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val haptics = LocalIOSHaptics.current
    val firstName = user.fullName.split(" ").firstOrNull() ?: "Dharanidharan"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CampuProDesign.AppBackground),
        contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = CampuProDesign.TextPrimary,
                        modifier = Modifier.size(20.dp).offset(x = 4.dp)
                    )
                }
                Text(
                    text = "Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.TextPrimary
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { haptics?.lightImpact() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CampuProDesign.TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 2. Avatar & Name
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(CampuProDesign.PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = firstName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CampuProDesign.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BCA001",
                    fontSize = 14.sp,
                    color = CampuProDesign.TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Bachelor of Computer Applications",
                    fontSize = 13.sp,
                    color = CampuProDesign.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 3. Info Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip("BCA", "Programme")
                InfoChip("2nd Year", "Academic Year")
                InfoChip("Section A", "Section")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 4. Menu List
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CampuProDesign.CardBackground),
                border = BorderStroke(1.dp, CampuProDesign.CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    MenuRow(Icons.Default.PersonOutline, "Personal Information")
                    HorizontalDivider(color = CampuProDesign.CardBorder, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    MenuRow(Icons.Default.School, "Academic Details")
                    HorizontalDivider(color = CampuProDesign.CardBorder, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    MenuRow(Icons.Default.CheckCircleOutline, "Attendance Overview")
                    HorizontalDivider(color = CampuProDesign.CardBorder, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    MenuRow(Icons.Default.StarOutline, "Exam Performance")
                    HorizontalDivider(color = CampuProDesign.CardBorder, thickness = 1.dp, modifier = Modifier.padding(start = 56.dp))
                    MenuRow(Icons.Default.Settings, "Settings")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // 5. Quote Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CampuProDesign.AccentBlueHighlightFill)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = CampuProDesign.AccentBlue.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Discipline today creates freedom tomorrow.",
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        color = CampuProDesign.TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 30.dp, height = 3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(CampuProDesign.AccentBlue)
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.InfoChip(value: String, label: String) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CampuProDesign.CardBorder.copy(alpha = 0.5f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CampuProDesign.TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = CampuProDesign.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MenuRow(icon: ImageVector, label: String) {
    val haptics = LocalIOSHaptics.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { haptics?.lightImpact() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CampuProDesign.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = CampuProDesign.TextPrimary
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = CampuProDesign.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
