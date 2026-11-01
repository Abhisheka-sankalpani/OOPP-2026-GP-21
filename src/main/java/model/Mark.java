package model;

import exception.InvalidMarkException;

public class Mark {
    private int markId;
    private int assessmentId;
    private String studentId;
    private double score;

    public Mark(int markId, int assessmentId, String studentId, double score) throws InvalidMarkException {
        this.markId = markId;
        this.assessmentId = assessmentId;
        this.studentId = studentId;
        setScore(score);
    }

    // Encapsulation: the score is validated before it is stored
    public void setScore(double score) throws InvalidMarkException {
        if (score < 0 || score > 100) {
            throw new InvalidMarkException("Marks must be between 0 and 100, but got " + score);
        }
        this.score = score;
    }

    public int getMarkId() { return markId; }
    public int getAssessmentId() { return assessmentId; }
    public String getStudentId() { return studentId; }
    public double getScore() { return score; }
}
