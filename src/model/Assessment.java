package model;

/** One CA component of a course (Quiz 1, Assignment 1 ...). */
public class Assessment {
    private int assessmentId;
    private String courseId;
    private String name;
    private double weightage;   // % contribution to the CA total

    public Assessment(int assessmentId, String courseId, String name, double weightage) {
        this.assessmentId = assessmentId;
        this.courseId = courseId;
        this.name = name;
        this.weightage = weightage;
    }

    public int getAssessmentId() { return assessmentId; }
    public String getCourseId() { return courseId; }
    public String getName() { return name; }
    public double getWeightage() { return weightage; }
}
