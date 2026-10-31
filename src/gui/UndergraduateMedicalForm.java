package gui;

import dao.MedicalDAO;
import model.Medical;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
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

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadMedicalData();
    }

    private void initUI() {
        // 1. Header
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Submissions");
        btnRefresh.addActionListener(e -> loadMedicalData());

        JPanel header = UITheme.createTabHeader(
                "My Medical Submissions",
                "Submit and track medical certificates for missed lectures and laboratory practicals.",
                btnRefresh
        );
        add(header, BorderLayout.NORTH);

        // 2. Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        // Top Card: Table of Submissions
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel lblTableTitle = new JLabel("Submitted Medical Records");
        lblTableTitle.setFont(UITheme.FONT_SECTION);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] colNames = {"Course ID", "Session Number", "Session Type", "Medical Date", "Approval Status"};
        modelMedical = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableMedical = new JTable(modelMedical);
        UITheme.styleTable(tableMedical);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        tableMedical.getColumnModel().getColumn(0).setPreferredWidth(120);
        tableMedical.getColumnModel().getColumn(1).setPreferredWidth(140);
        tableMedical.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableMedical.getColumnModel().getColumn(2).setPreferredWidth(140);
        tableMedical.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableMedical.getColumnModel().getColumn(3).setPreferredWidth(140);
        tableMedical.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableMedical.getColumnModel().getColumn(4).setPreferredWidth(140);
        tableMedical.getColumnModel().getColumn(4).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(tableMedical);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, 180));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);

        // Submit New Medical Form
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 12));

        JLabel lblFormTitle = new JLabel("Submit New Medical Certificate");
        lblFormTitle.setFont(UITheme.FONT_SECTION);
        lblFormTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(3, 4, 12, 10));
        formGrid.setOpaque(false);

        // Row 1
        txtCourseId = new JTextField();
        txtSessionNo = new JTextField();
        cmbSessionType = new JComboBox<>(new String[]{"Theory", "Practical"});
        txtDate = new JTextField(java.time.LocalDate.now().toString());

        // Row 2
        txtDescription = new JTextField();
        txtImagePath = new JTextField();
        txtImagePath.setEditable(false);
        JButton btnBrowse = UITheme.createSecondaryButton("Browse File...");
        btnBrowse.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txtImagePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton btnSubmit = UITheme.createPrimaryButton("+ Submit Medical");
        btnSubmit.addActionListener(e -> submitNewMedical());

        // Adding to Grid
        formGrid.add(createFormField("Course ID (e.g. ICT2133):", txtCourseId));
        formGrid.add(createFormField("Session No:", txtSessionNo));
        formGrid.add(createFormField("Session Type:", cmbSessionType));
        formGrid.add(createFormField("Medical Date (YYYY-MM-DD):", txtDate));

        formGrid.add(createFormField("Reason / Description:", txtDescription));

        JPanel docPanel = new JPanel(new BorderLayout(6, 0));
        docPanel.setOpaque(false);
        docPanel.add(txtImagePath, BorderLayout.CENTER);
        docPanel.add(btnBrowse, BorderLayout.EAST);
        formGrid.add(createFormField("Medical Document Attachment:", docPanel));

        // Submit button panel
        JPanel submitContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 18));
        submitContainer.setOpaque(false);
        submitContainer.add(btnSubmit);
        formGrid.add(new JLabel(""));
        formGrid.add(submitContainer);

        formCard.add(formGrid, BorderLayout.CENTER);
        centerPanel.add(formCard, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createFormField(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.COLOR_TEXT_MAIN);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void submitNewMedical() {
        String courseId = txtCourseId.getText().trim();
        String sessionNoStr = txtSessionNo.getText().trim();
        String type = cmbSessionType.getSelectedItem().toString();
        String date = txtDate.getText().trim();
        String description = txtDescription.getText().trim();
        String imagePath = txtImagePath.getText().trim();

        if (courseId.isEmpty() || sessionNoStr.isEmpty() || date.isEmpty() || description.isEmpty() || imagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields and select the medical document!", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int sessionNo = Integer.parseInt(sessionNoStr);

            boolean isSuccess = medicalDAO.addMedical(studentId, courseId, sessionNo, type, date);

            if (isSuccess) {
                JOptionPane.showMessageDialog(this, "Medical certificate submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMedicalData();

                txtCourseId.setText("");
                txtSessionNo.setText("");
                txtDescription.setText("");
                txtImagePath.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit medical certificate. Please verify details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Session number must be a valid numeric digit!", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadMedicalData() {
        modelMedical.setRowCount(0);
        List<Medical> list = medicalDAO.getMedicalsByStudentId(studentId);

        for (Medical m : list) {
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