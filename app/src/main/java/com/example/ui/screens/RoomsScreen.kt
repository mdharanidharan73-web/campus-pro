package com.example.ui.screens

import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.model.Message
import com.example.model.Room
import com.example.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance
) {
    val context = LocalContext.current
    val rooms by repository.rooms.collectAsState()
    val allMessages by repository.messages.collectAsState()
    val subjects by repository.subjects.collectAsState()
    val enrollments by repository.enrollments.collectAsState()

    // Filter accessible rooms: Global + enrolled subjects + DMs
    val visibleRooms = remember(rooms, enrollments, user) {
        if (user.role == "teacher") {
            rooms
        } else {
            val enrolledSubIds = enrollments.filter { it.studentId == user.id }.map { it.subjectId }
            rooms.filter { room ->
                room.type == "global" ||
                (room.type == "subject" && room.subjectId in enrolledSubIds) ||
                (room.type == "dm" && (room.participantIds.isEmpty() || user.id in room.participantIds))
            }
        }
    }

    var activeRoom by remember { mutableStateOf(visibleRooms.firstOrNull()) }

    // If active room not selected yet, pick first visible
    LaunchedEffect(visibleRooms) {
        if (activeRoom == null && visibleRooms.isNotEmpty()) {
            activeRoom = visibleRooms.first()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Room selection horizontal chips
        ScrollableTabRow(
            selectedTabIndex = visibleRooms.indexOf(activeRoom).coerceAtLeast(0),
            edgePadding = 12.dp
        ) {
            visibleRooms.forEach { room ->
                Tab(
                    selected = activeRoom?.id == room.id,
                    onClick = { activeRoom = room },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (room.type) {
                                    "global" -> Icons.Default.Campaign
                                    "dm" -> Icons.Default.Forum
                                    else -> Icons.Default.School
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(room.name, fontSize = 12.sp)
                        }
                    }
                )
            }
        }

        activeRoom?.let { currentRoom ->
            ChatView(
                room = currentRoom,
                currentUser = user,
                messages = allMessages.filter { it.roomId == currentRoom.id },
                onSendMessage = { text, isAnon ->
                    val res = repository.sendMessage(currentRoom.id, user.id, text, isAnon)
                    if (res.isFailure) {
                        Toast.makeText(context, res.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                    }
                },
                onDeleteMessage = { msgId ->
                    val res = repository.softDeleteMessage(msgId, user.id, currentRoom.subjectId)
                    if (res.isSuccess) {
                        Toast.makeText(context, "Message soft-deleted by moderator.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, res.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                    }
                },
                onTogglePin = { msgId ->
                    val res = repository.togglePinMessage(msgId, user.id)
                    if (res.isSuccess) {
                        Toast.makeText(context, "Announcement pin status updated.", Toast.LENGTH_SHORT).show()
                    }
                },
                canModerate = repository.canModerateRoom(user.id, currentRoom.subjectId)
            )
        }
    }
}

@Composable
fun ChatView(
    room: Room,
    currentUser: User,
    messages: List<Message>,
    onSendMessage: (String, Boolean) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    canModerate: Boolean
) {
    var messageText by remember { mutableStateOf("") }
    var isAnonymousDoubt by remember { mutableStateOf(false) }

    val pinnedMessages = messages.filter { it.isPinned && it.deletedAt == null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Pinned Global Notices Banner (Page 28)
        if (pinnedMessages.isNotEmpty()) {
            pinnedMessages.forEach { pin ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PushPin, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ANNOUNCEMENT (${pin.senderName})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57F17)
                            )
                            Text(
                                text = pin.content,
                                fontSize = 12.sp,
                                color = Color(0xFF37474F)
                            )
                        }
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMyMessage = msg.senderId == currentUser.id
                val isDeleted = msg.deletedAt != null

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isDeleted -> MaterialTheme.colorScheme.surfaceVariant
                                isMyMessage -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Sender name display
                                val displayName = if (msg.isAnonymous) {
                                    if (currentUser.role == "teacher") "Anonymous (${msg.senderName})" else "Anonymous Student"
                                } else {
                                    msg.senderName
                                }

                                Text(
                                    text = displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.isAnonymous) Color(0xFF6A1B9A) else MaterialTheme.colorScheme.onSurface
                                )

                                if (msg.isAnonymous) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEDE7F6))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("DOUBT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                                    }
                                }

                                Text(
                                    text = msg.createdAt.takeLast(5),
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (isDeleted) {
                                Text(
                                    text = "This message was removed by moderator.",
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = msg.content,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Moderator Actions (Teachers / Authorized CR)
                            if (canModerate && !isDeleted) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (currentUser.role == "teacher") {
                                        IconButton(
                                            onClick = { onTogglePin(msg.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.PushPin,
                                                contentDescription = "Pin Announcement",
                                                tint = if (msg.isPinned) Color(0xFFF57F17) else Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteMessage(msg.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Soft Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Anonymous toggle (Page 25 & 26)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isAnonymousDoubt,
                    onCheckedChange = { isAnonymousDoubt = it }
                )
                Text(
                    text = "Send as Anonymous Doubt",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAnonymousDoubt) Color(0xFF6A1B9A) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isAnonymousDoubt) {
                Text(
                    text = "Name hidden from classmates",
                    fontSize = 10.sp,
                    color = Color(0xFF6A1B9A)
                )
            }
        }

        // Input and Send button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text(if (isAnonymousDoubt) "Ask doubt anonymously..." else "Type message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText.trim(), isAnonymousDoubt)
                        messageText = ""
                        isAnonymousDoubt = false
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}
