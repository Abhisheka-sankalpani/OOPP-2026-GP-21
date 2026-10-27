package gui;

import dao.TimetableDAO;
import model.Timetable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UndergraduateTimetableForm extends JPanel {

    private String department;

    public UndergraduateTimetableForm(String department) {
        this.department = department;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Course Timetable - " + department + " Department", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Day", "Time Slot", "Course", "Session Type", "Venue"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        TimetableDAO dao = new TimetableDAO();
        List<Timetable> list = dao.getTimetableByDepartment(department);

        for (Timetable t : list) {
            tableModel.addRow(new Object[]{
                    t.getDayOfWeek(),
                    t.getTimeSlot(), 
                    t.getCourseId() + " - " + t.getCourseName(),
                    t.getSessionType(),
                    t.getVenueId()
            });
        }
    }
}
