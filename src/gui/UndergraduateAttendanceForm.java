package gui;

import dao.AttendanceDAO;
import model.Attendance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UndergraduateAttendanceForm extends JFrame {

    private String studentId;
    private AttendanceDAO attendanceDAO;
    private JTable tableAttendance;
    private DefaultTableModel tableModel;

    public UndergraduateAttendanceForm(String studentId) {
        this.studentId = studentId;
        this.attendanceDAO = new AttendanceDAO();

        setTitle("Attendance Details - " + studentId);
        setSize(780, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadAttendanceData();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel lblTitle = new JLabel("My Course Attendance", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 60, 114));

        JLabel lblSubtitle = new JLabel("Student ID: " + studentId + "  |  Required Threshold: 80%", JLabel.CENTER);
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        lblSubtitle.setForeground(Color.GRAY);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {
            "Course ID", "Course Title", "Theory (Att/Tot)", "Practical (Att/Tot)", "Overall %", "Eligibility (>=80%)"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is read-only
            }
        };

        tableAttendance = new JTable(tableModel);
        tableAttendance.setRowHeight(26);
        tableAttendance.setFont(new Font("Arial", Font.PLAIN, 13));
        tableAttendance.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tableAttendance.getTableHeader().setBackground(new Color(230, 235, 245));

        JScrollPane scrollPane = new JScrollPane(tableAttendance);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Footer Panel
        JPanel footerPanel = new JPanel(new BorderLayout());

        JLabel lblNote = new JLabel("Note: Approved medicals are counted into the attended session count.");
        lblNote.setFont(new Font("Arial", Font.ITALIC, 12));
        lblNote.setForeground(new Color(100, 100, 100));

        JButton btnClose = new JButton("Close");
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        footerPanel.add(lblNote, BorderLayout.WEST);
        footerPanel.add(btnClose, BorderLayout.EAST);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadAttendanceData() {
        tableModel.setRowCount(0);
        List<Attendance> list = attendanceDAO.getAttendanceByStudentId(studentId);

        for (Attendance a : list) {
            String eligibility = (a.getOverallPercentage() >= 80.0) ? "Eligible" : "Not Eligible";

            Object[] row = {
                a.getCourseId(),
                a.getCourseName(),
                a.getTheoryRatio(),
                a.getPracticalRatio(),
                a.getOverallPercentage() + "%",
                eligibility
            };
            tableModel.addRow(row);
        }
    }
}
