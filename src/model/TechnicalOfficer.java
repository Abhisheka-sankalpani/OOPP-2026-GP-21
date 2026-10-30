package model;

/**
 * Represents a Technical Officer user in the system.
 * Extends User — demonstrates Inheritance (OOP Pillar).
 * Overrides getRole() — demonstrates Polymorphism (OOP Pillar).
 */
public class TechnicalOfficer extends User {

    private String department;

    // Default constructor
    public TechnicalOfficer() {
        super();
    }

    // Parameterized constructor
    public TechnicalOfficer(String toId, String firstName, String lastName,
                            String email, String contactNo,
                            String profilePic, String department) {
        super(toId, firstName, lastName, email, contactNo, profilePic);
        this.department = department;
    }

    // Convenience getter
    public String getToId() { return getUserId(); }

    public String getDepartment()             { return department; }
    public void   setDepartment(String dept)  { this.department = dept; }

    /**
     * Polymorphic override of abstract method getRole().
     * Demonstrates Polymorphism (OOP Pillar).
     */
    @Override
    public String getRole() {
        return "Technical Officer";
    }
}
