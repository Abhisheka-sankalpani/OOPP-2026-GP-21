package gui;

import dao.AttendanceDAO;
import dao.CourseDAO;
import dao.NoticeDAO;
import dao.UndergraduateDAO;
import model.Attendance;
import model.Course;
import model.Notice;
import model.Undergraduate;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UndergraduateDashboard extends JFrame {

    private String studentId;
    private Undergraduate currentStudent;
    private UndergraduateDAO undergraduateDAO;
    private AttendanceDAO attendanceDAO;
    private CourseDAO courseDAO;
    private NoticeDAO noticeDAO;

    // Navigation and Content Panels
    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private List<JButton> navButtons = new ArrayList<>();
    private Map<String, JButton> navButtonMap = new HashMap<>();
    private JLabel lblHeaderAvatar;

    // Dashboard Overview widgets
    private JLabel lblStatAttendance;
    private JLabel lblStatAttendanceSub;
    private JLabel lblStatCourses;
    private JLabel lblStatNotices;
    private JLabel lblStatGPA;
    private DefaultTableModel modelRecentNotices;
    private JTable tableRecentNotices;

    // Individual Tab Panels
    private UndergraduateAttendancePanel attendancePanel;
    private UndergraduateCourseForm coursePanel;
    private UndergraduateCourseMaterialForm materialPanel;
    private UndergraduateEligibilityForm eligibilityPanel;
    private UndergraduateTimetableForm timetablePanel;
    private UndergraduateNoticeForm noticePanel;
    private UndergraduateMedicalForm medicalPanel;
    private UndergraduateProfileForm profilePanel;

    public UndergraduateDashboard(String studentId) {
        this.studentId = studentId;
        this.undergraduateDAO = new UndergraduateDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.courseDAO = new CourseDAO();
        this.noticeDAO = new NoticeDAO();
        this.currentStudent = undergraduateDAO.getStudentById(this.studentId);

        setTitle("Faculty of Technology MIS - Undergraduate Portal");
        setSize(1260, 780);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        refreshDashboardStats();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.COLOR_BG);

        // 1. Top Header Bar
        JPanel headerBar = createHeaderBar();
        rootPanel.add(headerBar, BorderLayout.NORTH);

        // 2. Left Navigation Sidebar
        sidebarPanel = createSidebar();
        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        // 3. Center Content Area
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UITheme.COLOR_BG);

        // Initialize Tab Panels
        JPanel overviewPanel = createOverviewPanel();
        attendancePanel = new UndergraduateAttendancePanel(studentId);
        coursePanel = new UndergraduateCourseForm(studentId);
        materialPanel = new UndergraduateCourseMaterialForm(studentId);
        eligibilityPanel = new UndergraduateEligibilityForm(studentId);
        timetablePanel = new UndergraduateTimetableForm(currentStudent != null ? currentStudent.getDepartment() : "ICT");
        noticePanel = new UndergraduateNoticeForm();
        medicalPanel = new UndergraduateMedicalForm(studentId);
        profilePanel = new UndergraduateProfileForm(studentId);
        profilePanel.setOnProfileUpdated(this::updateHeaderAvatar);

        mainContentPanel.add(overviewPanel, "overview");
        mainContentPanel.add(attendancePanel, "attendance");
        mainContentPanel.add(coursePanel, "courses");
        mainContentPanel.add(materialPanel, "materials");
        mainContentPanel.add(eligibilityPanel, "eligibility");
        mainContentPanel.add(timetablePanel, "timetable");
        mainContentPanel.add(noticePanel, "notices");
        mainContentPanel.add(medicalPanel, "medicals");
        mainContentPanel.add(profilePanel, "profile");

        rootPanel.add(mainContentPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);

        //Dashboard Overview
        selectNav("overview");
    }

    // TOP HEADER BAR
    private JPanel createHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout(15, 0));
        bar.setBackground(UITheme.COLOR_HEADER);
        bar.setBorder(new EmptyBorder(10, 20, 10, 20));
        bar.setPreferredSize(new Dimension(bar.getPreferredSize().width, 62));

        // Left Branding
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBrand.setOpaque(false);

        // University Building Logo
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);

                int[] xPoints = {2, 17, 32};
                int[] yPoints = {11, 2, 11};
                g2.fillPolygon(xPoints, yPoints, 3);

                g2.fillRect(2, 12, 30, 2);

                g2.fillRect(4, 15, 4, 11);
                g2.fillRect(11, 15, 4, 11);
                g2.fillRect(19, 15, 4, 11);
                g2.fillRect(26, 15, 4, 11);

                g2.fillRect(2, 27, 30, 3);
                g2.dispose();
            }
        };
        logoIcon.setPreferredSize(new Dimension(34, 32));
        logoIcon.setOpaque(false);
        leftBrand.add(logoIcon);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Faculty of Technology MIS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("University of Ruhuna, Sri Lanka | Undergraduate Workspace");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184)); // #94A3B8

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);
        leftBrand.add(titleBox);

        bar.add(leftBrand, BorderLayout.WEST);

        // Right Side
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightBox.setOpaque(false);

        String displayName = (currentStudent != null && currentStudent.getFullName() != null && !currentStudent.getFullName().trim().isEmpty())
                ? currentStudent.getFullName() + " (" + studentId + ")"
                : "Student (" + studentId + ")";

        lblHeaderAvatar = new JLabel();
        lblHeaderAvatar.setPreferredSize(new Dimension(32, 32));
        updateHeaderAvatar();
        rightBox.add(lblHeaderAvatar);

        JLabel lblUser = new JLabel(displayName);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(Color.WHITE);
        rightBox.add(lblUser);

        JButton btnSignOut = UITheme.createDangerButton("Sign Out");
        btnSignOut.addActionListener(e -> performLogout());
        rightBox.add(btnSignOut);

        bar.add(rightBox, BorderLayout.EAST);
        return bar;
    }

    public void updateHeaderAvatar() {
        if (lblHeaderAvatar == null) return;
        currentStudent = undergraduateDAO.getStudentById(studentId);

        if (currentStudent != null && currentStudent.getProfilePic() != null && !currentStudent.getProfilePic().trim().isEmpty()) {
            java.io.File file = new java.io.File(currentStudent.getProfilePic().trim());
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                java.awt.image.BufferedImage rounded = new java.awt.image.BufferedImage(32, 32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = rounded.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 32, 32));
                g2.drawImage(icon.getImage(), 0, 0, 32, 32, null);
                g2.dispose();
                lblHeaderAvatar.setIcon(new ImageIcon(rounded));
                return;
            }
        }

        // Default icon when no image uploaded
        java.awt.image.BufferedImage defImg = new java.awt.image.BufferedImage(28, 28, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = defImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillOval(9, 2, 10, 10);
        g2.fillArc(4, 13, 20, 14, 0, 180);
        g2.dispose();
        lblHeaderAvatar.setIcon(new ImageIcon(defImg));
    }

    // LEFT NAVIGATION SIDEBAR
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(235, 0));

        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        menuContainer.setBorder(new EmptyBorder(18, 12, 18, 12));

        // Navigation Menu Header Label
        JLabel lblMenu = new JLabel("NAVIGATION MENU");
        lblMenu.setFont(UITheme.FONT_NAV_LABEL);
        lblMenu.setForeground(new Color(100, 116, 139)); // #64748B
        lblMenu.setBorder(new EmptyBorder(0, 12, 12, 12));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuContainer.add(lblMenu);

        // Sidebar Navigation Buttons
        addNavButton(menuContainer, "Dashboard Overview", "overview");
        addNavButton(menuContainer, "My Attendance", "attendance");
        addNavButton(menuContainer, "Course Management", "courses");
        addNavButton(menuContainer, "Course Materials", "materials");
        addNavButton(menuContainer, "GPA & Eligibility", "eligibility");
        addNavButton(menuContainer, "Timetable Schedule", "timetable");
        addNavButton(menuContainer, "Notice Board", "notices");
        addNavButton(menuContainer, "Medical Submissions", "medicals");
        addNavButton(menuContainer, "My Profile", "profile");

        sidebar.add(menuContainer, BorderLayout.NORTH);
        return sidebar;
    }

    private void addNavButton(JPanel container, String text, String key) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(UITheme.COLOR_SIDEBAR_TEXT);
        btn.setBackground(UITheme.COLOR_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(210, 40));
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> selectNav(key));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(UITheme.COLOR_SIDEBAR_ACTIVE)) {
                    btn.setBackground(UITheme.COLOR_SIDEBAR_HOVER);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(UITheme.COLOR_SIDEBAR_ACTIVE)) {
                    btn.setBackground(UITheme.COLOR_SIDEBAR);
                }
            }
        });

        navButtons.add(btn);
        navButtonMap.put(key, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    public void selectNav(String key) {
        for (JButton b : navButtons) {
            b.setBackground(UITheme.COLOR_SIDEBAR);
            b.setForeground(UITheme.COLOR_SIDEBAR_TEXT);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        // Highlight selected
        JButton activeBtn = navButtonMap.get(key);
        if (activeBtn != null) {
            activeBtn.setBackground(UITheme.COLOR_SIDEBAR_ACTIVE);
            activeBtn.setForeground(Color.WHITE);
            activeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }

        cardLayout.show(mainContentPanel, key);

        if ("overview".equals(key)) {
            refreshDashboardStats();
            updateHeaderAvatar();
        } else if ("attendance".equals(key) && attendancePanel != null) {
            attendancePanel.loadAttendanceData();
        } else if ("courses".equals(key) && coursePanel != null) {
            coursePanel.loadCourseData();
        } else if ("eligibility".equals(key) && eligibilityPanel != null) {
            eligibilityPanel.loadEligibilityData();
        } else if ("timetable".equals(key) && timetablePanel != null) {
            timetablePanel.loadTimetableData();
        } else if ("notices".equals(key) && noticePanel != null) {
            noticePanel.loadNotices();
        } else if ("medicals".equals(key) && medicalPanel != null) {
            medicalPanel.loadMedicalData();
        }
    }

    // DASHBOARD OVERVIEW
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        String studentName = (currentStudent != null && currentStudent.getFullName() != null)
                ? currentStudent.getFullName()
                : "Student";

        JPanel header = UITheme.createTabHeader(
                "Welcome back, " + studentName,
                "Faculty of Technology Management Information System | Student Control Center",
                null
        );
        panel.add(header, BorderLayout.NORTH);

        // Center Container
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel cardAtt = UITheme.createStatCard("Overall Attendance", "0.0%", "Min 80% per module", UITheme.ACCENT_BLUE);
        JPanel cardCourse = UITheme.createStatCard("Total Courses", "0", "Enrolled syllabus modules", UITheme.ACCENT_TEAL);
        JPanel cardNotice = UITheme.createStatCard("Notices", "0", "Active faculty notices", UITheme.ACCENT_AMBER);
        JPanel cardGPA = UITheme.createStatCard("Current CGPA", "0.00", "Academic Standing: Good", UITheme.ACCENT_PURPLE);

        lblStatAttendance = findLabelInCard(cardAtt, UITheme.FONT_STAT_NUM);
        lblStatAttendanceSub = findSubLabelInCard(cardAtt);
        lblStatCourses = findLabelInCard(cardCourse, UITheme.FONT_STAT_NUM);
        lblStatNotices = findLabelInCard(cardNotice, UITheme.FONT_STAT_NUM);
        lblStatGPA = findLabelInCard(cardGPA, UITheme.FONT_STAT_NUM);

        kpiRow.add(cardAtt);
        kpiRow.add(cardCourse);
        kpiRow.add(cardNotice);
        kpiRow.add(cardGPA);
        center.add(kpiRow);
        center.add(Box.createVerticalStrut(16));

        JPanel midRow = new JPanel(new GridLayout(1, 2, 16, 0));
        midRow.setOpaque(false);
        midRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        JPanel cardHealth = UITheme.createCardPanel();
        cardHealth.setLayout(new BorderLayout());

        String regNo = currentStudent != null ? currentStudent.getRegNo() : "N/A";
        String dept = currentStudent != null ? currentStudent.getDepartment() : "ICT";
        String batch = currentStudent != null ? currentStudent.getBatch() : "N/A";

        JLabel lblStudentMeta = new JLabel("Enrolled: " + regNo + " | Dept: " + dept + " | Batch: " + batch);
        lblStudentMeta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStudentMeta.setForeground(UITheme.COLOR_TEXT_MAIN);
        cardHealth.add(lblStudentMeta, BorderLayout.CENTER);
        midRow.add(cardHealth);

        JPanel cardShortcuts = UITheme.createCardPanel();
        cardShortcuts.setLayout(new BorderLayout(0, 8));

        JLabel lblShortcutsTitle = new JLabel("Quick Management Shortcuts");
        lblShortcutsTitle.setFont(UITheme.FONT_SECTION);
        lblShortcutsTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        cardShortcuts.add(lblShortcutsTitle, BorderLayout.NORTH);

        JPanel btnBar = new JPanel(new GridLayout(1, 4, 8, 0));
        btnBar.setOpaque(false);

        JButton btnScMed = UITheme.createPrimaryButton("+ Medical");
        JButton btnScAtt = UITheme.createPrimaryButton("+ Attendance");
        JButton btnScMat = UITheme.createPrimaryButton("+ Materials");
        JButton btnScTime = UITheme.createPrimaryButton("+ Timetable");

        btnScMed.addActionListener(e -> selectNav("medicals"));
        btnScAtt.addActionListener(e -> selectNav("attendance"));
        btnScMat.addActionListener(e -> selectNav("materials"));
        btnScTime.addActionListener(e -> selectNav("timetable"));

        btnBar.add(btnScMed);
        btnBar.add(btnScAtt);
        btnBar.add(btnScMat);
        btnBar.add(btnScTime);
        cardShortcuts.add(btnBar, BorderLayout.CENTER);
        midRow.add(cardShortcuts);

        center.add(midRow);
        center.add(Box.createVerticalStrut(16));

        JPanel cardNotices = UITheme.createCardPanel();
        cardNotices.setLayout(new BorderLayout(0, 10));

        JPanel noticeHeader = new JPanel(new BorderLayout());
        noticeHeader.setOpaque(false);

        JLabel lblNoticeCardTitle = new JLabel("Latest Notices & Announcements");
        lblNoticeCardTitle.setFont(UITheme.FONT_SECTION);
        lblNoticeCardTitle.setForeground(UITheme.COLOR_TEXT_MAIN);

        JButton btnViewAll = new JButton("View All Notices →");
        btnViewAll.setFont(UITheme.FONT_BODY_BOLD);
        btnViewAll.setForeground(UITheme.COLOR_PRIMARY);
        btnViewAll.setContentAreaFilled(false);
        btnViewAll.setBorderPainted(false);
        btnViewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewAll.addActionListener(e -> selectNav("notices"));

        noticeHeader.add(lblNoticeCardTitle, BorderLayout.WEST);
        noticeHeader.add(btnViewAll, BorderLayout.EAST);
        cardNotices.add(noticeHeader, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Target Audience", "Posted Date"};
        modelRecentNotices = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableRecentNotices = new JTable(modelRecentNotices);
        UITheme.styleTable(tableRecentNotices);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableRecentNotices.getColumnModel().getColumn(0).setPreferredWidth(80);
        tableRecentNotices.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableRecentNotices.getColumnModel().getColumn(1).setPreferredWidth(500);
        tableRecentNotices.getColumnModel().getColumn(2).setPreferredWidth(140);
        tableRecentNotices.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableRecentNotices.getColumnModel().getColumn(3).setPreferredWidth(180);
        tableRecentNotices.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(tableRecentNotices);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        cardNotices.add(scroll, BorderLayout.CENTER);

        center.add(cardNotices);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    private JLabel findLabelInCard(JPanel card, Font targetFont) {
        for (Component c : card.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                for (Component sub : p.getComponents()) {
                    if (sub instanceof JLabel) {
                        JLabel lbl = (JLabel) sub;
                        if (lbl.getFont().equals(targetFont)) {
                            return lbl;
                        }
                    }
                }
            }
        }
        return new JLabel("0");
    }

    private JLabel findSubLabelInCard(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                for (Component sub : p.getComponents()) {
                    if (sub instanceof JLabel) {
                        JLabel lbl = (JLabel) sub;
                        if (lbl.getFont().equals(UITheme.FONT_SMALL)) {
                            return lbl;
                        }
                    }
                }
            }
        }
        return new JLabel("");
    }

    private void refreshDashboardStats() {
        List<Attendance> attList = attendanceDAO.getAttendanceByStudentId(studentId);
        double totalAttPct = 0.0;
        int activeCourses = 0;
        int eligibleCount = 0;

        for (Attendance a : attList) {
            if (a.getOverallPercentage() > 0 || (a.getTheoryRatio() != null && a.getTheoryRatio().contains("/") && !a.getTheoryRatio().equals("0/0"))) {
                totalAttPct += a.getOverallPercentage();
                activeCourses++;
                if (a.getOverallPercentage() >= 80.0) eligibleCount++;
            }
        }

        String avgAtt = (activeCourses > 0) ? String.format("%.1f%%", (totalAttPct / activeCourses)) : "0.0%";
        if (lblStatAttendance != null) lblStatAttendance.setText(avgAtt);
        if (lblStatAttendanceSub != null) {
            lblStatAttendanceSub.setText("Eligible: " + eligibleCount + " | Total: " + activeCourses + " modules");
        }

        List<Course> courses = courseDAO.getCoursesByStudent(studentId);
        if (lblStatCourses != null) lblStatCourses.setText(String.valueOf(courses.size()));

        List<Notice> notices = noticeDAO.getStudentNotices();
        if (lblStatNotices != null) lblStatNotices.setText(String.valueOf(notices.size()));

        if (modelRecentNotices != null) {
            modelRecentNotices.setRowCount(0);
            int count = 0;
            for (Notice n : notices) {
                modelRecentNotices.addRow(new Object[]{
                        n.getNoticeId(),
                        n.getTitle(),
                        n.getTargetRole(),
                        n.getPostedDate()
                });
                count++;
                if (count >= 5) break;
            }
        }

        String cgpa = undergraduateDAO.getStudentCGPA(studentId);
        if (lblStatGPA != null) lblStatGPA.setText(cgpa != null && !cgpa.isEmpty() ? cgpa : "N/A");
    }

    private void performLogout() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of the Undergraduate Workspace?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (option == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            new UndergraduateDashboard("tg1002").setVisible(true);
        });
    }
}
