package model;

import java.sql.Time;

public class Timetable {

    // Attributes
    private int timetableId;
    private String courseId;
    private String courseName;
    private String department;
    private String sessionType;
    private String dayOfWeek;
    private Time startTime;
    private Time endTime;
    private String venueId;

    // Default Constructor
    public Timetable() {
        this.department = "ICT";
        this.sessionType = "THEORY";
        this.dayOfWeek = "MONDAY";
        this.startTime = Time.valueOf("09:00:00");
        this.endTime = Time.valueOf("11:00:00");
    }

    // Parameterized Constructor
    public Timetable(int timetableId, String courseId,
                     String department, String sessionType,
                     String dayOfWeek, Time startTime,
                     Time endTime, String venueId) {

        this.timetableId = timetableId;
        this.courseId = courseId;
        this.department = department;
        this.sessionType = sessionType;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venueId = venueId;
    }

    // Constructor without Timetable ID
    public Timetable(String courseId, String department,
                     String sessionType, String dayOfWeek,
                     Time startTime, Time endTime,
                     String venueId) {

        this(0, courseId, department, sessionType,
                dayOfWeek, startTime, endTime, venueId);
    }

    // Getters and Setters

    public int getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(int timetableId) {
        this.timetableId = timetableId;
    }

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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    // Validate Time
    public boolean isTimeWindowValid() {

        if (startTime == null || endTime == null) {
            return false;
        }

        return endTime.after(startTime);
    }

    // Check Time Overlap
    public boolean overlapsWith(Timetable other) {

        if (other == null ||
                dayOfWeek == null ||
                other.dayOfWeek == null) {
            return false;
        }

        if (!dayOfWeek.equalsIgnoreCase(other.dayOfWeek)) {
            return false;
        }

        if (!isTimeWindowValid() || !other.isTimeWindowValid()) {
            return false;
        }

        return startTime.before(other.endTime)
                && other.startTime.before(endTime);
    }

    // Display Timetable
    @Override
    public String toString() {
        return courseId + " [" + dayOfWeek + " "
                + startTime + " - " + endTime
                + " @ " + venueId + "]";
    }
}