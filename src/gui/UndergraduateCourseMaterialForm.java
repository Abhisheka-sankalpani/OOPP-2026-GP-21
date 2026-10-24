package gui;

import dao.CourseMaterialDAO;
import model.CourseMaterial;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UndergraduateCourseMaterialForm extends JPanel {

    private String studentId;
    private CourseMaterialDAO courseMaterialDAO;

    private JTable tableMaterial;
    private DefaultTableModel modelMaterial;
    private JLabel lblMaterialSummary;
    private JComboBox<String> cmbCourseFilter;

    public UndergraduateCourseMaterialForm(String studentId) {
        this.studentId = studentId;
        this.courseMaterialDAO = new CourseMaterialDAO();

        setLayout(new BorderLayout(15, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        initUI();
        populateCourseDropdown();
        loadMaterialData("All Courses");
    }

    private void initUI() {
        //Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());

        // Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblFilter = new JLabel("Filter by Course: ");
        lblFilter.setFont(new Font("Arial", Font.BOLD, 14));
        cmbCourseFilter = new JComboBox<>();
        cmbCourseFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cmbCourseFilter.getSelectedItem() != null) {
                    loadMaterialData(cmbCourseFilter.getSelectedItem().toString());
                }
            }
        });
        filterPanel.add(lblFilter);
        filterPanel.add(cmbCourseFilter);
        topPanel.add(filterPanel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnOpenFile = new JButton("Open Selected File");
        btnOpenFile.setBackground(new Color(41, 128, 185));
        btnOpenFile.setForeground(Color.WHITE);
        btnOpenFile.addActionListener(e -> openSelectedFile());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> {
            populateCourseDropdown();
            loadMaterialData("All Courses");
        });

        btnPanel.add(btnOpenFile);
        btnPanel.add(btnRefresh);
        topPanel.add(btnPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        //Table Panel
        String[] colNames = {"Material ID", "Course ID", "Title", "File Path", "Uploaded Date"};
        modelMaterial = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableMaterial = new JTable(modelMaterial);
        tableMaterial.setRowHeight(25);
        tableMaterial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tableMaterial);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        lblMaterialSummary = new JLabel("Total Materials: Calculating...");
        lblMaterialSummary.setFont(new Font("Arial", Font.BOLD, 12));
        add(lblMaterialSummary, BorderLayout.SOUTH);
    }

    private void populateCourseDropdown() {
        cmbCourseFilter.removeAllItems();
        cmbCourseFilter.addItem("All Courses");

        List<CourseMaterial> allMaterials = courseMaterialDAO.getAllMaterials();
        List<String> uniqueCourses = new ArrayList<>();

        for (CourseMaterial m : allMaterials) {
            if (!uniqueCourses.contains(m.getCourseId())) {
                uniqueCourses.add(m.getCourseId());
                cmbCourseFilter.addItem(m.getCourseId());
            }
        }
    }

    private void loadMaterialData(String selectedCourse) {
        modelMaterial.setRowCount(0);
        List<CourseMaterial> list;

        if (selectedCourse.equals("All Courses")) {
            list = courseMaterialDAO.getAllMaterials();
        } else {
            list = courseMaterialDAO.getMaterialsByCourse(selectedCourse);
        }

        for (CourseMaterial m : list) {
            Object[] row = {
                    m.getMaterialId(),
                    m.getCourseId(),
                    m.getTitle(),
                    m.getFilePath(),
                    m.getUploadedDate()
            };
            modelMaterial.addRow(row);
        }
        lblMaterialSummary.setText("Total Available Materials: " + list.size());
    }

    private void openSelectedFile() {
        int selectedRow = tableMaterial.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a material from the table first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String filePath = (String) modelMaterial.getValueAt(selectedRow, 3);
        if (filePath == null || filePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No file path available for this material.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            File file = new File(filePath);
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(this, "Desktop operations are not supported on this OS.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "File not found at:\n" + filePath, "File Missing", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open the file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}