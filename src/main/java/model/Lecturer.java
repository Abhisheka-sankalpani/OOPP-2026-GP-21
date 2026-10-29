package model;

public class Lecturer extends User {

    // Lecturer-specific attribute
    private String department;

    // Default Constructor
    public Lecturer() {
        super();
        setRole("LECTURER");
    }

    // Parameterized Constructor
    public Lecturer(String userId, String passwordHash,
                    String firstName, String lastName,
                    String email, String contactNo,
                    String profilePic, String department) {

        super(userId, passwordHash, firstName, lastName,
                email, contactNo, profilePic, "LECTURER");

        this.department = department;
    }

    // Constructor with fewer parameters
    public Lecturer(String userId, String passwordHash,
                    String firstName, String lastName,
                    String email, String department) {

        super(userId, passwordHash, firstName, lastName,
                email, null, null, "LECTURER");

        this.department = department;
    }

    // Getter
    public String getDepartment() {
        return department;
    }

    // Setter
    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getRoleTitle() {
        return "Lecturer (" +
                (department != null ? department : "General") + ")";
    }

    @Override
    public String getRoleSpecificDetails() {
        return "Department: " +
                (department != null ? department : "N/A");
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }
}