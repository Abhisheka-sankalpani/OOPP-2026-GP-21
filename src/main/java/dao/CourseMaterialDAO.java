package dao;

import exception.DatabaseException;
import model.LectureMaterial;

import java.util.List;
import java.time.LocalDate;

public interface CourseMaterialDAO {
    void addMaterial(LectureMaterial material) throws DatabaseException;
    List<LectureMaterial> getMaterialsByCourse(String courseId) throws DatabaseException;
    void updateMaterial(int materialId, String title, String filePath,
                        LocalDate availableFrom, LocalDate availableTo) throws DatabaseException;
    void deleteMaterial(int materialId) throws DatabaseException;
}
