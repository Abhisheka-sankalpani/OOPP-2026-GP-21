package model;

/** Shared parent class for Admin, Lecturer, Student, Undergraduate and TechnicalOfficer. */
public abstract class User {
    private String userId;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNo;
    private String profilePic;
    private String role;

    public User() {
    }

    // Constructor used by the 2125 Admin/System Foundation models.
    public User(String userId, String passwordHash, String firstName,
                String lastName, String email, String contactNo,
                String profilePic, String role) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contactNo = contactNo;
        this.profilePic = profilePic;
        this.role = role;
    }

    // Constructor used by the Lecturer, Student and Technical Officer contributions.
    public User(String userId, String firstName, String lastName,
                String email, String contactNo, String profilePic) {
        this(userId, null, firstName, lastName, email, contactNo, profilePic, null);
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() {
        return ((firstName == null ? "" : firstName) + " "
                + (lastName == null ? "" : lastName)).trim();
    }

    // Retain the method name used by the Admin classes.
    public String getFullname() { return getFullName(); }

    // Default implementations allow older Undergraduate classes to remain compatible.
    // Role-specific classes can override these methods.
    public String getRoleTitle() { return getRole(); }
    public String getRoleSpecificDetails() { return "Role: " + getRole(); }
    public boolean hasAdminPrivileges() { return false; }
}
