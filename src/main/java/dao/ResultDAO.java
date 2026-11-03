package dao;

import exception.DatabaseException;
import model.Result;

import java.util.List;

public interface ResultDAO {
    void saveResult(Result result) throws DatabaseException;
    List<Result> getResultsByStudent(String studentId) throws DatabaseException;
    List<Result> getResultsByCourse(String courseId) throws DatabaseException;
}
