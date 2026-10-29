package dao;

import dao.CourseDAO;
import model.Course;

import java.sql.SQLException;

public class TestCourseDAO {

    public static void main(String[] args) {

        Course course = new Course();

        course.setCourseId("ICT2192");
        course.setCourseName("OOP Practicum");
        course.setCredit(2);
        course.setType("Core");

        CourseDAO dao = new CourseDAO();

        try {
            if (!course.isCreditSumValid()) {
                System.out.println("Invalid Course Credits!");
                return;
            }

            boolean result = dao.addCourse(course);

            if (result) {
                System.out.println("Course Added Successfully!");
            } else {
                System.out.println("Course Not Added!");
            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
}