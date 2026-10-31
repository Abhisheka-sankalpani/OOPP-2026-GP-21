package service;

import model.User;
import service.AuthService.AuthenticationException;

import java.util.Scanner;

public class TestLogin {

    public static void main(String[] args) {

        AuthService authService = new AuthService();

        SessionManager session =
                SessionManager.getInstance();

        Scanner scanner = new Scanner(System.in);

        try {

            System.out.print("Enter User ID: ");
            String userId = scanner.nextLine();

            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            // Authenticate User
            User user = authService.login(userId, password);

            System.out.println("Login Successful!");
            System.out.println("Welcome: " + user.getFullname());
            System.out.println("Role: " + user.getRole());

            // Check Session
            System.out.println(
                    "Logged In: " + session.isLoggedIn()
            );

            System.out.println(
                    "Is Admin: " + session.isAdmin()
            );

            // Logout
            authService.logout();

            System.out.println("Logged Out Successfully!");

            System.out.println(
                    "Logged In: " + session.isLoggedIn()
            );

        } catch (AuthenticationException e) {

            System.out.println(
                    "Login Failed: " + e.getMessage()
            );
        }
    }
}