package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.BuildConfig

data class ChatMessage(val text: String, val isUser: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAssistantDialog(
    onDismiss: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf(ChatMessage("Hi! I'm your AI Assistant. How can I help you?", false))) }
    var currentInput by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Assistant", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                // Chat Messages
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    if (isTyping) {
                        item {
                            Text("AI is typing...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Input Area
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask something...") },
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (currentInput.isNotBlank()) {
                                val userMsg = ChatMessage(currentInput, true)
                                messages = messages + userMsg
                                val prompt = currentInput
                                currentInput = ""
                                isTyping = true
                                coroutineScope.launch {
                                    try {
                                        val req = GenerateContentRequest(
                                            contents = listOf(Content(parts = listOf(Part(text = prompt))))
                                        )
                                        val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, req)
                                        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "I couldn't understand that."
                                        messages = messages + ChatMessage(text, false)
                                    } catch (e: Exception) {
                                        messages = messages + ChatMessage("Error connecting to AI.", false)
                                    } finally {
                                        isTyping = false
                                    }
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}
