package dao;

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
}