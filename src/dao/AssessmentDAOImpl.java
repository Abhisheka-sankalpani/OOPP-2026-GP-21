package dao;

import db.DBConnection;
import exception.DatabaseException;
import model.Assessment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssessmentDAOImpl implements AssessmentDAO {

    @Override
    public void addAssessment(Assessment a) throws DatabaseException {
        String sql = "INSERT INTO assessment (course_id, name, weightage) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, a.getCourseId());
            ps.setString(2, a.getName());
            ps.setDouble(3, a.getWeightage());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the assessment", e);
        }
    }

    @Override
    public List<Assessment> getAssessmentsByCourse(String courseId) throws DatabaseException {
        List<Assessment> list = new ArrayList<>();
        String sql = "SELECT * FROM assessment WHERE course_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Assessment(rs.getInt("assessment_id"), rs.getString("course_id"),
                            rs.getString("name"), rs.getDouble("weightage")));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read assessments", e);
        }
        return list;
    }
}
