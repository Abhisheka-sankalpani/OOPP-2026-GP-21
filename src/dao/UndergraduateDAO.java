package dao;

import db.DBConnection;
import model.Undergraduate;

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
}
