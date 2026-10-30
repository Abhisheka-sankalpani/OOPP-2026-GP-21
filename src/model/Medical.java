package model;

import java.sql.Date;

/**
 * Represents a medical-certificate submission made by a student
 * for a missed attendance session.
 * Maps to the 'medical' table.
 */
public class Medical {

    private int    medicalId;
    private String studentId;
    private String courseId;
    private int    sessionNo;
    private String sessionType;      // "THEORY" | "PRACTICAL"
    private Date   medicalDate;
    private Date   submissionDate;
    private String description;
    private String approvalStatus;   // "PENDING" | "APPROVED" | "REJECTED"

    // Default constructor
    public Medical() {}

    // Full parameterized constructor
    public Medical(int medicalId, String studentId, String courseId,
                   int sessionNo, String sessionType,
                   Date medicalDate, Date submissionDate,
                   String description, String approvalStatus) {
        this.medicalId      = medicalId;
        this.studentId      = studentId;
        this.courseId       = courseId;
        this.sessionNo      = sessionNo;
        this.sessionType    = sessionType;
        this.medicalDate    = medicalDate;
        this.submissionDate = submissionDate;
        this.description    = description;
        this.approvalStatus = approvalStatus;
    }

    // Getters & Setters
    public int    getMedicalId()               { return medicalId; }
    public void   setMedicalId(int id)          { this.medicalId = id; }

    public String getStudentId()               { return studentId; }
    public void   setStudentId(String s)        { this.studentId = s; }

    public String getCourseId()                { return courseId; }
    public void   setCourseId(String c)         { this.courseId = c; }

    public int    getSessionNo()               { return sessionNo; }
    public void   setSessionNo(int n)          { this.sessionNo = n; }

    public String getSessionType()             { return sessionType; }
    public void   setSessionType(String t)      { this.sessionType = t; }

    public Date   getMedicalDate()             { return medicalDate; }
    public void   setMedicalDate(Date d)        { this.medicalDate = d; }

    public Date   getSubmissionDate()          { return submissionDate; }
    public void   setSubmissionDate(Date d)     { this.submissionDate = d; }

    public String getDescription()             { return description; }
    public void   setDescription(String desc)   { this.description = desc; }

    /** Always returns a non-null status; defaults to "PENDING". */
    public String getApprovalStatus() {
        return (approvalStatus != null && !approvalStatus.isEmpty())
               ? approvalStatus : "PENDING";
    }
    public void setApprovalStatus(String s)    { this.approvalStatus = s; }
}
