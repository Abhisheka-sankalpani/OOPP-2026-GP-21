package dao;

import database.DBConnection;
import exception.DatabaseException;
import model.CourseMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Student-specific DAO for reading course materials.
public class UndergraduateCourseMaterialDAO {

    private static final String AVAILABLE_NOW =
            "available_from <= CURRENT_DATE() AND available_to >= CURRENT_DATE()";

    public List<CourseMaterial> getMaterialsByCourse(String courseId) throws DatabaseException {
        CourseMaterialSchema.ensureAvailabilityColumns();
        List<CourseMaterial> list = new ArrayList<>();
        String sql = (courseId == null || courseId.equalsIgnoreCase("ALL"))
                ? "SELECT material_id, course_id, title, file_path, uploaded_date, available_from, available_to " +
                  "FROM course_material WHERE " + AVAILABLE_NOW + " ORDER BY uploaded_date DESC"
                : "SELECT material_id, course_id, title, file_path, uploaded_date, available_from, available_to " +
                  "FROM course_material WHERE course_id = ? AND " + AVAILABLE_NOW + " ORDER BY uploaded_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (courseId != null && !courseId.equalsIgnoreCase("ALL")) {
                stmt.setString(1, courseId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new CourseMaterial(
                            rs.getInt("material_id"),
                            rs.getString("course_id"),
                            rs.getString("title"),
                            rs.getString("file_path"),
                            rs.getTimestamp("uploaded_date"),
                            rs.getDate("available_from").toLocalDate(),
                            rs.getDate("available_to").toLocalDate()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load available course materials", e);
        }
        return list;
    }

    public List<CourseMaterial> getAllMaterials() throws DatabaseException {
        return getMaterialsByCourse("ALL");
    }

    public boolean isMaterialAvailableNow(int materialId) throws DatabaseException {
        CourseMaterialSchema.ensureAvailabilityColumns();
        String sql = "SELECT 1 FROM course_material WHERE material_id = ? AND " + AVAILABLE_NOW;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, materialId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not verify course material availability", e);
        }
    }
}