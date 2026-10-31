package dao;

import db.DBConnection;
import model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    public List<Course> getCoursesByStudent(String studentId) {
        List<Course> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = "SELECT c.course_id, c.course_name, c.credit, c.type, "
                + "c.theory_credits, c.practical_credits, c.has_theory, c.has_practical "
                + "FROM course c "
                + "INNER JOIN student_course sc ON c.course_id = sc.course_id "
                + "WHERE sc.student_id = ? "
                + "ORDER BY c.course_id";

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Course(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("credit"),
                        rs.getString("type"),
                        rs.getInt("theory_credits"),
                        rs.getInt("practical_credits"),
                        rs.getBoolean("has_theory"),
                        rs.getBoolean("has_practical")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching student courses: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
            }
        }

        if (list.isEmpty()) {
            return getAllCourses();
        }
        return list;
    }

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = "SELECT course_id, course_name, credit, type, "
                + "theory_credits, practical_credits, has_theory, has_practical "
                + "FROM course ORDER BY course_id";

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Course(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getInt("credit"),
                        rs.getString("type"),
                        rs.getInt("theory_credits"),
                        rs.getInt("practical_credits"),
                        rs.getBoolean("has_theory"),
                        rs.getBoolean("has_practical")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching all courses: " + e.getMessage());
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
}