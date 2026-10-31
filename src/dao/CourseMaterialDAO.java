package dao;

import exception.DatabaseException;
import model.LectureMaterial;

import java.util.List;

public interface CourseMaterialDAO {
    void addMaterial(LectureMaterial material) throws DatabaseException;
    List<LectureMaterial> getMaterialsByCourse(String courseId) throws DatabaseException;
    void updateMaterial(int materialId, String title, String filePath) throws DatabaseException;
    void deleteMaterial(int materialId) throws DatabaseException;
}
