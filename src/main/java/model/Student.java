package model;

public class Student extends User {

    // Student-specific attributes
    private String regNo;
    private String department;
    private String batch;
    private boolean isRepeat;

    // Default Constructor
    public Student() {
        super();
        setRole("STUDENT");
    }

    // Parameterized Constructor
    public Student(String userId, String passwordHash,
                   String firstName, String lastName,
                   String email, String contactNo,
                   String profilePic, String regNo,
                   String department, String batch,
                   boolean isRepeat) {

        super(userId, passwordHash, firstName, lastName,
                email, contactNo, profilePic, "STUDENT");

        this.regNo = regNo;
        this.department = department;
        this.batch = batch;
        this.isRepeat = isRepeat;
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

    public void setRepeat(boolean repeat) {
        this.isRepeat = repeat;
    }

    // Abstract Methods Implementation

    @Override
    public String getRoleTitle() {
        return "Undergraduate (" +
                (regNo != null ? regNo : "Student") + ")";
    }

    @Override
    public String getRoleSpecificDetails() {

        return "Reg No: " + regNo +
                " | Department: " + department +
                " | Batch: " + batch +
                (isRepeat ? " [REPEAT]" : "");
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }
}