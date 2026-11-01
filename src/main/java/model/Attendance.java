package model;

import java.sql.Date;

public class Attendance {
    private String courseId;
    private String courseName;
    private int theoryAttended;
    private int theoryTotal;
    private int practicalAttended;
    private int practicalTotal;
    private double overallPercentage;

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
        this.overallPercentage = overallPercentage;
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

    public double getOverallPercentage() {
        return overallPercentage;
    }

    public void setOverallPercentage(double overallPercentage) {
        this.overallPercentage = overallPercentage;
    }

    public String getTheoryRatio() {
        return theoryAttended + " / " + theoryTotal;
    }

    public String getPracticalRatio() {
        return practicalAttended + " / " + practicalTotal;
    }

    // --- Per-session data used by Technical Officer ---
    private int attendanceId;
    private String studentId;
    private int sessionNo;
    private String sessionType;
    private Date sessionDate;
    private int sessionDuration;
    private String status;

    public Attendance(int attendanceId, String studentId, String courseId,
                      String courseName, int sessionNo, String sessionType,
                      Date sessionDate, int sessionDuration, String status) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.sessionNo = sessionNo;
        this.sessionType = sessionType;
        this.sessionDate = sessionDate;
        this.sessionDuration = sessionDuration;
        this.status = status;
    }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int value) { this.attendanceId = value; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String value) { this.studentId = value; }
    public int getSessionNo() { return sessionNo; }
    public void setSessionNo(int value) { this.sessionNo = value; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String value) { this.sessionType = value; }
    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date value) { this.sessionDate = value; }
    public int getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(int value) { this.sessionDuration = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public boolean isPresent() { return "PRESENT".equalsIgnoreCase(status); }

}
