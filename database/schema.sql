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
