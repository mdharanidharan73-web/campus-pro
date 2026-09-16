-- Supabase Schema for ClassHub
-- Run this in your Supabase SQL Editor

-- 1. Users Profile Table (Tied to Supabase Auth)
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    roll_no TEXT UNIQUE,
    role TEXT NOT NULL CHECK (role IN ('student', 'cr', 'teacher')),
    email TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 2. Subjects Table
CREATE TABLE subjects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    faculty_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    faculty_name TEXT,
    is_common BOOLEAN DEFAULT true
);

-- 3. Enrollments Table (for Language Electives / specific tracks)
CREATE TABLE enrollments (
    student_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
    PRIMARY KEY (student_id, subject_id)
);

-- 4. CR Permissions
CREATE TABLE cr_permissions (
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
    can_mark_attendance BOOLEAN DEFAULT false,
    can_moderate_room BOOLEAN DEFAULT false,
    PRIMARY KEY (user_id, subject_id)
);

-- 5. Timetable Slots
CREATE TABLE timetable_slots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room TEXT NOT NULL
);

-- 6. Timetable Overrides (Room changes, Cancellations)
CREATE TABLE timetable_overrides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slot_id UUID REFERENCES timetable_slots(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('cancelled', 'room_changed', 'rescheduled')),
    note TEXT
);

-- 7. Attendance Sessions (A record of a class happening)
CREATE TABLE attendance_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    marked_by UUID REFERENCES profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 8. Attendance Records (Student presence per session)
CREATE TABLE attendance_records (
    session_id UUID REFERENCES attendance_sessions(id) ON DELETE CASCADE,
    student_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    status TEXT NOT NULL CHECK (status IN ('present', 'absent')),
    PRIMARY KEY (session_id, student_id)
);

-- 9. Chat Rooms
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type TEXT NOT NULL CHECK (type IN ('global', 'subject', 'dm')),
    name TEXT NOT NULL,
    subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
    participant_ids UUID[] -- array of user ids for DMs
);

-- 10. Chat Messages
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID REFERENCES rooms(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    sender_name TEXT NOT NULL,
    content TEXT NOT NULL,
    is_anonymous BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL,
    is_pinned BOOLEAN DEFAULT false,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- 11. Assignments
CREATE TABLE assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
    description TEXT,
    due_date DATE
);

-- 12. Assignment Status
CREATE TABLE assignment_status (
    assignment_id UUID REFERENCES assignments(id) ON DELETE CASCADE,
    student_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    is_done BOOLEAN DEFAULT false,
    submission_file TEXT,
    grade TEXT,
    feedback TEXT,
    submitted_at DATE,
    PRIMARY KEY (assignment_id, student_id)
);

-- 13. Resource Shelf
CREATE TABLE resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
    file_type TEXT NOT NULL,
    date_added DATE DEFAULT CURRENT_DATE
);

-- 14. App Settings (Global singleton)
CREATE TABLE app_settings (
    id INTEGER PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    min_attendance_percent INTEGER DEFAULT 75,
    semester_end_date DATE NOT NULL
);

-- 15. Events
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    image_url TEXT,
    caption TEXT NOT NULL,
    posted_by TEXT NOT NULL,
    date DATE DEFAULT CURRENT_DATE
);

-- ==========================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==========================================

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public profiles are viewable by everyone."
  ON profiles FOR SELECT USING (true);

ALTER TABLE attendance_records ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Students can view their own attendance."
  ON attendance_records FOR SELECT 
  USING (auth.uid() = student_id);

CREATE POLICY "Teachers can view all attendance."
  ON attendance_records FOR SELECT
  USING (EXISTS (SELECT 1 FROM profiles WHERE id = auth.uid() AND role = 'teacher'));

ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Messages can be read by room participants"
  ON messages FOR SELECT USING (true); -- simplify for demo

CREATE POLICY "Users can insert their own messages"
  ON messages FOR INSERT WITH CHECK (auth.uid() = sender_id);

-- Realtime Setup
alter publication supabase_realtime add table messages;
alter publication supabase_realtime add table attendance_records;
