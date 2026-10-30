package service;

import dao.UserDAO;
import model.User;

import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO;
    private final SessionManager sessionManager;

    // Default Constructor
    public AuthService() {
        this.userDAO = new UserDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    // Parameterized Constructor
    public AuthService(UserDAO userDAO, SessionManager sessionManager) {
        this.userDAO = userDAO;
        this.sessionManager = sessionManager;
    }

    // LOGIN - Authenticate User
    public User login(String userId, String password)
            throws AuthenticationException {

        // Validate User ID
        if (userId == null || userId.isBlank()) {
            throw new AuthenticationException(
                    "User ID cannot be empty"
            );
        }

        // Validate Password
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException(
                    "Password cannot be empty"
            );
        }

        try {

            // Find User
            User user = userDAO.getUserById(userId.trim());

            if (user == null) {
                throw new AuthenticationException(
                        "Invalid User ID or Password"
                );
            }

            // Verify Password
            boolean isValid = PasswordService.verifyPassword(
                    password,
                    user.getPasswordHash()
            );

            if (!isValid) {
                throw new AuthenticationException(
                        "Invalid User ID or Password"
                );
            }

            // Create Session
            sessionManager.setCurrentUser(user);

            return user;

        } catch (SQLException e) {

            throw new AuthenticationException(
                    "Unable to complete login. Please try again.",
                    e
            );
        }
    }

    // LOGOUT
    public void logout() {

        sessionManager.logout();

    }

    // Custom Exception
    public static class AuthenticationException extends Exception {

        public AuthenticationException(String message) {
            super(message);
        }

        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}