package model;

import java.sql.Date;

public class Medical {
    private int medicalId;
    private String studentId;
    private String courseId;
    private int sessionNo;
    private String sessionType;
    private Date medicalDate;
    private Date submissionDate;
    private String description;
    private String approvalStatus;

    public Medical() {
    }

    public Medical(int medicalId, String studentId, String courseId, int sessionNo,
                   String sessionType, Date medicalDate, Date submissionDate,
                   String description, String approvalStatus) {
        this.medicalId = medicalId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.sessionNo = sessionNo;
        this.sessionType = sessionType;
        this.medicalDate = medicalDate;
        this.submissionDate = submissionDate;
        this.description = description;
        this.approvalStatus = approvalStatus;
    }

    public int getMedicalId() {
        return medicalId;
    }

    public void setMedicalId(int medicalId) {
        this.medicalId = medicalId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public int getSessionNo() {
        return sessionNo;
    }

    public void setSessionNo(int sessionNo) {
        this.sessionNo = sessionNo;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public Date getMedicalDate() {
        return medicalDate;
    }

    public void setMedicalDate(Date medicalDate) {
        this.medicalDate = medicalDate;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApprovalStatus() {
        return (approvalStatus != null && !approvalStatus.isEmpty()) ? approvalStatus : "PENDING";
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
}
