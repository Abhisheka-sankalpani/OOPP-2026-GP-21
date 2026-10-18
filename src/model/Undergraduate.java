package model;

public class Undergraduate extends User {
    private String regNo;
    private String department;
    private String batch;
    private boolean isRepeat;

    // Default constructor
    public Undergraduate() {
        super();
    }

    // Parameterized constructor
    public Undergraduate(String studentId, String regNo, String firstName, String lastName,
                         String email, String contactNo, String department, String batch,
                         String profilePic, boolean isRepeat) {
        // Call superclass constructor (Inheritance)
        super(studentId, firstName, lastName, email, contactNo, profilePic);
        this.regNo = regNo;
        this.department = department;
        this.batch = batch;
        this.isRepeat = isRepeat;
    }

    public String getStudentId() {
        return getUserId();
    }

    public void setStudentId(String studentId) {
        setUserId(studentId);
    }

    // Getters and Setters
    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    // Override abstract method from User (Polymorphism)
    @Override
    public String getRole() {
        return "Undergraduate";
    }
}
