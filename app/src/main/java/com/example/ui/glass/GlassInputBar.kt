package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Section 19: AI INPUT AREA
 *
 * Floating Liquid Glass input bar for CampuPro AI Assistant:
 * - Floating glass surface with subtle translucency and dynamic lensing
 * - Voice button, attachment button, send button, text field
 * - Interactive touch response and subtle illumination flex
 */
@Composable
fun GlassInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onVoiceClick: () -> Unit,
    onAttachClick: () -> Unit,
    isThinking: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = LiquidGlassTokens.isAppDark()
    val brandBlue = if (isDark) LiquidGlassTokens.BrandBlueDark else LiquidGlassTokens.BrandBlue
    val iconMuted = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(26.dp),
                    variant = GlassVariant.REGULAR,
                    elevation = 10.dp,
                    borderWidth = 1.dp,
                    interactive = true
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment Button
            IconButton(
                onClick = onAttachClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach Document",
                    tint = iconMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Voice Button
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Dictation",
                    tint = brandBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Text Input Field
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "Ask CampuPro AI...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send Button with animated presence and spring tactile feedback
            val canSend = inputText.isNotBlank() && !isThinking
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .liquidGlass(
                        shape = CircleShape,
                        variant = GlassVariant.REGULAR,
                        tint = if (canSend) brandBlue else brandBlue.copy(alpha = 0.35f),
                        elevation = if (canSend) 4.dp else 0.dp,
                        interactive = canSend,
                        onClick = if (canSend) { { onSend(inputText) } } else null
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
