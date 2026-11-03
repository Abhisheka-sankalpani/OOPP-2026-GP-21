package model;

import exception.InvalidMarkException;
import util.CACalculator;
import util.Grade;
import util.GradeCalculator;

/** Final result of one student for one course. */
public class Result {
    private String studentId;
    private String courseId;
    private int credits;            // from the course table (needed for GPA)
    private boolean gpaModule = true;
    private boolean repeat;
    private double caTotal;         // out of 100
    private double finalExamScore;  // out of 100
    private double totalScore;
    private Grade grade;
    private double gradePoint;

    public Result(String studentId, String courseId, int credits, boolean repeat) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.credits = credits;
        this.repeat = repeat;
    }

    /** Old style: assessment weightages are a % of the CA part itself (they add up to 100). */
    public void calculateCaTotal(java.util.List<Assessment> assessments, java.util.List<Mark> marks) {
        calculateCaTotal(assessments, marks, 100);
    }

    /**
     * Weightages are a % of the whole course and add up to caWeight (e.g. 30). Only the best two quizzes count.
     * caTotal is stored as a percentage of the CA part (out of 100).
     */
    public void calculateCaTotal(java.util.List<Assessment> assessments, java.util.List<Mark> marks, double caWeight) {
        double earned = CACalculator.earned(studentId, assessments, marks);
        this.caTotal = Math.min(100.0, earned / caWeight * 100.0);
    }

    /** caWeight = % of the final total that comes from CA (e.g. 40 or 50, as per the course unit structure). */
    public void calculateTotal(double caWeight) throws InvalidMarkException {
        totalScore = caTotal * caWeight / 100.0 + finalExamScore * (100 - caWeight) / 100.0;
        grade = GradeCalculator.getGrade(totalScore, repeat);
        gradePoint = grade.getGradePoint();
    }

    /** Used by the DAO to restore an already calculated result from the database. */
    public void loadStored(double totalScore, Grade grade, double gradePoint) {
        this.totalScore = totalScore;
        this.grade = grade;
        this.gradePoint = gradePoint;
    }

    public void setCaTotal(double caTotal) { this.caTotal = caTotal; }
    public void setFinalExamScore(double finalExamScore) { this.finalExamScore = finalExamScore; }
    public void setGpaModule(boolean gpaModule) { this.gpaModule = gpaModule; }

    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public int getCredits() { return credits; }
    public boolean isGpaModule() { return gpaModule; }
    public boolean isRepeat() { return repeat; }
    public double getCaTotal() { return caTotal; }
    public double getFinalExamScore() { return finalExamScore; }
    public double getTotalScore() { return totalScore; }
    public Grade getGrade() { return grade; }
    public double getGradePoint() { return gradePoint; }
}
