package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("roll_no") val rollNo: String? = null,
    val role: String, // "teacher", "cr", "student"
    val email: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class RosterAllowlist(
    @SerialName("roll_no") val rollNo: String,
    @SerialName("full_name") val fullName: String,
    val claimed: Boolean = false
)

@Serializable
data class Subject(
    val id: String,
    val name: String,
    @SerialName("faculty_id") val facultyId: String? = null,
    val facultyName: String = "",
    @SerialName("is_common") val isCommon: Boolean = true
)

@Serializable
data class SubjectEnrollment(
    @SerialName("student_id") val studentId: String,
    @SerialName("subject_id") val subjectId: String
)

@Serializable
data class CRPermission(
    @SerialName("user_id") val userId: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("can_mark_attendance") val canMarkAttendance: Boolean = false,
    @SerialName("can_moderate_room") val canModerateRoom: Boolean = false
)

@Serializable
data class TimetableSlot(
    val id: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("day_of_week") val dayOfWeek: Int, // 1 = Mon, 2 = Tue, ..., 6 = Sat
    @SerialName("start_time") val startTime: String, // "09:00"
    @SerialName("end_time") val endTime: String,     // "10:00"
    val room: String = "Room 204"
)

@Serializable
data class TimetableOverride(
    val id: String,
    @SerialName("slot_id") val slotId: String,
    val date: String, // "YYYY-MM-DD"
    val status: String, // "cancelled", "room_changed", "rescheduled"
    val note: String = ""
)

@Serializable
data class AttendanceSession(
    val id: String,
    @SerialName("subject_id") val subjectId: String,
    val date: String,
    @SerialName("marked_by") val markedBy: String,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class AttendanceRecord(
    @SerialName("session_id") val sessionId: String,
    @SerialName("student_id") val studentId: String,
    val status: String // "present", "absent"
)

@Serializable
data class Room(
    val id: String,
    val type: String, // "global", "subject", "dm"
    val name: String,
    @SerialName("subject_id") val subjectId: String? = null,
    val participantIds: List<String> = emptyList()
)

@Serializable
data class Message(
    val id: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("sender_id") val senderId: String,
    val senderName: String,
    val content: String,
    @SerialName("is_anonymous") val isAnonymous: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    val isPinned: Boolean = false,
    @SerialName("deleted_at") val deletedAt: String? = null
)

@Serializable
data class AppSettings(
    @SerialName("min_attendance_percent") val minAttendancePercent: Int = 75,
    @SerialName("semester_end_date") val semesterEndDate: String = "2026-12-15"
)

@Serializable
data class Assignment(
    val id: String,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val description: String,
    val dueDate: String
)

@Serializable
data class AssignmentStatus(
    val assignmentId: String,
    val studentId: String,
    val isDone: Boolean,
    val submissionFile: String? = null,
    val grade: String? = null,
    val feedback: String? = null,
    val submittedAt: String? = null
)

@Serializable
data class ResourceItem(
    val id: String,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val fileType: String, // "PDF", "PPT", "DOC"
    val dateAdded: String = "2026-09-10"
)

@Serializable
data class EventPost(
    val id: String,
    val imageUrl: String,
    val caption: String,
    val postedBy: String,
    val date: String
)
