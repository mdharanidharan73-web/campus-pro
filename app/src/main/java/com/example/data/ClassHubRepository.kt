package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class SubjectAttendanceSummary(
    val subject: Subject,
    val attendedCount: Int,
    val tardyCount: Int = 0,
    val heldCount: Int,
    val percentage: Int,
    val safeSkips: Int,
    val isWarning: Boolean,
    val trendHistory: List<Int> // e.g. [90, 88, 85, 82]
)

data class NextClassInfo(
    val subject: Subject,
    val slot: TimetableSlot,
    val effectiveRoom: String,
    val overrideStatus: String?, // "room_changed", "rescheduled", etc.
    val overrideNote: String?,
    val formattedTime: String,
    val countdownText: String
)

class ClassHubRepository private constructor() {

    companion object {
        val instance = ClassHubRepository()
    }

    // App Settings
    private val _settings = MutableStateFlow(AppSettings(minAttendancePercent = 75, semesterEndDate = "2026-12-15"))
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Current User Session
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Roster allowlist
    private val _rosterAllowlist = MutableStateFlow<List<RosterAllowlist>>(emptyList())
    val rosterAllowlist: StateFlow<List<RosterAllowlist>> = _rosterAllowlist.asStateFlow()

    // Users
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // Subjects
    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    // Subject Enrollments
    private val _enrollments = MutableStateFlow<List<SubjectEnrollment>>(emptyList())
    val enrollments: StateFlow<List<SubjectEnrollment>> = _enrollments.asStateFlow()

    // CR Permissions
    private val _crPermissions = MutableStateFlow<List<CRPermission>>(emptyList())
    val crPermissions: StateFlow<List<CRPermission>> = _crPermissions.asStateFlow()

    // Timetable Slots
    private val _timetableSlots = MutableStateFlow<List<TimetableSlot>>(emptyList())
    val timetableSlots: StateFlow<List<TimetableSlot>> = _timetableSlots.asStateFlow()

    // Timetable Overrides
    private val _timetableOverrides = MutableStateFlow<List<TimetableOverride>>(emptyList())
    val timetableOverrides: StateFlow<List<TimetableOverride>> = _timetableOverrides.asStateFlow()

    // Attendance Sessions & Records
    private val _attendanceSessions = MutableStateFlow<List<AttendanceSession>>(emptyList())
    val attendanceSessions: StateFlow<List<AttendanceSession>> = _attendanceSessions.asStateFlow()

    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords.asStateFlow()

    // Rooms & Messages
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Stretch: Assignments
    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

    private val _assignmentStatuses = MutableStateFlow<List<AssignmentStatus>>(emptyList())
    val assignmentStatuses: StateFlow<List<AssignmentStatus>> = _assignmentStatuses.asStateFlow()

    // Stretch: Resources
    private val _resources = MutableStateFlow<List<ResourceItem>>(emptyList())
    val resources: StateFlow<List<ResourceItem>> = _resources.asStateFlow()

    // Events
    private val _events = MutableStateFlow<List<com.example.model.EventPost>>(emptyList())
    val events: StateFlow<List<com.example.model.EventPost>> = _events.asStateFlow()

    // Exams
    private val _exams = MutableStateFlow<List<Exam>>(emptyList())
    val exams: StateFlow<List<Exam>> = _exams.asStateFlow()

    init {
        seedAllData()
    }

    private fun seedAllData() {
        // 1. Roster Allowlist for BCA001 to BCA033
        val studentNames = listOf(
            "Dharanidharan M", "Aditi Sharma", "Priya Sundaram", "Rohan Gupta", "Sneha Reddy",
            "Vikram Singh", "Rahul Verma", "Ananya Rao", "Karthik Iyer", "Meera Krishnan",
            "Nikhil Joshi", "Pooja Hegde", "Divya Nair", "Arjun Menon", "Varun Prabhu",
            "Ritu Bhat", "Sanjay Kumar", "Deepa Pillai", "Manoj Gowda", "Swathi Shetty",
            "Pranav Deshmukh", "Kavya Raman", "Harish Chandra", "Keerthi V", "Chetan Rao",
            "Shalini Murthy", "Abhinav Das", "Bhavana N", "Tejaswini K", "Naveen Kumar",
            "Lavanya M", "Gautam Sen", "Sandhya R"
        )

        val allowlist = studentNames.mapIndexed { idx, name ->
            val roll = String.format("BCA%03d", idx + 1)
            RosterAllowlist(rollNo = roll, fullName = name, claimed = idx < 10) // First 10 claimed in seed
        }
        _rosterAllowlist.value = allowlist

        // 2. Admin & Teachers
        val adminUser = User(
            id = "admin_01",
            fullName = "Campus Administrator",
            role = "admin",
            email = "admin@campupro.edu",
            createdAt = "2026-08-01"
        )
        val teacher1 = User(
            id = "teach_01",
            fullName = "Prof. Rajesh Sharma",
            role = "teacher",
            email = "sharma@classhub.edu",
            createdAt = "2026-08-01"
        )
        val teacher2 = User(
            id = "teach_02",
            fullName = "Dr. Meenakshi Sundaram",
            role = "teacher",
            email = "meenakshi@classhub.edu",
            createdAt = "2026-08-01"
        )

        // 3. Students
        val studentUsers = studentNames.mapIndexed { idx, name ->
            val roll = String.format("BCA%03d", idx + 1)
            val role = if (roll == "BCA003") "cr" else "student" // BCA003 Priya is CR!
            User(
                id = "student_$roll",
                fullName = name,
                rollNo = roll,
                role = role,
                email = "${roll.lowercase()}@classhub.edu",
                createdAt = "2026-08-05"
            )
        }

        val allUserList = listOf(adminUser, teacher1, teacher2) + studentUsers
        _allUsers.value = allUserList

        // Default login to Student Dharanidharan M (BCA001) for initial load
        _currentUser.value = studentUsers.find { it.fullName.contains("Dharanidharan") } ?: studentUsers.first()

        // 4. Subjects
        val subDbms = Subject("sub_dbms", "Database Systems", teacher1.id, teacher1.fullName, isCommon = true, code = "BCA301", room = "Room 204")
        val subPython = Subject("sub_python", "Python Programming", teacher2.id, teacher2.fullName, isCommon = true, code = "BCA302", room = "Computing Lab 2")
        val subWeb = Subject("sub_web", "Web Technologies", teacher1.id, teacher1.fullName, isCommon = true, code = "BCA303", room = "Hall 204")
        val subKan = Subject("sub_kannada", "Kannada Literature", teacher2.id, teacher2.fullName, isCommon = false, code = "BCA304-K", room = "Room 101")
        val subTam = Subject("sub_tamil", "Tamil Literature", teacher2.id, teacher2.fullName, isCommon = false, code = "BCA304-T", room = "Room 102")
        val subSan = Subject("sub_sanskrit", "Sanskrit Shastra", teacher2.id, teacher2.fullName, isCommon = false, code = "BCA304-S", room = "Room 103")
        val subjectList = listOf(subDbms, subPython, subWeb, subKan, subTam, subSan)
        _subjects.value = subjectList

        // 5. Enrollments
        val enrollmentList = mutableListOf<SubjectEnrollment>()
        // Common subjects: All 33 students enrolled
        studentUsers.forEach { st ->
            enrollmentList.add(SubjectEnrollment(st.id, subDbms.id))
            enrollmentList.add(SubjectEnrollment(st.id, subPython.id))
            enrollmentList.add(SubjectEnrollment(st.id, subWeb.id))
        }
        // Split language enrollment realistically
        studentUsers.forEachIndexed { idx, st ->
            when (idx % 3) {
                0 -> enrollmentList.add(SubjectEnrollment(st.id, subKan.id)) // 11 in Kannada
                1 -> enrollmentList.add(SubjectEnrollment(st.id, subTam.id)) // 11 in Tamil
                2 -> enrollmentList.add(SubjectEnrollment(st.id, subSan.id)) // 11 in Sanskrit
            }
        }
        _enrollments.value = enrollmentList

        // 6. CR Permissions
        // Priya Sundaram (student_BCA003) has CR permissions for DBMS ONLY!
        _crPermissions.value = listOf(
            CRPermission(
                userId = "student_BCA003",
                subjectId = subDbms.id,
                canMarkAttendance = true,
                canModerateRoom = true
            )
        )

        // 7. Timetable (Monday = 1, Tuesday = 2, ... Saturday = 6)
        val slots = listOf(
            // Monday
            TimetableSlot("slot_m1", subDbms.id, 1, "09:00", "10:00", "Room 204"),
            TimetableSlot("slot_m2", subPython.id, 1, "10:15", "11:15", "Lab 2"),
            TimetableSlot("slot_m3", subWeb.id, 1, "11:30", "12:30", "Room 204"),
            TimetableSlot("slot_m4", subKan.id, 1, "13:30", "14:30", "Room 101"),
            TimetableSlot("slot_m5", subTam.id, 1, "13:30", "14:30", "Room 102"),
            TimetableSlot("slot_m6", subSan.id, 1, "13:30", "14:30", "Room 103"),

            // Tuesday
            TimetableSlot("slot_t1", subPython.id, 2, "09:00", "10:00", "Lab 2"),
            TimetableSlot("slot_t2", subKan.id, 2, "10:15", "11:15", "Room 101"),
            TimetableSlot("slot_t3", subTam.id, 2, "10:15", "11:15", "Room 102"),
            TimetableSlot("slot_t4", subSan.id, 2, "10:15", "11:15", "Room 103"),
            TimetableSlot("slot_t5", subDbms.id, 2, "11:30", "12:30", "Room 204"),
            TimetableSlot("slot_t6", subWeb.id, 2, "13:30", "14:30", "Room 204"),

            // Wednesday
            TimetableSlot("slot_w1", subWeb.id, 3, "09:00", "10:00", "Lab 1"),
            TimetableSlot("slot_w2", subDbms.id, 3, "10:15", "11:15", "Room 204"),
            TimetableSlot("slot_w3", subPython.id, 3, "11:30", "12:30", "Lab 2"),

            // Thursday
            TimetableSlot("slot_th1", subKan.id, 4, "09:00", "10:00", "Room 101"),
            TimetableSlot("slot_th2", subTam.id, 4, "09:00", "10:00", "Room 102"),
            TimetableSlot("slot_th3", subSan.id, 4, "09:00", "10:00", "Room 103"),
            TimetableSlot("slot_th4", subWeb.id, 4, "10:15", "11:15", "Room 204"),
            TimetableSlot("slot_th5", subDbms.id, 4, "11:30", "12:30", "Room 204"),

            // Friday
            TimetableSlot("slot_f1", subPython.id, 5, "09:00", "10:00", "Lab 2"),
            TimetableSlot("slot_f2", subDbms.id, 5, "10:15", "11:15", "Room 204"),
            TimetableSlot("slot_f3", subWeb.id, 5, "11:30", "12:30", "Room 204"),

            // Saturday
            TimetableSlot("slot_s1", subWeb.id, 6, "09:00", "10:30", "Lab 1"),
            TimetableSlot("slot_s2", subPython.id, 6, "10:45", "12:15", "Lab 2")
        )
        _timetableSlots.value = slots

        // 8. Timetable Override (for demonstration: DBMS Room Changed to Room 302 today!)
        val todayStr = LocalDate.now().toString()
        _timetableOverrides.value = listOf(
            TimetableOverride(
                id = "ov_1",
                slotId = "slot_m1",
                date = todayStr,
                status = "room_changed",
                note = "Moved to Room 302 due to projector maintenance in 204"
            )
        )

        // 9. Attendance Sessions & Records (Past 6 sessions per subject)
        val sessions = mutableListOf<AttendanceSession>()
        val records = mutableListOf<AttendanceRecord>()
        val sampleDates = listOf("2026-09-01", "2026-09-03", "2026-09-05", "2026-09-08", "2026-09-10", "2026-09-12")

        subjectList.forEach { sub ->
            val enrolledStudents = studentUsers.filter { st ->
                enrollmentList.any { it.studentId == st.id && it.subjectId == sub.id }
            }

            sampleDates.forEachIndexed { sIdx, date ->
                val sessionId = "sess_${sub.id}_$sIdx"
                sessions.add(
                    AttendanceSession(
                        id = sessionId,
                        subjectId = sub.id,
                        date = date,
                        markedBy = teacher1.id,
                        createdAt = "$date 12:00:00"
                    )
                )

                enrolledStudents.forEachIndexed { stIdx, st ->
                    // Make Rahul (BCA007) have ~82% in DBMS, 74% in Python (Warning!), 90% in Web Dev
                    val isPresent = when {
                        st.rollNo == "BCA007" && sub.id == subPython.id && sIdx in listOf(2, 4) -> false
                        st.rollNo == "BCA007" && sub.id == subDbms.id && sIdx == 3 -> false
                        (stIdx + sIdx) % 7 == 0 -> false // Realistic absent variation
                        else -> true
                    }
                    records.add(
                        AttendanceRecord(
                            sessionId = sessionId,
                            studentId = st.id,
                            status = if (isPresent) "present" else "absent"
                        )
                    )
                }
            }
        }
        _attendanceSessions.value = sessions
        _attendanceRecords.value = records

        // 10. Rooms
        val roomGlobal = Room("room_global", "global", "BCA Batch 2026 Global", null)
        val roomDbms = Room("room_dbms", "subject", "DBMS Discussion", subDbms.id)
        val roomPython = Room("room_python", "subject", "Python Programming Room", subPython.id)
        val roomWeb = Room("room_web", "subject", "Web Dev Hub", subWeb.id)
        val roomDmTeacher = Room("room_dm_sharma", "dm", "Prof. Sharma (Direct)", null, listOf("student_BCA007", teacher1.id))
        _rooms.value = listOf(roomGlobal, roomDbms, roomPython, roomWeb, roomDmTeacher)

        // 11. Messages
        _messages.value = listOf(
            Message(
                id = "m1",
                roomId = roomGlobal.id,
                senderId = teacher1.id,
                senderName = teacher1.fullName,
                content = "Welcome everyone to ClassHub! Timetable and official attendance will be tracked here.",
                isAnonymous = false,
                createdAt = "2026-09-14 08:30",
                isPinned = true
            ),
            Message(
                id = "m2",
                roomId = roomGlobal.id,
                senderId = "student_BCA003",
                senderName = "Priya Sundaram (CR)",
                content = "Reminder: Please verify your language subject enrollments by tomorrow noon.",
                isAnonymous = false,
                createdAt = "2026-09-14 09:15"
            ),
            Message(
                id = "m3",
                roomId = roomDbms.id,
                senderId = "student_BCA012",
                senderName = "Pooja Hegde",
                content = "Sir, will the ER-diagram lab assignment cover Boyce-Codd normal form?",
                isAnonymous = false,
                createdAt = "2026-09-14 11:00"
            ),
            Message(
                id = "m4",
                roomId = roomDbms.id,
                senderId = "student_BCA007", // Real sender kept for moderation accountability!
                senderName = "Anonymous Student",
                content = "Could someone explain 3NF vs BCNF with a quick practical table example?",
                isAnonymous = true,
                createdAt = "2026-09-14 11:20"
            ),
            Message(
                id = "m5",
                roomId = roomDbms.id,
                senderId = teacher1.id,
                senderName = teacher1.fullName,
                content = "Good question! BCNF requires every determinant to be a superkey. We will practice 2 examples tomorrow in Room 302.",
                isAnonymous = false,
                createdAt = "2026-09-14 11:45"
            )
        )

        // 12. Assignments
        _assignments.value = listOf(
            Assignment(
                id = "asgn_today",
                title = "B-Tree Indexing & Query Execution Plans",
                subjectId = subDbms.id,
                subjectName = "Database Systems",
                description = "Measure query execution time and analyze EXPLAIN statements with clustered and non-clustered indexes.",
                dueDate = "2026-09-17",
                priority = "High"
            ),
            Assignment(
                id = "asgn_1",
                title = "Relational Schema Normalization (3NF & BCNF)",
                subjectId = subDbms.id,
                subjectName = "Database Systems",
                description = "Decompose student enrollment tables into 3NF and BCNF preserving functional dependencies.",
                dueDate = "2026-09-22",
                priority = "High"
            ),
            Assignment(
                id = "asgn_2",
                title = "RESTful API Endpoints with PostgreSQL",
                subjectId = subWeb.id,
                subjectName = "Web Technologies",
                description = "Implement authenticated student and faculty endpoints with JSON response payloads.",
                dueDate = "2026-09-25",
                priority = "Medium"
            ),
            Assignment(
                id = "asgn_3",
                title = "Async I/O Coroutine Task Pipeline",
                subjectId = subPython.id,
                subjectName = "Python Programming",
                description = "Build a multi-producer asynchronous queue using Python's asyncio module.",
                dueDate = "2026-09-28",
                priority = "Medium"
            ),
            Assignment(
                id = "asgn_overdue",
                title = "Entity-Relationship Data Modeling",
                subjectId = subDbms.id,
                subjectName = "Database Systems",
                description = "Construct an enhanced ER diagram with cardinality and generalization constraints.",
                dueDate = "2026-09-12",
                priority = "High"
            ),
            Assignment(
                id = "asgn_comp",
                title = "Responsive Grid Layout & Typography",
                subjectId = subWeb.id,
                subjectName = "Web Technologies",
                description = "Build a cross-device CSS Grid landing page with modern responsive breakpoints.",
                dueDate = "2026-09-10",
                priority = "Low"
            )
        )

        val defaultStudentId = studentUsers.first().id
        _assignmentStatuses.value = listOf(
            AssignmentStatus("asgn_comp", defaultStudentId, isDone = true),
            AssignmentStatus("asgn_1", defaultStudentId, isDone = true),
            AssignmentStatus("asgn_today", defaultStudentId, isDone = false),
            AssignmentStatus("asgn_2", defaultStudentId, isDone = false),
            AssignmentStatus("asgn_3", defaultStudentId, isDone = false),
            AssignmentStatus("asgn_overdue", defaultStudentId, isDone = false)
        )

        // 13. Exams
        _exams.value = listOf(
            Exam(
                id = "ex_1",
                subjectId = subDbms.id,
                subjectName = "Database Systems",
                subjectCode = "BCA301",
                title = "Midterm Theory Examination",
                date = "2026-09-24",
                time = "10:00 AM – 01:00 PM",
                room = "Auditorium Hall 2",
                seatNo = "A-18",
                syllabusTopics = "Units 1–3: Relational Algebra, ER Models, Normalization (1NF to BCNF)"
            ),
            Exam(
                id = "ex_2",
                subjectId = subPython.id,
                subjectName = "Python Programming",
                subjectCode = "BCA302",
                title = "Midterm Practical Lab Exam",
                date = "2026-09-29",
                time = "02:00 PM – 05:00 PM",
                room = "Computing Lab 2",
                seatNo = "L-08",
                syllabusTopics = "OOP Concepts, Asyncio, Lambdas, Generator Pipelines & File I/O"
            ),
            Exam(
                id = "ex_3",
                subjectId = subWeb.id,
                subjectName = "Web Technologies",
                subjectCode = "BCA303",
                title = "Semester Internal Assessment",
                date = "2026-10-06",
                time = "10:00 AM – 01:00 PM",
                room = "Exam Hall 1B",
                seatNo = "B-22",
                syllabusTopics = "HTML5/CSS3, JavaScript ES6+, RESTful APIs, Node runtime & Express"
            ),
            Exam(
                id = "ex_4",
                subjectId = subKan.id,
                subjectName = "Language Elective",
                subjectCode = "BCA304",
                title = "Term Language Assessment",
                date = "2026-10-14",
                time = "10:00 AM – 12:30 PM",
                room = "Hall 101",
                seatNo = "C-05",
                syllabusTopics = "Grammar, Classical Literature Analysis & Technical Translation"
            )
        )

        // 14. Resources
        _resources.value = listOf(
            ResourceItem("res_1", "DBMS Unit 2: SQL & Normalization Handout", subDbms.id, "Database Systems", "PDF"),
            ResourceItem("res_2", "Python Data Structures & OOP Cheatsheet", subPython.id, "Python Programming", "PDF"),
            ResourceItem("res_3", "Responsive Web Design with Flexbox & Grid", subWeb.id, "Web Technologies", "PPT"),
            ResourceItem("res_4", "Lab Manual - Semester 3 BCA Complete", subDbms.id, "Database Systems", "DOC")
        )

        // 15. Events
        _events.value = listOf(
            com.example.model.EventPost("ev_1", "", "Annual Tech Fest announced! Dates: Oct 15-18", "Prof. Sharma", "2026-09-12"),
            com.example.model.EventPost("ev_2", "", "Guest Lecture on AI tomorrow at 2 PM in Seminar Hall.", "Prof. Sharma", "2026-09-14")
        )
    }

    fun toggleAssignmentStatus(assignmentId: String, studentId: String) {
        val current = _assignmentStatuses.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.assignmentId == assignmentId && it.studentId == studentId }
        if (existingIndex >= 0) {
            val old = current[existingIndex]
            current[existingIndex] = old.copy(isDone = !old.isDone)
        } else {
            current.add(AssignmentStatus(assignmentId = assignmentId, studentId = studentId, isDone = true))
        }
        _assignmentStatuses.value = current
    }

    fun addEventPost(caption: String, imageUrl: String, postedBy: String) {
        val newEvent = com.example.model.EventPost(
            id = "ev_${System.currentTimeMillis()}",
            imageUrl = imageUrl,
            caption = caption,
            postedBy = postedBy,
            date = java.time.LocalDate.now().toString()
        )
        _events.value = listOf(newEvent) + _events.value
    }

    // --- Authentication & User Operations ---

    fun login(email: String, pass: String): Result<User> {
        val user = _allUsers.value.find { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return Result.failure(Exception("No account found with email $email"))
        _currentUser.value = user
        return Result.success(user)
    }

    fun signup(fullName: String, rollNo: String, email: String, pass: String): Result<User> {
        val cleanRoll = rollNo.trim().uppercase()
        val rosterItem = _rosterAllowlist.value.find { it.rollNo.equals(cleanRoll, ignoreCase = true) }
            ?: return Result.failure(Exception("Roll number '$cleanRoll' not found in official class allowlist."))

        if (rosterItem.claimed) {
            return Result.failure(Exception("Roll number '$cleanRoll' has already been registered."))
        }

        if (_allUsers.value.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return Result.failure(Exception("Email already associated with an account."))
        }

        val newUser = User(
            id = "student_$cleanRoll",
            fullName = rosterItem.fullName, // Use the allowlisted official name as specified!
            rollNo = cleanRoll,
            role = if (cleanRoll == "BCA003") "cr" else "student",
            email = email.trim(),
            createdAt = LocalDate.now().toString()
        )

        // Mark roster claimed
        _rosterAllowlist.value = _rosterAllowlist.value.map {
            if (it.rollNo == cleanRoll) it.copy(claimed = true) else it
        }
        _allUsers.value = _allUsers.value + newUser
        _currentUser.value = newUser

        // Auto enroll in common subjects
        val commonSubjects = _subjects.value.filter { it.isCommon }
        val newEnrollments = commonSubjects.map { SubjectEnrollment(newUser.id, it.id) }
        _enrollments.value = _enrollments.value + newEnrollments

        return Result.success(newUser)
    }

    fun switchDemoUser(role: String, specificId: String? = null) {
        val targetUser = when {
            specificId != null -> _allUsers.value.find { it.id == specificId }
            role == "admin" -> _allUsers.value.find { it.role == "admin" }
            role == "teacher" -> _allUsers.value.find { it.role == "teacher" }
            role == "cr" -> _allUsers.value.find { it.role == "cr" }
            else -> _allUsers.value.find { it.rollNo == "BCA007" } ?: _allUsers.value.firstOrNull { it.role == "student" }
        }
        _currentUser.value = targetUser
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- Next Class Algorithm ---
    fun getNextClass(studentId: String): NextClassInfo? {
        val enrolledSubjectIds = _enrollments.value
            .filter { it.studentId == studentId }
            .map { it.subjectId }

        val todayDayOfWeek = LocalDate.now().dayOfWeek.value // 1 = Monday ... 7 = Sunday
        val nowTime = LocalTime.now()

        val allSlots = _timetableSlots.value
            .filter { it.subjectId in enrolledSubjectIds }
            .sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))

        if (allSlots.isEmpty()) return null

        val todayStr = LocalDate.now().toString()
        val overrides = _timetableOverrides.value.filter { it.date == todayStr }

        // Look for next slot today
        for (slot in allSlots.filter { it.dayOfWeek == todayDayOfWeek }) {
            val override = overrides.find { it.slotId == slot.id }
            if (override?.status == "cancelled") continue

            val slotStartTime = try {
                LocalTime.parse(slot.startTime, DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                null
            }

            if (slotStartTime != null && slotStartTime.isAfter(nowTime)) {
                val sub = _subjects.value.find { it.id == slot.subjectId } ?: continue
                val effectiveRoom = if (override?.status == "room_changed") "Room 302" else slot.room
                val minutesUntil = java.time.Duration.between(nowTime, slotStartTime).toMinutes()
                val countdown = if (minutesUntil <= 60) "Starts in $minutesUntil min" else "Starts at ${slot.startTime}"

                return NextClassInfo(
                    subject = sub,
                    slot = slot,
                    effectiveRoom = effectiveRoom,
                    overrideStatus = override?.status,
                    overrideNote = override?.note,
                    formattedTime = "${slot.startTime} - ${slot.endTime}",
                    countdownText = countdown
                )
            }
        }

        // Otherwise return the first scheduled slot on the next active day
        val nextSlot = allSlots.firstOrNull { it.dayOfWeek > todayDayOfWeek } ?: allSlots.first()
        val sub = _subjects.value.find { it.id == nextSlot.subjectId } ?: return null
        val dayName = when (nextSlot.dayOfWeek) {
            1 -> "Monday"; 2 -> "Tuesday"; 3 -> "Wednesday"; 4 -> "Thursday"; 5 -> "Friday"; 6 -> "Saturday"; else -> "Sunday"
        }

        return NextClassInfo(
            subject = sub,
            slot = nextSlot,
            effectiveRoom = nextSlot.room,
            overrideStatus = null,
            overrideNote = null,
            formattedTime = "${nextSlot.startTime} - ${nextSlot.endTime}",
            countdownText = "$dayName at ${nextSlot.startTime}"
        )
    }

    // --- Safe-To-Bunk Calculator & Attendance Summaries ---
    // Formula from PDF:
    // A = classes attended so far
    // H = classes held so far
    // R = classes remaining on timetable before semester_end_date, excluding already-cancelled classes
    // threshold = min_attendance_percent / 100
    // max_safe_skips = floor( A + R - threshold * (H + R) )
    // Clamp: 0 <= max_safe_skips <= R
    fun calculateSafeToBunk(attendedA: Int, heldH: Int, remainingR: Int, minPercent: Int): Int {
        if (heldH + remainingR == 0) return 0
        val threshold = minPercent.toDouble() / 100.0
        val rawSkips = floor(attendedA.toDouble() + remainingR.toDouble() - threshold * (heldH + remainingR).toDouble()).toInt()
        return rawSkips.coerceIn(0, remainingR)
    }

    fun getStudentAttendanceSummaries(studentId: String): List<SubjectAttendanceSummary> {
        val enrolledSubjectIds = _enrollments.value
            .filter { it.studentId == studentId }
            .map { it.subjectId }

        val enrolledSubjects = _subjects.value.filter { it.id in enrolledSubjectIds }
        val sessions = _attendanceSessions.value
        val records = _attendanceRecords.value
        val minPercent = _settings.value.minAttendancePercent

        return enrolledSubjects.map { subject ->
            val subSessions = sessions.filter { it.subjectId == subject.id }
            val subSessionIds = subSessions.map { it.id }
            val studentRecords = records.filter { it.studentId == studentId && it.sessionId in subSessionIds }

            val attendedCount = studentRecords.count { it.status == "present" || it.status == "tardy" }
            val heldCount = subSessions.size
            val tardyCount = studentRecords.count { it.status == "tardy" }
            val percentage = if (heldCount > 0) ((attendedCount.toDouble() / heldCount.toDouble()) * 100).toInt() else 100

            // Estimate R (remaining sessions in semester, approx 20 remaining)
            val remainingR = 20
            val safeSkips = calculateSafeToBunk(attendedCount, heldCount, remainingR, minPercent)
            val isWarning = safeSkips <= 1 || percentage < minPercent

            // Simulated trend history for display
            val trendHistory = listOf(
                max(60, percentage + 6),
                max(60, percentage + 3),
                max(60, percentage + 1),
                percentage
            )

            SubjectAttendanceSummary(
                subject = subject,
                attendedCount = attendedCount,
                heldCount = heldCount,
                tardyCount = tardyCount,
                percentage = percentage,
                safeSkips = safeSkips,
                isWarning = isWarning,
                trendHistory = trendHistory
            )
        }
    }

    // --- CR Permissions Verification ---
    fun canMarkAttendance(userId: String, subjectId: String): Boolean {
        val user = _allUsers.value.find { it.id == userId } ?: return false
        if (user.role == "teacher") return true
        if (user.role == "cr") {
            return _crPermissions.value.any { it.userId == userId && it.subjectId == subjectId && it.canMarkAttendance }
        }
        return false
    }

    fun canModerateRoom(userId: String, subjectId: String?): Boolean {
        val user = _allUsers.value.find { it.id == userId } ?: return false
        if (user.role == "teacher") return true
        if (user.role == "cr" && subjectId != null) {
            return _crPermissions.value.any { it.userId == userId && it.subjectId == subjectId && it.canModerateRoom }
        }
        return false
    }

    // --- Attendance Marking ---
    fun markAttendance(
        subjectId: String,
        date: String,
        markerUserId: String,
        studentStatuses: Map<String, String> // studentId -> "present" / "absent"
    ): Result<Unit> {
        if (!canMarkAttendance(markerUserId, subjectId)) {
            return Result.failure(Exception("Unauthorized: You do not have permission to mark attendance for this subject."))
        }

        val sessionId = "sess_${subjectId}_${System.currentTimeMillis()}"
        val newSession = AttendanceSession(
            id = sessionId,
            subjectId = subjectId,
            date = date,
            markedBy = markerUserId,
            createdAt = "$date 10:00:00"
        )

        val newRecords = studentStatuses.map { (studentId, status) ->
            AttendanceRecord(
                sessionId = sessionId,
                studentId = studentId,
                status = status
            )
        }

        _attendanceSessions.value = _attendanceSessions.value + newSession
        _attendanceRecords.value = _attendanceRecords.value + newRecords
        return Result.success(Unit)
    }

    // --- Timetable Override ---
    fun addTimetableOverride(slotId: String, date: String, status: String, note: String): Result<Unit> {
        val newOverride = TimetableOverride(
            id = "ov_${System.currentTimeMillis()}",
            slotId = slotId,
            date = date,
            status = status,
            note = note
        )
        _timetableOverrides.value = _timetableOverrides.value + newOverride
        return Result.success(Unit)
    }

    // --- Messaging & Rooms ---
    fun sendMessage(roomId: String, senderId: String, content: String, isAnonymous: Boolean): Result<Message> {
        val sender = _allUsers.value.find { it.id == senderId }
            ?: return Result.failure(Exception("Sender not found"))

        val timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val dateStr = LocalDate.now().toString()
        val newMessage = Message(
            id = "msg_${System.currentTimeMillis()}",
            roomId = roomId,
            senderId = senderId,
            senderName = if (isAnonymous) "Anonymous Student" else sender.fullName,
            content = content,
            isAnonymous = isAnonymous,
            createdAt = "$dateStr $timeStr",
            isPinned = false
        )

        _messages.value = _messages.value + newMessage
        return Result.success(newMessage)
    }

    fun softDeleteMessage(messageId: String, moderatorUserId: String, subjectId: String?): Result<Unit> {
        if (!canModerateRoom(moderatorUserId, subjectId)) {
            return Result.failure(Exception("Unauthorized to moderate messages in this room."))
        }

        _messages.value = _messages.value.map { msg ->
            if (msg.id == messageId) {
                msg.copy(deletedAt = LocalDate.now().toString())
            } else {
                msg
            }
        }
        return Result.success(Unit)
    }

    fun togglePinMessage(messageId: String, teacherId: String): Result<Unit> {
        val user = _allUsers.value.find { it.id == teacherId }
        if (user?.role != "teacher") return Result.failure(Exception("Only teachers can pin announcements."))

        _messages.value = _messages.value.map {
            if (it.id == messageId) it.copy(isPinned = !it.isPinned) else it
        }
        return Result.success(Unit)
    }

    // --- Assignments (Phase 5) ---
    fun toggleAssignmentDone(assignmentId: String, studentId: String) {
        val currentStatuses = _assignmentStatuses.value.toMutableList()
        val existingIndex = currentStatuses.indexOfFirst { it.assignmentId == assignmentId && it.studentId == studentId }
        if (existingIndex >= 0) {
            val curr = currentStatuses[existingIndex]
            currentStatuses[existingIndex] = curr.copy(isDone = !curr.isDone)
        } else {
            currentStatuses.add(AssignmentStatus(assignmentId, studentId, true))
        }
        _assignmentStatuses.value = currentStatuses
    }

    fun submitAssignment(assignmentId: String, studentId: String, fileName: String): Result<Unit> {
        val currentList = _assignmentStatuses.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.assignmentId == assignmentId && it.studentId == studentId }
        val dateNow = java.time.LocalDate.now().toString()
        if (existingIndex >= 0) {
            currentList[existingIndex] = currentList[existingIndex].copy(
                isDone = true,
                submissionFile = fileName,
                submittedAt = dateNow
            )
        } else {
            currentList.add(AssignmentStatus(assignmentId, studentId, true, fileName, null, null, dateNow))
        }
        _assignmentStatuses.value = currentList
        return Result.success(Unit)
    }

    fun gradeAssignment(assignmentId: String, studentId: String, grade: String, feedback: String): Result<Unit> {
        val currentList = _assignmentStatuses.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.assignmentId == assignmentId && it.studentId == studentId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = currentList[existingIndex].copy(
                grade = grade,
                feedback = feedback
            )
            _assignmentStatuses.value = currentList
            return Result.success(Unit)
        }
        return Result.failure(Exception("Submission not found"))
    }

    fun addAssignment(title: String, subjectId: String, desc: String, dueDate: String): Result<Unit> {
        val sub = _subjects.value.find { it.id == subjectId }
        val newAsgn = Assignment(
            id = "asgn_${System.currentTimeMillis()}",
            title = title,
            subjectId = subjectId,
            subjectName = sub?.name ?: "Subject",
            description = desc,
            dueDate = dueDate
        )
        _assignments.value = _assignments.value + newAsgn
        return Result.success(Unit)
    }

    fun addResource(title: String, subjectId: String, fileType: String): Result<Unit> {
        val sub = _subjects.value.find { it.id == subjectId }
        val newRes = com.example.model.ResourceItem(
            id = "res_${System.currentTimeMillis()}",
            title = title,
            subjectId = subjectId,
            subjectName = sub?.name ?: "Subject",
            fileType = fileType,
            dateAdded = java.time.LocalDate.now().toString()
        )
        _resources.value = _resources.value + newRes
        return Result.success(Unit)
    }

    // --- CR Permissions Management (Teacher) ---
    fun updateCRPermission(studentId: String, subjectId: String, canMark: Boolean, canModerate: Boolean) {
        val list = _crPermissions.value.filterNot { it.userId == studentId && it.subjectId == subjectId }.toMutableList()
        list.add(CRPermission(studentId, subjectId, canMark, canModerate))
        _crPermissions.value = list
    }

    // --- Settings Management (Teacher) ---
    fun updateSettings(minAttendance: Int, semesterEnd: String) {
        _settings.value = AppSettings(minAttendancePercent = minAttendance, semesterEndDate = semesterEnd)
    }

    // =========================================================================
    // --- ADMIN-ONLY REAL DATA MANAGEMENT (BACKEND ENFORCED AUTHORIZATION) ---
    // =========================================================================

    private fun requireAdmin(actor: User) {
        if (actor.role != "admin") {
            throw SecurityException("Access Denied: Administrator privileges are required to perform this action.")
        }
    }

    // 1. Classes / Subjects CRUD
    fun addSubject(actor: User, name: String, code: String, facultyName: String, room: String): Result<Subject> {
        return try {
            requireAdmin(actor)
            if (name.isBlank() || code.isBlank() || facultyName.isBlank() || room.isBlank()) {
                return Result.failure(IllegalArgumentException("All subject fields (name, code, faculty, room) are required."))
            }
            val newSubject = Subject(
                id = "sub_${System.currentTimeMillis()}",
                name = name.trim(),
                facultyName = facultyName.trim(),
                isCommon = true,
                code = code.trim().uppercase(),
                room = room.trim()
            )
            _subjects.value = _subjects.value + newSubject

            // Automatically enroll all existing students in common subjects
            val newEnrollments = _allUsers.value
                .filter { it.role == "student" || it.role == "cr" }
                .map { SubjectEnrollment(it.id, newSubject.id) }
            _enrollments.value = _enrollments.value + newEnrollments

            Result.success(newSubject)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateSubject(actor: User, id: String, name: String, code: String, facultyName: String, room: String): Result<Subject> {
        return try {
            requireAdmin(actor)
            if (name.isBlank() || code.isBlank() || facultyName.isBlank() || room.isBlank()) {
                return Result.failure(IllegalArgumentException("All subject fields (name, code, faculty, room) are required."))
            }
            val list = _subjects.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index < 0) return Result.failure(IllegalArgumentException("Subject not found."))
            val updated = list[index].copy(
                name = name.trim(),
                code = code.trim().uppercase(),
                facultyName = facultyName.trim(),
                room = room.trim()
            )
            list[index] = updated
            _subjects.value = list
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteSubject(actor: User, id: String): Result<Unit> {
        return try {
            requireAdmin(actor)
            _subjects.value = _subjects.value.filterNot { it.id == id }
            _timetableSlots.value = _timetableSlots.value.filterNot { it.subjectId == id }
            _enrollments.value = _enrollments.value.filterNot { it.subjectId == id }
            _assignments.value = _assignments.value.filterNot { it.subjectId == id }
            _exams.value = _exams.value.filterNot { it.subjectId == id }
            _attendanceSessions.value = _attendanceSessions.value.filterNot { it.subjectId == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Faculty CRUD
    fun addFaculty(actor: User, fullName: String, email: String): Result<User> {
        return try {
            requireAdmin(actor)
            if (fullName.isBlank() || email.isBlank()) {
                return Result.failure(IllegalArgumentException("Faculty name and email are required."))
            }
            val newFaculty = User(
                id = "teach_${System.currentTimeMillis()}",
                fullName = fullName.trim(),
                role = "teacher",
                email = email.trim().lowercase(),
                createdAt = LocalDate.now().toString()
            )
            _allUsers.value = _allUsers.value + newFaculty
            Result.success(newFaculty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateFaculty(actor: User, id: String, fullName: String, email: String): Result<User> {
        return try {
            requireAdmin(actor)
            if (fullName.isBlank() || email.isBlank()) {
                return Result.failure(IllegalArgumentException("Faculty name and email are required."))
            }
            val list = _allUsers.value.toMutableList()
            val index = list.indexOfFirst { it.id == id && it.role == "teacher" }
            if (index < 0) return Result.failure(IllegalArgumentException("Faculty member not found."))
            val updated = list[index].copy(fullName = fullName.trim(), email = email.trim().lowercase())
            list[index] = updated
            _allUsers.value = list
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteFaculty(actor: User, id: String): Result<Unit> {
        return try {
            requireAdmin(actor)
            _allUsers.value = _allUsers.value.filterNot { it.id == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Students CRUD
    fun addStudent(actor: User, fullName: String, rollNo: String, email: String): Result<User> {
        return try {
            requireAdmin(actor)
            if (fullName.isBlank() || rollNo.isBlank() || email.isBlank()) {
                return Result.failure(IllegalArgumentException("Student name, roll number, and email are required."))
            }
            val cleanRoll = rollNo.trim().uppercase()
            val newStudent = User(
                id = "student_$cleanRoll",
                fullName = fullName.trim(),
                rollNo = cleanRoll,
                role = "student",
                email = email.trim().lowercase(),
                createdAt = LocalDate.now().toString()
            )
            _allUsers.value = _allUsers.value + newStudent

            // Auto enroll in common subjects
            val common = _subjects.value.filter { it.isCommon }
            _enrollments.value = _enrollments.value + common.map { SubjectEnrollment(newStudent.id, it.id) }
            Result.success(newStudent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateStudent(actor: User, id: String, fullName: String, rollNo: String, email: String): Result<User> {
        return try {
            requireAdmin(actor)
            if (fullName.isBlank() || rollNo.isBlank() || email.isBlank()) {
                return Result.failure(IllegalArgumentException("Student name, roll number, and email are required."))
            }
            val list = _allUsers.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index < 0) return Result.failure(IllegalArgumentException("Student not found."))
            val updated = list[index].copy(
                fullName = fullName.trim(),
                rollNo = rollNo.trim().uppercase(),
                email = email.trim().lowercase()
            )
            list[index] = updated
            _allUsers.value = list
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteStudent(actor: User, id: String): Result<Unit> {
        return try {
            requireAdmin(actor)
            _allUsers.value = _allUsers.value.filterNot { it.id == id }
            _enrollments.value = _enrollments.value.filterNot { it.studentId == id }
            _attendanceRecords.value = _attendanceRecords.value.filterNot { it.studentId == id }
            _assignmentStatuses.value = _assignmentStatuses.value.filterNot { it.studentId == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. Schedule (Timetable Slots) CRUD
    fun addTimetableSlot(actor: User, subjectId: String, dayOfWeek: Int, startTime: String, endTime: String, room: String): Result<TimetableSlot> {
        return try {
            requireAdmin(actor)
            if (subjectId.isBlank() || startTime.isBlank() || endTime.isBlank() || room.isBlank()) {
                return Result.failure(IllegalArgumentException("Subject, day, start time, end time, and room are required."))
            }
            if (dayOfWeek !in 1..7) {
                return Result.failure(IllegalArgumentException("Day of week must be between 1 (Monday) and 7 (Sunday)."))
            }
            val newSlot = TimetableSlot(
                id = "slot_${System.currentTimeMillis()}",
                subjectId = subjectId,
                dayOfWeek = dayOfWeek,
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                room = room.trim()
            )
            _timetableSlots.value = _timetableSlots.value + newSlot
            Result.success(newSlot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateTimetableSlot(actor: User, id: String, subjectId: String, dayOfWeek: Int, startTime: String, endTime: String, room: String): Result<TimetableSlot> {
        return try {
            requireAdmin(actor)
            if (subjectId.isBlank() || startTime.isBlank() || endTime.isBlank() || room.isBlank()) {
                return Result.failure(IllegalArgumentException("Subject, day, start time, end time, and room are required."))
            }
            val list = _timetableSlots.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index < 0) return Result.failure(IllegalArgumentException("Schedule slot not found."))
            val updated = list[index].copy(
                subjectId = subjectId,
                dayOfWeek = dayOfWeek,
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                room = room.trim()
            )
            list[index] = updated
            _timetableSlots.value = list
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteTimetableSlot(actor: User, id: String): Result<Unit> {
        return try {
            requireAdmin(actor)
            _timetableSlots.value = _timetableSlots.value.filterNot { it.id == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. Assignments CRUD
    fun addAssignmentAdmin(actor: User, title: String, subjectId: String, description: String, dueDate: String, priority: String): Result<Assignment> {
        return try {
            requireAdmin(actor)
            if (title.isBlank() || subjectId.isBlank() || description.isBlank() || dueDate.isBlank()) {
                return Result.failure(IllegalArgumentException("Subject, title, description, and due date are required."))
            }
            val sub = _subjects.value.find { it.id == subjectId }
            val newAsgn = Assignment(
                id = "asgn_${System.currentTimeMillis()}",
                title = title.trim(),
                subjectId = subjectId,
                subjectName = sub?.name ?: "Subject",
                description = description.trim(),
                dueDate = dueDate.trim(),
                priority = priority.ifBlank { "Medium" }
            )
            _assignments.value = _assignments.value + newAsgn
            Result.success(newAsgn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateAssignmentAdmin(actor: User, id: String, title: String, subjectId: String, description: String, dueDate: String, priority: String): Result<Assignment> {
        return try {
            requireAdmin(actor)
            if (title.isBlank() || subjectId.isBlank() || description.isBlank() || dueDate.isBlank()) {
                return Result.failure(IllegalArgumentException("Subject, title, description, and due date are required."))
            }
            val sub = _subjects.value.find { it.id == subjectId }
            val list = _assignments.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index < 0) return Result.failure(IllegalArgumentException("Assignment not found."))
            val updated = list[index].copy(
                title = title.trim(),
                subjectId = subjectId,
                subjectName = sub?.name ?: list[index].subjectName,
                description = description.trim(),
                dueDate = dueDate.trim(),
                priority = priority
            )
            list[index] = updated
            _assignments.value = list
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteAssignmentAdmin(actor: User, id: String): Result<Unit> {
        return try {
            requireAdmin(actor)
            _assignments.value = _assignments.value.filterNot { it.id == id }
            _assignmentStatuses.value = _assignmentStatuses.value.filterNot { it.assignmentId == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. Exams CRUD
    fun addExamAdmin(actor: User, title: String, subjectId: String, date: String, time: String, room: String, seatNo: String, syllabusTopics: String): Result<Exam> {
        return try {
            requireAdmin(actor)
            if (title.isBlank() || subjectId.isBlank() || date.isBlank() || time.isBlank() || room.isBlank()) {
                return Result.failure(IllegalArgumentException("Subject, title, date, time, and room are required."))
            }
            val sub = _subjects.value.find { it.id == subjectId }
            val newExam = Exam(
                id = "ex_${System.currentTimeMillis()}",
                subjectId = subjectId,
                subjectName = sub?.name ?: "Subject",
                subjectCode = sub?.code ?: "BCA301",
                title = title.trim(),
                date = date.trim(),
                time = time.trim(),
                room = room.trim(),
                seatNo = seatNo.ifBlank { "Unassigned" },
                syllabusTopics = syllabusTopics.ifBlank { "All units" }
            )
            _exams.value = _exams.value + newExam
            Result.success(newExam)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateExamAdmin(actor: User, id: String, title: String, subjectId: String, date: String, time: String, room: String, seatNo: String, syllabusTopics: String): Result<Exam> {
        return try {
            requireAdmin(actor)
            if (title.isBlank() || subjectId.isBlank() || date.isBlank() || time.isBlank() || room.isBlank()) {
                return Result.failure(IllegalArgumentException("Subject, title, date, time, and room are required."))
            }
            val sub = _subjects.value.find { it.id == subjectId }
            val list = _exams.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index < 0) return Result.failure(IllegalArgumentException("Exam not found."))
            val updated = list[index].copy(
                subjectId = subjectId,
                subjectName = sub?.name ?: list[index].subjectName,
                subjectCode = sub?.code ?: list[index].subjectCode,
                title = title.trim(),
                date = date.trim(),
                time = time.trim(),
                room = room.trim(),
                seatNo = seatNo.trim(),
                syllabusTopics = syllabusTopics.trim()
            )
            list[index] = updated
            _exams.value = list
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteExamAdmin(actor: User, id: String): Result<Unit> {
        return try {
            requireAdmin(actor)
            _exams.value = _exams.value.filterNot { it.id == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 7. Attendance Session Management (Admin)
    fun deleteAttendanceSessionAdmin(actor: User, sessionId: String): Result<Unit> {
        return try {
            requireAdmin(actor)
            _attendanceSessions.value = _attendanceSessions.value.filterNot { it.id == sessionId }
            _attendanceRecords.value = _attendanceRecords.value.filterNot { it.sessionId == sessionId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 8. Announcements CRUD
    fun addAnnouncementAdmin(actor: User, caption: String, postedBy: String): Result<com.example.model.EventPost> {
        return try {
            requireAdmin(actor)
            if (caption.isBlank()) {
                return Result.failure(IllegalArgumentException("Announcement message cannot be empty."))
            }
            val newEvent = com.example.model.EventPost(
                id = "ev_${System.currentTimeMillis()}",
                imageUrl = "",
                caption = caption.trim(),
                postedBy = postedBy.ifBlank { "Administration" },
                date = LocalDate.now().toString()
            )
            _events.value = listOf(newEvent) + _events.value
            Result.success(newEvent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateAnnouncementAdmin(actor: User, id: String, caption: String): Result<com.example.model.EventPost> {
        return try {
            requireAdmin(actor)
            if (caption.isBlank()) {
                return Result.failure(IllegalArgumentException("Announcement message cannot be empty."))
            }
            val list = _events.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index < 0) return Result.failure(IllegalArgumentException("Announcement not found."))
            val updated = list[index].copy(caption = caption.trim())
            list[index] = updated
            _events.value = list
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteAnnouncementAdmin(actor: User, id: String): Result<Unit> {
        return try {
            requireAdmin(actor)
            _events.value = _events.value.filterNot { it.id == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
