package model;

import java.sql.Timestamp;
import java.time.LocalDate;

public class CourseMaterial {
    private int materialId;
    private String courseId;
    private String title;
    private String filePath;
    private Timestamp uploadedDate;
    private LocalDate availableFrom;
    private LocalDate availableTo;

    public CourseMaterial() {
    }

    public CourseMaterial(int materialId, String courseId, String title, String filePath,
                          Timestamp uploadedDate, LocalDate availableFrom, LocalDate availableTo) {
        this.materialId = materialId;
        this.courseId = courseId;
        this.title = title;
        this.filePath = filePath;
        this.uploadedDate = uploadedDate;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Timestamp getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Timestamp uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public LocalDate getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(LocalDate availableFrom) {
        this.availableFrom = availableFrom;
    }

    public LocalDate getAvailableTo() {
        return availableTo;
    }

    public void setAvailableTo(LocalDate availableTo) {
        this.availableTo = availableTo;
    }
}
