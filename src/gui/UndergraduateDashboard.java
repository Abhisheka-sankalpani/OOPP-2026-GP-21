package gui;
import dao.UndergraduateDAO;
import model.Attendance;
import model.Undergraduate;
import model.Medical;

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
        tabbedPane.addTab("Home", createHomePanel());
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));


        //Add Attendance Tab
        tabbedPane.addTab("My Attendance", createAttendancePanel());
        // Add Medical Tab
        tabbedPane.addTab("My Medicals", new UndergraduateMedicalForm(studentId));
        //Add Course tab
        tabbedPane.addTab("My Courses", new UndergraduateCourseForm(studentId));
        //Add Course Material Tab
        tabbedPane.addTab("Course Materials", new UndergraduateCourseMaterialForm(studentId));
        //Add Exam Eligibility
        tabbedPane.addTab("GPA & Exam Eligibility", new UndergraduateEligibilityForm(studentId));
        //Add Timetable Tab
        tabbedPane.addTab("Timetable", new UndergraduateTimetableForm("ICT"));
        //Add Notice Tab
        tabbedPane.addTab("Notices", new UndergraduateNoticeForm());
        // Add Profile Tab
        tabbedPane.addTab("My Profile", new UndergraduateProfileForm(studentId));
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

    // Home Tab
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Quick Overview Panel
        JPanel pnlOverview = new JPanel(new GridLayout(1, 3, 15, 15));
        pnlOverview.setBorder(BorderFactory.createTitledBorder("Quick Overview"));

        String currentGPA = undergraduateDAO.getStudentCGPA(studentId);

        java.util.List<model.Attendance> attList = attendanceDAO.getAttendanceByStudentId(studentId);
        double totalAtt = 0.0;
        int courseCount = 0;
        for (model.Attendance a : attList) {
            totalAtt += a.getOverallPercentage();
            courseCount++;
        }
        String overallAttendance = (courseCount > 0) ? String.format("%.1f%%", (totalAtt / courseCount)) : "N/A";

        pnlOverview.add(createStudentInfoCard(currentStudent));
        // 2. Attendance & 3. GPA
        pnlOverview.add(createSummaryCard("Attendance", overallAttendance));
        pnlOverview.add(createSummaryCard("Current GPA", currentGPA));

        // 2. Today's Schedule Panel
        JPanel pnlSchedule = new JPanel(new BorderLayout());
        pnlSchedule.setBorder(BorderFactory.createTitledBorder("Today's Schedule"));

        String todaySchedule = "  No schedule for today.";
        try (java.sql.Connection conn = db.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(
                     "SELECT start_time, end_time, course_name, location FROM timetable WHERE day_of_week = DAYNAME(CURDATE()) LIMIT 1")) {
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    todaySchedule = "  " + rs.getString("start_time") + " - " + rs.getString("end_time") + " : " + rs.getString("course_name") + " (" + rs.getString("location") + ")";
                }
            }
        } catch (Exception e) {
            System.out.println("Timetable DB Error: " + e.getMessage());
        }

        JLabel lblSchedule = new JLabel(todaySchedule);
        lblSchedule.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlSchedule.add(lblSchedule, BorderLayout.CENTER);

        // 3. Recent Notices Panel
        JPanel pnlNotices = new JPanel(new BorderLayout());
        pnlNotices.setBorder(BorderFactory.createTitledBorder("Recent Notices"));

        String recentNotice = "  No recent notices.";
        try (java.sql.Connection conn = db.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(
                     "SELECT posted_date, title FROM notice ORDER BY posted_date DESC LIMIT 1")) {
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    recentNotice = "  [" + rs.getString("posted_date") + "] " + rs.getString("title");
                }
            }
        } catch (Exception e) {
            System.out.println("Notice DB Error: " + e.getMessage());
        }

        JLabel lblNotice = new JLabel(recentNotice);
        lblNotice.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlNotices.add(lblNotice, BorderLayout.CENTER);

        panel.add(pnlOverview);
        panel.add(pnlSchedule);
        panel.add(pnlNotices);

        return panel;
    }

    private JPanel createSummaryCard(String title, String value) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(new Color(240, 248, 255));
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240)));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 18));
        lblValue.setForeground(new Color(0, 102, 204));

        card.add(lblTitle);
        card.add(lblValue);
        return card;
    }

    private JPanel createStudentInfoCard(Undergraduate student) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(240, 248, 255));
        card.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240)));

        JLabel lblTitle = new JLabel("Student Info", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBackground(new Color(240, 248, 255));

        if (student != null) {
            JLabel lblReg = new JLabel("Reg No: " + student.getRegNo(), SwingConstants.CENTER);
            JLabel lblBatch = new JLabel("Batch: " + student.getBatch(), SwingConstants.CENTER);
            JLabel lblDept = new JLabel("Dept: " + student.getDepartment(), SwingConstants.CENTER);

            Font smallFont = new Font("Arial", Font.BOLD, 12);
            lblReg.setFont(smallFont);
            lblBatch.setFont(smallFont);
            lblDept.setFont(smallFont);

            Color blueColor = new Color(0, 102, 204);
            lblReg.setForeground(blueColor);
            lblBatch.setForeground(blueColor);
            lblDept.setForeground(blueColor);

            infoPanel.add(lblReg);
            infoPanel.add(lblBatch);
            infoPanel.add(lblDept);
        } else {
            JLabel lblNA = new JLabel("N/A", SwingConstants.CENTER);
            lblNA.setFont(new Font("Arial", Font.BOLD, 18));
            infoPanel.add(lblNA);
        }

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UndergraduateDashboard("tg1001").setVisible(true);
        });
    }

}



