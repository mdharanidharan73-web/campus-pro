package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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

/**
 * Section 10 & 14: CAMPUPRO NAVIGATION HEADER
 *
 * Floating Liquid Glass top header for CampuPro.
 * - Floats above content with subtle lensing boundary
 * - Concentric geometry and soft ambient shadow
 * - Displays brand mark, role capsule, and interactive profile switcher
 */
@Composable
fun GlassHeader(
    currentUser: User?,
    onSwitchRole: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LiquidGlassTokens.isAppDark()
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                variant = GlassVariant.REGULAR,
                elevation = 8.dp,
                borderWidth = 0.5.dp
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) LiquidGlassTokens.BrandBlueDark else LiquidGlassTokens.BrandBlue),
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

            // Right Actions: Role Badge & Profile Menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentUser != null) {
                    val (badgeText, badgeBg, badgeTextColor) = when (currentUser.role) {
                        "teacher" -> Triple("FACULTY", Color(0xFF34C759).copy(alpha = 0.18f), Color(0xFF34C759))
                        "cr" -> Triple("CR", Color(0xFFFF9500).copy(alpha = 0.18f), Color(0xFFFF9500))
                        else -> Triple(currentUser.rollNo ?: "STUDENT", Color(0xFF007AFF).copy(alpha = 0.18f), if (isDark) LiquidGlassTokens.BrandBlueDark else LiquidGlassTokens.BrandBlue)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
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

                // Profile Avatar & Morphing Dropdown Menu
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .liquidGlass(
                                shape = CircleShape,
                                variant = GlassVariant.CLEAR,
                                elevation = 2.dp,
                                interactive = true,
                                onClick = { showMenu = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0x350A84FF) else Color(0x25007AFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.fullName?.take(1)?.uppercase() ?: "U",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDark) LiquidGlassTokens.BrandBlueDark else LiquidGlassTokens.BrandBlue
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xF01C1E24) else Color(0xF0FFFFFF))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Switch to Student") },
                            onClick = {
                                onSwitchRole("student")
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = LiquidGlassTokens.BrandBlue) }
                        )
                        DropdownMenuItem(
                            text = { Text("Switch to Faculty") },
                            onClick = {
                                onSwitchRole("teacher")
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF34C759)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Switch to CR") },
                            onClick = {
                                onSwitchRole("cr")
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF9500)) }
                        )
                        HorizontalDivider(color = if (isDark) Color(0x20FFFFFF) else Color(0x15000000))
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
    }
}
