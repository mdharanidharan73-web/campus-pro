package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.ClassHubRepository
import com.example.model.User
import com.example.ui.glass.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AIChatMessage(
    val id: String,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: String = "Just now",
    val quickActionTarget: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    user: User,
    repository: ClassHubRepository = ClassHubRepository.instance,
    onBack: (() -> Unit)? = null,
    onNavigateToAttendance: (() -> Unit)? = null,
    onNavigateToTimetable: (() -> Unit)? = null,
    onNavigateToAssignments: (() -> Unit)? = null,
    onNavigateToExams: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val initialMessages = remember {
        listOf(
            AIChatMessage(
                id = "m_welcome",
                sender = "ai",
                text = "Hello Dharanidharan! I am your CampuPro Intelligence Assistant. I can track your schedule, calculate your safe attendance skips, summarize assignments, or help you prep for upcoming exams. What would you like to check today?"
            )
        )
    }

    var messages by remember { mutableStateOf(initialMessages) }

    val suggestedPrompts = listOf(
        "What's my next class?",
        "Show pending assignments.",
        "What exams are coming up?",
        "Calculate my attendance.",
        "What should I study today?"
    )

    fun handleSend(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || isThinking) return

        val userMsg = AIChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = "user",
            text = trimmed
        )
        messages = messages + userMsg
        inputText = ""
        isThinking = true

        coroutineScope.launch {
            delay(200)
            listState.animateScrollToItem(messages.size)
            delay(500) // Realistic thoughtful generation

            val replyText: String
            var target: String? = null

            val lower = trimmed.lowercase()
            when {
                lower.contains("next class") || lower.contains("timetable") || lower.contains("schedule") -> {
                    val next = repository.getNextClass(user.id)
                    replyText = if (next != null) {
                        "Your next class is **${next.subject.name}** with ${next.subject.facultyName.ifBlank { "the professor" }} at **${next.formattedTime}** in **${next.slot.room}** (${next.countdownText ?: "starting soon"})."
                    } else {
                        "You have no remaining classes scheduled for today! You can review tomorrow's timetable in the Schedule tab."
                    }
                    target = "timetable"
                }
                lower.contains("assignment") || lower.contains("homework") || lower.contains("due") -> {
                    val asgns = repository.assignments.value
                    val statuses = repository.assignmentStatuses.value.filter { it.studentId == user.id }
                    val pending = asgns.filter { a -> statuses.find { it.assignmentId == a.id }?.isDone != true }
                    val highPri = pending.filter { it.priority.equals("High", ignoreCase = true) }
                    replyText = "You have **${pending.size} pending assignments**:\n\n" +
                            pending.joinToString("\n") { "• **${it.title}** (${it.subjectName}) — Due: ${it.dueDate} [${it.priority} Priority]" } +
                            if (highPri.isNotEmpty()) "\n\n💡 Focus suggestion: Complete **${highPri.first().title}** first since it has High priority." else ""
                    target = "assignments"
                }
                lower.contains("attendance") || lower.contains("skip") || lower.contains("safe") -> {
                    val summaries = repository.getStudentAttendanceSummaries(user.id)
                    val overallAttended = summaries.sumOf { it.attendedCount }
                    val overallHeld = summaries.sumOf { it.heldCount }
                    val overallPct = if (overallHeld > 0) ((overallAttended.toDouble() / overallHeld) * 100).toInt() else 100
                    val minSafe = summaries.minOfOrNull { it.safeSkips } ?: 3
                    replyText = "Your overall attendance is currently **$overallPct%** ($overallAttended of $overallHeld sessions attended).\n\n" +
                            "• Safe skips remaining: **$minSafe class(es)** minimum across subjects while staying above the mandatory 75% threshold.\n" +
                            "• DBMS: 86% (3 safe skips)\n" +
                            "• Python: 82% (2 safe skips)\n" +
                            "• Web Technologies: 91% (5 safe skips)"
                    target = "attendance"
                }
                lower.contains("exam") || lower.contains("test") || lower.contains("midterm") -> {
                    val exams = repository.exams.value.sortedBy { it.date }
                    val nearest = exams.firstOrNull()
                    replyText = if (nearest != null) {
                        "Your nearest exam is **${nearest.title}** for **${nearest.subjectName} (${nearest.subjectCode})**.\n\n" +
                                "📅 **Date:** ${nearest.date} at ${nearest.time}\n" +
                                "📍 **Venue:** ${nearest.room} (Seat: ${nearest.seatNo})\n" +
                                "📖 **Syllabus:** ${nearest.syllabusTopics}"
                    } else {
                        "No examinations currently scheduled."
                    }
                    target = "exams"
                }
                lower.contains("study") || lower.contains("prep") || lower.contains("review") -> {
                    replyText = "Recommended study plan for today, Dharanidharan:\n\n" +
                            "1. **Database Systems (BCA301)**: Finish the B-Tree Indexing and Normalization queries due soon.\n" +
                            "2. **Midterm Preparation**: Review Unit 1 & 2 Relational Algebra formulas for the upcoming exam on Sep 24.\n" +
                            "3. **Python Lab**: 30 minutes practice on Asyncio producer-consumer coroutines."
                }
                else -> {
                    replyText = "I've logged your request: \"$trimmed\". You can ask me about your class schedule, attendance calculation, upcoming midterms, or pending assignments anytime!"
                }
            }

            val aiMsg = AIChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                sender = "ai",
                text = replyText,
                quickActionTarget = target
            )
            messages = messages + aiMsg
            isThinking = false
            delay(100)
            listState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GlassNavigationBar(
                title = "CampuPro AI",
                subtitle = "Campus Intelligence Engine",
                backTitle = "Done",
                onBack = onBack
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Quick suggested pills with GlassCapsule
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    items(suggestedPrompts) { prompt ->
                        GlassCapsule(
                            text = prompt,
                            onClick = { handleSend(prompt) }
                        )
                    }
                }

                // Liquid Glass floating input bar
                GlassInputBar(
                    inputText = inputText,
                    onInputTextChange = { inputText = it },
                    onSend = { text -> handleSend(text) },
                    onVoiceClick = { handleSend("What's my next class?") },
                    onAttachClick = { handleSend("Summarize my pending assignments") },
                    isThinking = isThinking
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF007AFF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(
                        modifier = Modifier.widthIn(max = 290.dp),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        val bubbleShape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                        Box(
                            modifier = Modifier
                                .clip(bubbleShape)
                                .then(
                                    if (!isUser) Modifier.border(0.5.dp, MaterialTheme.colorScheme.outline, bubbleShape)
                                    else Modifier
                                )
                                .background(
                                    if (isUser) Color(0xFF007AFF)
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Quick link button if applicable
                        if (!isUser && msg.quickActionTarget != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            when (msg.quickActionTarget) {
                                "timetable" -> onNavigateToTimetable?.let { nav ->
                                    FilledTonalButton(
                                        onClick = nav,
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Open Schedule", fontSize = 12.sp)
                                    }
                                }
                                "attendance" -> onNavigateToAttendance?.let { nav ->
                                    FilledTonalButton(
                                        onClick = nav,
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("View Attendance Breakdown", fontSize = 12.sp)
                                    }
                                }
                                "assignments" -> onNavigateToAssignments?.let { nav ->
                                    FilledTonalButton(
                                        onClick = nav,
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("View Assignments", fontSize = 12.sp)
                                    }
                                }
                                "exams" -> onNavigateToExams?.let { nav ->
                                    FilledTonalButton(
                                        onClick = nav,
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("View Exam Schedule", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF007AFF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "CampuPro is thinking...",
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
