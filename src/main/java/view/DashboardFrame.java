package view;

import model.User;
import service.AuthService;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

public class DashboardFrame extends JFrame {

    private final User user;
    private final AuthService authService;

    // Constructor
    public DashboardFrame(
            User user,
            AuthService authService) {

        this.user = user;
        this.authService = authService;

        if (!SessionManager.getInstance().isLoggedIn()) {
            throw new IllegalStateException(
                    "User must be logged in"
            );
        }

        initComponents();
    }


    // Initialize Dashboard
    private void initComponents() {

        setTitle(
                "FOT-MIS | Dashboard"
        );

        setSize(650, 430);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);


        // Main Panel
        JPanel mainPanel = new JPanel(
                new BorderLayout(0, 20)
        );

        mainPanel.setBackground(
                UIStyle.COLOR_BG
        );

        mainPanel.setBorder(
                new EmptyBorder(30, 30, 30, 30)
        );


        // Header
        JLabel titleLabel = new JLabel(
                "FOT-MIS Dashboard"
        );

        titleLabel.setFont(
                UIStyle.FONT_TITLE
        );

        titleLabel.setForeground(
                UIStyle.COLOR_PRIMARY
        );

        mainPanel.add(
                titleLabel,
                BorderLayout.NORTH
        );


        // Center Card
        JPanel centerPanel = new JPanel(
                new GridLayout(4, 1, 10, 10)
        );

        centerPanel.setBackground(Color.WHITE);

        centerPanel.setBorder(
                UIStyle.createCardBorder()
        );


        JLabel welcomeLabel = new JLabel(
                "Welcome, " + user.getFullname()
        );

        welcomeLabel.setFont(
                UIStyle.FONT_SUBTITLE
        );


        JLabel userIdLabel = new JLabel(
                "User ID: " + user.getUserId()
        );


        JLabel roleLabel = new JLabel(
                "Role: " + user.getRole()
        );


        JLabel messageLabel = new JLabel(
                "Login and Session Management are working."
        );


        centerPanel.add(welcomeLabel);
        centerPanel.add(userIdLabel);
        centerPanel.add(roleLabel);
        centerPanel.add(messageLabel);

        mainPanel.add(
                centerPanel,
                BorderLayout.CENTER
        );


        // Logout Button
        JButton logoutButton =
                UIStyle.createPrimaryButton(
                        "Logout"
                );

        logoutButton.addActionListener(e -> {

            authService.logout();

            dispose();

            new LoginFrame().setVisible(true);
        });


        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        buttonPanel.setOpaque(false);

        buttonPanel.add(logoutButton);

        mainPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );


        setContentPane(mainPanel);
    }
}