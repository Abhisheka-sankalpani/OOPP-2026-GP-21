package dao;

import db.DBConnection;
import model.CourseMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//CourseMaterialDAO handles fetching and managing course materials.
public class CourseMaterialDAO {

    public List<CourseMaterial> getMaterialsByCourse(String courseId) {
        List<CourseMaterial> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = (courseId == null || courseId.equalsIgnoreCase("ALL"))
                ? "SELECT material_id, course_id, title, file_path, uploaded_date FROM course_material ORDER BY uploaded_date DESC"
                : "SELECT material_id, course_id, title, file_path, uploaded_date FROM course_material WHERE course_id = ? ORDER BY uploaded_date DESC";

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);

            if (courseId != null && !courseId.equalsIgnoreCase("ALL")) {
                stmt.setString(1, courseId);
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new CourseMaterial(
                        rs.getInt("material_id"),
                        rs.getString("course_id"),
                        rs.getString("title"),
                        rs.getString("file_path"),
                        rs.getTimestamp("uploaded_date")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching course materials: " + e.getMessage());
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

    public List<CourseMaterial> getAllMaterials() {
        return getMaterialsByCourse("ALL");
    }
}