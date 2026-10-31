package dao;

import db.DBConnection;
import model.Medical;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicalDAO {

    public List<Medical> getMedicalsByStudentId(String studentId) {
        List<Medical> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = "SELECT medical_id, student_id, course_id, session_no, session_type, "
                + "medical_date, submission_date, description, approval_status "
                + "FROM medical WHERE student_id = ? ORDER BY medical_date DESC";

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Medical(
                        rs.getInt("medical_id"),
                        rs.getString("student_id"),
                        rs.getString("course_id"),
                        rs.getInt("session_no"),
                        rs.getString("session_type"),
                        rs.getDate("medical_date"),
                        rs.getDate("submission_date"),
                        rs.getString("description"),
                        rs.getString("approval_status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching medicals: " + e.getMessage());
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

    public boolean submitMedical(Medical m) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean isSuccess = false;

        String sql = "INSERT INTO medical (student_id, course_id, session_no, session_type, medical_date, description, approval_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, m.getStudentId());
            stmt.setString(2, m.getCourseId());
            stmt.setInt(3, m.getSessionNo());
            stmt.setString(4, m.getSessionType());
            stmt.setDate(5, m.getMedicalDate());
            stmt.setString(6, m.getDescription());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                isSuccess = true;
            }

        } catch (SQLException e) {
            System.out.println("Error submitting medical: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
            }
        }
        return isSuccess;
    }
    public boolean addMedical(String studentId, String courseId, int sessionNo, String type, String date) {
        String query = "INSERT INTO medical (student_id, course_id, session_no, session_type, medical_date, approval_status) VALUES (?, ?, ?, ?, ?, 'Pending')";
        try (java.sql.Connection conn = db.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            stmt.setInt(3, sessionNo);
            stmt.setString(4, type);
            stmt.setString(5, date);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}