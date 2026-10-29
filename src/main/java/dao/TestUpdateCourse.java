package dao;

import model.Course;
import java.sql.SQLException;

public class TestUpdateCourse {

    public static void main(String[] args) {

        Course course = new Course();

        course.setCourseId("ICT2192");
        course.setCourseName("Updated Test Course");
        course.setCredit(2);

        CourseDAO dao = new CourseDAO();

        try {
            boolean result = dao.updateCourse(course);

            if (result) {
                System.out.println("Course Updated Successfully!");
            } else {
                System.out.println("Course Not Found!");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}