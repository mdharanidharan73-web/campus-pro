package com.example.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.glass.*
import com.example.ui.haptics.IOSHaptics
import com.example.ui.haptics.LocalIOSHaptics

/**
 * All typed destinations in CampuPro stack-based navigation.
 */
sealed class CampuProScreen {
    // Root level screens
    object Dashboard : CampuProScreen()
    object Classes : CampuProScreen()
    object Schedule : CampuProScreen()
    object Attendance : CampuProScreen()
    object Rooms : CampuProScreen()
    object MoreMenu : CampuProScreen()

    // Sub-screens pushed onto the stack
    data class SubjectDetails(val subjectId: String) : CampuProScreen()
    data class AssignmentDetails(val assignmentId: String, val subjectId: String? = null) : CampuProScreen()
    data class FacultyDetails(val facultyName: String, val subjectCode: String) : CampuProScreen()
    object AssignmentsList : CampuProScreen()
    object ExamsList : CampuProScreen()
    object Profile : CampuProScreen()
    object Settings : CampuProScreen()
    object Roster : CampuProScreen()
    object CRPermissions : CampuProScreen()
    object StretchFeatures : CampuProScreen()

    open val title: String
        get() = when (this) {
            is Dashboard -> "Today"
            is Classes -> "Classes"
            is Schedule -> "Schedule"
            is Attendance -> "Attendance"
            is Rooms -> "Rooms"
            is MoreMenu -> "More"
            is SubjectDetails -> "Subject"
            is AssignmentDetails -> "Assignment"
            is FacultyDetails -> "Faculty"
            is AssignmentsList -> "Assignments"
            is ExamsList -> "Exams"
            is Profile -> "Profile"
            is Settings -> "Settings"
            is Roster -> "Class Roster"
            is CRPermissions -> "CR Controls"
            is StretchFeatures -> "Smart Campus"
        }
}

/**
 * Modals presented over the current screen.
 */
sealed class CampuProModal {
    object AIAssistant : CampuProModal()
}

/**
 * Bottom Navigation Tabs.
 */
enum class CampuProTab(
    val titleStudent: String,
    val titleTeacher: String,
    val iconStudent: ImageVector,
    val iconTeacher: ImageVector
) {
    TODAY("Today", "Today", Icons.Default.Today, Icons.Default.Today),
    CLASSES("Classes", "Attendance", Icons.Default.School, Icons.Default.CheckCircle),
    SCHEDULE("Schedule", "Schedule", Icons.Default.Schedule, Icons.Default.Schedule),
    ATTENDANCE("Attendance", "Rooms", Icons.Default.CheckCircle, Icons.Default.MeetingRoom),
    MORE("More", "Admin", Icons.Default.MoreHoriz, Icons.Default.AdminPanelSettings)
}

/**
 * Centralized, reliable source of truth for Stack-Based Tab Navigation.
 */
class CampuProNavController(
    val isTeacher: Boolean,
    initialTab: CampuProTab = CampuProTab.TODAY,
    private val haptics: IOSHaptics? = null
) {
    var currentTab by mutableStateOf(initialTab)
        private set

    // Independent navigation stack for each tab
    val tabStacks: Map<CampuProTab, SnapshotStateList<CampuProScreen>> = CampuProTab.values().associateWith { tab ->
        mutableStateListOf(getDefaultRoot(tab, isTeacher))
    }

    var activeModal by mutableStateOf<CampuProModal?>(null)
        private set

    val currentStack: SnapshotStateList<CampuProScreen>
        get() = tabStacks.getValue(currentTab)

    val currentScreen: CampuProScreen
        get() = currentStack.last()

    val canPop: Boolean
        get() = currentStack.size > 1

    val previousScreen: CampuProScreen?
        get() = if (currentStack.size > 1) currentStack[currentStack.size - 2] else null

    fun selectTab(tab: CampuProTab) {
        if (currentTab != tab) {
            haptics?.selection()
            currentTab = tab
        }
    }

    fun push(destination: CampuProScreen) {
        haptics?.navigation()
        currentStack.add(destination)
    }

    fun pop(): Boolean {
        if (activeModal != null) {
            haptics?.heavyImpact()
            activeModal = null
            return true
        }
        if (canPop) {
            haptics?.navigation()
            currentStack.removeAt(currentStack.lastIndex)
            return true
        }
        return false
    }

    fun presentModal(modal: CampuProModal) {
        haptics?.heavyImpact()
        activeModal = modal
    }

    fun dismissModal() {
        haptics?.heavyImpact()
        activeModal = null
    }

    companion object {
        fun getDefaultRoot(tab: CampuProTab, isTeacher: Boolean): CampuProScreen = when (tab) {
            CampuProTab.TODAY -> CampuProScreen.Dashboard
            CampuProTab.CLASSES -> if (isTeacher) CampuProScreen.Attendance else CampuProScreen.Classes
            CampuProTab.SCHEDULE -> CampuProScreen.Schedule
            CampuProTab.ATTENDANCE -> if (isTeacher) CampuProScreen.Rooms else CampuProScreen.Attendance
            CampuProTab.MORE -> CampuProScreen.MoreMenu
        }
    }
}

@Composable
fun rememberCampuProNavController(isTeacher: Boolean): CampuProNavController {
    val haptics = LocalIOSHaptics.current
    return remember(isTeacher, haptics) {
        CampuProNavController(isTeacher = isTeacher, initialTab = CampuProTab.TODAY, haptics = haptics)
    }
}

/**
 * Native iOS interactive swipe-from-left-edge gesture for popping the navigation stack.
 */
fun Modifier.iosSwipeBack(
    enabled: Boolean,
    onPop: () -> Unit
): Modifier = composed {
    val haptics = LocalIOSHaptics.current
    val density = LocalDensity.current
    val edgeThreshold = remember(density) { with(density) { 48.dp.toPx() } }
    val popThreshold = remember(density) { with(density) { 85.dp.toPx() } }

    var isStartedAtEdge by remember { mutableStateOf(false) }
    var totalDragX by remember { mutableFloatStateOf(0f) }

    if (!enabled) return@composed this

    pointerInput(enabled) {
        detectHorizontalDragGestures(
            onDragStart = { offset ->
                isStartedAtEdge = offset.x <= edgeThreshold
                totalDragX = 0f
            },
            onHorizontalDrag = { _, dragAmount ->
                if (isStartedAtEdge) {
                    totalDragX += dragAmount
                }
            },
            onDragEnd = {
                if (isStartedAtEdge && totalDragX > popThreshold) {
                    haptics?.mediumImpact()
                    onPop()
                }
                isStartedAtEdge = false
                totalDragX = 0f
            },
            onDragCancel = {
                isStartedAtEdge = false
                totalDragX = 0f
            }
        )
    }
}

/**
 * Standard iOS-style top navigation bar with back chevron, title, and trailing actions.
 * Powered by Apple Liquid Glass material with subtle lensing, specular rim, and ambient shadow.
 */
@Composable
fun IOSNavigationBar(
    title: String,
    subtitle: String? = null,
    backTitle: String? = "Back",
    onBack: (() -> Unit)? = null,
    trailingActions: @Composable (RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    GlassNavigationBar(
        title = title,
        subtitle = subtitle,
        backTitle = backTitle,
        onBack = onBack,
        trailingActions = trailingActions,
        modifier = modifier
    )
}
