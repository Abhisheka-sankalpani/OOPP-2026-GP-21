package model;

public class EligibilityResult {
    private String courseId;
    private double caMark;
    private double attendancePercentage;
    private boolean caEligible;
    private boolean attendanceEligible;
    private boolean finalEligible;

    // Default constructor
    public EligibilityResult() {
    }

    // Parameterized constructor
    public EligibilityResult(String courseId, double caMark, double attendancePercentage,
                             boolean caEligible, boolean attendanceEligible, boolean finalEligible) {
        this.courseId = courseId;
        this.caMark = caMark;
        this.attendancePercentage = attendancePercentage;
        this.caEligible = caEligible;
        this.attendanceEligible = attendanceEligible;
        this.finalEligible = finalEligible;
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public double getCaMark() {
        return caMark;
    }

    public void setCaMark(double caMark) {
        this.caMark = caMark;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public boolean isCaEligible() {
        return caEligible;
    }

    public void setCaEligible(boolean caEligible) {
        this.caEligible = caEligible;
    }

    public boolean isAttendanceEligible() {
        return attendanceEligible;
    }

    public void setAttendanceEligible(boolean attendanceEligible) {
        this.attendanceEligible = attendanceEligible;
    }

    public boolean isFinalEligible() {
        return finalEligible;
    }

    public void setFinalEligible(boolean finalEligible) {
        this.finalEligible = finalEligible;
    }
}
