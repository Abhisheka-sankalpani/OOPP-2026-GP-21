package gui;

import dao.UndergraduateDAO;
import model.Undergraduate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class UndergraduateProfileForm extends JPanel {

    private String studentId;
    private UndergraduateDAO undergraduateDAO;
    private Undergraduate currentStudent;

    private JTextField txtEmail;
    private JTextField txtContact;
    private String profilePicPath = "";
    private JLabel lblProfileImage = new JLabel();
    private Runnable onProfileUpdated;

    public void setOnProfileUpdated(Runnable onProfileUpdated) {
        this.onProfileUpdated = onProfileUpdated;
    }

    public UndergraduateProfileForm(String studentId) {
        this.studentId = studentId;
        this.undergraduateDAO = new UndergraduateDAO();
        this.currentStudent = undergraduateDAO.getStudentById(this.studentId);

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // 1. Header
        JPanel header = UITheme.createTabHeader(
                "My Profile Information",
                "View and manage your student enrollment details, contact numbers, and profile photo.",
                null
        );
        add(header, BorderLayout.NORTH);

        // 2. Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(18, 0));
        centerPanel.setOpaque(false);

        // Student Profile Information
        JPanel detailsCard = UITheme.createCardPanel();
        detailsCard.setLayout(new BorderLayout(0, 16));

        JLabel lblCardTitle = new JLabel("Academic & Contact Particulars");
        lblCardTitle.setFont(UITheme.FONT_SECTION);
        lblCardTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        detailsCard.add(lblCardTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 14, 14));
        fieldsPanel.setOpaque(false);

        if (currentStudent != null) {
            profilePicPath = currentStudent.getProfilePic();

            fieldsPanel.add(createReadonlyRow("Student ID (System ID):", currentStudent.getStudentId()));
            fieldsPanel.add(createReadonlyRow("Registration Number:", currentStudent.getRegNo()));
            fieldsPanel.add(createReadonlyRow("Full Name:", currentStudent.getFullName()));
            fieldsPanel.add(createReadonlyRow("Academic Department:", currentStudent.getDepartment()));
            fieldsPanel.add(createReadonlyRow("Academic Batch:", currentStudent.getBatch()));
            fieldsPanel.add(createReadonlyRow("Student Status:", currentStudent.isRepeat() ? "Repeat Student" : "Regular Undergraduate"));

            // Editable Fields Card
            JPanel editContainer = new JPanel(new GridLayout(2, 2, 14, 12));
            editContainer.setOpaque(false);

            txtEmail = new JTextField(currentStudent.getEmail() != null ? currentStudent.getEmail() : "");
            txtEmail.setFont(UITheme.FONT_BODY);
            txtContact = new JTextField(currentStudent.getContactNo() != null ? currentStudent.getContactNo() : "");
            txtContact.setFont(UITheme.FONT_BODY);

            editContainer.add(createEditRow("Email Address:", txtEmail));
            editContainer.add(createEditRow("Contact Phone Number:", txtContact));

            JPanel leftContent = new JPanel();
            leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
            leftContent.setOpaque(false);
            leftContent.add(fieldsPanel);
            leftContent.add(Box.createVerticalStrut(16));
            leftContent.add(new JSeparator());
            leftContent.add(Box.createVerticalStrut(16));
            leftContent.add(editContainer);

            // Bottom Update Button
            JButton btnUpdate = UITheme.createPrimaryButton("Save Profile Changes");
            btnUpdate.addActionListener(e -> {
                String email = txtEmail.getText().trim();
                String contactNo = txtContact.getText().trim();

                if (email.isEmpty() || contactNo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Email and Contact Number cannot be empty!", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean isSuccess = undergraduateDAO.updateContactDetails(studentId, email, contactNo, profilePicPath);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    if (onProfileUpdated != null) {
                        onProfileUpdated.run();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update profile details.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
            btnRow.setOpaque(false);
            btnRow.add(btnUpdate);

            leftContent.add(btnRow);
            detailsCard.add(leftContent, BorderLayout.CENTER);
        } else {
            detailsCard.add(new JLabel("Student data could not be retrieved from the database."), BorderLayout.CENTER);
        }

        centerPanel.add(detailsCard, BorderLayout.CENTER);

        //  Profile Picture
        JPanel avatarCard = UITheme.createCardPanel();
        avatarCard.setLayout(new BoxLayout(avatarCard, BoxLayout.Y_AXIS));
        avatarCard.setPreferredSize(new Dimension(280, 0));

        JLabel lblAvatarTitle = new JLabel("Profile Picture");
        lblAvatarTitle.setFont(UITheme.FONT_SECTION);
        lblAvatarTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        lblAvatarTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarCard.add(lblAvatarTitle);
        avatarCard.add(Box.createVerticalStrut(20));

        updateProfileImage(profilePicPath);
        lblProfileImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarCard.add(lblProfileImage);
        avatarCard.add(Box.createVerticalStrut(20));

        JButton btnBrowse = UITheme.createSecondaryButton("Upload Photo...");
        btnBrowse.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBrowse.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                profilePicPath = fileChooser.getSelectedFile().getAbsolutePath();
                updateProfileImage(profilePicPath);
            }
        });
        avatarCard.add(btnBrowse);
        avatarCard.add(Box.createVerticalStrut(10));

        JLabel lblHint = new JLabel("Supported: JPG, PNG, JPEG");
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(UITheme.COLOR_TEXT_MUTED);
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarCard.add(lblHint);

        centerPanel.add(avatarCard, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createReadonlyRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        JLabel val = new JLabel((value != null && !value.isEmpty()) ? value : "N/A");
        val.setFont(UITheme.FONT_BODY_BOLD);
        val.setForeground(UITheme.COLOR_TEXT_MAIN);

        p.add(lbl, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JPanel createEditRow(String label, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void updateProfileImage(String path) {
        if (path != null && !path.isEmpty() && new File(path).exists()) {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            lblProfileImage.setIcon(new ImageIcon(img));
            lblProfileImage.setText("");
            lblProfileImage.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 2));
        } else {
            lblProfileImage.setIcon(null);

            String initials = "ST";
            if (currentStudent != null && currentStudent.getFullName() != null) {
                String[] parts = currentStudent.getFullName().trim().split("\\s+");
                if (parts.length >= 2) {
                    initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
                } else if (parts.length == 1 && !parts[0].isEmpty()) {
                    initials = ("" + parts[0].charAt(0)).toUpperCase();
                }
            }

            lblProfileImage.setText(initials);
            lblProfileImage.setFont(new Font("Segoe UI", Font.BOLD, 42));
            lblProfileImage.setForeground(UITheme.COLOR_PRIMARY);
            lblProfileImage.setHorizontalAlignment(SwingConstants.CENTER);
            lblProfileImage.setVerticalAlignment(SwingConstants.CENTER);

            lblProfileImage.setPreferredSize(new Dimension(140, 140));
            lblProfileImage.setMinimumSize(new Dimension(140, 140));
            lblProfileImage.setMaximumSize(new Dimension(140, 140));
            lblProfileImage.setOpaque(true);
            lblProfileImage.setBackground(new Color(239, 246, 255));
            lblProfileImage.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 2));
        }
    }
}