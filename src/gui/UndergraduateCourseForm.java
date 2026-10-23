package gui;

import dao.CourseDAO;
import model.Course;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UndergraduateCourseForm extends JPanel {

    private String studentId;
    private CourseDAO courseDAO;

    private JTable tableCourse;
    private DefaultTableModel modelCourse;
    private JLabel lblCourseSummary;

    public UndergraduateCourseForm(String studentId) {
        this.studentId = studentId;
        this.courseDAO = new CourseDAO();

        setLayout(new BorderLayout(15, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        initUI();
        loadCourseData();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("My Registered Courses");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnRefreshCourse = new JButton("Refresh");
        btnRefreshCourse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadCourseData();
            }
        });
        topPanel.add(btnRefreshCourse, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        String[] colNames = {"Course ID", "Course Name", "Credits", "Type"};
        modelCourse = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableCourse = new JTable(modelCourse);
        tableCourse.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(tableCourse);
        add(scrollPane, BorderLayout.CENTER);

        lblCourseSummary = new JLabel("Total Credits: Calculating...");
        lblCourseSummary.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblCourseSummary, BorderLayout.SOUTH);
    }

    private void loadCourseData() {
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

        lblCourseSummary.setText("Total Registered Modules: " + list.size() + "  |  Total Credits: " + totalCredits);
    }
}