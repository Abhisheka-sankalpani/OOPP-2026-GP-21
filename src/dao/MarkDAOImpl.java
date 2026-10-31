package dao;

import db.DBConnection;
import exception.DatabaseException;
import exception.InvalidMarkException;
import model.Mark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarkDAOImpl implements MarkDAO {

    @Override
    public void addMark(Mark mark) throws DatabaseException {
        String sql = "INSERT INTO mark (assessment_id, student_id, score) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, mark.getAssessmentId());
            ps.setString(2, mark.getStudentId());
            ps.setDouble(3, mark.getScore());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the mark", e);
        }
    }

    @Override
    public void updateMark(int markId, double newScore) throws DatabaseException {
        if (newScore < 0 || newScore > 100) {
            throw new DatabaseException("Marks must be between 0 and 100");
        }
        String sql = "UPDATE mark SET score = ? WHERE mark_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, newScore);
            ps.setInt(2, markId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the mark", e);
        }
    }

    @Override
    public List<Mark> getMarksByStudent(String studentId) throws DatabaseException {
        return query("SELECT * FROM mark WHERE student_id = ?", studentId);
    }

    @Override
    public List<Mark> getMarksByAssessment(int assessmentId) throws DatabaseException {
        return query("SELECT * FROM mark WHERE assessment_id = ?", String.valueOf(assessmentId));
    }

    private List<Mark> query(String sql, String param) throws DatabaseException {
        List<Mark> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Mark(rs.getInt("mark_id"), rs.getInt("assessment_id"),
                            rs.getString("student_id"), rs.getDouble("score")));
                }
            }
        } catch (SQLException | InvalidMarkException e) {
            throw new DatabaseException("Could not read marks", e);
        }
        return list;
    }
}
