package dao;

import java.sql.SQLException;

public class TestDeleteCourse {
    public static void main(String[] args) {
        CourseDAO dao = new CourseDAO();

        try {
            boolean result = dao.deleteCourse("ICT2192");

            if (result) {
                System.out.println("Course deleted successfully");
            } else {
                System.out.println("Course not found");
            }
        }
            catch(SQLException e ){
                System.out.println("Database error" + e.getMassage());

            }
        }
        }


