-- ============================================================
--  Member 2 — SQL Additions / Test Data
--  These statements extend the existing schema.sql.
--  Run AFTER the main schema.sql has been applied.
-- ============================================================

-- Sample attendance records for tg1001 in ICT2132
-- 15 THEORY sessions (2 hrs each), 15 PRACTICAL sessions (2 hrs each)

INSERT INTO student_course (student_id, course_id) VALUES
('tg1001','ICT2132'),('tg1002','ICT2132'),('tg1003','ICT2132'),
('tg1004','ICT2132'),('tg1005','ICT2132')
ON DUPLICATE KEY UPDATE student_id = student_id;

-- ── THEORY sessions for tg1001 ───────────────────────────────────────────
INSERT INTO attendance
  (student_id, course_id, session_no, session_type, session_date, session_duration, status)
VALUES
('tg1001','ICT2132', 1,'THEORY','2026-02-03',2,'PRESENT'),
('tg1001','ICT2132', 2,'THEORY','2026-02-10',2,'PRESENT'),
('tg1001','ICT2132', 3,'THEORY','2026-02-17',2,'PRESENT'),
('tg1001','ICT2132', 4,'THEORY','2026-02-24',2,'ABSENT'),
('tg1001','ICT2132', 5,'THEORY','2026-03-03',2,'PRESENT'),
('tg1001','ICT2132', 6,'THEORY','2026-03-10',2,'PRESENT'),
('tg1001','ICT2132', 7,'THEORY','2026-03-17',2,'PRESENT'),
('tg1001','ICT2132', 8,'THEORY','2026-03-24',2,'ABSENT'),
('tg1001','ICT2132', 9,'THEORY','2026-03-31',2,'PRESENT'),
('tg1001','ICT2132',10,'THEORY','2026-04-07',2,'PRESENT'),
('tg1001','ICT2132',11,'THEORY','2026-04-14',2,'PRESENT'),
('tg1001','ICT2132',12,'THEORY','2026-04-21',2,'PRESENT'),
('tg1001','ICT2132',13,'THEORY','2026-04-28',2,'PRESENT'),
('tg1001','ICT2132',14,'THEORY','2026-05-05',2,'PRESENT'),
('tg1001','ICT2132',15,'THEORY','2026-05-12',2,'PRESENT')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- ── PRACTICAL sessions for tg1001 ────────────────────────────────────────
INSERT INTO attendance
  (student_id, course_id, session_no, session_type, session_date, session_duration, status)
VALUES
('tg1001','ICT2132', 1,'PRACTICAL','2026-02-03',2,'PRESENT'),
('tg1001','ICT2132', 2,'PRACTICAL','2026-02-10',2,'PRESENT'),
('tg1001','ICT2132', 3,'PRACTICAL','2026-02-17',2,'ABSENT'),
('tg1001','ICT2132', 4,'PRACTICAL','2026-02-24',2,'PRESENT'),
('tg1001','ICT2132', 5,'PRACTICAL','2026-03-03',2,'PRESENT'),
('tg1001','ICT2132', 6,'PRACTICAL','2026-03-10',2,'ABSENT'),
('tg1001','ICT2132', 7,'PRACTICAL','2026-03-17',2,'PRESENT'),
('tg1001','ICT2132', 8,'PRACTICAL','2026-03-24',2,'PRESENT'),
('tg1001','ICT2132', 9,'PRACTICAL','2026-03-31',2,'PRESENT'),
('tg1001','ICT2132',10,'PRACTICAL','2026-04-07',2,'PRESENT'),
('tg1001','ICT2132',11,'PRACTICAL','2026-04-14',2,'PRESENT'),
('tg1001','ICT2132',12,'PRACTICAL','2026-04-21',2,'ABSENT'),
('tg1001','ICT2132',13,'PRACTICAL','2026-04-28',2,'PRESENT'),
('tg1001','ICT2132',14,'PRACTICAL','2026-05-05',2,'PRESENT'),
('tg1001','ICT2132',15,'PRACTICAL','2026-05-12',2,'PRESENT')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- ── THEORY sessions for tg1002 ───────────────────────────────────────────
INSERT INTO attendance
  (student_id, course_id, session_no, session_type, session_date, session_duration, status)
VALUES
('tg1002','ICT2132', 1,'THEORY','2026-02-03',2,'PRESENT'),
('tg1002','ICT2132', 2,'THEORY','2026-02-10',2,'ABSENT'),
('tg1002','ICT2132', 3,'THEORY','2026-02-17',2,'ABSENT'),
('tg1002','ICT2132', 4,'THEORY','2026-02-24',2,'ABSENT'),
('tg1002','ICT2132', 5,'THEORY','2026-03-03',2,'PRESENT'),
('tg1002','ICT2132', 6,'THEORY','2026-03-10',2,'PRESENT'),
('tg1002','ICT2132', 7,'THEORY','2026-03-17',2,'ABSENT'),
('tg1002','ICT2132', 8,'THEORY','2026-03-24',2,'ABSENT'),
('tg1002','ICT2132', 9,'THEORY','2026-03-31',2,'PRESENT'),
('tg1002','ICT2132',10,'THEORY','2026-04-07',2,'PRESENT'),
('tg1002','ICT2132',11,'THEORY','2026-04-14',2,'ABSENT'),
('tg1002','ICT2132',12,'THEORY','2026-04-21',2,'PRESENT'),
('tg1002','ICT2132',13,'THEORY','2026-04-28',2,'PRESENT'),
('tg1002','ICT2132',14,'THEORY','2026-05-05',2,'PRESENT'),
('tg1002','ICT2132',15,'THEORY','2026-05-12',2,'PRESENT')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- ── PRACTICAL sessions for tg1002 ────────────────────────────────────────
INSERT INTO attendance
  (student_id, course_id, session_no, session_type, session_date, session_duration, status)
VALUES
('tg1002','ICT2132', 1,'PRACTICAL','2026-02-03',2,'PRESENT'),
('tg1002','ICT2132', 2,'PRACTICAL','2026-02-10',2,'PRESENT'),
('tg1002','ICT2132', 3,'PRACTICAL','2026-02-17',2,'PRESENT'),
('tg1002','ICT2132', 4,'PRACTICAL','2026-02-24',2,'PRESENT'),
('tg1002','ICT2132', 5,'PRACTICAL','2026-03-03',2,'PRESENT'),
('tg1002','ICT2132', 6,'PRACTICAL','2026-03-10',2,'PRESENT'),
('tg1002','ICT2132', 7,'PRACTICAL','2026-03-17',2,'ABSENT'),
('tg1002','ICT2132', 8,'PRACTICAL','2026-03-24',2,'ABSENT'),
('tg1002','ICT2132', 9,'PRACTICAL','2026-03-31',2,'PRESENT'),
('tg1002','ICT2132',10,'PRACTICAL','2026-04-07',2,'PRESENT'),
('tg1002','ICT2132',11,'PRACTICAL','2026-04-14',2,'PRESENT'),
('tg1002','ICT2132',12,'PRACTICAL','2026-04-21',2,'PRESENT'),
('tg1002','ICT2132',13,'PRACTICAL','2026-04-28',2,'PRESENT'),
('tg1002','ICT2132',14,'PRACTICAL','2026-05-05',2,'PRESENT'),
('tg1002','ICT2132',15,'PRACTICAL','2026-05-12',2,'PRESENT')
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- ── Medical submissions ───────────────────────────────────────────────────
-- tg1001 submits medical for Theory session 4 (ABSENT above)
INSERT INTO medical
  (student_id, course_id, session_no, session_type, medical_date, submission_date, description, approval_status)
VALUES
('tg1001','ICT2132',4,'THEORY','2026-02-24','2026-03-01','Fever — hospital report attached','PENDING'),
('tg1001','ICT2132',8,'THEORY','2026-03-24','2026-03-27','Medical certificate from doctor','PENDING'),
('tg1001','ICT2132',3,'PRACTICAL','2026-02-17','2026-02-20','Dengue fever treatment','APPROVED'),
('tg1002','ICT2132',2,'THEORY','2026-02-10','2026-02-14','Accident — clinic report','PENDING'),
('tg1002','ICT2132',3,'THEORY','2026-02-17','2026-02-19','Follow-up check-up','REJECTED')
ON DUPLICATE KEY UPDATE description = VALUES(description);
