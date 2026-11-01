package dao;

import database.DBConnection;
import service.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Enrollment operations for the existing FOT-MIS course/undergraduate/student_course tables. */
public class CourseEnrollmentDAO {

    public static final class CourseInfo {
        public final String id;
        public final String name;

        public CourseInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() {
            return id + " - " + name;
        }
    }

    public static final class StudentInfo {
        public final String id;
        public final String fullName;
        public final String department;
        public final String batch;

        public StudentInfo(String id, String fullName, String department, String batch) {
            this.id = id;
            this.fullName = fullName;
            this.department = department;
            this.batch = batch;
        }
    }

    private static void requireAdmin() {
        if (!SessionManager.getInstance().isAdmin()) {
            throw new SecurityException("Administrator session required for enrollment management.");
        }
    }

    public List<CourseInfo> getCourses() throws SQLException {
        requireAdmin();
        String sql = "SELECT course_id, course_name FROM course ORDER BY course_id";
        List<CourseInfo> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CourseInfo(rs.getString("course_id"), rs.getString("course_name")));
            }
        }
        return list;
    }

    public List<String> getDepartments() throws SQLException {
        requireAdmin();
        String sql = "SELECT DISTINCT department FROM undergraduate "
                + "WHERE department IS NOT NULL AND TRIM(department) <> '' ORDER BY department";
        List<String> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    public List<String> getBatches(String department) throws SQLException {
        requireAdmin();
        List<String> list = new ArrayList<>();
        if (department == null || department.isBlank()) return list;
        String sql = "SELECT DISTINCT batch FROM undergraduate "
                + "WHERE department = ? AND batch IS NOT NULL AND TRIM(batch) <> '' ORDER BY batch";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, department);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        }
        return list;
    }

    public List<StudentInfo> getEnrolledStudents(String courseId) throws SQLException {
        requireAdmin();
        List<StudentInfo> list = new ArrayList<>();
        if (courseId == null || courseId.isBlank()) return list;
        String sql = "SELECT u.user_id, u.first_name, u.last_name, ug.department, ug.batch "
                + "FROM student_course sc "
                + "JOIN undergraduate ug ON ug.student_id = sc.student_id "
                + "JOIN users u ON u.user_id = ug.student_id "
                + "WHERE sc.course_id = ? ORDER BY u.user_id";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentInfo(
                            rs.getString("user_id"),
                            (rs.getString("first_name") + " " + rs.getString("last_name")).trim(),
                            rs.getString("department"), rs.getString("batch")));
                }
            }
        }
        return list;
    }

    /** Returns 1 if enrolled; 0 if already enrolled or ID doesn't exist as a student. */
    public int enrollIndividual(String studentId, String courseId) throws SQLException {
        requireAdmin();
        if (studentId == null || studentId.isBlank() || courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Student ID and Course are required.");
        }
        String sql = "INSERT INTO student_course (student_id, course_id) "
                + "SELECT ug.student_id, ? FROM undergraduate ug "
                + "WHERE ug.student_id = ? "
                + "AND NOT EXISTS (SELECT 1 FROM student_course sc "
                + "WHERE sc.student_id = ug.student_id AND sc.course_id = ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, courseId);
            ps.setString(2, studentId.trim());
            ps.setString(3, courseId);
            return ps.executeUpdate();
        }
    }

    /** Idempotent batch enrollment: returns number of newly enrolled students. */
    public int enrollBatch(String department, String batch, String courseId) throws SQLException {
        requireAdmin();
        if (department == null || department.isBlank() || batch == null || batch.isBlank()
                || courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course, Department and Batch must be selected.");
        }
        String sql = "INSERT INTO student_course (student_id, course_id) "
                + "SELECT ug.student_id, ? FROM undergraduate ug "
                + "WHERE ug.department = ? AND ug.batch = ? "
                + "AND NOT EXISTS (SELECT 1 FROM student_course sc "
                + "WHERE sc.student_id = ug.student_id AND sc.course_id = ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, courseId);
            ps.setString(2, department);
            ps.setString(3, batch);
            ps.setString(4, courseId);
            return ps.executeUpdate();
        }
    }
}
