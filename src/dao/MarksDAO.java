
package dao;

import db.DBConnection;
import model.Marks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarksDAO {

    // Retrieve marks list for all courses for a given student
    public List<Marks> getMarksByStudentId(String studentId) {
        List<Marks> list = new ArrayList<>();

        String sql = "SELECT c.course_id, c.course_name, c.credit, "
                + "r.ca_total, r.final_exam_score, r.total_score, r.grade, r.grade_point "
                + "FROM course c "
                + "LEFT JOIN result r ON c.course_id = r.course_id AND r.student_id = ? "
                + "ORDER BY c.course_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String courseId = rs.getString("course_id");
                    String courseName = rs.getString("course_name");
                    int credit = rs.getInt("credit");
                    double caTotal = rs.getDouble("ca_total");
                    double finalExamScore = rs.getDouble("final_exam_score");
                    double totalScore = rs.getDouble("total_score");
                    String grade = rs.getString("grade");
                    double gradePoint = rs.getDouble("grade_point");

                    Marks m = new Marks(
                            courseId,
                            courseName,
                            credit,
                            caTotal,
                            finalExamScore,
                            totalScore,
                            grade,
                            gradePoint
                    );
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching marks list: " + e.getMessage());
        }
        return list;
    }

    // Retrieve only graded courses for results display
    public List<Marks> getGradedMarksByStudentId(String studentId) {
        List<Marks> list = new ArrayList<>();

        String sql = "SELECT c.course_id, c.course_name, c.credit, "
                + "r.ca_total, r.final_exam_score, r.total_score, r.grade, r.grade_point "
                + "FROM result r "
                + "INNER JOIN course c ON r.course_id = c.course_id "
                + "WHERE r.student_id = ? "
                + "ORDER BY c.course_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Marks(
                            rs.getString("course_id"),
                            rs.getString("course_name"),
                            rs.getInt("credit"),
                            rs.getDouble("ca_total"),
                            rs.getDouble("final_exam_score"),
                            rs.getDouble("total_score"),
                            rs.getString("grade"),
                            rs.getDouble("grade_point")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching graded marks: " + e.getMessage());
        }

        // If no rows in result table for this student, fall back
        if (list.isEmpty()) {
            return getMarksByStudentId(studentId);
        }
        return list;
    }

    // Retrieve CA mark for a specific course (used by Exam Eligibility check)
    public double getCAMark(String studentId, String courseId) {
        String sql = "SELECT ca_total FROM result WHERE student_id = ? AND course_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, courseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("ca_total");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching CA mark: " + e.getMessage());
        }
        return 0.0;
    }

    // Calculate Grade Point Average (GPA = Total Quality Points / Total Credits)
    public double calculateGPA(List<Marks> marksList) {
        double totalQualityPoints = 0.0;
        int totalCredits = 0;

        for (Marks m : marksList) {
            // Only include courses where final grade is released
            if (m.getGrade() != null && !m.getGrade().trim().isEmpty()) {
                totalQualityPoints += (m.getGradePoint() * m.getCredit());
                totalCredits += m.getCredit();
            }
        }

        if (totalCredits > 0) {
            double gpa = totalQualityPoints / totalCredits;
            return Math.round(gpa * 100.0) / 100.0; // Round to 2 decimal places
        }
        return 0.0;
    }
}
