package view;

import model.Admin;
import model.User;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class LoginFrame extends JFrame {

    // Attributes
    private final AuthService authService;

    private JTextField usernameField;
    private JPasswordField passwordField;

    private JLabel statusLabel;

    private JButton loginButton;
    private JButton exitButton;

    // Constructor
    public LoginFrame() {

        authService = new AuthService();

        initComponents();
    }

    // Initialize Login GUI
    private void initComponents() {

        setTitle("Login - Faculty of Technology MIS | University of Ruhuna");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(490, 560);

        setResizable(false);

        setLocationRelativeTo(null);


        // =====================================
        // MAIN PANEL
        // =====================================

        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.setBackground(UIStyle.COLOR_BG);


        // =====================================
        // HEADER PANEL
        // =====================================

        JPanel headerPanel = new JPanel();

        headerPanel.setLayout(
                new BoxLayout(headerPanel, BoxLayout.Y_AXIS)
        );

        headerPanel.setBackground(UIStyle.COLOR_PRIMARY);

        headerPanel.setBorder(
                new EmptyBorder(28, 24, 24, 24)
        );


        JLabel universityLabel = new JLabel(
                "UNIVERSITY OF RUHUNA"
        );

        universityLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        universityLabel.setForeground(
                UIStyle.COLOR_TEXT_LIGHT
        );

        universityLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        JLabel titleLabel = new JLabel(
                "Faculty of Technology"
        );

        titleLabel.setFont(UIStyle.FONT_TITLE);

        titleLabel.setForeground(Color.WHITE);

        titleLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        JLabel subLabel = new JLabel(
                "Management Information System (FOT-MIS)"
        );

        subLabel.setFont(UIStyle.FONT_BODY);

        subLabel.setForeground(
                UIStyle.COLOR_TEXT_LIGHT
        );

        subLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        headerPanel.add(universityLabel);

        headerPanel.add(
                Box.createRigidArea(new Dimension(0, 5))
        );

        headerPanel.add(titleLabel);

        headerPanel.add(
                Box.createRigidArea(new Dimension(0, 5))
        );

        headerPanel.add(subLabel);


        mainPanel.add(
                headerPanel,
                BorderLayout.NORTH
        );


        // =====================================
        // CENTER FORM
        // =====================================

        JPanel cardWrapper = new JPanel(
                new GridBagLayout()
        );

        cardWrapper.setBackground(UIStyle.COLOR_BG);

        cardWrapper.setBorder(
                new EmptyBorder(20, 24, 20, 24)
        );


        JPanel formCard = new JPanel(
                new GridBagLayout()
        );

        formCard.setBackground(Color.WHITE);

        formCard.setBorder(
                UIStyle.createCardBorder()
        );

        formCard.setPreferredSize(
                new Dimension(410, 340)
        );


        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;

        gbc.weightx = 1;

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.insets = new Insets(6, 12, 6, 12);


        // =====================================
        // SIGN IN TITLE
        // =====================================

        gbc.gridy = 0;

        JLabel signInTitle = new JLabel(
                "Sign In to Portal"
        );

        signInTitle.setFont(
                UIStyle.FONT_SUBTITLE
        );

        signInTitle.setForeground(
                UIStyle.COLOR_TEXT_MAIN
        );

        formCard.add(signInTitle, gbc);


        // =====================================
        // USER ID
        // =====================================

        gbc.gridy = 1;

        JLabel userLabel = new JLabel("User ID");

        userLabel.setFont(
                UIStyle.FONT_BODY_BOLD
        );

        formCard.add(userLabel, gbc);


        gbc.gridy = 2;

        usernameField =
                UIStyle.createTextField(20);

        usernameField.setToolTipText(
                "Enter your User ID"
        );

        formCard.add(usernameField, gbc);


        // =====================================
        // PASSWORD
        // =====================================

        gbc.gridy = 3;

        JLabel passwordLabel = new JLabel("Password");

        passwordLabel.setFont(
                UIStyle.FONT_BODY_BOLD
        );

        formCard.add(passwordLabel, gbc);


        gbc.gridy = 4;

        passwordField =
                UIStyle.createPasswordField(20);

        formCard.add(passwordField, gbc);


        // =====================================
        // SHOW / HIDE PASSWORD
        // =====================================

        gbc.gridy = 5;

        JCheckBox showPassword = new JCheckBox(
                "Show Password"
        );

        showPassword.setFont(
                UIStyle.FONT_SMALL
        );

        showPassword.setBackground(Color.WHITE);

        showPassword.setFocusPainted(false);

        char defaultEchoChar =
                passwordField.getEchoChar();

        showPassword.addActionListener(e -> {

            if (showPassword.isSelected()) {

                passwordField.setEchoChar((char) 0);

            } else {

                passwordField.setEchoChar(
                        defaultEchoChar
                );
            }
        });

        formCard.add(showPassword, gbc);


        // =====================================
        // STATUS MESSAGE
        // =====================================

        gbc.gridy = 6;

        statusLabel = new JLabel(" ");

        statusLabel.setFont(
                UIStyle.FONT_SMALL
        );

        statusLabel.setForeground(
                UIStyle.COLOR_DANGER
        );

        formCard.add(statusLabel, gbc);


        // =====================================
        // BUTTONS
        // =====================================

        gbc.gridy = 7;

        JPanel buttonPanel = new JPanel(
                new GridLayout(1, 2, 10, 0)
        );

        buttonPanel.setOpaque(false);


        // Login Button
        loginButton =
                UIStyle.createPrimaryButton("Login");

        loginButton.addActionListener(
                this::handleLogin
        );


        // Exit Button
        exitButton =
                UIStyle.createSecondaryButton("Exit");

        exitButton.addActionListener(e -> {

            dispose();

            System.exit(0);
        });


        buttonPanel.add(loginButton);

        buttonPanel.add(exitButton);

        formCard.add(buttonPanel, gbc);


        cardWrapper.add(formCard);

        mainPanel.add(
                cardWrapper,
                BorderLayout.CENTER
        );


        // =====================================
        // FOOTER
        // =====================================

        JPanel footerPanel = new JPanel(
                new BorderLayout()
        );

        footerPanel.setBackground(
                new Color(241, 245, 249)
        );

        footerPanel.setBorder(
                new EmptyBorder(10, 16, 10, 16)
        );


        JLabel footerLabel = new JLabel(
                "Faculty of Technology | University of Ruhuna"
        );

        footerLabel.setFont(
                UIStyle.FONT_SMALL
        );

        footerLabel.setForeground(
                UIStyle.COLOR_TEXT_MUTED
        );

        footerLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );


        footerPanel.add(
                footerLabel,
                BorderLayout.CENTER
        );

        mainPanel.add(
                footerPanel,
                BorderLayout.SOUTH
        );


        // Set Main Panel
        setContentPane(mainPanel);


        // Press Enter to Login
        getRootPane().setDefaultButton(
                loginButton
        );
    }


    // =====================================
    // HANDLE LOGIN
    // =====================================

    private void handleLogin(ActionEvent e) {

        String userId =
                usernameField.getText().trim();

        char[] passwordChars =
                passwordField.getPassword();


        // Validate User ID
        if (userId.isEmpty()) {

            Arrays.fill(passwordChars, '\0');

            showError(
                    "Please enter your User ID."
            );

            usernameField.requestFocusInWindow();

            return;
        }


        // Validate Password
        if (passwordChars.length == 0) {

            Arrays.fill(passwordChars, '\0');

            showError(
                    "Please enter your password."
            );

            passwordField.requestFocusInWindow();

            return;
        }


        // Password required by AuthService
        String password = new String(passwordChars);

        Arrays.fill(passwordChars, '\0');

        passwordField.setText("");


        // Disable Login Button
        loginButton.setEnabled(false);

        statusLabel.setForeground(
                UIStyle.COLOR_PRIMARY_LIGHT
        );

        statusLabel.setText(
                "Authenticating..."
        );


        // Background Authentication
        SwingWorker<User, Void> worker =
                new SwingWorker<User, Void>() {

                    @Override
                    protected User doInBackground()
                            throws Exception {

                        return authService.login(
                                userId,
                                password
                        );
                    }


                    @Override
                    protected void done() {

                        try {

                            User user = get();

                            statusLabel.setText(
                                    "Login successful!"
                            );

                            // Open Correct Dashboard
                            openDashboard(user);

                        } catch (InterruptedException ex) {

                            Thread.currentThread().interrupt();

                            showError(
                                    "Login was interrupted."
                            );

                        } catch (ExecutionException ex) {

                            Throwable cause = ex.getCause();

                            if (cause instanceof
                                    AuthService.AuthenticationException) {

                                showError(
                                        cause.getMessage()
                                );

                            } else {

                                showError(
                                        "Unexpected system error."
                                );
                            }

                        } catch (RuntimeException ex) {

                            authService.logout();

                            showError(
                                    "Unable to open dashboard."
                            );

                        } finally {

                            loginButton.setEnabled(true);
                        }
                    }
                };

        worker.execute();
    }


    // =====================================
    // OPEN DASHBOARD
    // =====================================

    private void openDashboard(User user) {

        if (user == null || user.getRole() == null) {

            authService.logout();

            showError("Invalid user account.");

            return;
        }


        // ADMIN LOGIN
        if ("ADMIN".equals(user.getRole())) {

            if (!(user instanceof Admin)) {

                authService.logout();

                showError(
                        "Invalid administrator account."
                );

                return;
            }

            AdminDashboard dashboard =
                    new AdminDashboard((Admin) user);

            dashboard.setVisible(true);

            dispose();

        } else {

            // OTHER USER ROLES
            switch (user.getRole()) {

                case "LECTURER":
                case "TECHNICAL_OFFICER":
                case "STUDENT":

                    DashboardFrame dashboard =
                            new DashboardFrame(
                                    user,
                                    authService
                            );

                    dashboard.setVisible(true);

                    dispose();

                    break;

                default:

                    authService.logout();

                    showError(
                            "Unknown user role."
                    );
            }
        }
    }


    // =====================================
    // SHOW ERROR MESSAGE
    // =====================================

    private void showError(String message) {

        statusLabel.setForeground(
                UIStyle.COLOR_DANGER
        );

        statusLabel.setText(message);
    }
}