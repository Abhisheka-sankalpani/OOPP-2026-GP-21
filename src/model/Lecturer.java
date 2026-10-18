package model;

public class Lecturer extends User {
    private String department;

    public Lecturer(String userId, String password, String firstName, String lastName,
                    String email, String contactNo, String profilePic, String department) {
        super(userId, password, firstName, lastName, email, contactNo, profilePic);
        this.department = department;
    }

    @Override
    public String getRole() {
        return "LECTURER";
    }

    @Override
    public void showDashboard() {
        // Replace with the Swing/JavaFX Lecturer dashboard later
        System.out.println("Lecturer Dashboard - " + getFullName() + " (" + department + ")");
    }

    public String getDepartment() { return department; }
}
