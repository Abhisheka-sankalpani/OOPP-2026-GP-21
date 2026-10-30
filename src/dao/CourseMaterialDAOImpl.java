package dao;

import db.DBConnection;
import exception.DatabaseException;
import model.LectureMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CourseMaterialDAOImpl implements CourseMaterialDAO {

    @Override
    public void addMaterial(LectureMaterial m) throws DatabaseException {
        String sql = "INSERT INTO course_material (course_id, title, file_path) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getCourseId());
            ps.setString(2, m.getTitle());
            ps.setString(3, m.getFilePath());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the material", e);
        }
    }

    @Override
    public List<LectureMaterial> getMaterialsByCourse(String courseId) throws DatabaseException {
        List<LectureMaterial> list = new ArrayList<>();
        String sql = "SELECT * FROM course_material WHERE course_id = ? ORDER BY material_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp time = rs.getTimestamp("uploaded_date");
                    String date = (time == null) ? "" : time.toString().substring(0, 16);
                    list.add(new LectureMaterial(rs.getInt("material_id"), rs.getString("course_id"),
                            rs.getString("title"), rs.getString("file_path"), date));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read the materials", e);
        }
        return list;
    }

    @Override
    public void updateMaterial(int materialId, String title, String filePath) throws DatabaseException {
        String sql = "UPDATE course_material SET title = ?, file_path = ? WHERE material_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, filePath);
            ps.setInt(3, materialId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the material", e);
        }
    }

    @Override
    public void deleteMaterial(int materialId) throws DatabaseException {
        String sql = "DELETE FROM course_material WHERE material_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, materialId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete the material", e);
        }
    }
}
