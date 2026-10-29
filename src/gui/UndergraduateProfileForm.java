package gui;

import dao.UndergraduateDAO;
import model.Undergraduate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UndergraduateProfileForm extends JPanel {

    private String studentId;
    private UndergraduateDAO undergraduateDAO;
    private Undergraduate currentStudent;

    private JTextField txtEmail;
    private JTextField txtContact;
    private String profilePicPath = "";
    private JLabel lblProfileImage = new JLabel();

    private void updateProfileImage(String path) {
        if (path != null && !path.isEmpty() && new java.io.File(path).exists()) {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
            lblProfileImage.setIcon(new ImageIcon(img));
            lblProfileImage.setText("");
        } else {
            lblProfileImage.setIcon(null);

            String initials = "U";
            if (currentStudent != null && currentStudent.getFullName() != null) {
                String[] parts = currentStudent.getFullName().trim().split("\\s+");
                if (parts.length >= 2) {
                    initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
                } else if (parts.length == 1 && !parts[0].isEmpty()) {
                    initials = ("" + parts[0].charAt(0)).toUpperCase();
                }
            }

            lblProfileImage.setText(initials);
            lblProfileImage.setFont(new Font("Arial", Font.BOLD, 36));
            lblProfileImage.setForeground(Color.DARK_GRAY);
            lblProfileImage.setHorizontalAlignment(SwingConstants.CENTER);
            lblProfileImage.setVerticalAlignment(SwingConstants.CENTER);

            lblProfileImage.setPreferredSize(new Dimension(130, 130));
            lblProfileImage.setMinimumSize(new Dimension(130, 130));
            lblProfileImage.setMaximumSize(new Dimension(130, 130));
            lblProfileImage.setOpaque(true);
            lblProfileImage.setBackground(new Color(220, 220, 220));
            lblProfileImage.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }
    }

    public UndergraduateProfileForm(String studentId) {
        this.studentId = studentId;
        this.undergraduateDAO = new UndergraduateDAO();
        this.currentStudent = undergraduateDAO.getStudentById(this.studentId);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("My Profile Information");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(7, 2, 10, 10));

        if (currentStudent != null) {
            profilePicPath = currentStudent.getProfilePic();
            updateProfileImage(profilePicPath);

            centerPanel.add(new JLabel("Student ID:"));
            centerPanel.add(new JLabel(currentStudent.getStudentId()));

            centerPanel.add(new JLabel("Registration No:"));
            centerPanel.add(new JLabel(currentStudent.getRegNo()));

            centerPanel.add(new JLabel("Full Name:"));
            centerPanel.add(new JLabel(currentStudent.getFullName()));

            centerPanel.add(new JLabel("Department:"));
            centerPanel.add(new JLabel(currentStudent.getDepartment()));

            // Email Field
            centerPanel.add(new JLabel("Email:"));
            txtEmail = new JTextField(currentStudent.getEmail());
            centerPanel.add(txtEmail);

            // Contact Number Field
            centerPanel.add(new JLabel("Contact Number:"));
            txtContact = new JTextField(currentStudent.getContactNo());
            centerPanel.add(txtContact);

            centerPanel.add(new JLabel(""));
            JButton btnUpdate = new JButton("Update");

            btnUpdate.addActionListener(e -> {
                String email = txtEmail.getText();
                String contactNo = txtContact.getText();
                boolean isSuccess = undergraduateDAO.updateContactDetails(studentId, email, contactNo, profilePicPath);

                if (isSuccess) {
                    JOptionPane.showMessageDialog(UndergraduateProfileForm.this, "Updated Successfully!");
                } else {
                    JOptionPane.showMessageDialog(UndergraduateProfileForm.this, "Update Failed!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            centerPanel.add(btnUpdate);

        } else {
            centerPanel.add(new JLabel("Student data not found in Database!"));
        }

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        lblProfileImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(lblProfileImage);
        rightPanel.add(Box.createVerticalStrut(15));

        JButton btnBrowse = new JButton("Edit Profile");
        btnBrowse.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnBrowse.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(UndergraduateProfileForm.this) == JFileChooser.APPROVE_OPTION) {
                profilePicPath = fileChooser.getSelectedFile().getAbsolutePath();
                updateProfileImage(profilePicPath);
            }
        });
        rightPanel.add(btnBrowse);

        JPanel mainContentPanel = new JPanel(new BorderLayout(20, 0));
        mainContentPanel.add(centerPanel, BorderLayout.CENTER);
        mainContentPanel.add(rightPanel, BorderLayout.EAST);

        add(mainContentPanel, BorderLayout.CENTER);
    }
}