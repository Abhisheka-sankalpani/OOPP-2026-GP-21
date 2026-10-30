package service;

public class TestPasswordService {

    public static void main(String[] args) {

        String password = "ExamplePass123!";

        // Hash Password
        String hashedPassword =
                PasswordService.hashPassword(password);

        // Verify Correct Password
        boolean result1 =
                PasswordService.verifyPassword(
                        password, hashedPassword
                );

        // Verify Wrong Password
        boolean result2 =
                PasswordService.verifyPassword(
                        "WrongPassword123!", hashedPassword
                );

        System.out.println("Correct Password: " + result1);
        System.out.println("Wrong Password: " + result2);
    }
}