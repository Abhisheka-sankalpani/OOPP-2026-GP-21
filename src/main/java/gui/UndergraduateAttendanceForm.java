package gui;

import javax.swing.*;

public class UndergraduateAttendanceForm extends JFrame {

    private String studentId;
    private UndergraduateAttendancePanel attendancePanel;

    public UndergraduateAttendanceForm(String studentId) {
        this.studentId = studentId;

        setTitle("My Course Attendance - " + studentId);
        setSize(960, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        attendancePanel = new UndergraduateAttendancePanel(studentId);
        setContentPane(attendancePanel);
    }
}
