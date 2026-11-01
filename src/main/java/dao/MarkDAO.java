package dao;

import exception.DatabaseException;
import model.Mark;

import java.util.List;

public interface MarkDAO {
    void addMark(Mark mark) throws DatabaseException;
    void updateMark(int markId, double newScore) throws DatabaseException;
    List<Mark> getMarksByStudent(String studentId) throws DatabaseException;
    List<Mark> getMarksByAssessment(int assessmentId) throws DatabaseException;
}
