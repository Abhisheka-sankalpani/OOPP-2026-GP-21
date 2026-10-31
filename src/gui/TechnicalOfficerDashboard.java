package gui;

import dao.*;
import exception.InvalidSessionException;
import exception.MedicalSubmissionException;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

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

    // ─── Theme colours (match existing project style) ─────────────────────
    private static final Color DARK_GREEN  = new Color(11, 102, 77);
    private static final Color LIGHT_GREEN = new Color(224, 255, 230);
    private static final Color ACCENT      = new Color(0, 153, 102);

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

        setTitle("Technical Officer Dashboard — FOT MIS");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    // =====================================================================
    //  UI CONSTRUCTION
    // =====================================================================

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeaderBar(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 13));

        tabs.addTab("📋  Profile",              buildProfilePanel());
        tabs.addTab("✅  Record Attendance",    buildAttendancePanel());
        tabs.addTab("🏥  Medical Submissions",  buildMedicalPanel());
        tabs.addTab("📊  Attendance Summary",   buildSummaryPanel());
        tabs.addTab("🗂️  Category View",        buildCategoryPanel());

        root.add(tabs, BorderLayout.CENTER);
        add(root);
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(DARK_GREEN);
        bar.setBorder(new EmptyBorder(10, 20, 10, 20));

        String name = (currentTO != null) ? currentTO.getFullName() : "Unknown TO";
        String dept = (currentTO != null) ? currentTO.getDepartment() : "";

        JLabel lbl = new JLabel("Welcome, " + name + "  |  Dept: " + dept
                                + "  |  Role: Technical Officer");
        lbl.setFont(new Font("Arial", Font.BOLD, 15));
        lbl.setForeground(Color.WHITE);
        bar.add(lbl, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(Color.RED);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) dispose();
        });
        bar.add(btnLogout, BorderLayout.EAST);
        return bar;
    }

    // =====================================================================
    //  TAB 1 — PROFILE
    // =====================================================================
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        panel.setBackground(LIGHT_GREEN);

        JLabel title = new JLabel("Technical Officer Profile");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(DARK_GREEN);
        panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 12));
        grid.setBackground(LIGHT_GREEN);

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
        btnRecord.setBackground(DARK_GREEN);
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
        btnLoad.setBackground(ACCENT);
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
                                    ? new Color(198, 255, 198)
                                    : new Color(255, 198, 198));
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
        btnLoadMed.setBackground(ACCENT);
        btnLoadMed.setForeground(Color.WHITE);
        controls.add(btnLoadMed);

        JButton btnLoadAll = new JButton("Load All");
        btnLoadAll.setBackground(DARK_GREEN);
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
        btnApprove.setBackground(new Color(0, 160, 0));
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
        btnLoad.setBackground(DARK_GREEN);
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
        btnFilter.setBackground(DARK_GREEN);
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
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(DARK_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(153, 204, 153));
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
                                    ? new Color(198, 255, 198)
                                    : new Color(255, 220, 220));
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
