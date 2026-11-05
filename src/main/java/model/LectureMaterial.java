package model;

import java.time.LocalDate;

/** One lecture note / course material row of the course_material table. */
public class LectureMaterial {
    private int materialId;
    private String courseId;
    private String title;
    private String filePath;      // relative path, e.g. materials/ICT2132/1730000000_notes.pdf
    private String uploadedDate;
    private LocalDate availableFrom;
    private LocalDate availableTo;

    public LectureMaterial(int materialId, String courseId, String title, String filePath,
                           String uploadedDate, LocalDate availableFrom, LocalDate availableTo) {
        this.materialId = materialId;
        this.courseId = courseId;
        this.title = title;
        this.filePath = filePath;
        this.uploadedDate = uploadedDate;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }

    public int getMaterialId() { return materialId; }
    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public String getUploadedDate() { return uploadedDate; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public LocalDate getAvailableTo() { return availableTo; }

    @Override
    public String toString() {
        return courseId + " - " + title;
    }
}
