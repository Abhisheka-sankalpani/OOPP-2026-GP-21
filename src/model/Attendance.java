package model;

import java.sql.Date;

/**
 * Represents a single attendance record for one session.
 * Maps directly to the 'attendance' table in the database.
 *
 * Project rule: 15 sessions per module component (THEORY / PRACTICAL),
 *               each session is 2 hours long.
 */
public class Attendance {

    private int    attendanceId;
    private String studentId;
    private String courseId;
    private String courseName;
    private int    sessionNo;            // 1 – 15
    private String sessionType;          // "THEORY" | "PRACTICAL"
    private Date   sessionDate;
    private int    sessionDuration;      // always 2 hours
    private String status;               // "PRESENT" | "ABSENT"

    // Default constructor
    public Attendance() {}

    // Full parameterized constructor
    public Attendance(int attendanceId, String studentId, String courseId,
                      String courseName, int sessionNo, String sessionType,
                      Date sessionDate, int sessionDuration, String status) {
        this.attendanceId   = attendanceId;
        this.studentId      = studentId;
        this.courseId       = courseId;
        this.courseName     = courseName;
        this.sessionNo      = sessionNo;
        this.sessionType    = sessionType;
        this.sessionDate    = sessionDate;
        this.sessionDuration = sessionDuration;
        this.status         = status;
    }

    // Getters & Setters
    public int    getAttendanceId()              { return attendanceId; }
    public void   setAttendanceId(int id)         { this.attendanceId = id; }

    public String getStudentId()                 { return studentId; }
    public void   setStudentId(String s)          { this.studentId = s; }

    public String getCourseId()                  { return courseId; }
    public void   setCourseId(String c)           { this.courseId = c; }

    public String getCourseName()                { return courseName; }
    public void   setCourseName(String n)         { this.courseName = n; }

    public int    getSessionNo()                 { return sessionNo; }
    public void   setSessionNo(int n)            { this.sessionNo = n; }

    public String getSessionType()               { return sessionType; }
    public void   setSessionType(String t)        { this.sessionType = t; }

    public Date   getSessionDate()               { return sessionDate; }
    public void   setSessionDate(Date d)          { this.sessionDate = d; }

    public int    getSessionDuration()           { return sessionDuration; }
    public void   setSessionDuration(int d)      { this.sessionDuration = d; }

    public String getStatus()                    { return status; }
    public void   setStatus(String s)             { this.status = s; }

    /** Convenience: true when student was present. */
    public boolean isPresent() {
        return "PRESENT".equalsIgnoreCase(status);
    }
}
