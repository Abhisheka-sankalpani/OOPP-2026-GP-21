package gui;

import dao.UndergraduateDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class UndergraduateEligibilityForm extends JPanel {
    private String studentId;
    private UndergraduateDAO undergraduateDAO;

    public UndergraduateEligibilityForm(String studentId) {
        this.studentId = studentId;
        this.undergraduateDAO = new UndergraduateDAO();
        setLayout(new BorderLayout(15, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        // 1. Top Header Panel (Title + Refresh & Check Eligibility Buttons)
        JPanel topPanel = new JPanel(new BorderLayout());


        JLabel lblTitle = new JLabel("My Grades & Exam Eligibility");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(lblTitle, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnCheckEligibility = new JButton("Check Full Eligibility");

        btnRefresh.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Grades & Eligibility reloaded successfully!");
        });

        // Check Full Eligibility Button Click Event (Opens the detailed dialog engine)
        btnCheckEligibility.addActionListener(event -> {
            JDialog eligibilityDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Final Examination Eligibility Engine", true);
            eligibilityDialog.setSize(800, 450);
            eligibilityDialog.setLocationRelativeTo(this);
            eligibilityDialog.setLayout(new BorderLayout(10, 10));

            // Top Criteria Panel
            JPanel topInfoPanel = new JPanel(new GridLayout(2, 1));
            topInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

            JLabel lblEngineTitle = new JLabel("Final Examination Eligibility Engine", SwingConstants.CENTER);
            lblEngineTitle.setFont(new Font("Arial", Font.BOLD, 16));
            lblEngineTitle.setForeground(new Color(0, 51, 102));

            JLabel lblCriteria = new JLabel("Criteria: Continuous Assessment (CA) >= 40% AND Attendance >= 80%", SwingConstants.CENTER);
            lblCriteria.setFont(new Font("Arial", Font.BOLD, 12));
            lblCriteria.setForeground(Color.RED);

            topInfoPanel.add(lblEngineTitle);
            topInfoPanel.add(lblCriteria);
            eligibilityDialog.add(topInfoPanel, BorderLayout.NORTH);

            // Detailed Table Setup
            String[] columns = {"Course ID", "Course Title", "CA Mark", "CA Status", "Attendance %", "Attendance Status", "Final Exam Status"};
            DefaultTableModel dialogTableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            JTable dialogTable = new JTable(dialogTableModel);
            dialogTable.setRowHeight(25);

            java.util.List<Object[]> engineData = undergraduateDAO.getDetailedEligibilityData(studentId);
            if (engineData != null && !engineData.isEmpty()) {
                for (Object[] row : engineData) {
                    dialogTableModel.addRow(row);
                }
            } else {
                dialogTableModel.addRow(new Object[]{"N/A", "No records found", "0.0", "Not Eligible", "0.0%", "Not Eligible", "Not Eligible"});
            }

            JScrollPane scrollPane = new JScrollPane(dialogTable);
            eligibilityDialog.add(scrollPane, BorderLayout.CENTER);

            eligibilityDialog.setVisible(true);
        });

        btnPanel.add(btnRefresh);
        btnPanel.add(btnCheckEligibility);
        topPanel.add(btnPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));

        JPanel summaryPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        String actualCGPA = undergraduateDAO.getStudentCGPA(studentId);

        JLabel lblSGPA = new JLabel("Your SGPA: " + actualCGPA);
        lblSGPA.setFont(new Font("Arial", Font.BOLD, 14));
        lblSGPA.setForeground(new Color(0, 100, 0));

        JLabel lblCGPA = new JLabel("Your CGPA: " + actualCGPA);
        lblCGPA.setFont(new Font("Arial", Font.BOLD, 14));
        lblCGPA.setForeground(new Color(0, 100, 0));

        summaryPanel.add(lblSGPA);
        summaryPanel.add(lblCGPA);
        centerContainer.add(summaryPanel, BorderLayout.NORTH);

        // Grades Table Setup
        String[] colNames = {"Course", "CA", "CA Pass", "End", "Total", "Grade", "GPA", "Eligibility Status"};
        DefaultTableModel modelCombined = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tableCombined = new JTable(modelCombined);
        tableCombined.setRowHeight(25);

        java.util.List<Object[]> gradesList = undergraduateDAO.getDetailedEligibilityData(studentId);
        if (gradesList != null && !gradesList.isEmpty()) {
            for (Object[] row : gradesList) {
                modelCombined.addRow(new Object[]{
                        row[0], // 1. Course ID
                        row[2], // 2. CA Marks
                        row[3], // 3. CA Pass (Eligible/Not Eligible)
                        "-",    // 4. End
                        "-",    // 5. Total
                        "-",    // 6. Grade
                        "-",    // 7. GPA
                        row[6]  // 8. Eligibility Status
                });
            }
        }

        JScrollPane scrollPane = new JScrollPane(tableCombined);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        // Footer Eligibility Note
        JLabel lblEligibilityNote = new JLabel("Status Note: To be eligible for final exams, minimum Continuous Assessment (CA) >= 40% and Attendance >= 80% are required.");
        lblEligibilityNote.setFont(new Font("Arial", Font.ITALIC, 12));
        lblEligibilityNote.setForeground(new Color(100, 100, 100));
        centerContainer.add(lblEligibilityNote, BorderLayout.SOUTH);

        add(centerContainer, BorderLayout.CENTER);
    }

}