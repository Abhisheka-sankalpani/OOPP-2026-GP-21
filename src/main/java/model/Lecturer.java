package model;

/** Shared Lecturer model for the Admin login and Lecturer dashboard modules. */
public class Lecturer extends User {
    private String department;

    // Used by the Admin UserDAO and user-management forms.
    public Lecturer() {
        super();
        setRole("LECTURER");
    }

    // Used by the Admin/System Foundation module.
    public Lecturer(String userId, String passwordHash,
                    String firstName, String lastName,
                    String email, String contactNo,
                    String profilePic, String department) {
        super(userId, passwordHash, firstName, lastName,
              email, contactNo, profilePic, "LECTURER");
        this.department = department;
    }

    // Shorter Admin constructor.
    public Lecturer(String userId, String passwordHash,
                    String firstName, String lastName,
                    String email, String department) {
        this(userId, passwordHash, firstName, lastName,
             email, null, null, department);
    }

    // Used by the original LecturerDashboard and older Lecturer code.
    public Lecturer(String userId, String firstName, String lastName,
                    String email, String contactNo,
                    String profilePic, String department) {
        super(userId, firstName, lastName, email, contactNo, profilePic);
        setRole("LECTURER");
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getRoleTitle() {
        return "Lecturer (" + (department != null ? department : "General") + ")";
    }

    @Override
    public String getRoleSpecificDetails() {
        return "Department: " + (department != null ? department : "N/A");
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }

    // Kept for compatibility with the original Lecturer module's demo Main.
    public void showDashboard() {
        System.out.println("Lecturer Dashboard - " + getFullName()
                + " (" + department + ")");
    }
}
