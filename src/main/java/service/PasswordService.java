package service;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {

    private static final int BCRYPT_ROUNDS = 12;

    // Hash Password
    public static String hashPassword(String plainPassword) {

        String error = validatePasswordComplexity(plainPassword);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        return BCrypt.hashpw(
                plainPassword,
                BCrypt.gensalt(BCRYPT_ROUNDS)
        );
    }

    // Verify Password
    public static boolean verifyPassword(String plainPassword,
                                         String storedHash) {

        if (plainPassword == null || storedHash == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, storedHash);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Validate Password
    public static String validatePasswordComplexity(String password) {

        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        return null;
    }
}