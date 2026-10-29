package dao;

import db.DBConnection;
import model.Undergraduate;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UndergraduateDAO {

    // Retrieve a student by studentId (joining users and undergraduate tables)
    public Undergraduate getStudentById(String studentId) {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.contact_no, u.profile_pic, "
                   + "s.reg_no, s.department, s.batch, s.is_repeat "
                   + "FROM users u "
                   + "INNER JOIN undergraduate s ON u.user_id = s.student_id "
                   + "WHERE u.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Undergraduate(
                        rs.getString("user_id"),
                        rs.getString("reg_no"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("contact_no"),
                        rs.getString("department"),
                        rs.getString("batch"),
                        rs.getString("profile_pic"),
                        rs.getBoolean("is_repeat")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching student: " + e.getMessage());
        }
        return null;
    }
    // Update contact details
    public boolean updateContactDetails(String studentId, String email, String contactNo, String profilePic) {
        String sql = "UPDATE users SET email = ?, contact_no = ?, profile_pic = ? WHERE user_id = ?";

        try (java.sql.Connection conn = db.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, contactNo);
            stmt.setString(3, profilePic);
            stmt.setString(4, studentId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (java.sql.SQLException e) {
            System.out.println("Error updating student profile: " + e.getMessage());
            return false;
        }
    }

    public java.util.List<Object[]> getDetailedEligibilityData(String studentId) {
        java.util.List<Object[]> list = new java.util.ArrayList<>();

        dao.AttendanceDAO attDao = new dao.AttendanceDAO();
        java.util.List<model.Attendance> attList = attDao.getAttendanceByStudentId(studentId);

        try (java.sql.Connection conn = db.DBConnection.getConnection()) {

            String sql = "SELECT COALESCE(SUM(m.score * (a.weightage / 100.0)), 0.0) AS ca_total " +
                    "FROM mark m JOIN assessment a ON m.assessment_id = a.assessment_id " +
                    "WHERE m.student_id = ? AND a.course_id = ?";

            try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (model.Attendance att : attList) {
                    String courseId = att.getCourseId();
                    String courseTitle = att.getCourseName();
                    double attendancePct = att.getOverallPercentage();

                    double caMark = 0.0;

                    stmt.setString(1, studentId);
                    stmt.setString(2, courseId);

                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            caMark = rs.getDouble("ca_total");
                        }
                    }

                    String caStatus = (caMark >= 40.0) ? "Eligible" : "Not Eligible";
                    String attendanceStatus = (attendancePct >= 80.0) ? "Eligible" : "Not Eligible";
                    String finalExamStatus = (caMark >= 40.0 && attendancePct >= 80.0) ? "Eligible" : "Not Eligible";

                    list.add(new Object[]{
                            courseId,
                            courseTitle,
                            String.format("%.1f", caMark),
                            caStatus,
                            String.format("%.1f%%", attendancePct),
                            attendanceStatus,
                            finalExamStatus
                    });
                }
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error fetching CA marks from database: " + e.getMessage());
        }

        return list;
    }
    public String getStudentCGPA(String studentId) {
        String sql = "SELECT SUM(r.grade_point * c.credit) / SUM(c.credit) AS cgpa " +
                "FROM result r " +
                "JOIN course c ON r.course_id = c.course_id " +
                "WHERE r.student_id = ?";

        try (java.sql.Connection conn = db.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double cgpa = rs.getDouble("cgpa");
                    if (!rs.wasNull()) {
                        return String.format("%.2f", cgpa);
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error calculating CGPA: " + e.getMessage());
        }
        return "N/A";
    }
}
