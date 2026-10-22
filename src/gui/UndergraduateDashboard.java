package gui;

import dao.UndergraduateDAO;
import model.Undergraduate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UndergraduateDashboard extends JFrame {

    private String studentId;
    private Undergraduate currentStudent;
    private UndergraduateDAO undergraduateDAO;
    private JTabbedPane tabbedPane;
    private JTextField txtEmail;
    private JTextField txtContact;
    private JTextField txtProfilePic;

    public UndergraduateDashboard(String studentId) {
        this.studentId = studentId;
        this.undergraduateDAO = new UndergraduateDAO();
        this.currentStudent = undergraduateDAO.getStudentById(this.studentId);

        setTitle("Undergraduate Portal");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());

        // 1. Top Header Bar
        JPanel headerBar = createHeaderBar();
        rootPanel.add(headerBar, BorderLayout.NORTH);

        // 2. Tabbed Navigation Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));

        // Add Profile Tab
        tabbedPane.addTab("My Profile", createProfilePanel());

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        add(rootPanel);
    }

    private JPanel createHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 10));
        bar.setBackground(new Color(11, 102, 77)); // Dark Green Background
        bar.setBorder(new EmptyBorder(10, 20, 10, 20));

        String displayName = (currentStudent != null) ? currentStudent.getFullName() : "Unknown User";

        JLabel lblWelcome = new JLabel("Welcome, " + displayName);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        lblWelcome.setForeground(Color.WHITE);
        bar.add(lblWelcome, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(Color.RED);
        btnLogout.setForeground(Color.WHITE);

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(UndergraduateDashboard.this, "Are you sure you want to log out?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });

        bar.add(btnLogout, BorderLayout.EAST);
        return bar;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("My Profile Information");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(8, 2, 10, 10));

        if (currentStudent != null) {
            centerPanel.add(new JLabel("Student ID:"));
            centerPanel.add(new JLabel(currentStudent.getStudentId()));

            centerPanel.add(new JLabel("Registration No:"));
            centerPanel.add(new JLabel(currentStudent.getRegNo()));

            centerPanel.add(new JLabel("Full Name:"));
            centerPanel.add(new JLabel(currentStudent.getFullName()));

            centerPanel.add(new JLabel("Department:"));
            centerPanel.add(new JLabel(currentStudent.getDepartment()));

            // Email
            centerPanel.add(new JLabel("Email:"));
            txtEmail = new JTextField(currentStudent.getEmail());
            centerPanel.add(txtEmail);

            // Contact Number
            centerPanel.add(new JLabel("Contact Number:"));
            txtContact = new JTextField(currentStudent.getContactNo());
            centerPanel.add(txtContact);

            // Profile Picture
            centerPanel.add(new JLabel("Profile Picture:"));

            JPanel picPanel = new JPanel(new BorderLayout(5, 0));
            txtProfilePic = new JTextField(currentStudent.getProfilePic());
            JButton btnBrowse = new JButton("Browse...");

            btnBrowse.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(panel);
                if (result == JFileChooser.APPROVE_OPTION) {
                    txtProfilePic.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            });

            picPanel.add(txtProfilePic, BorderLayout.CENTER);
            picPanel.add(btnBrowse, BorderLayout.EAST);

            centerPanel.add(picPanel);

            // Update button
            centerPanel.add(new JLabel(""));
            JButton btnUpdate = new JButton("Update");

            btnUpdate.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 1. Get Data in Text Field
                    String email = txtEmail.getText();
                    String contactNo = txtContact.getText();
                    String profilePicPath = txtProfilePic.getText();

                    // 2. Update database
                    boolean isSuccess = undergraduateDAO.updateContactDetails(studentId, email, contactNo, profilePicPath);

                    if (isSuccess == true) {
                        JOptionPane.showMessageDialog(panel, "Updated Successfully!");
                    }
                }
            });

            centerPanel.add(btnUpdate);
        } else {
            centerPanel.add(new JLabel("Student data not found in Database!"));
        }

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UndergraduateDashboard("tg1001").setVisible(true);
        });
    }
}