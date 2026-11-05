package gui;

import dao.AssessmentDAO;
import dao.AssessmentDAOImpl;
import dao.CourseMaterialDAO;
import dao.CourseMaterialDAOImpl;
import dao.CourseDAO;
import dao.MarkDAO;
import dao.MarkDAOImpl;
import dao.NoticeBoardDAO;
import dao.NoticeBoardDAOImpl;
import dao.ResultDAO;
import dao.ResultDAOImpl;
import dao.LecturerReadOnlyDAO;
import dao.LecturerReadOnlyDAO.StudentInfo;
import dao.LecturerReadOnlyDAO.CourseInfo;
import exception.DatabaseException;
import exception.InvalidMarkException;
import model.Assessment;
import model.Course;
import model.LectureMaterial;
import model.Lecturer;
import model.Mark;
import model.NoticeItem;
import model.Result;
import util.CACalculator;
import util.EligibilityChecker;
import util.FileStorage;
import util.GPACalculator;

import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.regex.Pattern;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class LecturerDashboard extends JFrame {

    // Consistent with the Admin Dashboard colour palette.
    private static final Color HEADER = new Color(27, 54, 91);
    private static final Color SIDEBAR = new Color(15, 23, 42);
    private static final Color ACTIVE = new Color(37, 99, 235);
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color BORDER = new Color(221, 229, 239);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font UI_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JButton> navigation = new LinkedHashMap<>();
    private JPanel noticesPanel;
    private JLabel headerPhotoLabel;

    private final Lecturer lecturer;
    private final AssessmentDAO assessmentDAO = new AssessmentDAOImpl();
    private final MarkDAO markDAO = new MarkDAOImpl();
    private final ResultDAO resultDAO = new ResultDAOImpl();

    // Lecturer-only, read-only views: undergraduates, exam eligibility, attendance/medical.
    private final LecturerReadOnlyDAO readOnlyDAO = new LecturerReadOnlyDAO();
    private List<StudentInfo> allStudents = new ArrayList<>();
    private boolean studentListsLoaded = false;
    private JTextField undergraduateSearch;
    private JTable undergraduateTable;
    private DefaultTableModel undergraduateModel;
    private JTextArea undergraduateDetails;
    private JComboBox<String> eligibilityBatch;
    private JComboBox<CourseInfo> eligibilityCourse;
    private JTextField eligibilityStudentId;
    private JSpinner eligibilityCaWeight;
    private JTable eligibilityTable;
    private DefaultTableModel eligibilityModel;
    private JLabel eligibilitySummaryLabel;
    private JComboBox<StudentInfo> attendanceStudent;
    private JComboBox<CourseInfo> attendanceCourse;
    private JTextArea attendanceSummaryText;
    private DefaultTableModel attendanceDetailsModel;
    private DefaultTableModel medicalDetailsModel;

    // Upload Marks tab
    private JTextField txtMarkCourse;
    private JComboBox<String> cmbAssessment;
    private JTextField txtMarkStudent;
    private JTextField txtScore;
    private List<Assessment> loadedAssessments = new ArrayList<>();
    private JComboBox<String> cmbType;
    private JTextField txtAssessNo;
    private JTextField txtWeight;
    private JTextField txtSetupCaWeight;
    private JLabel lblWeightTotal;

    // Final Results tab
    private JTextField txtResCourse;
    private JTextField txtResStudent;
    private JTextField txtFinalScore;
    private JTextField txtCaWeight;
    private JCheckBox chkRepeat;
    private JLabel lblResultSummary;

    // Notices tab
    private final NoticeBoardDAO noticeDAO = new NoticeBoardDAOImpl();
    private DefaultTableModel noticeTableModel;
    private JTable noticeTable;
    private JTextArea txtNoticeView;
    private JTextField txtNoticeTitle;
    private JComboBox<String> cmbNoticeTarget;
    private JTextArea txtNoticeContent;
    private List<NoticeItem> loadedNotices = new ArrayList<>();

    // Course Materials tab
    private final CourseMaterialDAO materialDAO = new CourseMaterialDAOImpl();
    private JComboBox<Course> cmbMatCourse;
    private JTextField txtMatTitle;
    private JTextField txtMatFile;
    private JSpinner matAvailableFrom;
    private JSpinner matAvailableTo;
    private File selectedFile;
    private JTable materialTable;
    private DefaultTableModel materialTableModel;
    private List<LectureMaterial> loadedMaterials = new ArrayList<>();

    // Grades & GPA tab
    private JTextField txtGradeStudent;
    private DefaultTableModel gradeTableModel;
    private JLabel lblGpa;

    public LecturerDashboard(Lecturer lecturer) {
        this.lecturer = lecturer;
        setTitle("FOT-MIS | Lecturer Dashboard");
        setSize(1360, 900);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmLogout();
            }
        });
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(createHeaderBar(), BorderLayout.NORTH);
        root.add(createSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(BACKGROUND);
        contentPanel.add(createHomePanel(), "DASHBOARD");
        contentPanel.add(wrapContent(createProfilePanel()), "PROFILE");
        contentPanel.add(wrapContent(createUploadMarksPanel()), "MARKS");
        contentPanel.add(wrapContent(createFinalResultPanel()), "RESULTS");
        contentPanel.add(wrapContent(createGradesPanel()), "GRADES");
        contentPanel.add(wrapContent(createMaterialsPanel()), "MATERIALS");
        contentPanel.add(wrapContent(createUndergraduatesPanel()), "UNDERGRADUATES");
        contentPanel.add(wrapContent(createEligibilityPanel()), "ELIGIBILITY");
        contentPanel.add(wrapContent(createAttendanceMedicalPanel()), "ATTENDANCE_MEDICAL");

        noticesPanel = createNoticesPanel();
        contentPanel.add(wrapContent(noticesPanel), "NOTICES");
        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
        switchCard("DASHBOARD");
    }

    // Header and navigation match the Admin Dashboard's layout and colours.
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(12, 28, 12, 28));

        JPanel branding = new JPanel();
        branding.setOpaque(false);
        branding.setLayout(new BoxLayout(branding, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("FACULTY OF TECHNOLOGY MIS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("University of Ruhuna, Sri Lanka | Lecturer Workspace");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(222, 233, 248));
        branding.add(title);
        branding.add(Box.createVerticalStrut(4));
        branding.add(subtitle);
        header.add(branding, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 5));
        actions.setOpaque(false);
        JLabel user = new JLabel(lecturer.getFirstName() + " " + lecturer.getLastName());
        user.setFont(UI_BOLD);
        user.setForeground(Color.WHITE);
        // Circular profile picture displayed immediately before the lecturer's name.
        headerPhotoLabel = new JLabel();
        headerPhotoLabel.setPreferredSize(new Dimension(44, 44));
        refreshHeaderPhoto();

        JButton logout = new JButton("Sign Out");
        logout.setFont(UI_BOLD);
        logout.setBackground(new Color(239, 68, 68));
        logout.setForeground(Color.WHITE);
        logout.setOpaque(true);
        logout.setFocusPainted(false);
        logout.setBorder(new EmptyBorder(11, 22, 11, 22));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> confirmLogout());
        actions.add(headerPhotoLabel);
        actions.add(user);
        actions.add(logout);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    /** Refresh the header avatar after the lecturer saves or removes their profile photo. */
    public void refreshHeaderPhoto() {
        if (headerPhotoLabel == null) return;

        final int size = 44;
        BufferedImage avatar = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = avatar.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(ACTIVE);
        g.fillOval(1, 1, size - 2, size - 2);

        boolean hasPhoto = false;
        String photoPath = lecturer.getProfilePic();
        if (photoPath != null && !photoPath.isBlank()) {
            try {
                BufferedImage photo = ImageIO.read(new File(photoPath));
                if (photo != null && photo.getWidth() > 0 && photo.getHeight() > 0) {
                    final int innerSize = size - 4;
                    double scale = Math.max((double) innerSize / photo.getWidth(),
                            (double) innerSize / photo.getHeight());
                    int width = (int) Math.ceil(photo.getWidth() * scale);
                    int height = (int) Math.ceil(photo.getHeight() * scale);
                    g.setClip(new Ellipse2D.Double(2, 2, innerSize, innerSize));
                    g.drawImage(photo, (size - width) / 2, (size - height) / 2,
                            width, height, null);
                    g.setClip(null);
                    hasPhoto = true;
                }
            } catch (IOException | RuntimeException ignored) {
                // Display the lecturer's first initial when the saved photo cannot be loaded.
            }
        }

        if (!hasPhoto) {
            String firstName = lecturer.getFirstName();
            String initial = firstName == null || firstName.isBlank()
                    ? "L" : firstName.substring(0, 1).toUpperCase();
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(initial, (size - fm.stringWidth(initial)) / 2,
                    (size - fm.getHeight()) / 2 + fm.getAscent());
        }

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(1, 1, size - 3, size - 3);
        g.dispose();
        headerPhotoLabel.setIcon(new ImageIcon(avatar));
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(265, 0));
        sidebar.setBackground(SIDEBAR);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(SIDEBAR);
        menu.setBorder(new EmptyBorder(25, 8, 12, 8));

        JLabel label = new JLabel("NAVIGATION MENU");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(148, 163, 184));
        label.setBorder(new EmptyBorder(3, 8, 20, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(label);

        addNavButton(menu, "Dashboard Overview", "DASHBOARD");
        addNavButton(menu, "My Profile", "PROFILE");
        addNavButton(menu, "Upload Marks", "MARKS");
        addNavButton(menu, "Final Results", "RESULTS");
        addNavButton(menu, "Grades & GPA", "GRADES");
        addNavButton(menu, "Undergraduates", "UNDERGRADUATES");
        addNavButton(menu, "Eligibility", "ELIGIBILITY");
        addNavButton(menu, "Attendance & Medical", "ATTENDANCE_MEDICAL");
        addNavButton(menu, "Course Materials", "MATERIALS");
        addNavButton(menu, "Notices", "NOTICES");
        sidebar.add(menu, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 8, 12, 8));
        JButton logout = new JButton("Sign Out");
        logout.setFont(UI_BOLD);
        logout.setBackground(new Color(30, 41, 59));
        logout.setForeground(new Color(248, 113, 113));
        logout.setOpaque(true);
        logout.setFocusPainted(false);
        logout.setBorder(new EmptyBorder(14, 14, 14, 14));
        logout.addActionListener(e -> confirmLogout());
        bottom.add(logout, BorderLayout.CENTER);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavButton(JPanel menu, String label, String card) {
        JButton button = new JButton(label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 53));
        button.setPreferredSize(new Dimension(240, 53));
        button.setFont(UI_BOLD);
        button.setForeground(new Color(226, 232, 240));
        button.setBackground(SIDEBAR);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(14, 16, 14, 8));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> switchCard(card));
        navigation.put(card, button);
        menu.add(button);
        menu.add(Box.createVerticalStrut(4));
    }

    private void switchCard(String card) {
        cardLayout.show(contentPanel, card);
        for (Map.Entry<String, JButton> entry : navigation.entrySet()) {
            boolean active = entry.getKey().equals(card);
            JButton button = entry.getValue();
            button.setBackground(active ? ACTIVE : SIDEBAR);
            button.setForeground(Color.WHITE);
        }
        // Keep the original on-demand notice-refresh behaviour.
        if ("NOTICES".equals(card) && noticesPanel != null) {
            loadNotices();
        }
        if (("UNDERGRADUATES".equals(card) || "ELIGIBILITY".equals(card)
                || "ATTENDANCE_MEDICAL".equals(card)) && !studentListsLoaded) {
            refreshStudentLists();
        }
    }

    private JPanel createHomePanel() {
        JPanel home = new JPanel(new BorderLayout(0, 22));
        home.setBackground(BACKGROUND);
        home.setBorder(new EmptyBorder(34, 36, 32, 36));

        JPanel intro = new JPanel();
        intro.setBackground(BACKGROUND);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        JLabel welcome = new JLabel("Welcome back, " + lecturer.getFirstName());
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcome.setForeground(TEXT);
        JLabel description = new JLabel("Faculty of Technology Management Information System | Lecturer Control Center");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(MUTED);
        intro.add(welcome);
        intro.add(Box.createVerticalStrut(8));
        intro.add(description);
        home.add(intro, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(3, 3, 18, 18));
        cards.setOpaque(false);
        cards.add(featureCard("My Profile", "Lecturer details", "PROFILE", new Color(37, 99, 235)));
        cards.add(featureCard("Upload Marks", "Assessments and student marks", "MARKS", new Color(16, 185, 129)));
        cards.add(featureCard("Final Results", "Calculate and save results", "RESULTS", new Color(245, 158, 11)));
        cards.add(featureCard("Grades & GPA", "View academic performance", "GRADES", new Color(124, 58, 237)));
        cards.add(featureCard("Course Materials", "Manage lecture materials", "MATERIALS", new Color(14, 165, 233)));
        cards.add(featureCard("Notices", "View and post notices", "NOTICES", new Color(239, 68, 68)));
        cards.add(featureCard("Undergraduates", "Search and view student profiles", "UNDERGRADUATES", new Color(37, 99, 235)));
        cards.add(featureCard("Eligibility", "Batch or individual exam eligibility", "ELIGIBILITY", new Color(124, 58, 237)));
        cards.add(featureCard("Attendance & Medical", "Read-only attendance and medicals", "ATTENDANCE_MEDICAL", new Color(14, 165, 233)));
        JPanel cardsHolder = new JPanel(new BorderLayout());
        cardsHolder.setOpaque(false);
        cardsHolder.add(cards, BorderLayout.NORTH);
        home.add(cardsHolder, BorderLayout.CENTER);
        return home;
    }

    private JPanel featureCard(String title, String subtitle, String card, Color accent) {
        JPanel item = new JPanel(new BorderLayout(0, 12));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 1, 1, accent),
                new EmptyBorder(22, 20, 22, 20)));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(TEXT);
        JLabel details = new JLabel(subtitle);
        details.setFont(UI_FONT);
        details.setForeground(MUTED);
        text.add(heading);
        text.add(Box.createVerticalStrut(8));
        text.add(details);
        item.add(text, BorderLayout.CENTER);
        JButton open = new JButton("Open  →");
        open.setFont(UI_BOLD);
        open.setForeground(ACTIVE);
        open.setContentAreaFilled(false);
        open.setBorderPainted(false);
        open.setHorizontalAlignment(SwingConstants.LEFT);
        open.setFocusPainted(false);
        open.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        open.addActionListener(e -> switchCard(card));
        item.add(open, BorderLayout.SOUTH);
        return item;
    }

    /** Add consistent styling without changing any of the existing DAO or button actions. */
    private JPanel wrapContent(JPanel original) {
        // LecturerProfilePanel styles its own blue information card; leave those colours intact.
        if (!(original instanceof LecturerProfilePanel)) {
            applyFieldStyling(original);
        }
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(BORDER));
        card.add(original, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.setBorder(new EmptyBorder(24, 26, 24, 26));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void applyFieldStyling(Component comp) {
        if (comp instanceof JPanel) {
            ((JPanel) comp).setBackground(Color.WHITE);
        }
        if (comp instanceof JLabel) {
            JLabel label = (JLabel) comp;
            Font old = label.getFont();
            int size = old == null ? 13 : old.getSize();
            int weight = old == null ? Font.PLAIN : old.getStyle();
            label.setFont(new Font("Segoe UI", weight, size));
            label.setForeground(TEXT);
        } else if (comp instanceof JButton) {
            JButton button = (JButton) comp;
            button.setFont(UI_BOLD);
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.setForeground(Color.WHITE);
            button.setBackground(button.getText().toLowerCase().contains("delete")
                    ? new Color(239, 68, 68) : ACTIVE);
            button.setBorder(new EmptyBorder(10, 16, 10, 16));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (comp instanceof JTextField) {
            JTextField field = (JTextField) comp;
            field.setFont(UI_FONT);
            field.setBackground(field.isEditable() ? Color.WHITE : BACKGROUND);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), new EmptyBorder(6, 8, 6, 8)));
        } else if (comp instanceof JTextArea) {
            JTextArea area = (JTextArea) comp;
            area.setFont(UI_FONT);
            area.setBackground(area.isEditable() ? Color.WHITE : BACKGROUND);
        } else if (comp instanceof JCheckBox) {
            JCheckBox checkbox = (JCheckBox) comp;
            checkbox.setFont(UI_FONT);
            checkbox.setBackground(Color.WHITE);
        } else if (comp instanceof JComboBox) {
            JComboBox<?> combo = (JComboBox<?>) comp;
            combo.setFont(UI_FONT);
            combo.setBackground(Color.WHITE);
        } else if (comp instanceof JTable) {
            JTable table = (JTable) comp;
            table.setFont(UI_FONT);
            table.setRowHeight(30);
            table.setShowVerticalLines(false);
            table.setGridColor(BORDER);
            table.setSelectionBackground(new Color(219, 234, 254));
            table.setSelectionForeground(TEXT);
            table.getTableHeader().setFont(UI_BOLD);
            table.getTableHeader().setBackground(new Color(238, 243, 249));
            table.getTableHeader().setForeground(TEXT);
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyFieldStyling(child);
            }
        }
    }

    private void confirmLogout() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            // Original behaviour preserved: closing the lecturer window.
            // Actual session invalidation / returning to LoginFrame should be wired separately.
            dispose();
        }
    }

    // ---------------------------------------------------------------- Undergraduates / Eligibility / Attendance & Medical

    private DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JPanel createUndergraduatesPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        searchBar.add(new JLabel("Search by ID, name, registration, batch or department:"));
        undergraduateSearch = new JTextField(24);
        searchBar.add(undergraduateSearch);
        JButton refresh = new JButton("Refresh Students");
        refresh.addActionListener(e -> refreshStudentLists());
        searchBar.add(refresh);
        panel.add(searchBar, BorderLayout.NORTH);

        undergraduateModel = readOnlyModel("Student ID", "Name", "Registration No.", "Batch", "Department");
        undergraduateTable = new JTable(undergraduateModel);
        undergraduateTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(undergraduateModel);
        undergraduateTable.setRowSorter(sorter);
        undergraduateSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String value = undergraduateSearch.getText().trim();
                sorter.setRowFilter(value.isEmpty() ? null :
                        RowFilter.regexFilter("(?i)" + Pattern.quote(value)));
            }
            @Override public void insertUpdate(DocumentEvent e) { filter(); }
            @Override public void removeUpdate(DocumentEvent e) { filter(); }
            @Override public void changedUpdate(DocumentEvent e) { filter(); }
        });
        undergraduateTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelectedUndergraduate();
        });
        undergraduateTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    StudentInfo selected = selectedUndergraduate();
                    if (selected != null) {
                        selectAttendanceStudent(selected.id());
                        switchCard("ATTENDANCE_MEDICAL");
                        loadAttendanceMedical();
                    }
                }
            }
        });

        undergraduateDetails = new JTextArea(7, 40);
        undergraduateDetails.setEditable(false);
        undergraduateDetails.setLineWrap(true);
        undergraduateDetails.setWrapStyleWord(true);
        undergraduateDetails.setText("Select a student to view read-only profile details.\nDouble-click to view attendance and medicals.");
        JScrollPane detailScroll = new JScrollPane(undergraduateDetails);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Selected Student (read-only)"));
        detailScroll.setPreferredSize(new Dimension(0, 190));
        panel.add(new JScrollPane(undergraduateTable), BorderLayout.CENTER);
        panel.add(detailScroll, BorderLayout.SOUTH);
        return panel;
    }

    private StudentInfo selectedUndergraduate() {
        if (undergraduateTable == null || undergraduateTable.getSelectedRow() < 0) return null;
        int index = undergraduateTable.convertRowIndexToModel(undergraduateTable.getSelectedRow());
        String id = (String) undergraduateModel.getValueAt(index, 0);
        for (StudentInfo student : allStudents) {
            if (student.id().equals(id)) return student;
        }
        return null;
    }

    private void showSelectedUndergraduate() {
        StudentInfo student = selectedUndergraduate();
        if (student == null) {
            undergraduateDetails.setText("Select a student to view their details.");
            return;
        }
        undergraduateDetails.setText("Student ID: " + student.id()
                + "\nFull Name: " + student.name()
                + "\nRegistration Number: " + valueOrDash(student.regNo())
                + "\nBatch: " + valueOrDash(student.batch())
                + "\nDepartment: " + valueOrDash(student.department())
                + "\nEmail: " + valueOrDash(student.email())
                + "\nContact No: " + valueOrDash(student.contact())
                + "\nRepeat Student: " + (student.repeat() ? "Yes" : "No")
                + "\nProfile Picture Path: " + valueOrDash(student.photo()));
        undergraduateDetails.setCaretPosition(0);
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private JPanel createEligibilityPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        eligibilityBatch = new JComboBox<>();
        eligibilityBatch.addItem("All Batches");
        eligibilityBatch.setPreferredSize(new Dimension(105, 32));
        eligibilityCourse = new JComboBox<>();
        eligibilityCourse.setPreferredSize(new Dimension(205, 32));
        eligibilityStudentId = new JTextField(11);
        eligibilityStudentId.setToolTipText("Optional: enter an individual student ID; blank means selected batch");
        eligibilityCaWeight = new JSpinner(new SpinnerNumberModel(30.0, 1.0, 100.0, 1.0));
        eligibilityCaWeight.setPreferredSize(new Dimension(72, 32));
        JButton check = new JButton("Check Eligibility");
        toolbar.add(new JLabel("Batch:")); toolbar.add(eligibilityBatch);
        toolbar.add(new JLabel("Course:")); toolbar.add(eligibilityCourse);
        toolbar.add(new JLabel("Student ID (optional):")); toolbar.add(eligibilityStudentId);
        toolbar.add(new JLabel("CA Weight %:")); toolbar.add(eligibilityCaWeight);
        toolbar.add(check);

        eligibilityModel = readOnlyModel("Student ID", "Name", "Batch", "Course", "CA %",
                "Attendance %", "With Medical %", "CA Status", "Attendance Status", "Final Eligibility");
        eligibilityTable = new JTable(eligibilityModel);
        eligibilityTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        eligibilityTable.setRowHeight(28);
        for (int i = 0; i < eligibilityTable.getColumnCount(); i++) {
            eligibilityTable.getColumnModel().getColumn(i).setPreferredWidth(i == 1 ? 170 : 125);
        }
        check.addActionListener(e -> checkEligibility(check));

        eligibilitySummaryLabel = new JLabel("Choose a course and batch, then press Check Eligibility.");
        eligibilitySummaryLabel.setBorder(new EmptyBorder(10, 2, 4, 2));
        JTextArea note = new JTextArea("Rule: CA >= 40% AND attendance >= 80%. Approved medicals are applied to ABSENT sessions only.\n"
                + "The result uses CURRENT recorded data. Until all assessments and sessions are complete, treat it as provisional.\n"
                + "CA Weight must match the sum of the configured counted assessment weights. Missing data shows PENDING.");
        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setBackground(Color.WHITE);
        note.setFont(UI_FONT);
        JPanel bottom = new JPanel(new BorderLayout(0, 7));
        bottom.setBackground(Color.WHITE);
        bottom.add(eligibilitySummaryLabel, BorderLayout.NORTH);
        bottom.add(note, BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(eligibilityTable), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void checkEligibility(JButton button) {
        CourseInfo course = (CourseInfo) eligibilityCourse.getSelectedItem();
        if (course == null) {
            JOptionPane.showMessageDialog(this, "No course found. Refresh Students first.");
            return;
        }
        String studentId = eligibilityStudentId.getText().trim();
        String batch = (String) eligibilityBatch.getSelectedItem();
        double caWeight = ((Number) eligibilityCaWeight.getValue()).doubleValue();
        List<StudentInfo> students = new ArrayList<>();
        for (StudentInfo student : allStudents) {
            if (!studentId.isEmpty()) {
                if (student.id().equalsIgnoreCase(studentId)) students.add(student);
            } else if ("All Batches".equals(batch) || student.batch().equals(batch)) {
                students.add(student);
            }
        }
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching students. Check Student ID or Batch.");
            return;
        }
        button.setEnabled(false);
        eligibilitySummaryLabel.setText("Loading eligibility records...");
        // Batch queries involve several DAO reads. Keep the Swing UI responsive.
        SwingWorker<List<LecturerReadOnlyDAO.EligibilityRow>, Void> worker = new SwingWorker<>() {
            @Override protected List<LecturerReadOnlyDAO.EligibilityRow> doInBackground() throws Exception {
                return readOnlyDAO.getEligibility(students, course, caWeight);
            }
            @Override protected void done() {
                button.setEnabled(true);
                try {
                    List<LecturerReadOnlyDAO.EligibilityRow> rows = get();
                    eligibilityModel.setRowCount(0);
                    int eligible = 0, notEligible = 0, pending = 0;
                    for (LecturerReadOnlyDAO.EligibilityRow row : rows) {
                        eligibilityModel.addRow(new Object[]{
                                row.student().id(), row.student().name(), row.student().batch(),
                                row.courseId(), displayPct(row.caPct()), displayPct(row.attendancePct()),
                                displayPct(row.adjustedPct()), row.caStatus(), row.attendanceStatus(),
                                row.finalStatus()});
                        switch (row.finalStatus()) {
                            case "ELIGIBLE" -> eligible++;
                            case "NOT ELIGIBLE" -> notEligible++;
                            default -> pending++;
                        }
                    }
                    eligibilitySummaryLabel.setText("Students: " + rows.size() + "    Eligible: "
                            + eligible + "    Not Eligible: " + notEligible + "    Pending Data: " + pending
                            + "    (Current data; provisional)");
                } catch (Exception ex) {
                    Throwable reason = ex.getCause() != null ? ex.getCause() : ex;
                    showError(new Exception("Could not load eligibility: " + reason.getMessage(), reason));
                    eligibilitySummaryLabel.setText("Eligibility query failed. No result displayed.");
                    eligibilityModel.setRowCount(0);
                }
            }
        };
        worker.execute();
    }

    private String displayPct(Double pct) {
        return pct == null ? "N/A" : String.format("%.1f%%", pct);
    }

    private JPanel createAttendanceMedicalPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        attendanceStudent = new JComboBox<>();
        attendanceStudent.setPreferredSize(new Dimension(270, 32));
        attendanceCourse = new JComboBox<>();
        attendanceCourse.setPreferredSize(new Dimension(230, 32));
        JButton load = new JButton("View Records");
        load.addActionListener(e -> loadAttendanceMedical());
        toolbar.add(new JLabel("Student:")); toolbar.add(attendanceStudent);
        toolbar.add(new JLabel("Course:")); toolbar.add(attendanceCourse);
        toolbar.add(load);

        attendanceSummaryText = new JTextArea(3, 55);
        attendanceSummaryText.setEditable(false);
        attendanceSummaryText.setText("Select a student and course, then click View Records.");
        attendanceSummaryText.setLineWrap(true);
        attendanceSummaryText.setWrapStyleWord(true);
        attendanceSummaryText.setBorder(BorderFactory.createTitledBorder("Attendance Percentage (read-only)"));

        attendanceDetailsModel = readOnlyModel("Type", "Session", "Date", "Status", "Approved Medical?");
        JTable attendanceRecords = new JTable(attendanceDetailsModel);
        medicalDetailsModel = readOnlyModel("ID", "Type", "Session", "Medical Date", "Submitted", "Description", "Status");
        JTable medicalRecords = new JTable(medicalDetailsModel);
        medicalRecords.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < medicalRecords.getColumnCount(); i++) {
            medicalRecords.getColumnModel().getColumn(i).setPreferredWidth(i == 5 ? 240 : 120);
        }
        JScrollPane attendanceScroll = new JScrollPane(attendanceRecords);
        attendanceScroll.setBorder(BorderFactory.createTitledBorder("Attendance Sessions (read-only)"));
        JScrollPane medicalScroll = new JScrollPane(medicalRecords);
        medicalScroll.setBorder(BorderFactory.createTitledBorder("Medical Submissions (read-only)"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, attendanceScroll, medicalScroll);
        split.setResizeWeight(0.5);
        split.setDividerLocation(240);
        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setBackground(Color.WHITE);
        north.add(toolbar, BorderLayout.NORTH);
        north.add(attendanceSummaryText, BorderLayout.CENTER);
        panel.add(north, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void selectAttendanceStudent(String studentId) {
        if (attendanceStudent == null) return;
        for (int i = 0; i < attendanceStudent.getItemCount(); i++) {
            StudentInfo item = attendanceStudent.getItemAt(i);
            if (item.id().equals(studentId)) {
                attendanceStudent.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadAttendanceMedical() {
        StudentInfo student = (StudentInfo) attendanceStudent.getSelectedItem();
        CourseInfo course = (CourseInfo) attendanceCourse.getSelectedItem();
        if (student == null || course == null) {
            JOptionPane.showMessageDialog(this, "Select a student and course first.");
            return;
        }
        try {
            LecturerReadOnlyDAO.AttendanceSummary a = readOnlyDAO.getAttendance(student.id(), course.id());
            List<LecturerReadOnlyDAO.MedicalRow> medicals = readOnlyDAO.getMedicals(student.id(), course.id());
            String prefix = student.name() + "  |  " + course.id() + "\n";
            if (a.hasData()) {
                attendanceSummaryText.setText(prefix
                        + "Theory: " + a.theoryPresent() + "/" + a.theoryTotal() + " (" + displayPct(a.theoryTotal() == 0 ? null : a.theoryPct()) + ")"
                        + "  |  Practical: " + a.practicalPresent() + "/" + a.practicalTotal() + " (" + displayPct(a.practicalTotal() == 0 ? null : a.practicalPct()) + ")\n"
                        + "Combined: " + displayPct(a.combined())
                        + "  |  With APPROVED Medical: " + displayPct(a.combinedAdjusted())
                        + "  |  Recorded sessions only (provisional).");
            } else {
                attendanceSummaryText.setText(prefix + "No attendance sessions recorded for this course.");
            }
            attendanceDetailsModel.setRowCount(0);
            for (LecturerReadOnlyDAO.AttendanceRow r : a.rows()) {
                attendanceDetailsModel.addRow(new Object[]{r.type(), r.session(), r.date(),
                        r.status(), r.approvedMedical() && "ABSENT".equalsIgnoreCase(r.status()) ? "YES" : "NO"});
            }
            medicalDetailsModel.setRowCount(0);
            for (LecturerReadOnlyDAO.MedicalRow m : medicals) {
                medicalDetailsModel.addRow(new Object[]{m.id(), m.type(), m.session(),
                        m.medicalDate(), m.submitted(), m.description(), m.status()});
            }
            attendanceSummaryText.setCaretPosition(0);
        } catch (SQLException ex) {
            attendanceDetailsModel.setRowCount(0);
            medicalDetailsModel.setRowCount(0);
            attendanceSummaryText.setText("Could not load attendance and medical records.");
            showError(ex);
        }
    }

    private void refreshStudentLists() {
        try {
            List<StudentInfo> students = readOnlyDAO.getStudents();
            List<CourseInfo> courses = readOnlyDAO.getCourses();
            allStudents = students;
            undergraduateModel.setRowCount(0);
            for (StudentInfo student : students) {
                undergraduateModel.addRow(new Object[]{student.id(), student.name(),
                        student.regNo(), student.batch(), student.department()});
            }
            undergraduateDetails.setText("Select a student to view read-only details. Double-click to view attendance.");
            Object priorBatch = eligibilityBatch.getSelectedItem();
            eligibilityBatch.removeAllItems();
            eligibilityBatch.addItem("All Batches");
            java.util.Set<String> batches = new java.util.TreeSet<>();
            for (StudentInfo student : students) if (student.batch() != null) batches.add(student.batch());
            for (String batch : batches) eligibilityBatch.addItem(batch);
            if (priorBatch != null) eligibilityBatch.setSelectedItem(priorBatch);
            CourseInfo priorCourse = (CourseInfo) eligibilityCourse.getSelectedItem();
            CourseInfo priorAttendanceCourse = (CourseInfo) attendanceCourse.getSelectedItem();
            String priorStudentId = attendanceStudent.getSelectedItem() instanceof StudentInfo si ? si.id() : null;
            eligibilityCourse.removeAllItems();
            attendanceCourse.removeAllItems();
            for (CourseInfo course : courses) {
                eligibilityCourse.addItem(course);
                attendanceCourse.addItem(course);
            }
            if (priorCourse != null) eligibilityCourse.setSelectedItem(priorCourse);
            if (priorAttendanceCourse != null) attendanceCourse.setSelectedItem(priorAttendanceCourse);
            attendanceStudent.removeAllItems();
            for (StudentInfo student : students) attendanceStudent.addItem(student);
            if (priorStudentId != null) selectAttendanceStudent(priorStudentId);
            studentListsLoaded = true;
        } catch (SQLException ex) {
            showError(ex);
            studentListsLoaded = false;
        }
    }

    // ---------------------------------------------------------------- profile

    private JPanel createProfilePanel() {
        return new LecturerProfilePanel(lecturer);
    }

    // ---------------------------------------------------------------- upload marks

    private JPanel createUploadMarksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Assessments and Marks (out of 100)");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel lblHint = new JLabel("Weightage = % of the whole course. Quizzes share one weightage "
                + "(e.g. 10) and the best " + CACalculator.BEST_QUIZZES_COUNTED + " quiz marks are counted.");
        JPanel north = new JPanel(new GridLayout(2, 1, 5, 5));
        north.add(lblTitle);
        north.add(lblHint);
        panel.add(north, BorderLayout.NORTH);

        // Part 1: create / load the assessments (Quiz, Mid Exam ...) of a course
        JPanel setup = new JPanel(new GridLayout(6, 2, 10, 10));
        setup.setBorder(BorderFactory.createTitledBorder("1. Assessments of the course"));

        txtMarkCourse = new JTextField();
        txtSetupCaWeight = new JTextField("30");
        cmbType = new JComboBox<>(new String[]{"Quiz", "Mid Exam", "Assignment", "Practical", "Presentation"});
        txtAssessNo = new JTextField();
        txtWeight = new JTextField();
        JButton btnLoad = new JButton("Load Assessments");
        JButton btnAdd = new JButton("Add Assessment");

        setup.add(new JLabel("Course ID (e.g. ICT2132):"));
        setup.add(txtMarkCourse);
        setup.add(new JLabel("CA weight of the course (%):"));
        setup.add(txtSetupCaWeight);
        setup.add(new JLabel("Assessment type:"));
        setup.add(cmbType);
        setup.add(new JLabel("Number (optional, e.g. 1):"));
        setup.add(txtAssessNo);
        setup.add(new JLabel("Weightage (% of the course):"));
        setup.add(txtWeight);
        setup.add(btnLoad);
        setup.add(btnAdd);

        // Part 2: enter marks for one assessment
        JPanel marks = new JPanel(new GridLayout(5, 2, 10, 10));
        marks.setBorder(BorderFactory.createTitledBorder("2. Enter marks"));

        cmbAssessment = new JComboBox<>();
        lblWeightTotal = new JLabel("-");
        txtMarkStudent = new JTextField();
        txtScore = new JTextField();
        JButton btnSave = new JButton("Save Mark");

        marks.add(new JLabel("Assessment:"));
        marks.add(cmbAssessment);
        marks.add(new JLabel("CA weightage added so far:"));
        marks.add(lblWeightTotal);
        marks.add(new JLabel("Student ID (e.g. tg1001):"));
        marks.add(txtMarkStudent);
        marks.add(new JLabel("Score (0 - 100):"));
        marks.add(txtScore);
        marks.add(new JLabel(""));
        marks.add(btnSave);

        btnLoad.addActionListener(e -> loadAssessments());
        btnAdd.addActionListener(e -> addAssessment());
        btnSave.addActionListener(e -> saveMark());

        JPanel wrapper = new JPanel(new GridLayout(2, 1, 10, 10));
        wrapper.add(setup);
        wrapper.add(marks);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private double getSetupCaWeight() {
        try {
            double w = Double.parseDouble(txtSetupCaWeight.getText().trim());
            return (w > 0 && w <= 100) ? w : 30;
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    private void loadAssessments() {
        String courseId = txtMarkCourse.getText().trim();
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a course ID first.");
            return;
        }
        try {
            refreshAssessments(courseId);
            if (loadedAssessments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No assessments yet for " + courseId + ". Add one using the form above.");
            }
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    /** Reloads the assessments of the course into the list, the combo box and the weightage total. */
    private void refreshAssessments(String courseId) throws DatabaseException {
        loadedAssessments = assessmentDAO.getAssessmentsByCourse(courseId);
        cmbAssessment.removeAllItems();
        for (Assessment a : loadedAssessments) {
            cmbAssessment.addItem(a.getName() + " (" + a.getWeightage() + "%)");
        }
        lblWeightTotal.setText(format(CACalculator.countedWeight(loadedAssessments))
                + " / " + format(getSetupCaWeight()));
    }

    private void addAssessment() {
        String courseId = txtMarkCourse.getText().trim();
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a course ID first.");
            return;
        }
        try {
            double weight = Double.parseDouble(txtWeight.getText().trim());
            double caWeight = getSetupCaWeight();
            if (weight <= 0 || weight > caWeight) {
                JOptionPane.showMessageDialog(this, "Weightage must be more than 0 and not more than the CA weight ("
                        + format(caWeight) + ").");
                return;
            }

            String name = (String) cmbType.getSelectedItem();
            String number = txtAssessNo.getText().trim();
            if (!number.isEmpty()) {
                name = name + " " + number;
            }
            Assessment candidate = new Assessment(0, courseId, name, weight);

            refreshAssessments(courseId);   // make sure we check against the latest data
            for (Assessment a : loadedAssessments) {
                if (a.getName().equalsIgnoreCase(name)) {
                    JOptionPane.showMessageDialog(this, name + " already exists for " + courseId);
                    return;
                }
                if (CACalculator.isQuiz(candidate) && CACalculator.isQuiz(a)
                        && Math.abs(a.getWeightage() - weight) > 0.001) {
                    JOptionPane.showMessageDialog(this, "All quizzes share one weightage ("
                            + a.getWeightage() + "). Use the same value.");
                    return;
                }
            }

            List<Assessment> after = new ArrayList<>(loadedAssessments);
            after.add(candidate);
            double newTotal = CACalculator.countedWeight(after);
            if (newTotal > caWeight + 0.001) {
                JOptionPane.showMessageDialog(this, "The CA weightage would become " + format(newTotal)
                        + ", which is more than the CA weight (" + format(caWeight) + ").");
                return;
            }

            assessmentDAO.addAssessment(candidate);
            refreshAssessments(courseId);
            txtAssessNo.setText("");
            txtWeight.setText("");
            JOptionPane.showMessageDialog(this, name + " added to " + courseId);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Weightage must be a number.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void saveMark() {
        int index = cmbAssessment.getSelectedIndex();
        if (index < 0 || index >= loadedAssessments.size()) {
            JOptionPane.showMessageDialog(this, "Load and select an assessment first.");
            return;
        }
        String studentId = txtMarkStudent.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a student ID.");
            return;
        }
        try {
            double score = Double.parseDouble(txtScore.getText().trim());
            Assessment assessment = loadedAssessments.get(index);
            Mark mark = new Mark(0, assessment.getAssessmentId(), studentId, score); // validates 0-100
            markDAO.addMark(mark);
            JOptionPane.showMessageDialog(this, "Mark saved for " + studentId);
            txtScore.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Score must be a number.", "Invalid input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (InvalidMarkException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid mark", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    // ---------------------------------------------------------------- final results

    private JPanel createFinalResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Calculate Final Result and Grade");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        txtResCourse = new JTextField();
        txtResStudent = new JTextField();
        txtFinalScore = new JTextField();
        txtCaWeight = new JTextField("30");
        chkRepeat = new JCheckBox("Repeat attempt (highest grade is C)");
        JButton btnCalc = new JButton("Calculate and Save");

        form.add(new JLabel("Course ID:"));
        form.add(txtResCourse);
        form.add(new JLabel("Student ID:"));
        form.add(txtResStudent);
        form.add(new JLabel("Final exam score (0 - 100):"));
        form.add(txtFinalScore);
        form.add(new JLabel("CA weight of the course (%):"));
        form.add(txtCaWeight);
        form.add(new JLabel(""));
        form.add(chkRepeat);
        form.add(new JLabel(""));
        form.add(btnCalc);

        btnCalc.addActionListener(e -> calculateAndSaveResult());

        lblResultSummary = new JLabel(" ");
        lblResultSummary.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.add(form, BorderLayout.NORTH);
        wrapper.add(lblResultSummary, BorderLayout.CENTER);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void calculateAndSaveResult() {
        String courseId = txtResCourse.getText().trim();
        String studentId = txtResStudent.getText().trim();
        if (courseId.isEmpty() || studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the course ID and the student ID.");
            return;
        }
        try {
            double finalScore = Double.parseDouble(txtFinalScore.getText().trim());
            double caWeight = Double.parseDouble(txtCaWeight.getText().trim());
            if (caWeight <= 0 || caWeight > 100) {
                JOptionPane.showMessageDialog(this, "CA weight must be more than 0 and not more than 100.");
                return;
            }

            List<Assessment> assessments = assessmentDAO.getAssessmentsByCourse(courseId);
            List<Mark> marks = markDAO.getMarksByStudent(studentId);

            double counted = CACalculator.countedWeight(assessments);
            if (Math.abs(counted - caWeight) > 0.001) {
                lblResultSummary.setText("Assessments of " + courseId + " add up to " + format(counted)
                        + " but the CA weight is " + format(caWeight) + ". Fix the assessments first. Nothing saved.");
                return;
            }

            // credits are read from the course table when results are loaded, so 0 is fine for saving
            Result result = new Result(studentId, courseId, 0, chkRepeat.isSelected());
            result.calculateCaTotal(assessments, marks, caWeight);   // best 2 quizzes are counted

            if (!EligibilityChecker.isCaEligible(result.getCaTotal())) {
                lblResultSummary.setText("CA = " + format(result.getCaTotal())
                        + " / 100 (below 40). Not eligible for the final exam. Nothing saved.");
                return;
            }

            result.setFinalExamScore(finalScore);
            result.calculateTotal(caWeight);   // also works out grade and grade point
            resultDAO.saveResult(result);

            lblResultSummary.setText("CA = " + format(result.getCaTotal()) + " / 100 ("
                    + format(result.getCaTotal() * caWeight / 100.0) + " / " + format(caWeight) + ")"
                    + "   Total = " + format(result.getTotalScore())
                    + "   Grade = " + result.getGrade().getLabel()
                    + "   GPV = " + result.getGradePoint() + "   (saved)");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Final score and CA weight must be numbers.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidMarkException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid mark", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    // ---------------------------------------------------------------- grades and GPA

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Student Grades and GPA");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        txtGradeStudent = new JTextField(12);
        JButton btnView = new JButton("View Grades");
        top.add(new JLabel("Student ID:"));
        top.add(txtGradeStudent);
        top.add(btnView);

        JPanel north = new JPanel(new BorderLayout());
        north.add(lblTitle, BorderLayout.NORTH);
        north.add(top, BorderLayout.CENTER);
        panel.add(north, BorderLayout.NORTH);

        String[] columns = {"Course", "Credits", "CA", "Final Exam", "Total", "Grade", "GPV"};
        gradeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(gradeTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        lblGpa = new JLabel(" ");
        lblGpa.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblGpa, BorderLayout.SOUTH);

        btnView.addActionListener(e -> viewGrades());
        return panel;
    }

    private void viewGrades() {
        String studentId = txtGradeStudent.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a student ID.");
            return;
        }
        try {
            List<Result> results = resultDAO.getResultsByStudent(studentId);
            gradeTableModel.setRowCount(0);
            for (Result r : results) {
                gradeTableModel.addRow(new Object[]{
                        r.getCourseId(),
                        r.getCredits(),
                        format(r.getCaTotal()),
                        format(r.getFinalExamScore()),
                        format(r.getTotalScore()),
                        r.getGrade() == null ? "-" : r.getGrade().getLabel(),
                        r.getGradePoint()
                });
            }
            if (results.isEmpty()) {
                lblGpa.setText("No results found for " + studentId);
            } else {
                lblGpa.setText("GPA (all results shown) = " + format(GPACalculator.calculateCGPA(results)));
            }
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    // ---------------------------------------------------------------- course materials

    private JPanel createMaterialsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Course Materials (lecture notes)");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle, BorderLayout.NORTH);

        cmbMatCourse = new JComboBox<>();
        txtMatTitle = new JTextField();
        txtMatFile = new JTextField();
        txtMatFile.setEditable(false);
        LocalDate today = LocalDate.now();
        matAvailableFrom = createDateSpinner(today);
        matAvailableTo = createDateSpinner(today.plusDays(30));
        JButton btnBrowse = new JButton("Browse...");

        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.add(txtMatFile, BorderLayout.CENTER);
        filePanel.add(btnBrowse, BorderLayout.EAST);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.add(new JLabel("Course:"));
        form.add(cmbMatCourse);
        form.add(new JLabel("Title:"));
        form.add(txtMatTitle);
        form.add(new JLabel("Available from (inclusive):"));
        form.add(matAvailableFrom);
        form.add(new JLabel("Available until (inclusive):"));
        form.add(matAvailableTo);
        form.add(new JLabel("File:"));
        form.add(filePanel);

        String[] columns = {"ID", "Course", "Title", "File", "Available From", "Available Until", "Uploaded"};
        materialTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        materialTable = new JTable(materialTableModel);
        materialTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton btnLoad = new JButton("Load Materials");
        JButton btnUpload = new JButton("Upload New");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnOpen = new JButton("Open File");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttons.add(btnLoad);
        buttons.add(btnUpload);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnOpen);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(form, BorderLayout.NORTH);
        center.add(new JScrollPane(materialTable), BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);

        btnBrowse.addActionListener(e -> browseFile());
        btnLoad.addActionListener(e -> loadMaterials());
        btnUpload.addActionListener(e -> uploadMaterial());
        btnUpdate.addActionListener(e -> updateMaterial());
        btnDelete.addActionListener(e -> deleteMaterial());
        btnOpen.addActionListener(e -> openMaterial());
        materialTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onMaterialSelected();
            }
        });
        populateMaterialCourses();
        return panel;
    }

    private JSpinner createDateSpinner(LocalDate initialDate) {
        Date initialValue = Date.from(initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner spinner = new JSpinner(new SpinnerDateModel(initialValue, null, null, java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        return spinner;
    }

    private LocalDate getSelectedDate(JSpinner spinner) throws ParseException {
        spinner.commitEdit();
        Date date = (Date) spinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void setSelectedDate(JSpinner spinner, LocalDate date) {
        Date value = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        spinner.setValue(value);
    }

    private void populateMaterialCourses() {
        cmbMatCourse.removeAllItems();
        try {
            for (Course course : new CourseDAO().getAllCourses()) {
                cmbMatCourse.addItem(course);
            }
        } catch (SQLException ex) {
            showError(new Exception("Could not load courses for lecture materials.", ex));
        }
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            txtMatFile.setText(selectedFile.getAbsolutePath());
            if (txtMatTitle.getText().trim().isEmpty()) {
                txtMatTitle.setText(selectedFile.getName());
            }
        }
    }

    private void loadMaterials() {
        Course course = (Course) cmbMatCourse.getSelectedItem();
        if (course == null) {
            JOptionPane.showMessageDialog(this, "Select a course first.");
            return;
        }
        String courseId = course.getCourseId();
        try {
            refreshMaterials(courseId);
            if (loadedMaterials.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No materials yet for " + courseId);
            }
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void refreshMaterials(String courseId) throws DatabaseException {
        loadedMaterials = materialDAO.getMaterialsByCourse(courseId);
        materialTableModel.setRowCount(0);
        for (LectureMaterial m : loadedMaterials) {
            materialTableModel.addRow(new Object[]{
                    m.getMaterialId(),
                    m.getCourseId(),
                    m.getTitle(),
                    FileStorage.displayName(m.getFilePath()),
                    m.getAvailableFrom(),
                    m.getAvailableTo(),
                    m.getUploadedDate()
            });
        }
    }

    private void onMaterialSelected() {
        int row = materialTable.getSelectedRow();
        if (row >= 0 && row < loadedMaterials.size()) {
            LectureMaterial material = loadedMaterials.get(row);
            cmbMatCourse.setSelectedItem(findCourse(material.getCourseId()));
            txtMatTitle.setText(material.getTitle());
            setSelectedDate(matAvailableFrom, material.getAvailableFrom());
            setSelectedDate(matAvailableTo, material.getAvailableTo());
            selectedFile = null;
            txtMatFile.setText("");
        }
    }

    private Course findCourse(String courseId) {
        for (int i = 0; i < cmbMatCourse.getItemCount(); i++) {
            Course course = cmbMatCourse.getItemAt(i);
            if (course.getCourseId().equals(courseId)) {
                return course;
            }
        }
        return null;
    }

    private void clearMaterialForm() {
        txtMatTitle.setText("");
        txtMatFile.setText("");
        selectedFile = null;
        LocalDate today = LocalDate.now();
        setSelectedDate(matAvailableFrom, today);
        setSelectedDate(matAvailableTo, today.plusDays(30));
    }

    private void uploadMaterial() {
        Course course = (Course) cmbMatCourse.getSelectedItem();
        String courseId = course == null ? "" : course.getCourseId();
        String title = txtMatTitle.getText().trim();
        if (courseId.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a course and enter a title.");
            return;
        }
        LocalDate availableFrom;
        LocalDate availableTo;
        try {
            availableFrom = getSelectedDate(matAvailableFrom);
            availableTo = getSelectedDate(matAvailableTo);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter both dates using yyyy-MM-dd format.");
            return;
        }
        if (availableFrom.isAfter(availableTo)) {
            JOptionPane.showMessageDialog(this,
                    "The available-from date must be on or before the available-until date.");
            return;
        }
        if (selectedFile == null || !selectedFile.isFile()) {
            JOptionPane.showMessageDialog(this, "Choose a file with the Browse button first.");
            return;
        }
        try {
            String savedPath = FileStorage.saveCopy(selectedFile, courseId);
            try {
                materialDAO.addMaterial(new LectureMaterial(0, courseId, title, savedPath, null,
                        availableFrom, availableTo));
            } catch (DatabaseException ex) {
                FileStorage.delete(savedPath);   // do not keep a file that is not in the database
                throw ex;
            }
            refreshMaterials(courseId);
            clearMaterialForm();
            JOptionPane.showMessageDialog(this, "Material uploaded for " + courseId);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not copy the file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void updateMaterial() {
        int row = materialTable.getSelectedRow();
        if (row < 0 || row >= loadedMaterials.size()) {
            JOptionPane.showMessageDialog(this, "Select a material from the table first.");
            return;
        }
        String title = txtMatTitle.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The title cannot be empty.");
            return;
        }
        LectureMaterial old = loadedMaterials.get(row);
        LocalDate availableFrom;
        LocalDate availableTo;
        try {
            availableFrom = getSelectedDate(matAvailableFrom);
            availableTo = getSelectedDate(matAvailableTo);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter both dates using yyyy-MM-dd format.");
            return;
        }
        if (availableFrom.isAfter(availableTo)) {
            JOptionPane.showMessageDialog(this,
                    "The available-from date must be on or before the available-until date.");
            return;
        }
        String newPath = old.getFilePath();
        try {
            boolean replaceFile = selectedFile != null;
            if (replaceFile) {
                if (!selectedFile.isFile()) {
                    JOptionPane.showMessageDialog(this, "The chosen file does not exist.");
                    return;
                }
                newPath = FileStorage.saveCopy(selectedFile, old.getCourseId());
            }
            try {
                materialDAO.updateMaterial(old.getMaterialId(), title, newPath, availableFrom, availableTo);
            } catch (DatabaseException ex) {
                if (replaceFile) {
                    FileStorage.delete(newPath);
                }
                throw ex;
            }
            if (replaceFile) {
                FileStorage.delete(old.getFilePath());   // remove the replaced file
            }
            refreshMaterials(old.getCourseId());
            clearMaterialForm();
            JOptionPane.showMessageDialog(this, "Material updated.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not copy the file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void deleteMaterial() {
        int row = materialTable.getSelectedRow();
        if (row < 0 || row >= loadedMaterials.size()) {
            JOptionPane.showMessageDialog(this, "Select a material from the table first.");
            return;
        }
        LectureMaterial material = loadedMaterials.get(row);
        int option = JOptionPane.showConfirmDialog(this,
                "Delete \"" + material.getTitle() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            materialDAO.deleteMaterial(material.getMaterialId());
            FileStorage.delete(material.getFilePath());
            refreshMaterials(material.getCourseId());
            clearMaterialForm();
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void openMaterial() {
        int row = materialTable.getSelectedRow();
        if (row < 0 || row >= loadedMaterials.size()) {
            JOptionPane.showMessageDialog(this, "Select a material from the table first.");
            return;
        }
        File file = FileStorage.getFile(loadedMaterials.get(row).getFilePath());
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File not found: " + file.getPath());
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, "Opening files is not supported on this computer.");
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open the file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------- notices

    private JPanel createNoticesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Notices");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Part 1: notices for lecturers (and for everyone)
        String[] columns = {"ID", "Title", "For", "Posted"};
        noticeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        noticeTable = new JTable(noticeTableModel);
        noticeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtNoticeView = new JTextArea(4, 20);
        txtNoticeView.setEditable(false);
        txtNoticeView.setLineWrap(true);
        txtNoticeView.setWrapStyleWord(true);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadNotices());
        noticeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onNoticeSelected();
            }
        });

        JPanel viewPanel = new JPanel(new BorderLayout(5, 5));
        viewPanel.setBorder(BorderFactory.createTitledBorder("1. Notices for lecturers"));
        viewPanel.add(new JScrollPane(noticeTable), BorderLayout.CENTER);
        JPanel viewBottom = new JPanel(new BorderLayout(5, 5));
        viewBottom.add(new JScrollPane(txtNoticeView), BorderLayout.CENTER);
        viewBottom.add(btnRefresh, BorderLayout.EAST);
        viewPanel.add(viewBottom, BorderLayout.SOUTH);

        // Part 2: post a new notice
        txtNoticeTitle = new JTextField();
        cmbNoticeTarget = new JComboBox<>(new String[]{"ALL", "STUDENT", "LECTURER", "TECHNICAL_OFFICER"});
        txtNoticeContent = new JTextArea(3, 20);
        txtNoticeContent.setLineWrap(true);
        txtNoticeContent.setWrapStyleWord(true);
        JButton btnPost = new JButton("Post Notice");
        btnPost.addActionListener(e -> postNotice());

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.add(new JLabel("Title:"));
        form.add(txtNoticeTitle);
        form.add(new JLabel("Send to:"));
        form.add(cmbNoticeTarget);

        JPanel postPanel = new JPanel(new BorderLayout(5, 5));
        postPanel.setBorder(BorderFactory.createTitledBorder("2. Post a notice"));
        postPanel.add(form, BorderLayout.NORTH);
        postPanel.add(new JScrollPane(txtNoticeContent), BorderLayout.CENTER);
        postPanel.add(btnPost, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new GridLayout(2, 1, 10, 10));
        wrapper.add(viewPanel);
        wrapper.add(postPanel);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void loadNotices() {
        try {
            loadedNotices = noticeDAO.getNoticesForRole(lecturer.getRole());
            noticeTableModel.setRowCount(0);
            for (NoticeItem n : loadedNotices) {
                noticeTableModel.addRow(new Object[]{
                        n.getNoticeId(), n.getTitle(), n.getTargetRole(), n.getPostedDate()
                });
            }
            txtNoticeView.setText("");
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void onNoticeSelected() {
        int row = noticeTable.getSelectedRow();
        if (row >= 0 && row < loadedNotices.size()) {
            txtNoticeView.setText(loadedNotices.get(row).getContent());
            txtNoticeView.setCaretPosition(0);
        }
    }

    private void postNotice() {
        String title = txtNoticeTitle.getText().trim();
        String body = txtNoticeContent.getText().trim();
        if (title.isEmpty() || body.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a title and the notice text.");
            return;
        }
        if (title.length() > 100) {
            JOptionPane.showMessageDialog(this, "The title can have at most 100 characters.");
            return;
        }
        String target = (String) cmbNoticeTarget.getSelectedItem();
        // the notice table has no "posted by" column, so the name is added to the text
        String content = body + "\n\n- Posted by " + lecturer.getFirstName() + " "
                + lecturer.getLastName() + " (Lecturer)";
        try {
            noticeDAO.addNotice(new NoticeItem(0, title, content, null, target));
            txtNoticeTitle.setText("");
            txtNoticeContent.setText("");
            loadNotices();
            JOptionPane.showMessageDialog(this, "Notice posted to " + target);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    // ---------------------------------------------------------------- helpers

    private String nz(String value) {
        return value == null ? "-" : value;
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }

    private void showError(Exception ex) {
        String message = ex.getMessage();
        if (ex.getCause() != null) {
            message += "\n" + ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Temporary test lecturer - replace with the logged-in lecturer once the login screen is ready
            Lecturer lec = new Lecturer("lec1", "Kamal", "Silva",
                    "kamal@fot.ruh.ac.lk", null, null, "ICT");
            new LecturerDashboard(lec).setVisible(true);
        });
    }
}
