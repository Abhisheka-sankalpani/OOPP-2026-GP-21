package dao;

import exception.InvalidSessionException;
import db.DBConnection;
import model.Attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    // Retrieve attendance list for all courses for a given student
    public List<Attendance> getAttendanceByStudentId(String studentId) {
        List<Attendance> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = "SELECT c.course_id, c.course_name, "
                + "SUM(CASE WHEN a.session_type = 'THEORY' AND a.status = 'PRESENT' THEN 1 ELSE 0 END) AS theory_present, "
                + "SUM(CASE WHEN a.session_type = 'THEORY' THEN 1 ELSE 0 END) AS theory_total, "
                + "SUM(CASE WHEN a.session_type = 'PRACTICAL' AND a.status = 'PRESENT' THEN 1 ELSE 0 END) AS practical_present, "
                + "SUM(CASE WHEN a.session_type = 'PRACTICAL' THEN 1 ELSE 0 END) AS practical_total "
                + "FROM course c "
                + "LEFT JOIN attendance a ON c.course_id = a.course_id AND a.student_id = ? "
                + "GROUP BY c.course_id, c.course_name "
                + "ORDER BY c.course_id";

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String courseId = rs.getString("course_id");
                String courseName = rs.getString("course_name");
                int theoryPresent = rs.getInt("theory_present");
                int theoryTotal = rs.getInt("theory_total");
                int practicalPresent = rs.getInt("practical_present");
                int practicalTotal = rs.getInt("practical_total");

                int approvedMedicals = getApprovedMedicalCount(studentId, courseId);
                int totalSessions = theoryTotal + practicalTotal;
                int totalAttended = theoryPresent + practicalPresent + approvedMedicals;

                if (totalAttended > totalSessions && totalSessions > 0) {
                    totalAttended = totalSessions;
                }

                double percentage = totalSessions > 0
                        ? Math.round(((double) totalAttended / totalSessions) * 1000.0) / 10.0
                        : 0.0;

                Attendance att = new Attendance(
                        courseId, courseName, theoryPresent, theoryTotal, practicalPresent, practicalTotal, percentage
                );
                list.add(att);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching attendance list: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
            }
        }
        return list;
    }

    // Get attendance percentage for a specific course (used by eligibility check)
    public double getAttendancePercentage(String studentId, String courseId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        double finalPercentage = 0.0;

        String attSql = "SELECT COUNT(*) AS total_sessions, "
                + "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) AS present_count "
                + "FROM attendance WHERE student_id = ? AND course_id = ?";

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(attSql);
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int totalSessions = rs.getInt("total_sessions");
                int presentCount = rs.getInt("present_count");

                if (totalSessions > 0) {
                    int approvedMedicals = getApprovedMedicalCount(studentId, courseId);
                    int effectiveAttended = presentCount + approvedMedicals;
                    if (effectiveAttended > totalSessions) {
                        effectiveAttended = totalSessions;
                    }
                    finalPercentage = Math.round(((double) effectiveAttended / totalSessions) * 1000.0) / 10.0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error calculating attendance percentage: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
            }
        }
        return finalPercentage;
    }

    // Helper method to count approved medical submissions
    public int getApprovedMedicalCount(String studentId, String courseId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        String medSql = "SELECT COUNT(DISTINCT m.session_no, m.session_type) AS approved_count "
                + "FROM medical m "
                + "JOIN attendance a ON m.student_id = a.student_id "
                + "  AND m.course_id = a.course_id "
                + "  AND m.session_no = a.session_no "
                + "  AND m.session_type = a.session_type "
                + "WHERE m.student_id = ? AND m.course_id = ? "
                + "  AND m.approval_status = 'APPROVED' AND a.status = 'ABSENT'";

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(medSql);
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("approved_count");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching medical count: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
            }
        }
        return count;
    }

    // === Technical Officer attendance operations (1752) ===

    // ─── Constants ────────────────────────────────────────────────────────
    private static final int MIN_SESSION = 1;
    private static final int MAX_SESSION = 15;

    // ─── Record Attendance ────────────────────────────────────────────────

    /**
     * Inserts or updates one attendance record for a student.
     *
     * @param attendance the record to save
     * @throws InvalidSessionException if sessionNo is outside [1, 15]
     */
    public void recordAttendance(Attendance attendance) throws InvalidSessionException {
        // Exception Handling — validate session number
        if (attendance.getSessionNo() < MIN_SESSION || attendance.getSessionNo() > MAX_SESSION) {
            throw new InvalidSessionException(attendance.getSessionNo());
        }

        // Use INSERT … ON DUPLICATE KEY UPDATE so that re-running does not
        // create duplicate rows (student_id, course_id, session_no, session_type are unique).
        String sql = "INSERT INTO attendance (student_id, course_id, session_no, session_type, " +
                     "session_date, session_duration, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE session_date = VALUES(session_date), " +
                     "session_duration = VALUES(session_duration), status = VALUES(status)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, attendance.getStudentId());
            stmt.setString(2, attendance.getCourseId());
            stmt.setInt   (3, attendance.getSessionNo());
            stmt.setString(4, attendance.getSessionType());
            stmt.setDate  (5, attendance.getSessionDate());
            stmt.setInt   (6, attendance.getSessionDuration() > 0
                              ? attendance.getSessionDuration() : 2);
            stmt.setString(7, attendance.getStatus());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error recording attendance: " + e.getMessage());
        }
    }

    // ─── Bulk-record all 15 sessions for one student/course/type ─────────

    /**
     * Saves a list of attendance records (e.g. all 15 theory sessions at once).
     *
     * @param records the list to save
     * @throws InvalidSessionException if any record has an invalid session number
     */
    public void recordAllSessions(List<Attendance> records) throws InvalidSessionException {
        for (Attendance a : records) {
            recordAttendance(a);
        }
    }

    // ─── Read ─────────────────────────────────────────────────────────────

    /**
     * Returns all attendance rows for a student in a given course.
     *
     * @param studentId the student's user_id
     * @param courseId  the course code (e.g. "ICT2132")
     * @return list of Attendance objects (may be empty)
     */
    public List<Attendance> getAttendanceByStudentAndCourse(String studentId, String courseId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.attendance_id, a.student_id, a.course_id, c.course_name, " +
                     "a.session_no, a.session_type, a.session_date, a.session_duration, a.status " +
                     "FROM attendance a " +
                     "INNER JOIN course c ON a.course_id = c.course_id " +
                     "WHERE a.student_id = ? AND a.course_id = ? " +
                     "ORDER BY a.session_type, a.session_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Attendance a = new Attendance(
                        rs.getInt   ("attendance_id"),
                        rs.getString("student_id"),
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt   ("session_no"),
                        rs.getString("session_type"),
                        rs.getDate  ("session_date"),
                        rs.getInt   ("session_duration"),
                        rs.getString("status")
                    );
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns every attendance row for every student enrolled in a course.
     * Used by the Technical Officer to see the full class list.
     *
     * @param courseId the course code
     * @return list of Attendance objects
     */
    public List<Attendance> getAttendanceByCourse(String courseId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.attendance_id, a.student_id, a.course_id, c.course_name, " +
                     "a.session_no, a.session_type, a.session_date, a.session_duration, a.status " +
                     "FROM attendance a " +
                     "INNER JOIN course c ON a.course_id = c.course_id " +
                     "WHERE a.course_id = ? " +
                     "ORDER BY a.student_id, a.session_type, a.session_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Attendance a = new Attendance(
                        rs.getInt   ("attendance_id"),
                        rs.getString("student_id"),
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt   ("session_no"),
                        rs.getString("session_type"),
                        rs.getDate  ("session_date"),
                        rs.getInt   ("session_duration"),
                        rs.getString("status")
                    );
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching attendance by course: " + e.getMessage());
        }
        return list;
    }

    // ─── Aggregated counts ────────────────────────────────────────────────

    /**
     * Returns the number of sessions attended by a student in a given course
     * and session type ("THEORY" or "PRACTICAL").
     */
    public int countAttended(String studentId, String courseId, String sessionType) {
        String sql = "SELECT COUNT(*) FROM attendance " +
                     "WHERE student_id = ? AND course_id = ? " +
                     "AND session_type = ? AND status = 'PRESENT'";
        return runCountQuery(sql, studentId, courseId, sessionType);
    }

    /**
     * Returns the total number of sessions held for a course and session type.
     * (i.e., how many rows exist with any status)
     */
    public int countTotalSessions(String studentId, String courseId, String sessionType) {
        String sql = "SELECT COUNT(*) FROM attendance " +
                     "WHERE student_id = ? AND course_id = ? " +
                     "AND session_type = ?";
        return runCountQuery(sql, studentId, courseId, sessionType);
    }

    // Helper to avoid duplicated SQL-count boilerplate
    private int runCountQuery(String sql, String studentId, String courseId, String sessionType) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            stmt.setString(3, sessionType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error running count query: " + e.getMessage());
        }
        return 0;
    }

    // ─── Get all students in a course ────────────────────────────────────

    /**
     * Returns a distinct list of student IDs enrolled in the given course
     * (pulled from the attendance table — those who have at least one record).
     */
    public List<String> getStudentIdsByCourse(String courseId) {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT student_id FROM attendance WHERE course_id = ? ORDER BY student_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) ids.add(rs.getString("student_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student IDs: " + e.getMessage());
        }
        return ids;
    }

    /**
     * Returns a distinct list of student IDs from the student_course enrolment table.
     */
    public List<String> getEnrolledStudentIds(String courseId) {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT student_id FROM student_course WHERE course_id = ? ORDER BY student_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) ids.add(rs.getString("student_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching enrolled student IDs: " + e.getMessage());
        }
        return ids;
    }
}
