package gui;

import exception.InvalidSessionException;
import exception.MedicalSubmissionException;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import dao.TechnicalOfficerDAO;
import dao.AttendanceDAO;
import dao.MedicalDAO;
import dao.AttendanceSummaryDAO;

/**
 * Technical Officer Dashboard — Member 2 Implementation.
 *
 * Features:
 *  1. TO Profile display
 *  2. Attendance Recording Panel  (15 theory + 15 practical sessions, 2 h each)
 *  3. Medical Submissions Panel   (view all, approve / reject)
 *  4. Attendance Summary Panel    (Theory %, Practical %, Combined %)
 *  5. Category Filter Panel       (> 80 %, = 80 %, < 80 % — with / without medical)
 *
 * OOP features demonstrated:
 *  • Inheritance   — TechnicalOfficer extends User
 *  • Polymorphism  — AttendanceSummary subclasses override calculatePercentage()
 *  • Abstraction   — User.getRole() is abstract
 *  • Exception     — InvalidSessionException, MedicalSubmissionException
 *  • Encapsulation — all fields are private; accessed through getters/setters
 */
public class TechnicalOfficerDashboard extends JFrame {

    // The shared Admin / Lecturer / Technical Officer dashboard palette.
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

    // ─── State ────────────────────────────────────────────────────────────
    private final String           toId;
    private       TechnicalOfficer currentTO;
    private       String           selectedCourseId = "ICT2132"; // default

    // ─── DAO layer ────────────────────────────────────────────────────────
    private final TechnicalOfficerDAO  toDAO          = new TechnicalOfficerDAO();
    private final AttendanceDAO        attendanceDAO  = new AttendanceDAO();
    private final MedicalDAO           medicalDAO     = new MedicalDAO();
    private final AttendanceSummaryDAO summaryDAO     = new AttendanceSummaryDAO();

    // ─── Reusable UI references ───────────────────────────────────────────
    private JTable  attendanceTable;
    private JTable  medicalTable;
    private JTable  summaryTable;
    private JComboBox<String> cbCourse;
    private JComboBox<String> cbSummaryFilter;
    private JCheckBox chkWithMedical;

    // ─────────────────────────────────────────────────────────────────────
    public TechnicalOfficerDashboard(String toId) {
        this.toId = toId;
        this.currentTO = toDAO.getTechnicalOfficerById(toId);

        setTitle("FOT-MIS | Technical Officer Dashboard");
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

    // =====================================================================
    //  UI CONSTRUCTION
    // =====================================================================

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(buildHeaderBar(), BorderLayout.NORTH);
        root.add(createSidebar(), BorderLayout.WEST);

        contentPanel.setBackground(BACKGROUND);
        contentPanel.add(createHomePanel(), "DASHBOARD");
        contentPanel.add(wrapContent(buildProfilePanel()), "PROFILE");
        contentPanel.add(wrapContent(buildAttendancePanel()), "ATTENDANCE");
        contentPanel.add(wrapContent(buildMedicalPanel()), "MEDICAL");
        contentPanel.add(wrapContent(buildSummaryPanel()), "SUMMARY");
        contentPanel.add(wrapContent(buildCategoryPanel()), "CATEGORY");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
        switchCard("DASHBOARD");
    }

    // The same top header layout and blue palette as the Admin Dashboard.
    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setPreferredSize(new Dimension(0, 74));
        header.setBorder(new EmptyBorder(12, 28, 12, 28));

        JPanel branding = new JPanel();
        branding.setOpaque(false);
        branding.setLayout(new BoxLayout(branding, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("FACULTY OF TECHNOLOGY MIS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("University of Ruhuna, Sri Lanka | Technical Officer Workspace");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(222, 233, 248));
        branding.add(title);
        branding.add(Box.createVerticalStrut(4));
        branding.add(subtitle);
        header.add(branding, BorderLayout.WEST);

        String name = currentTO != null ? currentTO.getFullName() : toId;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 2));
        actions.setOpaque(false);
        JLabel user = new JLabel(name + " (" + toId + ")");
        user.setFont(UI_BOLD);
        user.setForeground(Color.WHITE);

        JButton logout = new JButton("Sign Out");
        logout.setFont(UI_BOLD);
        logout.setBackground(new Color(239, 68, 68));
        logout.setForeground(Color.WHITE);
        logout.setOpaque(true);
        logout.setFocusPainted(false);
        logout.setBorder(new EmptyBorder(11, 22, 11, 22));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> confirmLogout());
        actions.add(user);
        actions.add(logout);
        header.add(actions, BorderLayout.EAST);
        return header;
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
        addNavButton(menu, "Record Attendance", "ATTENDANCE");
        addNavButton(menu, "Medical Submissions", "MEDICAL");
        addNavButton(menu, "Attendance Summary", "SUMMARY");
        addNavButton(menu, "Category View", "CATEGORY");
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
    }

    private JPanel createHomePanel() {
        JPanel home = new JPanel(new BorderLayout(0, 22));
        home.setBackground(BACKGROUND);
        home.setBorder(new EmptyBorder(34, 36, 32, 36));

        JPanel intro = new JPanel();
        intro.setBackground(BACKGROUND);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        String name = currentTO != null ? currentTO.getFirstName() : "Technical Officer";
        JLabel welcome = new JLabel("Welcome back, " + name);
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcome.setForeground(TEXT);
        JLabel description = new JLabel("Faculty of Technology Management Information System | Technical Officer Control Center");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(MUTED);
        intro.add(welcome);
        intro.add(Box.createVerticalStrut(8));
        intro.add(description);
        home.add(intro, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, 18, 18));
        cards.setOpaque(false);
        cards.add(featureCard("My Profile", "My account and department", "PROFILE"));
        cards.add(featureCard("Record Attendance", "Record and view sessions", "ATTENDANCE"));
        cards.add(featureCard("Medical Submissions", "Review medical requests", "MEDICAL"));
        cards.add(featureCard("Attendance Summary", "View attendance percentages", "SUMMARY"));
        cards.add(featureCard("Category View", "Filter by attendance category", "CATEGORY"));
        // Empty grid cell keeps the overview neatly aligned in two rows.
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        cards.add(filler);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(cards, BorderLayout.NORTH);
        home.add(holder, BorderLayout.CENTER);
        return home;
    }

    private JPanel featureCard(String title, String subtitle, String card) {
        JPanel item = new JPanel(new BorderLayout(0, 12));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 1, 1, ACTIVE),
                new EmptyBorder(22, 20, 22, 20)));
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(TEXT);
        JLabel detail = new JLabel(subtitle);
        detail.setFont(UI_FONT);
        detail.setForeground(MUTED);
        labels.add(heading);
        labels.add(Box.createVerticalStrut(8));
        labels.add(detail);
        item.add(labels, BorderLayout.CENTER);
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

    /** Give existing functional panels consistent styling without changing event handlers. */
    private JPanel wrapContent(JPanel original) {
        applyFieldStyling(original);
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
            String buttonText = button.getText().toLowerCase();
            button.setForeground(Color.WHITE);
            button.setBackground(buttonText.contains("reject") || buttonText.contains("delete")
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
        if (comp instanceof JScrollPane) {
            ((JScrollPane) comp).getViewport().setBackground(Color.WHITE);
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyFieldStyling(child);
            }
        }
    }

    private void confirmLogout() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to sign out?", "Confirm Sign Out",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            // Preserve the existing Technical Officer sign-out behavior (close window).
            // Session invalidation and returning to LoginFrame can be integrated separately.
            dispose();
        }
    }

    // =====================================================================
    //  TAB 1 — PROFILE
    // =====================================================================
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Technical Officer Profile");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(HEADER);
        panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 12));
        grid.setBackground(Color.WHITE);

        if (currentTO != null) {
            addProfileRow(grid, "User ID",     currentTO.getUserId());
            addProfileRow(grid, "Full Name",   currentTO.getFullName());
            addProfileRow(grid, "Email",       currentTO.getEmail());
            addProfileRow(grid, "Contact No.", currentTO.getContactNo() != null
                    ? currentTO.getContactNo() : "N/A");
            addProfileRow(grid, "Department",  currentTO.getDepartment());
            addProfileRow(grid, "Role",        currentTO.getRole()); // Polymorphism call
        } else {
            grid.add(new JLabel("⚠  Technical Officer data not found in database!"));
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private void addProfileRow(JPanel grid, String label, String value) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        JLabel val = new JLabel(value);
        val.setFont(new Font("Arial", Font.PLAIN, 13));
        grid.add(lbl);
        grid.add(val);
    }

    // =====================================================================
    //  TAB 2 — RECORD ATTENDANCE
    // =====================================================================
    private JPanel buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ── Top controls ─────────────────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        controls.add(new JLabel("Course:"));
        cbCourse = new JComboBox<>(new String[]{
                "ICT2132", "ICT2113", "ICT2142", "ICT2152",
                "TCS2112", "ICT2121", "ICT2122", "ICT2162", "ENG2122"
        });
        cbCourse.setPreferredSize(new Dimension(140, 28));
        controls.add(cbCourse);

        controls.add(new JLabel("Student ID:"));
        JTextField tfStudent = new JTextField("tg1001", 8);
        controls.add(tfStudent);

        controls.add(new JLabel("Session No (1-15):"));
        JSpinner spSession = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        spSession.setPreferredSize(new Dimension(60, 28));
        controls.add(spSession);

        controls.add(new JLabel("Type:"));
        JComboBox<String> cbType = new JComboBox<>(new String[]{"THEORY", "PRACTICAL"});
        controls.add(cbType);

        controls.add(new JLabel("Status:"));
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"PRESENT", "ABSENT"});
        controls.add(cbStatus);

        JButton btnRecord = new JButton("Record");
        btnRecord.setBackground(HEADER);
        btnRecord.setForeground(Color.WHITE);
        controls.add(btnRecord);

        panel.add(controls, BorderLayout.NORTH);

        // ── Attendance table ──────────────────────────────────────────────
        String[] cols = {"Student ID", "Course", "Session#", "Type", "Date", "Duration (hrs)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        attendanceTable = new JTable(model);
        styleTable(attendanceTable);
        panel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

        // ── Record button action ──────────────────────────────────────────
        btnRecord.addActionListener(e -> {
            String studentId = tfStudent.getText().trim();
            String courseId  = (String) cbCourse.getSelectedItem();
            int    sessionNo = (int) spSession.getValue();
            String type      = (String) cbType.getSelectedItem();
            String status    = (String) cbStatus.getSelectedItem();

            if (studentId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a Student ID.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Exception Handling — InvalidSessionException
            try {
                Attendance a = new Attendance();
                a.setStudentId(studentId);
                a.setCourseId(courseId);
                a.setSessionNo(sessionNo);
                a.setSessionType(type);
                a.setSessionDate(Date.valueOf(LocalDate.now()));
                a.setSessionDuration(2);   // project rule: 2 hours
                a.setStatus(status);

                attendanceDAO.recordAttendance(a);
                JOptionPane.showMessageDialog(this,
                        "✅ Attendance recorded for " + studentId +
                                " | Session " + sessionNo + " (" + type + ") — " + status,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshAttendanceTable(studentId, courseId, model);

            } catch (InvalidSessionException ex) {
                // Exception Handling — display meaningful message to user
                JOptionPane.showMessageDialog(this,
                        "❌ " + ex.getMessage(), "Invalid Session", JOptionPane.ERROR_MESSAGE);
            }
        });

        // ── Load button ───────────────────────────────────────────────────
        JButton btnLoad = new JButton("Load Records");
        btnLoad.setBackground(ACTIVE);
        btnLoad.setForeground(Color.WHITE);
        btnLoad.addActionListener(e ->
                refreshAttendanceTable(tfStudent.getText().trim(),
                        (String) cbCourse.getSelectedItem(), model));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnLoad);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshAttendanceTable(String studentId, String courseId,
                                        DefaultTableModel model) {
        model.setRowCount(0);
        List<Attendance> list = attendanceDAO.getAttendanceByStudentAndCourse(studentId, courseId);
        for (Attendance a : list) {
            model.addRow(new Object[]{
                    a.getStudentId(), a.getCourseId(),
                    a.getSessionNo(), a.getSessionType(),
                    a.getSessionDate(), a.getSessionDuration(),
                    a.getStatus()
            });
        }
        colourAttendanceRows(attendanceTable, model);
    }

    private void colourAttendanceRows(JTable table, DefaultTableModel model) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String status = (String) model.getValueAt(row, 6);
                if (!isSelected) {
                    c.setBackground("PRESENT".equals(status)
                            ? new Color(226, 247, 235)
                            : new Color(254, 226, 226));
                }
                return c;
            }
        });
        table.repaint();
    }

    // =====================================================================
    //  TAB 3 — MEDICAL SUBMISSIONS
    // =====================================================================
    private JPanel buildMedicalPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ── Top controls ─────────────────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        controls.add(new JLabel("Course:"));
        JComboBox<String> cbMedCourse = new JComboBox<>(new String[]{
                "ICT2132", "ICT2113", "ICT2142", "ICT2152",
                "TCS2112", "ICT2121", "ICT2122", "ICT2162", "ENG2122"
        });
        cbMedCourse.setPreferredSize(new Dimension(140, 28));
        controls.add(cbMedCourse);

        JButton btnLoadMed = new JButton("Load Pending");
        btnLoadMed.setBackground(ACTIVE);
        btnLoadMed.setForeground(Color.WHITE);
        controls.add(btnLoadMed);

        JButton btnLoadAll = new JButton("Load All");
        btnLoadAll.setBackground(HEADER);
        btnLoadAll.setForeground(Color.WHITE);
        controls.add(btnLoadAll);

        panel.add(controls, BorderLayout.NORTH);

        // ── Medical table ─────────────────────────────────────────────────
        String[] cols = {"ID", "Student", "Course", "Session#", "Type",
                "Medical Date", "Submitted", "Description", "Status"};
        DefaultTableModel medModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        medicalTable = new JTable(medModel);
        styleTable(medicalTable);
        panel.add(new JScrollPane(medicalTable), BorderLayout.CENTER);

        // ── Action buttons ────────────────────────────────────────────────
        JButton btnApprove = new JButton("✔ Approve");
        btnApprove.setBackground(ACTIVE);
        btnApprove.setForeground(Color.WHITE);

        JButton btnReject = new JButton("✘ Reject");
        btnReject.setBackground(new Color(200, 0, 0));
        btnReject.setForeground(Color.WHITE);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        south.add(new JLabel("Select a row then:"));
        south.add(btnApprove);
        south.add(btnReject);
        panel.add(south, BorderLayout.SOUTH);

        // ── Event Handlers ─────────────────────────────────────────────────
        btnLoadMed.addActionListener(e -> {
            medModel.setRowCount(0);
            String cid = (String) cbMedCourse.getSelectedItem();
            List<Medical> list = medicalDAO.getPendingMedicalsByCourse(cid);
            for (Medical m : list) fillMedicalRow(medModel, m);
        });

        btnLoadAll.addActionListener(e -> {
            medModel.setRowCount(0);
            String cid = (String) cbMedCourse.getSelectedItem();
            List<Medical> list = medicalDAO.getAllMedicalsByCourse(cid);
            for (Medical m : list) fillMedicalRow(medModel, m);
        });

        // Exception Handling — MedicalSubmissionException
        btnApprove.addActionListener(e -> updateMedicalStatus(medicalTable, medModel, "APPROVED"));
        btnReject.addActionListener(e ->  updateMedicalStatus(medicalTable, medModel, "REJECTED"));

        return panel;
    }

    private void fillMedicalRow(DefaultTableModel model, Medical m) {
        model.addRow(new Object[]{
                m.getMedicalId(), m.getStudentId(), m.getCourseId(),
                m.getSessionNo(), m.getSessionType(),
                m.getMedicalDate(), m.getSubmissionDate(),
                m.getDescription(), m.getApprovalStatus()
        });
    }

    private void updateMedicalStatus(JTable table, DefaultTableModel model, String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a medical record first.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int medicalId = (int) model.getValueAt(row, 0);

        // Exception Handling — MedicalSubmissionException
        try {
            medicalDAO.updateApprovalStatus(medicalId, status);
            model.setValueAt(status, row, 8);
            JOptionPane.showMessageDialog(this,
                    "Medical #" + medicalId + " has been " + status + ".",
                    "Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (MedicalSubmissionException ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    //  TAB 4 — ATTENDANCE SUMMARY  (Theory % + Practical % + Combined %)
    // =====================================================================
    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ── Top controls ─────────────────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        controls.add(new JLabel("Course:"));
        JComboBox<String> cbSum = new JComboBox<>(new String[]{
                "ICT2132", "ICT2113", "ICT2142", "ICT2152",
                "TCS2112", "ICT2121", "ICT2122", "ICT2162", "ENG2122"
        });
        cbSum.setPreferredSize(new Dimension(140, 28));
        controls.add(cbSum);

        JButton btnLoad = new JButton("Load Summary");
        btnLoad.setBackground(HEADER);
        btnLoad.setForeground(Color.WHITE);
        controls.add(btnLoad);

        panel.add(controls, BorderLayout.NORTH);

        // ── Summary table ─────────────────────────────────────────────────
        String[] cols = {
                "Student ID", "Course",
                "Theory Attended", "Theory Total", "Theory %",
                "Pract. Attended", "Pract. Total", "Practical %",
                "Combined %", "Combined % (w/ Medical)", "Eligible?"
        };
        DefaultTableModel sumModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        summaryTable = new JTable(sumModel);
        styleTable(summaryTable);
        panel.add(new JScrollPane(summaryTable), BorderLayout.CENTER);

        // ── Load event ────────────────────────────────────────────────────
        btnLoad.addActionListener(e -> {
            sumModel.setRowCount(0);
            String cid = (String) cbSum.getSelectedItem();
            // Polymorphism in action: buildCombinedSummary() internally uses
            // TheoryAttendanceSummary & PracticalAttendanceSummary, each
            // overriding calculatePercentage() differently.
            List<CombinedAttendanceSummary> summaries = summaryDAO.buildClassSummary(cid);

            for (CombinedAttendanceSummary cs : summaries) {
                TheoryAttendanceSummary    ts = cs.getTheorySummary();
                PracticalAttendanceSummary ps = cs.getPracticalSummary();

                sumModel.addRow(new Object[]{
                        cs.getStudentId(),
                        cs.getCourseId(),
                        ts != null ? ts.getAttended() : 0,
                        ts != null ? ts.getTotal()    : 0,
                        ts != null ? String.format("%.1f%%", ts.calculatePercentage()) : "N/A",
                        ps != null ? ps.getAttended() : 0,
                        ps != null ? ps.getTotal()    : 0,
                        ps != null ? String.format("%.1f%%", ps.calculatePercentage()) : "N/A",
                        String.format("%.1f%%", cs.calculatePercentage()),
                        String.format("%.1f%%", cs.calculatePercentageWithMedical()),
                        cs.isEligible() ? "YES ✔" : "NO ✘"
                });
            }
            colourEligibilityRows(summaryTable, sumModel, 10);
        });

        return panel;
    }

    // =====================================================================
    //  TAB 5 — CATEGORY VIEW  (> 80 %, = 80 %, < 80 %)
    // =====================================================================
    private JPanel buildCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ── Top controls ─────────────────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        controls.add(new JLabel("Course:"));
        JComboBox<String> cbCat = new JComboBox<>(new String[]{
                "ICT2132", "ICT2113", "ICT2142", "ICT2152",
                "TCS2112", "ICT2121", "ICT2122", "ICT2162", "ENG2122"
        });
        cbCat.setPreferredSize(new Dimension(140, 28));
        controls.add(cbCat);

        controls.add(new JLabel("Category:"));
        cbSummaryFilter = new JComboBox<>(new String[]{"> 80%", "= 80%", "< 80%"});
        controls.add(cbSummaryFilter);

        chkWithMedical = new JCheckBox("Include Medical");
        chkWithMedical.setToolTipText("When checked, uses attendance % adjusted with approved medicals.");
        controls.add(chkWithMedical);

        JButton btnFilter = new JButton("Filter");
        btnFilter.setBackground(HEADER);
        btnFilter.setForeground(Color.WHITE);
        controls.add(btnFilter);

        panel.add(controls, BorderLayout.NORTH);

        // ── Result table ──────────────────────────────────────────────────
        String[] cols = {"Student ID", "Course", "Combined %", "Combined % (w/ Medical)",
                "Category (raw)", "Eligible?"};
        DefaultTableModel catModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable catTable = new JTable(catModel);
        styleTable(catTable);
        panel.add(new JScrollPane(catTable), BorderLayout.CENTER);

        // ── Summary label ─────────────────────────────────────────────────
        JLabel lblCount = new JLabel("Total: 0 students");
        lblCount.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(lblCount, BorderLayout.SOUTH);

        // ── Filter action ─────────────────────────────────────────────────
        btnFilter.addActionListener(e -> {
            catModel.setRowCount(0);
            String cid      = (String) cbCat.getSelectedItem();
            String category = (String) cbSummaryFilter.getSelectedItem();
            boolean withMed = chkWithMedical.isSelected();

            List<CombinedAttendanceSummary> all      = summaryDAO.buildClassSummary(cid);
            List<CombinedAttendanceSummary> filtered = summaryDAO.filterByCategory(all, category, withMed);

            for (CombinedAttendanceSummary cs : filtered) {
                catModel.addRow(new Object[]{
                        cs.getStudentId(),
                        cs.getCourseId(),
                        String.format("%.1f%%", cs.calculatePercentage()),
                        String.format("%.1f%%", cs.calculatePercentageWithMedical()),
                        cs.getCategory(),
                        cs.isEligible() ? "YES ✔" : "NO ✘"
                });
            }
            lblCount.setText("Total: " + filtered.size() + " student(s) in \"" + category + "\"");
            colourEligibilityRows(catTable, catModel, 5);
        });

        return panel;
    }

    // =====================================================================
    //  UTILITY HELPERS
    // =====================================================================

    private void styleTable(JTable table) {
        table.setRowHeight(24);
        table.setFont(UI_FONT);
        table.getTableHeader().setFont(UI_BOLD);
        table.getTableHeader().setBackground(new Color(238, 243, 249));
        table.getTableHeader().setForeground(TEXT);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    /** Colours eligible rows green, non-eligible rows light-red. */
    private void colourEligibilityRows(JTable table, DefaultTableModel model, int eligCol) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSel, hasFocus, row, col);
                if (!isSel) {
                    String elig = (String) model.getValueAt(row, eligCol);
                    c.setBackground(elig != null && elig.startsWith("YES")
                            ? new Color(226, 247, 235)
                            : new Color(254, 226, 226));
                }
                return c;
            }
        });
        table.repaint();
    }

    // =====================================================================
    //  ENTRY POINT
    // =====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TechnicalOfficerDashboard("to1").setVisible(true);
        });
    }
}
