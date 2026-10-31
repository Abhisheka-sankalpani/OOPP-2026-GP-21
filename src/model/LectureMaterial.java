package model;

/** One lecture note / course material row of the course_material table. */
public class LectureMaterial {
    private int materialId;
    private String courseId;
    private String title;
    private String filePath;      // relative path, e.g. materials/ICT2132/1730000000_notes.pdf
    private String uploadedDate;

    public LectureMaterial(int materialId, String courseId, String title, String filePath, String uploadedDate) {
        this.materialId = materialId;
        this.courseId = courseId;
        this.title = title;
        this.filePath = filePath;
        this.uploadedDate = uploadedDate;
    }

    public int getMaterialId() { return materialId; }
    public String getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public String getUploadedDate() { return uploadedDate; }

    @Override
    public String toString() {
        return courseId + " - " + title;
    }
}
