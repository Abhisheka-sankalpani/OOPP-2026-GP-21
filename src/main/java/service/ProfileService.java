package service;

import dao.UserDAO;
import model.User;

import java.sql.SQLException;

public class ProfileService {

    private final UserDAO userDAO = new UserDAO();

    // Get Currently Logged-in User
    private User requireCurrentUser() {

        User user = SessionManager.getInstance().getCurrentUser();

        if (user == null) {
            throw new SecurityException("Please log in first.");
        }

        return user;
    }

    // UPDATE - Own Profile
    public boolean updateMyProfile(
            String firstName,
            String lastName,
            String email,
            String contactNo,
            String profilePic) throws SQLException {

        User user = requireCurrentUser();

        if (firstName == null || firstName.isBlank()
                || firstName.length() > 50
                || lastName == null || lastName.isBlank()
                || lastName.length() > 50) {

            throw new IllegalArgumentException("Valid name is required.");
        }

        if (email == null || email.isBlank()
                || email.length() > 100 || !email.contains("@")) {

            throw new IllegalArgumentException("Valid email is required.");
        }

        if (contactNo != null && contactNo.length() > 15) {
            throw new IllegalArgumentException("Contact number is too long.");
        }

        if (profilePic != null && profilePic.length() > 255) {
            throw new IllegalArgumentException("Profile picture path is too long.");
        }

        boolean updated = userDAO.updateProfile(
                user.getUserId(),
                firstName.trim(),
                lastName.trim(),
                email.trim(),
                contactNo,
                profilePic
        );

        if (updated) {
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setEmail(email.trim());
            user.setContactNo(contactNo);
            user.setProfilePic(profilePic);
        }

        return updated;
    }

    // CHANGE PASSWORD
    public void changePassword(
            char[] currentPassword,
            char[] newPassword) throws SQLException {

        User currentUser = requireCurrentUser();

        if (currentPassword == null || currentPassword.length == 0) {
            throw new IllegalArgumentException(
                    "Current password is required."
            );
        }

        if (newPassword == null || newPassword.length < 8) {
            throw new IllegalArgumentException(
                    "New password must contain at least 8 characters."
            );
        }

        // Get latest password hash from database
        User storedUser =
                userDAO.getUserById(currentUser.getUserId());

        if (storedUser == null) {
            throw new SecurityException("Account not found.");
        }

        String storedHash = storedUser.getPasswordHash();

        // BCrypt library requires String inputs
        boolean valid = PasswordService.verifyPassword(
                new String(currentPassword),
                storedHash
        );

        if (!valid) {
            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }

        if (PasswordService.verifyPassword(
                new String(newPassword), storedHash)) {

            throw new IllegalArgumentException(
                    "New password must be different."
            );
        }

        String newHash = PasswordService.hashPassword(
                new String(newPassword)
        );

        boolean updated = userDAO.updatePassword(
                currentUser.getUserId(),
                storedHash,
                newHash
        );

        if (!updated) {
            throw new SQLException(
                    "Password was changed elsewhere. Please try again."
            );
        }

        currentUser.setPasswordHash(newHash);
    }
}