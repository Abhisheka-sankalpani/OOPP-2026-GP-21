package dao;

import db.DBConnection;
import exception.DatabaseException;
import model.Result;
import util.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultDAOImpl implements ResultDAO {

    private static final String SELECT_BASE =
            "SELECT r.*, c.credit, u.is_repeat FROM result r " +
            "JOIN course c ON r.course_id = c.course_id " +
            "JOIN undergraduate u ON r.student_id = u.student_id ";

    /** Updates the row if the student already has a result for the course, otherwise inserts. */
    @Override
    public void saveResult(Result r) throws DatabaseException {
        String check = "SELECT result_id FROM result WHERE student_id = ? AND course_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(check)) {
            ps.setString(1, r.getStudentId());
            ps.setString(2, r.getCourseId());
            boolean exists;
            try (ResultSet rs = ps.executeQuery()) {
                exists = rs.next();
            }
            String sql = exists
                    ? "UPDATE result SET ca_total=?, final_exam_score=?, total_score=?, grade=?, grade_point=? " +
                      "WHERE student_id=? AND course_id=?"
                    : "INSERT INTO result (ca_total, final_exam_score, total_score, grade, grade_point, " +
                      "student_id, course_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement save = con.prepareStatement(sql)) {
                save.setDouble(1, r.getCaTotal());
                save.setDouble(2, r.getFinalExamScore());
                save.setDouble(3, r.getTotalScore());
                save.setString(4, r.getGrade() == null ? null : r.getGrade().getLabel());
                save.setDouble(5, r.getGradePoint());
                save.setString(6, r.getStudentId());
                save.setString(7, r.getCourseId());
                save.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the result", e);
        }
    }

    @Override
    public List<Result> getResultsByStudent(String studentId) throws DatabaseException {
        return query(SELECT_BASE + "WHERE r.student_id = ?", studentId);
    }

    @Override
    public List<Result> getResultsByCourse(String courseId) throws DatabaseException {
        return query(SELECT_BASE + "WHERE r.course_id = ?", courseId);
    }

    private List<Result> query(String sql, String param) throws DatabaseException {
        List<Result> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Result r = new Result(rs.getString("student_id"), rs.getString("course_id"),
                            rs.getInt("credit"), rs.getBoolean("is_repeat"));
                    r.setCaTotal(rs.getDouble("ca_total"));
                    r.setFinalExamScore(rs.getDouble("final_exam_score"));
                    String label = rs.getString("grade");
                    r.loadStored(rs.getDouble("total_score"),
                            label == null ? null : Grade.fromLabel(label),
                            rs.getDouble("grade_point"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read results", e);
        }
        return list;
    }
}
