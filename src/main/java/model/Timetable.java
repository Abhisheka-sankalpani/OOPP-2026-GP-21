package model;

import java.sql.Time;

/**
 * Shared timetable model for the Admin and Undergraduate modules.
 *
 * Admin data access uses java.sql.Time. The Undergraduate module's
 * original constructor supplies time values as Strings, so that
 * constructor converts those values to the same internal representation.
 */
public class Timetable {

    private int timetableId;
    private String courseId;
    private String courseName;
    private String department;
    private String sessionType;
    private String dayOfWeek;
    private Time startTime;
    private Time endTime;
    private String venueId;

    // Admin default constructor and defaults.
    public Timetable() {
        this.department = "ICT";
        this.sessionType = "THEORY";
        this.dayOfWeek = "MONDAY";
        this.startTime = Time.valueOf("09:00:00");
        this.endTime = Time.valueOf("11:00:00");
    }

    // Admin constructor, with timetable ID and SQL Time fields.
    public Timetable(int timetableId, String courseId, String department,
                     String sessionType, String dayOfWeek, Time startTime,
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

    // Admin constructor for a new entry before the database assigns an ID.
    public Timetable(String courseId, String department, String sessionType,
                     String dayOfWeek, Time startTime, Time endTime,
                     String venueId) {
        this(0, courseId, department, sessionType, dayOfWeek,
                startTime, endTime, venueId);
    }

    // Student constructor: includes course name and time values as Strings.
    public Timetable(int timetableId, String courseId, String courseName,
                     String department, String sessionType, String dayOfWeek,
                     String startTime, String endTime, String venueId) {
        this(timetableId, courseId, department, sessionType, dayOfWeek,
                parseTime(startTime), parseTime(endTime), venueId);
        this.courseName = courseName;
    }

    // Convert Student-side SQL time strings to Time used by the Admin DAO.
    private static Time parseTime(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String value = text.trim();
        if (value.matches("\\d{1,2}:\\d{2}")) {
            value += ":00";
        }
        return Time.valueOf(value);
    }

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

    // Keep SQL Time return types: the Admin DAO uses PreparedStatement.setTime().
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

    // Student timetable display method.
    public String getTimeSlot() {
        return String.valueOf(startTime) + " - " + String.valueOf(endTime);
    }

    // Admin-side validation and overlap detection.
    public boolean isTimeWindowValid() {
        return startTime != null && endTime != null && endTime.after(startTime);
    }

    public boolean overlapsWith(Timetable other) {
        if (other == null || dayOfWeek == null || other.dayOfWeek == null) {
            return false;
        }
        if (!dayOfWeek.equalsIgnoreCase(other.dayOfWeek)) {
            return false;
        }
        if (!isTimeWindowValid() || !other.isTimeWindowValid()) {
            return false;
        }
        return startTime.before(other.endTime) && other.startTime.before(endTime);
    }

    @Override
    public String toString() {
        return courseId + " [" + dayOfWeek + " " + startTime + " - "
                + endTime + " @ " + venueId + "]";
    }
}
