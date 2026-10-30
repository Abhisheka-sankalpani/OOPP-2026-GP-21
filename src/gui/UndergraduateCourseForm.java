package gui;

import dao.CourseDAO;
import model.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UndergraduateCourseForm extends JPanel {

    private String studentId;
    private CourseDAO courseDAO;

    private JTable tableCourse;
    private DefaultTableModel modelCourse;
    private JLabel lblTotalModules;
    private JLabel lblTotalCredits;
    private JLabel lblCourseSummary;

    public UndergraduateCourseForm(String studentId) {
        this.studentId = studentId;
        this.courseDAO = new CourseDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadCourseData();
    }

    private void initUI() {
        // 1. Header
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Courses");
        btnRefresh.addActionListener(e -> loadCourseData());

        JPanel header = UITheme.createTabHeader(
                "My Registered Courses",
                "Official list of enrolled academic courses for the active semester.",
                btnRefresh
        );
        add(header, BorderLayout.NORTH);

        // 2. Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        // Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        statsRow.setOpaque(false);

        JPanel cardModules = UITheme.createStatCard("Registered Modules", "0", "Enrolled syllabus courses", UITheme.ACCENT_BLUE);
        JPanel cardCredits = UITheme.createStatCard("Academic Credits", "0", "Cumulative semester credits", UITheme.ACCENT_TEAL);

        lblTotalModules = findValueLabel(cardModules);
        lblTotalCredits = findValueLabel(cardCredits);

        statsRow.add(cardModules);
        statsRow.add(cardCredits);
        centerPanel.add(statsRow, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        JLabel lblTableTitle = new JLabel("Curriculum Module Details");
        lblTableTitle.setFont(UITheme.FONT_SECTION);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] colNames = {"Course ID", "Course Name", "Credits", "Course Type"};
        modelCourse = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableCourse = new JTable(modelCourse);
        UITheme.styleTable(tableCourse);

        tableCourse.getColumnModel().getColumn(0).setPreferredWidth(120);
        tableCourse.getColumnModel().getColumn(1).setPreferredWidth(450);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableCourse.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableCourse.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(tableCourse);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Footer Summary
        lblCourseSummary = new JLabel("Total Registered Modules: 0  |  Total Credits: 0");
        lblCourseSummary.setFont(UITheme.FONT_BODY_BOLD);
        lblCourseSummary.setForeground(UITheme.COLOR_TEXT_MAIN);
        tableCard.add(lblCourseSummary, BorderLayout.SOUTH);

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

    public void loadCourseData() {
        modelCourse.setRowCount(0);
        List<Course> list = courseDAO.getCoursesByStudent(studentId);

        int totalCredits = 0;
        for (Course c : list) {
            totalCredits += c.getCredit();

            Object[] row = {
                    c.getCourseId(),
                    c.getCourseName(),
                    c.getCredit(),
                    c.getType()
            };
            modelCourse.addRow(row);
        }

        if (lblTotalModules != null) lblTotalModules.setText(String.valueOf(list.size()));
        if (lblTotalCredits != null) lblTotalCredits.setText(String.valueOf(totalCredits));

        if (lblCourseSummary != null) {
            lblCourseSummary.setText("Total Registered Modules: " + list.size() + "  |  Total Academic Credits: " + totalCredits);
        }
    }
}