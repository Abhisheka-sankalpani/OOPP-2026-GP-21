package gui;

import dao.AttendanceDAO;
import model.Attendance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UndergraduateAttendancePanel extends JPanel {

    private String studentId;
    private AttendanceDAO attendanceDAO;
    private JTable tableAttendance;
    private DefaultTableModel modelAttendance;
    private JLabel lblTotalCourses;
    private JLabel lblEligibleCourses;
    private JLabel lblNotEligibleCourses;
    private JLabel lblAttendanceSummary;

    public UndergraduateAttendancePanel(String studentId) {
        this.studentId = studentId;
        this.attendanceDAO = new AttendanceDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadAttendanceData();
    }

    private void initUI() {
        // Top Header Bar
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Attendance");
        btnRefresh.addActionListener(e -> loadAttendanceData());

        JPanel header = UITheme.createTabHeader(
                "My Course Attendance",
                "Minimum academic requirement: 80% per course module for examination eligibility.",
                btnRefresh
        );
        add(header, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);

        JPanel cardTotal = UITheme.createStatCard("Enrolled Modules", "0", "Total active courses", UITheme.ACCENT_BLUE);
        JPanel cardEligible = UITheme.createStatCard("Eligible Modules", "0", ">= 80% attendance", UITheme.COLOR_SUCCESS);
        JPanel cardNotEligible = UITheme.createStatCard("At Risk / Ineligible", "0", "< 80% attendance threshold", UITheme.COLOR_DANGER);

        lblTotalCourses = findValueLabel(cardTotal);
        lblEligibleCourses = findValueLabel(cardEligible);
        lblNotEligibleCourses = findValueLabel(cardNotEligible);

        statsRow.add(cardTotal);
        statsRow.add(cardEligible);
        statsRow.add(cardNotEligible);
        centerPanel.add(statsRow, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        JLabel lblTableTitle = new JLabel("Attendance Breakdown per Module");
        lblTableTitle.setFont(UITheme.FONT_SECTION);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] colNames = {"Course ID", "Course Title", "Theory (Att/Tot)", "Practical (Att/Tot)", "Overall %", "Eligibility"};
        modelAttendance = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableAttendance = new JTable(modelAttendance);
        UITheme.styleTable(tableAttendance);

        // Center align ratios and percentage
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableAttendance.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableAttendance.getColumnModel().getColumn(1).setPreferredWidth(260);
        tableAttendance.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableAttendance.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableAttendance.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        tableAttendance.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(tableAttendance);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);

        lblAttendanceSummary = new JLabel("Eligible Course Modules: Calculating...");
        lblAttendanceSummary.setFont(UITheme.FONT_BODY_BOLD);
        lblAttendanceSummary.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel lblNote = new JLabel("<html><i>Note: Approved medicals are officially counted into the attended session count.</i></html>");
        lblNote.setFont(UITheme.FONT_SMALL);
        lblNote.setForeground(UITheme.COLOR_TEXT_MUTED);

        footerPanel.add(lblAttendanceSummary, BorderLayout.WEST);
        footerPanel.add(lblNote, BorderLayout.EAST);
        tableCard.add(footerPanel, BorderLayout.SOUTH);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JLabel findValueLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                for (Component sub : p.getComponents()) {
                    if (sub instanceof JLabel) {
                        JLabel lbl = (JLabel) sub;
                        if (lbl.getFont().equals(UITheme.FONT_STAT_NUM)) {
                            return lbl;
                        }
                    }
                }
            }
        }
        return new JLabel("0");
    }

    public void loadAttendanceData() {
        modelAttendance.setRowCount(0);
        List<Attendance> list = attendanceDAO.getAttendanceByStudentId(studentId);

        int total = 0;
        int eligible = 0;

        for (Attendance a : list) {
            if (a.getOverallPercentage() > 0 || (a.getTheoryRatio() != null && a.getTheoryRatio().contains("/") && !a.getTheoryRatio().equals("0/0"))) {
                total++;
                boolean isEligible = a.getOverallPercentage() >= 80.0;
                if (isEligible) eligible++;

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

        int notEligible = total - eligible;
        if (lblTotalCourses != null) lblTotalCourses.setText(String.valueOf(total));
        if (lblEligibleCourses != null) lblEligibleCourses.setText(String.valueOf(eligible));
        if (lblNotEligibleCourses != null) lblNotEligibleCourses.setText(String.valueOf(notEligible));

        if (lblAttendanceSummary != null) {
            lblAttendanceSummary.setText("Eligible Course Modules: " + eligible + " of " + total + " enrolled courses.");
        }
    }
}
