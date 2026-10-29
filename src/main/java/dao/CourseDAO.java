package dao;

import database.DBConnection;
import model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

public class CourseDAO {
    public boolean addCourse(Course course) throws SQLException {

        String sql = "INSERT INTO course" + "(course_id,course_name,credit,type," + "theory_credits, practical_credits, has_theory, has_practical)" + "values(?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, course.getCourseId());
            ps.setString(2, course.getCourseName());
            ps.setInt(3, course.getCredit());
            ps.setString(4, course.getType());
            ps.setInt(5, course.getTheoryCredits());
            ps.setInt(6, course.getPracticalCredits());
            ps.setBoolean(7, course.isHasTheory());
            ps.setBoolean(8, course.isHasPractical());

            int rows = ps.executeUpdate();

            return rows > 0;
        }

    }

    public List<Course> getCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();

        String sql = "SELECT * FROM course";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Course course = new Course();

                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                course.setCredit(rs.getInt("credit"));
                course.setType(rs.getString("type"));

                course.setTheoryCredits(rs.getInt("theory_credits"));
                course.setPracticalCredits(rs.getInt("practical_credits"));
                course.setHasTheory(rs.getBoolean("has_theory"));
                course.setHasPractical(rs.getBoolean("has_practical"));

                courses.add(course);
            }
            return courses;
        }
    }

    public boolean updateCourse(Course course) throws SQLException {


            if (!course.isCreditSumValid()) {
                throw new IllegalArgumentException("Invalid course credits");
            }

            String sql = "UPDATE course SET " +
                    "course_name = ?, " +
                    "credit = ?, " +
                    "type = ?, " +
                    "theory_credits = ?, " +
                    "practical_credits = ?, " +
                    "has_theory = ?, " +
                    "has_practical = ? " +
                    "WHERE course_id = ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, course.getCourseName());
                ps.setInt(2, course.getCredit());
                ps.setString(3, course.getType());
                ps.setInt(4, course.getTheoryCredits());
                ps.setInt(5, course.getPracticalCredits());
                ps.setBoolean(6, course.isHasTheory());
                ps.setBoolean(7, course.isHasPractical());
                ps.setString(8, course.getCourseId());

                int rows = ps.executeUpdate();

                return rows > 0;
            }

    }

    }

