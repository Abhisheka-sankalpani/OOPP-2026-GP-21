package gui;

import dao.UndergraduateDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UndergraduateEligibilityForm extends JPanel {

    private String studentId;
    private UndergraduateDAO undergraduateDAO;
    private JTable tableCombined;
    private DefaultTableModel modelCombined;
    private JLabel lblSGPAVal;
    private JLabel lblCGPAVal;

    public UndergraduateEligibilityForm(String studentId) {
        this.studentId = studentId;
        this.undergraduateDAO = new UndergraduateDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadEligibilityData();
    }

    private void initUI() {
        // 1. Top Header Bar
        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setOpaque(false);

        JButton btnCheckEligibility = UITheme.createPrimaryButton("Check Eligibility Engine");
        btnCheckEligibility.addActionListener(e -> openEligibilityEngineDialog());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Grades");
        btnRefresh.addActionListener(e -> {
            loadEligibilityData();
            JOptionPane.showMessageDialog(this, "Grades & Eligibility reloaded successfully!", "Refreshed", JOptionPane.INFORMATION_MESSAGE);
        });

        topActions.add(btnCheckEligibility);
        topActions.add(btnRefresh);

        JPanel header = UITheme.createTabHeader(
                "My Grades & Exam Eligibility",
                "Continuous Assessment (CA >= 40%) & Attendance (>= 80%) rules for final examinations.",
                topActions
        );
        add(header, BorderLayout.NORTH);

        // 2. Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        // Stat Cards Row for SGPA and CGPA
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        statsRow.setOpaque(false);

        JPanel cardSGPA = UITheme.createStatCard("Semester GPA (SGPA)", "0.00", "Current semester performance", UITheme.COLOR_SUCCESS);
        JPanel cardCGPA = UITheme.createStatCard("Cumulative GPA (CGPA)", "0.00", "Overall academic standing", UITheme.ACCENT_TEAL);

        lblSGPAVal = findValueLabel(cardSGPA);
        lblCGPAVal = findValueLabel(cardCGPA);

        statsRow.add(cardSGPA);
        statsRow.add(cardCGPA);
        centerPanel.add(statsRow, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        JLabel lblTableTitle = new JLabel("Academic Course Marks & Eligibility Summary");
        lblTableTitle.setFont(UITheme.FONT_SECTION);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] colNames = {"Course ID", "CA Score", "CA Status", "End Exam", "Total", "Grade", "GPA", "Exam Eligibility"};
        modelCombined = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableCombined = new JTable(modelCombined);
        UITheme.styleTable(tableCombined);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        tableCombined.getColumnModel().getColumn(0).setPreferredWidth(120);
        tableCombined.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableCombined.getColumnModel().getColumn(2).setCellRenderer(new UITheme.StatusBadgeRenderer());
        tableCombined.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableCombined.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tableCombined.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tableCombined.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tableCombined.getColumnModel().getColumn(7).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(tableCombined);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Footer Note
        JLabel lblNote = new JLabel("<html><i>Examination Regulation: Continuous Assessment (CA) score >= 40% AND Attendance >= 80% are mandatory to sit for final exam papers.</i></html>");
        lblNote.setFont(UITheme.FONT_SMALL);
        lblNote.setForeground(UITheme.COLOR_TEXT_MUTED);
        tableCard.add(lblNote, BorderLayout.SOUTH);

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
        return new JLabel("0.00");
    }

    public void loadEligibilityData() {
        String actualCGPA = undergraduateDAO.getStudentCGPA(studentId);
        if (lblSGPAVal != null) lblSGPAVal.setText(actualCGPA != null && !actualCGPA.isEmpty() ? actualCGPA : "N/A");
        if (lblCGPAVal != null) lblCGPAVal.setText(actualCGPA != null && !actualCGPA.isEmpty() ? actualCGPA : "N/A");

        modelCombined.setRowCount(0);
        List<Object[]> gradesList = undergraduateDAO.getDetailedEligibilityData(studentId);
        if (gradesList != null && !gradesList.isEmpty()) {
            for (Object[] row : gradesList) {
                modelCombined.addRow(new Object[]{
                        row[0], // Course ID
                        row[2], // CA Mark
                        row[3], // CA Pass
                        "-",    // End
                        "-",    // Total
                        "-",    // Grade
                        "-",    // GPA
                        row[6]  // Eligibility Status
                });
            }
        }
    }

    private void openEligibilityEngineDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow instanceof Frame ? (Frame) parentWindow : null, "Final Examination Eligibility Engine", true);
        dialog.setSize(880, 520);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(UITheme.COLOR_BG);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Info Banner Card
        JPanel bannerCard = UITheme.createCardPanel();
        bannerCard.setLayout(new BorderLayout());
        bannerCard.setBackground(new Color(239, 246, 255)); // Soft blue
        bannerCard.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 1));

        JLabel lblBannerTitle = new JLabel("Examination Eligibility Decision Engine");
        lblBannerTitle.setFont(UITheme.FONT_SECTION);
        lblBannerTitle.setForeground(new Color(30, 64, 175));

        JLabel lblBannerText = new JLabel("<html><b>Criteria:</b> Continuous Assessment (CA) &ge; 40.0% <b>AND</b> Module Attendance &ge; 80.0%</html>");
        lblBannerText.setFont(UITheme.FONT_BODY);
        lblBannerText.setForeground(new Color(30, 58, 138));

        bannerCard.add(lblBannerTitle, BorderLayout.NORTH);
        bannerCard.add(lblBannerText, BorderLayout.SOUTH);
        content.add(bannerCard, BorderLayout.NORTH);

        // Table
        String[] columns = {"Course ID", "Course Title", "CA Mark", "CA Status", "Attendance %", "Attendance Status", "Final Exam Status"};
        DefaultTableModel dialogModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable dialogTable = new JTable(dialogModel);
        UITheme.styleTable(dialogTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        dialogTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        dialogTable.getColumnModel().getColumn(1).setPreferredWidth(240);
        dialogTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        dialogTable.getColumnModel().getColumn(3).setCellRenderer(new UITheme.StatusBadgeRenderer());
        dialogTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        dialogTable.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusBadgeRenderer());
        dialogTable.getColumnModel().getColumn(6).setCellRenderer(new UITheme.StatusBadgeRenderer());

        List<Object[]> engineData = undergraduateDAO.getDetailedEligibilityData(studentId);
        if (engineData != null && !engineData.isEmpty()) {
            for (Object[] row : engineData) {
                dialogModel.addRow(row);
            }
        } else {
            dialogModel.addRow(new Object[]{"N/A", "No records found", "0.0", "Not Eligible", "0.0%", "Not Eligible", "Not Eligible"});
        }

        JScrollPane scrollPane = new JScrollPane(dialogTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        content.add(scrollPane, BorderLayout.CENTER);

        // Bottom Close Button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomBar.setOpaque(false);
        JButton btnClose = UITheme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        bottomBar.add(btnClose);
        content.add(bottomBar, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }
}