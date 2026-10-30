package model;

/**
 * Abstract base class representing a system user.
 * Demonstrates Abstraction and Inheritance (OOP Pillar).
 */
public abstract class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNo;
    private String profilePic;

    // Default constructor
    public User() {}

    // Parameterized constructor
    public User(String userId, String firstName, String lastName,
                String email, String contactNo, String profilePic) {
        this.userId    = userId;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.contactNo = contactNo;
        this.profilePic = profilePic;
    }

    // Getters & Setters
    public String getUserId()                   { return userId; }
    public void   setUserId(String userId)       { this.userId = userId; }

    public String getFirstName()                { return firstName; }
    public void   setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName()                 { return lastName; }
    public void   setLastName(String lastName)  { this.lastName = lastName; }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getEmail()                    { return email; }
    public void   setEmail(String email)        { this.email = email; }

    public String getContactNo()                { return contactNo; }
    public void   setContactNo(String contactNo){ this.contactNo = contactNo; }

    public String getProfilePic()               { return profilePic; }
    public void   setProfilePic(String p)       { this.profilePic = p; }

    /**
     * Abstract method — every user subclass must declare its role.
     * Demonstrates Abstraction (OOP Pillar).
     */
    public abstract String getRole();
}
