package gui;

import dao.CourseMaterialDAO;
import model.CourseMaterial;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        populateCourseDropdown();
        loadMaterialData("All Courses");
    }

    private void initUI() {
        // 1. Top Header & Action Controls
        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setOpaque(false);

        JButton btnOpenFile = UITheme.createPrimaryButton("Open Selected File");
        btnOpenFile.addActionListener(e -> openSelectedFile());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Materials");
        btnRefresh.addActionListener(e -> {
            populateCourseDropdown();
            loadMaterialData(cmbCourseFilter.getSelectedItem() != null ? cmbCourseFilter.getSelectedItem().toString() : "All Courses");
        });

        topActions.add(btnOpenFile);
        topActions.add(btnRefresh);

        JPanel header = UITheme.createTabHeader(
                "Course Materials & Learning Resources",
                "Download and access lecture slides, assignments, and laboratory guides.",
                topActions
        );
        add(header, BorderLayout.NORTH);

        // 2. Table Card with Filter Bar
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 14));

        // Filter Bar inside Card
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter by Course:");
        lblFilter.setFont(UITheme.FONT_BODY_BOLD);
        lblFilter.setForeground(UITheme.COLOR_TEXT_MAIN);

        cmbCourseFilter = new JComboBox<>();
        cmbCourseFilter.setFont(UITheme.FONT_BODY);
        cmbCourseFilter.setPreferredSize(new Dimension(200, 32));
        cmbCourseFilter.addActionListener(e -> {
            if (cmbCourseFilter.getSelectedItem() != null) {
                loadMaterialData(cmbCourseFilter.getSelectedItem().toString());
            }
        });

        filterBar.add(lblFilter);
        filterBar.add(cmbCourseFilter);
        card.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] colNames = {"Material ID", "Course ID", "Resource Title", "Document File Path", "Uploaded Date"};
        modelMaterial = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableMaterial = new JTable(modelMaterial);
        UITheme.styleTable(tableMaterial);
        tableMaterial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableMaterial.getColumnModel().getColumn(0).setPreferredWidth(90);
        tableMaterial.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableMaterial.getColumnModel().getColumn(2).setPreferredWidth(260);
        tableMaterial.getColumnModel().getColumn(3).setPreferredWidth(340);
        tableMaterial.getColumnModel().getColumn(4).setPreferredWidth(150);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableMaterial.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableMaterial.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableMaterial.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(tableMaterial);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        // Footer Summary
        lblMaterialSummary = new JLabel("Total Available Materials: Calculating...");
        lblMaterialSummary.setFont(UITheme.FONT_BODY_BOLD);
        lblMaterialSummary.setForeground(UITheme.COLOR_TEXT_MAIN);
        card.add(lblMaterialSummary, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
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

        if (selectedCourse == null || selectedCourse.equals("All Courses")) {
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
            JOptionPane.showMessageDialog(this, "Please select a material from the table first.", "Select Material", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Error opening file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}