package model;

public class Marks {
    private String courseId;
    private String courseName;
    private int credit;
    private double caTotal;
    private double finalExamScore;
    private double totalScore;
    private String grade;
    private double gradePoint;

    // Default constructor
    public Marks() {
    }

    // Parameterized constructor
    public Marks(String courseId, String courseName, int credit,
                 double caTotal, double finalExamScore, double totalScore,
                 String grade, double gradePoint) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credit = credit;
        this.caTotal = caTotal;
        this.finalExamScore = finalExamScore;
        this.totalScore = totalScore;
        this.grade = grade;
        this.gradePoint = gradePoint;
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public double getCaTotal() {
        return caTotal;
    }

    public void setCaTotal(double caTotal) {
        this.caTotal = caTotal;
    }

    public double getFinalExamScore() {
        return finalExamScore;
    }

    public void setFinalExamScore(double finalExamScore) {
        this.finalExamScore = finalExamScore;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(double gradePoint) {
        this.gradePoint = gradePoint;
    }

    public String getFormattedGrade() {
        return (grade != null && !grade.trim().isEmpty()) ? grade : "Pending";
    }
}
