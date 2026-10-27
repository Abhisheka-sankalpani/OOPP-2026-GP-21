package gui;

import dao.AttendanceDAO;
import dao.MarksDAO;
import model.Attendance;
import model.EligibilityResult;
import model.Marks;
import service.EligibilityService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UndergraduateEligibilityForm extends JPanel {

    private String studentId;
    private DefaultTableModel tableModel;

    public UndergraduateEligibilityForm(String studentId) {
        this.studentId = studentId;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Eligibility Status (CA >= 40% and Attendance >= 80%)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Course ID", "Course Title", "CA Mark", "CA Status", "Attendance %", "Attendance Status", "Final Status"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadEligibilityData();
    }

    private void loadEligibilityData() {
        EligibilityService service = new EligibilityService();
        List<Attendance> attList = new AttendanceDAO().getAttendanceByStudentId(studentId);
        List<Marks> marksList = new MarksDAO().getMarksByStudentId(studentId);

        for (Attendance att : attList) {
            double caMark = 0.0;

            for (Marks m : marksList) {
                if (m.getCourseId().equalsIgnoreCase(att.getCourseId())) {
                    caMark = m.getCaTotal();
                    break;
                }
            }

            EligibilityResult res = service.checkEligibility(att.getCourseId(), caMark, att.getOverallPercentage());

            tableModel.addRow(new Object[]{
                    att.getCourseId(),
                    att.getCourseName(),
                    res.getCaMark(),
                    res.getCaStatus(),
                    res.getAttendancePercentage() + "%",
                    res.getAttendanceStatus(),
                    res.getFinalStatus()
            });
        }
    }
}