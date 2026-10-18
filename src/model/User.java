package model;

/** Base class for Admin, Lecturer, TechnicalOfficer and Undergraduate. */
public abstract class User {
    private final String userId;      // username - cannot be changed
    private final String password;    // cannot be changed by the user here
    private String firstName;
    private String lastName;
    private String email;
    private String contactNo;
    private String profilePic;

    protected User(String userId, String password, String firstName, String lastName,
                   String email, String contactNo, String profilePic) {
        this.userId = userId;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contactNo = contactNo;
        this.profilePic = profilePic;
    }

    // Each role has its own role name and dashboard (polymorphism)
    public abstract String getRole();
    public abstract void showDashboard();

    public boolean checkPassword(String input) {
        return password.equals(input);
    }

    // Overloaded profile update methods (userId and password are not touched)
    public void updateProfile(String contactNo) {
        this.contactNo = contactNo;
    }

    public void updateProfile(String email, String contactNo, String profilePic) {
        this.email = email;
        this.contactNo = contactNo;
        this.profilePic = profilePic;
    }

    public String getFullName() { return firstName + " " + lastName; }
    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getContactNo() { return contactNo; }
    public String getProfilePic() { return profilePic; }
}
