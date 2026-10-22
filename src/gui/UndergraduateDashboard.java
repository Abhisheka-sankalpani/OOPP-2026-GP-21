package gui;

import dao.UndergraduateDAO;
import model.Attendance;
import model.Undergraduate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UndergraduateDashboard extends JFrame {

    private String studentId;
    private Undergraduate currentStudent;
    private UndergraduateDAO undergraduateDAO;
    private JTabbedPane tabbedPane;
    private JTextField txtEmail;
    private JTextField txtContact;
    private JTextField txtProfilePic;
    private dao.AttendanceDAO attendanceDAO;
    private javax.swing.table.DefaultTableModel modelAttendance;
    private JLabel lblAttendanceSummary;
    private JTable tableAttendance;

    public UndergraduateDashboard(String studentId) {
        this.studentId = studentId;
        this.undergraduateDAO = new UndergraduateDAO();
        this.currentStudent = undergraduateDAO.getStudentById(this.studentId);
        this.attendanceDAO = new dao.AttendanceDAO();

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
        //Add Attendance Tab
        tabbedPane.addTab("My Attendance", createAttendancePanel());
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
    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        // Top bar
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel leftBox = new JPanel();
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("My Attendance");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        leftBox.add(lblTitle);

        JLabel lblSubtitle = new JLabel("Minimum requirement: 80% per course module.");
        leftBox.add(lblSubtitle);

        topPanel.add(leftBox, BorderLayout.WEST);

        // Refresh button
        JButton btnRefreshAtt = new JButton("Refresh");
        btnRefreshAtt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAttendanceData();
            }
        });
        topPanel.add(btnRefreshAtt, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        String[] colNames = {"Course ID", "Course Title", "Theory (Att/Tot)", "Practical (Att/Tot)", "Overall %", "Eligibility"};
        modelAttendance = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableAttendance = new JTable(modelAttendance);
        tableAttendance.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tableAttendance);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Footer Summary
        lblAttendanceSummary = new JLabel("Attendance summary: Calculating...");
        lblAttendanceSummary.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblAttendanceSummary, BorderLayout.SOUTH);

        return panel;
    }

    private void loadAttendanceData() {
        modelAttendance.setRowCount(0);
        List<Attendance> list = attendanceDAO.getAttendanceByStudentId(studentId);

        int eligibleCount = 0;
        int total = 0;

        for (Attendance a : list) {
            // Only list relevant courses or enrolled courses
            if (a.getOverallPercentage() > 0 || (a.getTheoryRatio().contains("/") && !a.getTheoryRatio().equals("0/0"))) {
                total++;
                boolean isEligible = a.getOverallPercentage() >= 80.0;
                if (isEligible) eligibleCount++;

                Object[] row = {
                        a.getCourseId(),
                        a.getCourseName(),
                        a.getTheoryRatio(),
                        a.getPracticalRatio(),
                        String.format("%.1f%%", a.getOverallPercentage()),
                        isEligible ? "Eligible" : "Not Eligible"
                };
                modelAttendance.addRow(row);
            }
        }

        lblAttendanceSummary.setText("Eligible Course Modules: " + eligibleCount + " of " + total + " enrolled courses.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UndergraduateDashboard("tg1001").setVisible(true);
        });
    }

}