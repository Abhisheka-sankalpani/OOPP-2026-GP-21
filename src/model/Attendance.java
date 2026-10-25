package model;

public class Attendance {
    private String courseId;
    private String courseName;
    private int theoryAttended;
    private int theoryTotal;
    private int practicalAttended;
    private int practicalTotal;

    // Default constructor
    public Attendance() {
    }

    // Parameterized constructor
    public Attendance(String courseId, String courseName,
                      int theoryAttended, int theoryTotal,
                      int practicalAttended, int practicalTotal,
                      double overallPercentage) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.theoryAttended = theoryAttended;
        this.theoryTotal = theoryTotal;
        this.practicalAttended = practicalAttended;
        this.practicalTotal = practicalTotal;
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

    public int getTheoryAttended() {
        return theoryAttended;
    }

    public void setTheoryAttended(int theoryAttended) {
        this.theoryAttended = theoryAttended;
    }

    public int getTheoryTotal() {
        return theoryTotal;
    }

    public void setTheoryTotal(int theoryTotal) {
        this.theoryTotal = theoryTotal;
    }

    public int getPracticalAttended() {
        return practicalAttended;
    }

    public void setPracticalAttended(int practicalAttended) {
        this.practicalAttended = practicalAttended;
    }

    public int getPracticalTotal() {
        return practicalTotal;
    }

    public void setPracticalTotal(int practicalTotal) {
        this.practicalTotal = practicalTotal;
    }
}
