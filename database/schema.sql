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