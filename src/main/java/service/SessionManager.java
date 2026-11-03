package service;

import model.User;

public class SessionManager {

    // Singleton Instance
    private static final SessionManager instance =
            new SessionManager();

    // Currently Logged-in User
    private User currentUser;

    // Private Constructor
    private SessionManager() {

    }

    // Get Singleton Instance
    public static SessionManager getInstance() {
        return instance;
    }

    // Set Current User
    public void setCurrentUser(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        this.currentUser = user;
    }

    // Get Current User
    public User getCurrentUser() {
        return currentUser;
    }

    // Check Login Status
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Check Admin Role
    public boolean isAdmin() {

        return currentUser != null &&
                "ADMIN".equals(currentUser.getRole());
    }

    // Logout User
    public void logout() {
        currentUser = null;
    }
}