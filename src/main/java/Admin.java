
public class Admin extends User {

    private static final long serialVersionUID = 1L;

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(String userId, String passwordHash, String firstName, String lastName,
                 String email, String contactNo, String profilePic) {
        super(userId, passwordHash, firstName, lastName, email, contactNo, profilePic, "ADMIN");
    }

    public Admin(String userId, String passwordHash, String firstName, String lastName, String email) {
        super(userId, passwordHash, firstName, lastName, email, "ADMIN");
    }

    @Override
    public String getRoleTitle() {
        return "System Administrator";
    }

    @Override
    public String getRoleSpecificDetails() {
        return "Full Administrative Access - Dean's Office / MIS Unit";
    }

    @Override
    public boolean hasAdminPrivileges() {
        return true;
    }
}
