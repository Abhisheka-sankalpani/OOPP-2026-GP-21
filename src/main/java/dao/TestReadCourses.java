package dao;

import model.Course;
import java.sql.SQLException;
import java.util.List;

public class TestReadCourses {

    public static void main(String[] args) {

        CourseDAO dao = new CourseDAO();

        try {

            List<Course> courses = dao.getCourses();

            for (Course course : courses) {
                System.out.println(
                        course.getCourseId() + " - " +
                                course.getCourseName()
                );
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
