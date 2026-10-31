package dao;

import exception.DatabaseException;
import model.Assessment;

import java.util.List;

public interface AssessmentDAO {
    void addAssessment(Assessment assessment) throws DatabaseException;
    List<Assessment> getAssessmentsByCourse(String courseId) throws DatabaseException;
}
