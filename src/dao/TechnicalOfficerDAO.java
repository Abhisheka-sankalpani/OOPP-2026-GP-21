package dao;

import db.DBConnection;
import model.TechnicalOfficer;

import java.sql.*;

/**
 * Data Access Object for TechnicalOfficer entities.
 * Encapsulates all database operations relating to the technical_officer table.
 */
public class TechnicalOfficerDAO {

    /**
     * Retrieves a TechnicalOfficer by their user ID.
     * Joins the 'users' table and the 'technical_officer' table.
     *
     * @param toId the technical officer's user_id (e.g. "to1")
     * @return a populated TechnicalOfficer, or null if not found
     */
    public TechnicalOfficer getTechnicalOfficerById(String toId) {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, " +
                     "u.contact_no, u.profile_pic, t.department " +
                     "FROM users u " +
                     "INNER JOIN technical_officer t ON u.user_id = t.to_id " +
                     "WHERE u.user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, toId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new TechnicalOfficer(
                        rs.getString("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("contact_no"),
                        rs.getString("profile_pic"),
                        rs.getString("department")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Technical Officer: " + e.getMessage());
        }
        return null;
    }
}
