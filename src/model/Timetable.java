package model;

public class Timetable {
    private int timetableId;
    private String courseId;
    private String courseName;
    private String department;
    private String sessionType;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String venueId;

    // Default constructor
    public Timetable() {
    }

    // Parameterized constructor
    public Timetable(int timetableId, String courseId, String courseName, String department,
                     String sessionType, String dayOfWeek, String startTime, String endTime, String venueId) {
        this.timetableId = timetableId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.department = department;
        this.sessionType = sessionType;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venueId = venueId;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getTimeSlot() {
        return startTime + " - " + endTime;
    }
}
