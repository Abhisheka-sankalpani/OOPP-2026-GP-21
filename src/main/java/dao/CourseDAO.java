package dao;

import database.DBConnection;
import model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    // CREATE - Add New Course
    public boolean addCourse(Course course) throws SQLException {

        validateCourse(course);

        String sql = "INSERT INTO course " +
                "(course_id, course_name, credit, type, " +
                "theory_credits, practical_credits, " +
                "has_theory, has_practical) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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

            return ps.executeUpdate() > 0;
        }
    }


    // READ - Get All Courses
    public List<Course> getAllCourses() throws SQLException {

        List<Course> courses = new ArrayList<>();

        String sql = "SELECT * FROM course ORDER BY course_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        }

        return courses;
    }


    // Support Older Method Name
    public List<Course> getCourses() throws SQLException {
        return getAllCourses();
    }


    // READ - Get Course By ID
    public Course getCourseById(String courseId)
            throws SQLException {

        String sql = "SELECT * FROM course WHERE course_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        }

        return null;
    }


    // SEARCH - Search Courses
    public List<Course> searchCourses(String keyword)
            throws SQLException {

        List<Course> courses = new ArrayList<>();

        String sql = "SELECT * FROM course " +
                "WHERE LOWER(course_id) LIKE ? " +
                "OR LOWER(course_name) LIKE ? " +
                "ORDER BY course_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String search = keyword == null ? "" :
                    keyword.trim().toLowerCase();

            String pattern = "%" + search + "%";

            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        }

        return courses;
    }


    // CHECK - Course Already Exists
    public boolean courseExists(String courseId)
            throws SQLException {

        return getCourseById(courseId) != null;
    }


    // UPDATE - Update Existing Course
    public boolean updateCourse(Course course)
            throws SQLException {

        validateCourse(course);

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

            return ps.executeUpdate() > 0;
        }
    }


    // DELETE - Delete Course By ID
    public boolean deleteCourse(String courseId)
            throws SQLException {

        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid Course ID"
            );
        }

        String sql = "DELETE FROM course WHERE course_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, courseId);

            return ps.executeUpdate() > 0;
        }
    }


    // DELETE - Overloaded Method
    public boolean deleteCourse(Course course)
            throws SQLException {

        if (course == null) {
            throw new IllegalArgumentException(
                    "Course cannot be null"
            );
        }

        return deleteCourse(course.getCourseId());
    }


    // Convert ResultSet to Course Object
    private Course mapResultSetToCourse(ResultSet rs)
            throws SQLException {

        Course course = new Course();

        course.setCourseId(rs.getString("course_id"));
        course.setCourseName(rs.getString("course_name"));

        course.setCredit(rs.getInt("credit"));
        course.setType(rs.getString("type"));

        course.setTheoryCredits(
                rs.getInt("theory_credits")
        );

        course.setPracticalCredits(
                rs.getInt("practical_credits")
        );

        course.setHasTheory(
                rs.getBoolean("has_theory")
        );

        course.setHasPractical(
                rs.getBoolean("has_practical")
        );

        return course;
    }


    // Validate Course
    private void validateCourse(Course course) {

        if (course == null) {
            throw new IllegalArgumentException(
                    "Course cannot be null"
            );
        }

        if (course.getCourseId() == null ||
                course.getCourseId().isBlank() ||
                course.getCourseId().length() > 10) {

            throw new IllegalArgumentException(
                    "Valid Course ID is required"
            );
        }

        if (course.getCourseName() == null ||
                course.getCourseName().isBlank() ||
                course.getCourseName().length() > 100) {

            throw new IllegalArgumentException(
                    "Valid Course Name is required"
            );
        }

        if (course.getCredit() <= 0) {
            throw new IllegalArgumentException(
                    "Total credits must be positive"
            );
        }

        if (!course.isHasTheory() &&
                !course.isHasPractical()) {

            throw new IllegalArgumentException(
                    "Course must have Theory or Practical"
            );
        }

        if (!course.isCreditSumValid()) {
            throw new IllegalArgumentException(
                    "Total credits do not match Theory and Practical credits"
            );
        }
    }
}