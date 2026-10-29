package model;

public class Lecturer extends User {
    private String department;

    public Lecturer(String userId, String firstName, String lastName, String email,
                    String contactNo, String profilePic, String department) {
        super(userId, firstName, lastName, email, contactNo, profilePic);
        this.department = department;
    }

    @Override
    public String getRole() {
        return "LECTURER";
    }

    public void showDashboard() {
        // Replace with the Swing Lecturer dashboard later
        System.out.println("Lecturer Dashboard - " + getFirstName() + " " + getLastName()
                + " (" + department + ")");
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}