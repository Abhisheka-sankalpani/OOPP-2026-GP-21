package model;

public class TechnicalOfficer extends User {

    // Technical Officer-specific attribute
    private String department;

    // Default Constructor
    public TechnicalOfficer() {
        super();
        setRole("TECHNICAL_OFFICER");
    }

    // Parameterized Constructor
    public TechnicalOfficer(String userId, String passwordHash,
                            String firstName, String lastName,
                            String email, String contactNo,
                            String profilePic, String department) {

        super(userId, passwordHash, firstName, lastName,
                email, contactNo, profilePic, "TECHNICAL_OFFICER");

        this.department = department;
    }

    // Constructor with fewer parameters
    public TechnicalOfficer(String userId, String passwordHash,
                            String firstName, String lastName,
                            String email, String department) {

        this(userId, passwordHash, firstName, lastName,
                email, null, null, department);
    }

    // Getter
    public String getDepartment() {
        return department;
    }

    // Setter
    public void setDepartment(String department) {
        this.department = department;
    }

    // Method Overriding
    @Override
    public String getRoleTitle() {
        return "Technical Officer (" +
                (department != null ? department : "General") + ")";
    }

    @Override
    public String getRoleSpecificDetails() {
        return "Department / Lab: " +
                (department != null ? department : "N/A");
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }
}