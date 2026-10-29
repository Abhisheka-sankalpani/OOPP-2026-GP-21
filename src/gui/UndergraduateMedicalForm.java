package gui;

import dao.MedicalDAO;
import model.Medical;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UndergraduateMedicalForm extends JPanel {

    private String studentId;
    private MedicalDAO medicalDAO;
    private JTable tableMedical;
    private DefaultTableModel modelMedical;

    private JTextField txtCourseId;
    private JTextField txtSessionNo;
    private JComboBox<String> cmbSessionType;
    private JTextField txtDate;
    private JTextField txtDescription;
    private JTextField txtImagePath;

    public UndergraduateMedicalForm(String studentId) {
        this.studentId = studentId;
        this.medicalDAO = new MedicalDAO();

        setLayout(new BorderLayout(15, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        initUI();
        loadMedicalData();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("My Medical Submissions");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnRefreshMed = new JButton("Refresh");
        btnRefreshMed.addActionListener(e -> loadMedicalData());
        topPanel.add(btnRefreshMed, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        String[] colNames = {"Course ID", "Session No", "Type", "Medical Date", "Status"};
        modelMedical = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableMedical = new JTable(modelMedical);
        tableMedical.setRowHeight(25);
        add(new JScrollPane(tableMedical), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Submit New Medical"));
        bottomPanel.setBackground(new Color(240, 248, 255));

        txtCourseId = new JTextField(4);
        txtSessionNo = new JTextField(2);
        cmbSessionType = new JComboBox<>(new String[]{"Theory", "Practical"});
        txtDate = new JTextField(6);
        txtDescription = new JTextField(8);

        txtImagePath = new JTextField(8);
        txtImagePath.setEditable(false);
        JButton btnBrowse = new JButton("Browse...");

        btnBrowse.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txtImagePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton btnSubmit = new JButton("Submit");
        btnSubmit.setBackground(new Color(0, 102, 204));
        btnSubmit.setForeground(Color.WHITE);

        bottomPanel.add(new JLabel("Course:"));
        bottomPanel.add(txtCourseId);
        bottomPanel.add(new JLabel("Session:"));
        bottomPanel.add(txtSessionNo);
        bottomPanel.add(new JLabel("Type:"));
        bottomPanel.add(cmbSessionType);
        bottomPanel.add(new JLabel("Date:"));
        bottomPanel.add(txtDate);
        bottomPanel.add(new JLabel("Desc:"));
        bottomPanel.add(txtDescription);
        bottomPanel.add(new JLabel("Doc:"));
        bottomPanel.add(txtImagePath);
        bottomPanel.add(btnBrowse);
        bottomPanel.add(btnSubmit);

        add(bottomPanel, BorderLayout.SOUTH);

        btnSubmit.addActionListener(e -> submitNewMedical());
    }

    private void submitNewMedical() {
        String courseId = txtCourseId.getText().trim();
        String sessionNoStr = txtSessionNo.getText().trim();
        String type = cmbSessionType.getSelectedItem().toString();
        String date = txtDate.getText().trim();
        String description = txtDescription.getText().trim();
        String imagePath = txtImagePath.getText().trim();

        if (courseId.isEmpty() || sessionNoStr.isEmpty() || date.isEmpty() || description.isEmpty() || imagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields and select document!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int sessionNo = Integer.parseInt(sessionNoStr);

            boolean isSuccess = medicalDAO.addMedical(studentId, courseId, sessionNo, type, date);

            if (isSuccess) {
                JOptionPane.showMessageDialog(this, "Medical submitted successfully!");
                loadMedicalData();

                txtCourseId.setText("");
                txtSessionNo.setText("");
                txtDate.setText("");
                txtDescription.setText("");
                txtImagePath.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit medical.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Session No must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMedicalData() {
        modelMedical.setRowCount(0);
        java.util.List<model.Medical> list = medicalDAO.getMedicalsByStudentId(studentId);

        for (model.Medical m : list) {
            String sessionDisplay = (m.getSessionNo() == 0) ? "To be assigned" : "Session " + m.getSessionNo();

            Object[] row = {
                    m.getCourseId(),
                    sessionDisplay,
                    m.getSessionType(),
                    m.getMedicalDate(),
                    m.getApprovalStatus()
            };
            modelMedical.addRow(row);
        }
    }
}