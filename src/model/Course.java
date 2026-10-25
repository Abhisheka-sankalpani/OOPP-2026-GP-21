package model;

public class Course {
    private String courseId;
    private String courseName;
    private int credit;
    private String type;
    private int theoryCredits;
    private int practicalCredits;
    private boolean hasTheory;
    private boolean hasPractical;

    public Course() {
    }

    public Course(String courseId, String courseName, int credit, String type,
                  int theoryCredits, int practicalCredits, boolean hasTheory, boolean hasPractical) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credit = credit;
        this.type = type;
        this.theoryCredits = theoryCredits;
        this.practicalCredits = practicalCredits;
        this.hasTheory = hasTheory;
        this.hasPractical = hasPractical;
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

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getType() {
        return (type != null && !type.isEmpty()) ? type : "Core";
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTheoryCredits() {
        return theoryCredits;
    }

    public void setTheoryCredits(int theoryCredits) {
        this.theoryCredits = theoryCredits;
    }

    public int getPracticalCredits() {
        return practicalCredits;
    }

    public void setPracticalCredits(int practicalCredits) {
        this.practicalCredits = practicalCredits;
    }

    public boolean isHasTheory() {
        return hasTheory;
    }

    public void setHasTheory(boolean hasTheory) {
        this.hasTheory = hasTheory;
    }

    public boolean isHasPractical() {
        return hasPractical;
    }

    public void setHasPractical(boolean hasPractical) {
        this.hasPractical = hasPractical;
    }
}
