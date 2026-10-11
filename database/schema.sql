/*Create database*/
CREATE DATABASE IF NOT EXISTS fot_mis;
USE fot_mis;


/*1.User Table*/
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contact_no VARCHAR(15),
    profile_pic VARCHAR(255),
    role ENUM('ADMIN', 'LECTURER', 'TECHNICAL_OFFICER', 'STUDENT') NOT NULL
);


/*2. Admin Table*/
CREATE TABLE IF NOT EXISTS admin (
    admin_id VARCHAR(50) PRIMARY KEY,
    FOREIGN KEY (admin_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);


/*3. Lecturer Table*/
CREATE TABLE IF NOT EXISTS lecturer (
    lecturer_id VARCHAR(50) PRIMARY KEY,
    department VARCHAR(50) NOT NULL,
    FOREIGN KEY (lecturer_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);


/*4.Technical Officer Table*/
CREATE TABLE IF NOT EXISTS technical_officer (
    to_id VARCHAR(50) PRIMARY KEY,
    department VARCHAR(50) NOT NULL,
    FOREIGN KEY (to_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);


/*5.Undergraduate (Student) Table*/
CREATE TABLE IF NOT EXISTS undergraduate (
    student_id VARCHAR(50) PRIMARY KEY,
    reg_no VARCHAR(20) NOT NULL UNIQUE, /* e.g., TG/2024/1001*/
    department VARCHAR(50) NOT NULL,
    batch VARCHAR(10) NOT NULL,
    is_repeat BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (student_id) REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);

/*6.Course table*/
CREATE TABLE IF NOT EXISTS course (
    course_id VARCHAR(10) PRIMARY KEY, -- e.g., ICT2132
    course_name VARCHAR(100) NOT NULL,
    credit INT NOT NULL,
    type VARCHAR(20) DEFAULT 'Core', /*Compulsory / Optional*/
    theory_credits INT DEFAULT 1,
    practical_credits INT DEFAULT 1
);


/*7.Course Material Table*/
CREATE TABLE IF NOT EXISTS course_material (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id VARCHAR(10) NOT NULL,
    title VARCHAR(100) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    uploaded_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE ON UPDATE CASCADE
);


/*8.Notice Table*/
CREATE TABLE IF NOT EXISTS notice (
    notice_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    posted_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_role VARCHAR(20) DEFAULT 'ALL'
);


/*9.Timetable Table*/
CREATE TABLE IF NOT EXISTS timetable (
    timetable_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id VARCHAR(10) NOT NULL,
    department VARCHAR(50) NOT NULL,
    session_type ENUM('THEORY', 'PRACTICAL') NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    venue_id VARCHAR(50),
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE ON UPDATE CASCADE
);


/*10.Attendance Table (15 Sessions per module component)*/
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    course_id VARCHAR(10) NOT NULL,
    session_no INT NOT NULL CHECK (session_no BETWEEN 1 AND 15),
    session_type ENUM('THEORY', 'PRACTICAL') NOT NULL,
    session_date DATE NOT NULL,
    session_duration INT DEFAULT 2, /*2 hours per session*/
    status ENUM('PRESENT', 'ABSENT') NOT NULL,
    FOREIGN KEY (student_id) REFERENCES undergraduate(student_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE ON UPDATE CASCADE
);


/*11.Medical Table*/
CREATE TABLE IF NOT EXISTS medical (
    medical_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    course_id VARCHAR(10) NOT NULL,
    session_no INT NOT NULL,
    session_type ENUM('THEORY', 'PRACTICAL') NOT NULL,
    medical_date DATE NOT NULL,
    submission_date DATE DEFAULT (CURRENT_DATE),
    description TEXT,
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    FOREIGN KEY (student_id) REFERENCES undergraduate(student_id) ON UPDATE CASCADE
    ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
    ON UPDATE CASCADE
);


/*12.Assessment Table (CA structure)*/
CREATE TABLE IF NOT EXISTS assessment (
    assessment_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,  /*Quiz 1, Assignment 1*/
    weightage DOUBLE NOT NULL,  /*Percentage contributing to CA*/
    FOREIGN KEY (course_id) REFERENCES course(course_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);


/*13. Mark Table*/
CREATE TABLE IF NOT EXISTS mark (
    mark_id INT AUTO_INCREMENT PRIMARY KEY,
    assessment_id INT NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    score DOUBLE NOT NULL CHECK (score BETWEEN 0 AND 100),
    FOREIGN KEY (assessment_id) REFERENCES assessment(assessment_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (student_id) REFERENCES undergraduate(student_id) ON DELETE CASCADE ON UPDATE CASCADE
);

/*14.Undergraduate Course Enrollment (ERD: Enroll in)*/
CREATE TABLE IF NOT EXISTS student_course (
    student_id VARCHAR(50) NOT NULL,
    course_id VARCHAR(10) NOT NULL,
    PRIMARY KEY (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES undergraduate(student_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE ON UPDATE CASCADE
);


/*Final Exam & Overall Result Table*/
CREATE TABLE IF NOT EXISTS result (
    result_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    course_id VARCHAR(10) NOT NULL,
    ca_total DOUBLE DEFAULT 0.0,
    final_exam_score DOUBLE DEFAULT 0.0,
    total_score DOUBLE DEFAULT 0.0,
    grade VARCHAR(3), -- e.g., A+, A, B, etc.
    grade_point DOUBLE DEFAULT 0.0,
    FOREIGN KEY (student_id) REFERENCES undergraduate(student_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE ON UPDATE CASCADE
);
-- 1 Admin
INSERT INTO users (user_id, password, first_name, last_name, email, role)
VALUES( 'admin1','admin123', 'Sunil', 'Perera', 'admin@fot.ruh.ac.lk', 'ADMIN');
INSERT INTO admin (admin_id) VALUES ('admin1');

-- 5 Lecturers
INSERT INTO users (user_id,  password, first_name, last_name, email, role) VALUES
('lec1', 'lec123', 'Kamal', 'Silva', 'kamal@fot.ruh.ac.lk', 'LECTURER'),
('lec2', 'lec123', 'Nimal', 'Fernando', 'nimal@fot.ruh.ac.lk', 'LECTURER'),
('lec3', 'lec123', 'Saman', 'Kumara', 'saman@fot.ruh.ac.lk', 'LECTURER'),
('lec4', 'lec123', 'Ruwan', 'Jayasinghe', 'ruwan@fot.ruh.ac.lk', 'LECTURER'),
('lec5', 'lec123', 'Amal', 'Dissanayake', 'amal@fot.ruh.ac.lk', 'LECTURER');
INSERT INTO lecturer (lecturer_id, department) VALUES
('lec1', 'ICT'), ('lec2', 'ICT'), ('lec3', 'ET'), ('lec4', 'BST'), ('lec5', 'ICT');

-- 4 Technical Officers
INSERT INTO users (user_id,  password, first_name, last_name, email, role) VALUES
('to1', 'to123', 'Mahesh', 'Bandara', 'mahesh@fot.ruh.ac.lk', 'TECHNICAL_OFFICER'),
('to2', 'to123', 'Pradeep', 'Gunawardana', 'pradeep@fot.ruh.ac.lk', 'TECHNICAL_OFFICER'),
('to3', 'to123', 'Kavinda', 'Senanayake', 'kavinda@fot.ruh.ac.lk', 'TECHNICAL_OFFICER'),
('to4', 'to123', 'Nuwan', 'Wickramasinghe', 'nuwan@fot.ruh.ac.lk', 'TECHNICAL_OFFICER');
INSERT INTO technical_officer (to_id, department) VALUES
('to1', 'ICT'), ('to2', 'ICT'), ('to3', 'ET'), ('to4', 'BST');

-- 20 Undergraduates with repeaters
INSERT INTO users (user_id, password, first_name, last_name, email, role) VALUES
('tg1001', 'stu123', 'Kasun', 'Rajapakshe', 'tg1001@fot.ruh.ac.lk', 'STUDENT'),
('tg1002', 'stu123', 'Gayan', 'Prasanna', 'tg1002@fot.ruh.ac.lk', 'STUDENT'),
('tg1003', 'stu123', 'Chamara', 'Sampath', 'tg1003@fot.ruh.ac.lk', 'STUDENT'),
('tg1004', 'stu123', 'Sahan', 'Madushanka', 'tg1004@fot.ruh.ac.lk', 'STUDENT'),
('tg1005', 'stu123', 'Dilshan', 'Chathuranga', 'tg1005@fot.ruh.ac.lk', 'STUDENT'),
('tg1006', 'stu123', 'Isuru', 'Kumarasinghe', 'tg1006@fot.ruh.ac.lk', 'STUDENT'),
('tg1007', 'stu123', 'Sandun', 'Wijesinghe', 'tg1007@fot.ruh.ac.lk', 'STUDENT'),
('tg1008', 'stu123', 'Nipuna', 'Tharaka', 'tg1008@fot.ruh.ac.lk', 'STUDENT'),
('tg1009', 'stu123', 'Lakshan', 'Sandaruwan', 'tg1009@fot.ruh.ac.lk', 'STUDENT'),
('tg1010', 'stu123', 'Dhanushka', 'Ranasinghe', 'tg1010@fot.ruh.ac.lk', 'STUDENT'),
('tg1011', 'stu123', 'Tharindu', 'Mendis', 'tg1011@fot.ruh.ac.lk', 'STUDENT'),
('tg1012', 'stu123', 'Dasun', 'Gunawardhana', 'tg1012@fot.ruh.ac.lk', 'STUDENT'),
('tg1013', 'stu123', 'thesaru', 'perera', 'tg1013@fot.ruh.ac.lk', 'STUDENT'),
('tg1014', 'stu123', 'Charitha', 'sampath', 'tg1014@fot.ruh.ac.lk', 'STUDENT'),
('tg1015', 'stu123', 'Nipun', 'Nissanka', 'tg1015@fot.ruh.ac.lk', 'STUDENT'),
('tg1016', 'stu123', 'Samadhi', 'Mandhira', 'tg1016@fot.ruh.ac.lk', 'STUDENT'),
('tg1017', 'stu123', 'Buddhika', 'Rajapaksa', 'tg1017@fot.ruh.ac.lk', 'STUDENT'),
('tg1018', 'stu123', 'Lalantha', 'Kumara', 'tg1018@fot.ruh.ac.lk', 'STUDENT'),
('tg1019', 'stu123', 'Maheesha', 'Theekshani', 'tg1019@fot.ruh.ac.lk', 'STUDENT'),
('tg1020', 'stu123', 'Samudhini', 'Sewmini', 'tg1020@fot.ruh.ac.lk', 'STUDENT');

INSERT INTO undergraduate (student_id, reg_no, department, batch, is_repeat) VALUES
('tg1001', 'TG/2022/1001', 'ICT', '2021/2022', FALSE),
('tg1002', 'TG/2022/1002', 'ICT', '2021/2022', FALSE),
('tg1003', 'TG/2022/1003', 'ICT', '2021/2022', FALSE),
('tg1004', 'TG/2022/1004', 'ICT', '2021/2022', FALSE),
('tg1005', 'TG/2022/1005', 'ICT', '2021/2022', FALSE),
('tg1006', 'TG/2022/1006', 'ICT', '2021/2022', FALSE),
('tg1007', 'TG/2022/1007', 'ICT', '2021/2022', FALSE),
('tg1008', 'TG/2022/1008', 'ICT', '2021/2022', FALSE),
('tg1009', 'TG/2022/1009', 'ICT', '2021/2022', FALSE),
('tg1010', 'TG/2022/1010', 'ICT', '2021/2022', FALSE),
('tg1011', 'TG/2022/1011', 'ICT', '2021/2022', FALSE),
('tg1012', 'TG/2022/1012', 'ICT', '2021/2022', FALSE),
('tg1013', 'TG/2022/1013', 'ICT', '2021/2022', FALSE),
('tg1014', 'TG/2022/1014', 'ICT', '2021/2022', FALSE),
('tg1015', 'TG/2022/1015', 'ICT', '2021/2022', FALSE),
('tg1016', 'TG/2022/1016', 'ICT', '2021/2022', FALSE),
('tg1017', 'TG/2022/1017', 'ICT', '2021/2022', FALSE),
('tg1018', 'TG/2022/1018', 'ICT', '2021/2022', FALSE),
('tg1019', 'TG/2020/1019', 'ICT', '2019/2020', TRUE),  -- Repeat Student 1
('tg1020', 'TG/2020/1020', 'ICT', '2019/2020', TRUE);  -- Repeat Student 2

-- Sample Course
INSERT INTO course (course_id, course_name, credit, has_theory, has_practical)
VALUES ('ICT2132', 'Object Oriented Programming Practicum', 2, TRUE, TRUE);

INSERT INTO timetable (course_id, department, session_type, day_of_week, start_time, end_time, venue_id) VALUES
('ICT2113', 'ICT', 'THEORY', 'MONDAY', '09:00:00', '11:00:00', 'LAB-12'),
('ICT2113', 'ICT', 'PRACTICAL', 'MONDAY', '11:00:00', '01:00:00', 'LAB-12'),
('ICT2142', 'ICT', 'THEORY', 'TUESDAY', '09:00:00', '10:00:00', 'NEW-LAB'),
('ICT2142', 'ICT', 'PRACTICAL', 'TUESDAY', '10:00:00', '12:00:00', 'NEW-LAB'),
('ICT2152', 'ICT', 'THEORY', 'TUESDAY', '01:00:00', '03:00:00', 'LAB-12'),
('TCS2112', 'ICT', 'TUTORIAL', 'WEDNESDAY', '08:00:00', '09:00:00', 'ICT-HARDWARE-LAB'),
('ICT2121', 'ICT', 'THEORY', 'WEDNESDAY', '10:00:00', '12:00:00', 'ICT-HARDWARE-LAB'),
('ICT2122', 'ICT', 'THEORY', 'THURSDAY', '08:00:00', '10:00:00', 'LAB-12'),
('ICT2122', 'ICT', 'PRACTICAL', 'THURSDAY', '10:00:00', '12:00:00', 'LAB-12'),
('ICT2122', 'ICT', 'PRACTICAL', 'THURSDAY', '01:00:00', '04:00:00', 'LAB-12'),
('TCS2112', 'ICT', 'THEORY', 'FRIDAY', '08:00:00', '10:00:00', 'ICT-HARDWARE-LAB'),
('ICT2162', 'ICT', 'THEORY', 'FRIDAY', '10:00:00', '12:00:00', 'NEW-LAB'),
('ENG2122', 'ICT', 'THEORY', 'FRIDAY', '01:00:00', '03:00:00', 'ICT-HARDWARE-LAB');