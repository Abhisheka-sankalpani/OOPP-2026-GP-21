package view;

import dao.UserDAO;
import model.User;
import service.ProfileService;
import service.SessionManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class ProfilePanel extends JPanel {

    private final ProfileService profileService =
            new ProfileService();

    // Profile Fields
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField contactField;
    private JTextField photoField;

    private JLabel userIdLabel;
    private JLabel roleLabel;
    private JLabel photoLabel;

    private JButton saveButton;
    private JButton passwordButton;

    public ProfilePanel() {

        initComponents();
        refreshProfile();
    }

    // =====================================
    // INITIALIZE GUI
    // =====================================

    private void initComponents() {

        setLayout(new BorderLayout(15, 15));

        setBackground(UIStyle.COLOR_BG);

        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Page Title
        JLabel titleLabel = new JLabel("My Profile");

        titleLabel.setFont(UIStyle.FONT_TITLE);

        titleLabel.setForeground(UIStyle.COLOR_PRIMARY);

        add(titleLabel, BorderLayout.NORTH);

        // Main Card
        JPanel profileCard = new JPanel(new BorderLayout(25, 20));

        profileCard.setBackground(Color.WHITE);

        profileCard.setBorder(UIStyle.createCardBorder());

        // =====================================
        // PROFILE PHOTO
        // =====================================

        JPanel photoPanel = new JPanel();

        photoPanel.setLayout(
                new BoxLayout(photoPanel, BoxLayout.Y_AXIS)
        );

        photoPanel.setBackground(Color.WHITE);

        photoPanel.setPreferredSize(new Dimension(170, 350));

        photoLabel = new JLabel("No Photo", SwingConstants.CENTER);

        photoLabel.setPreferredSize(new Dimension(120, 120));
        photoLabel.setMinimumSize(new Dimension(120, 120));
        photoLabel.setMaximumSize(new Dimension(120, 120));

        photoLabel.setOpaque(true);

        photoLabel.setBackground(new Color(226, 232, 240));

        photoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton choosePhotoButton =
                UIStyle.createSecondaryButton("Choose Photo");

        choosePhotoButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        choosePhotoButton.addActionListener(e -> choosePhoto());

        photoPanel.add(photoLabel);

        photoPanel.add(
                Box.createRigidArea(new Dimension(0, 15))
        );

        photoPanel.add(choosePhotoButton);

        profileCard.add(photoPanel, BorderLayout.WEST);

        // =====================================
        // PROFILE DETAILS FORM
        // =====================================

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 12, 12));

        formPanel.setBackground(Color.WHITE);

        userIdLabel = new JLabel();

        roleLabel = new JLabel();

        firstNameField = UIStyle.createTextField(20);
        lastNameField = UIStyle.createTextField(20);
        emailField = UIStyle.createTextField(20);
        contactField = UIStyle.createTextField(20);
        photoField = UIStyle.createTextField(20);

        addFormRow(formPanel, "User ID", userIdLabel);

        addFormRow(formPanel, "User Role", roleLabel);

        addFormRow(formPanel, "First Name", firstNameField);

        addFormRow(formPanel, "Last Name", lastNameField);

        addFormRow(formPanel, "Email Address", emailField);

        addFormRow(formPanel, "Contact Number", contactField);

        addFormRow(formPanel, "Profile Picture Path", photoField);

        profileCard.add(formPanel, BorderLayout.CENTER);

        // =====================================
        // BUTTONS
        // =====================================

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 10)
        );

        buttonPanel.setBackground(Color.WHITE);

        JButton refreshButton =
                UIStyle.createSecondaryButton("Refresh");

        refreshButton.addActionListener(e -> refreshProfile());

        passwordButton =
                UIStyle.createSecondaryButton("Change Password");

        passwordButton.addActionListener(e -> showPasswordDialog());

        saveButton =
                UIStyle.createPrimaryButton("Save Changes");

        saveButton.addActionListener(e -> saveProfile());

        buttonPanel.add(refreshButton);
        buttonPanel.add(passwordButton);
        buttonPanel.add(saveButton);

        profileCard.add(buttonPanel, BorderLayout.SOUTH);

        add(profileCard, BorderLayout.CENTER);
    }

    // =====================================
    // FORM ROW
    // =====================================

    private void addFormRow(
            JPanel panel,
            String title,
            JComponent component) {

        JLabel label = new JLabel(title);

        label.setFont(UIStyle.FONT_BODY_BOLD);

        panel.add(label);

        panel.add(component);
    }

    // =====================================
    // LOAD PROFILE
    // =====================================

    public void refreshProfile() {

        User currentUser =
                SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showError("Please log in first.");
            return;
        }

        String userId = currentUser.getUserId();

        SwingWorker<User, Void> worker = new SwingWorker<>() {

            @Override
            protected User doInBackground() throws Exception {
                return new UserDAO().getUserById(userId);
            }

            @Override
            protected void done() {

                try {

                    User user = get();

                    if (user == null) {
                        showError("Profile not found.");
                        return;
                    }

                    userIdLabel.setText(user.getUserId());
                    roleLabel.setText(user.getRole());

                    firstNameField.setText(user.getFirstName());
                    lastNameField.setText(user.getLastName());
                    emailField.setText(user.getEmail());
                    contactField.setText(user.getContactNo());
                    photoField.setText(user.getProfilePic());

                    displayProfilePhoto(user.getProfilePic());

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();
                    showError("Loading interrupted.");

                } catch (ExecutionException ex) {

                    showError("Unable to load profile.");
                }
            }
        };

        worker.execute();
    }

    // =====================================
    // CHOOSE PHOTO
    // =====================================

    private void choosePhoto() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle("Choose Profile Photo");

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {

            String path =
                    chooser.getSelectedFile().getAbsolutePath();

            photoField.setText(path);

            displayProfilePhoto(path);
        }
    }

    // =====================================
    // DISPLAY PROFILE PHOTO
    // =====================================

    private void displayProfilePhoto(String path) {

        photoLabel.setIcon(null);
        photoLabel.setText("No Photo");

        if (path == null || path.isBlank()) {
            return;
        }

        try {

            BufferedImage image = ImageIO.read(new File(path));

            if (image == null) {
                return;
            }

            Image scaled = image.getScaledInstance(
                    110, 110, Image.SCALE_SMOOTH
            );

            photoLabel.setIcon(new ImageIcon(scaled));

            photoLabel.setText("");

        } catch (Exception e) {

            photoLabel.setText("Photo unavailable");
        }
    }

    // =====================================
    // SAVE PROFILE
    // =====================================

    private void saveProfile() {

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();
        String photo = photoField.getText().trim();

        saveButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                return profileService.updateMyProfile(
                        firstName,
                        lastName,
                        email,
                        contact,
                        photo
                );
            }

            @Override
            protected void done() {

                try {

                    if (get()) {

                        refreshProfile();

                        // Find Parent Window
                        Window window =
                                SwingUtilities.getWindowAncestor(
                                        ProfilePanel.this
                                );

                        // Refresh Admin Dashboard Header
                        if (window instanceof AdminDashboard dashboard) {

                            dashboard.refreshHeaderProfile();
                        }

                        JOptionPane.showMessageDialog(
                                ProfilePanel.this,
                                "Profile Updated Successfully!"
                        );
                    } else {

                        showError("Profile update failed.");
                    }

                } catch (Exception ex) {

                    Throwable cause = ex;

                    if (ex instanceof ExecutionException &&
                            ex.getCause() != null) {
                        cause = ex.getCause();
                    }

                    showError(cause.getMessage());

                } finally {

                    saveButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // =====================================
    // CHANGE PASSWORD DIALOG
    // =====================================

    private void showPasswordDialog() {

        JPasswordField currentField = new JPasswordField(20);
        JPasswordField newField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        panel.add(new JLabel("Current Password:"));
        panel.add(currentField);

        panel.add(new JLabel("New Password:"));
        panel.add(newField);

        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Change Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        char[] oldPassword = currentField.getPassword();
        char[] newPassword = newField.getPassword();
        char[] confirmation = confirmField.getPassword();

        currentField.setText("");
        newField.setText("");
        confirmField.setText("");

        if (!Arrays.equals(newPassword, confirmation)) {

            Arrays.fill(oldPassword, '\0');
            Arrays.fill(newPassword, '\0');
            Arrays.fill(confirmation, '\0');

            showError("New passwords do not match.");
            return;
        }

        Arrays.fill(confirmation, '\0');

        passwordButton.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {

                try {

                    profileService.changePassword(
                            oldPassword,
                            newPassword
                    );

                } finally {

                    Arrays.fill(oldPassword, '\0');
                    Arrays.fill(newPassword, '\0');
                }

                return null;
            }

            @Override
            protected void done() {

                try {

                    get();

                    JOptionPane.showMessageDialog(
                            ProfilePanel.this,
                            "Password Changed Successfully!"
                    );

                } catch (Exception ex) {

                    Throwable cause = ex;

                    if (ex instanceof ExecutionException &&
                            ex.getCause() != null) {
                        cause = ex.getCause();
                    }

                    showError(cause.getMessage());

                } finally {

                    passwordButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // =====================================
    // ERROR MESSAGE
    // =====================================

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "My Profile",
                JOptionPane.ERROR_MESSAGE
        );
    }
}