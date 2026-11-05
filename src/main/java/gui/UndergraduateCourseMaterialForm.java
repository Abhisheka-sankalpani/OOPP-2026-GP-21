package gui;

import dao.UndergraduateCourseMaterialDAO;
import exception.DatabaseException;
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
    private UndergraduateCourseMaterialDAO courseMaterialDAO;

    private JTable tableMaterial;
    private DefaultTableModel modelMaterial;
    private JLabel lblMaterialSummary;
    private JComboBox<String> cmbCourseFilter;
    private boolean refreshingCourseFilter;

    public UndergraduateCourseMaterialForm(String studentId) {
        this.studentId = studentId;
        this.courseMaterialDAO = new UndergraduateCourseMaterialDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshMaterialList();
    }

    private void initUI() {
        // 1. Top Header & Action Controls
        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setOpaque(false);

        JButton btnOpenFile = UITheme.createPrimaryButton("Open Selected File");
        btnOpenFile.addActionListener(e -> openSelectedFile());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Materials");
        btnRefresh.addActionListener(e -> refreshMaterialList());

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
            if (!refreshingCourseFilter && cmbCourseFilter.getSelectedItem() != null) {
                try {
                    loadMaterialData(cmbCourseFilter.getSelectedItem().toString());
                } catch (DatabaseException ex) {
                    showMaterialError(ex);
                }
            }
        });

        filterBar.add(lblFilter);
        filterBar.add(cmbCourseFilter);
        card.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] colNames = {"Material ID", "Course ID", "Resource Title", "Document File Path",
                "Available From", "Available Until", "Uploaded Date"};
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
        tableMaterial.getColumnModel().getColumn(4).setPreferredWidth(120);
        tableMaterial.getColumnModel().getColumn(5).setPreferredWidth(120);
        tableMaterial.getColumnModel().getColumn(6).setPreferredWidth(150);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableMaterial.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableMaterial.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        for (int column = 4; column < 7; column++) {
            tableMaterial.getColumnModel().getColumn(column).setCellRenderer(centerRenderer);
        }

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

    private void refreshMaterialList() {
        String selectedCourse = cmbCourseFilter.getSelectedItem() == null
                ? "All Courses" : cmbCourseFilter.getSelectedItem().toString();
        try {
            populateCourseDropdown();
            if (!containsCourse(selectedCourse)) {
                selectedCourse = "All Courses";
            }
            refreshingCourseFilter = true;
            try {
                cmbCourseFilter.setSelectedItem(selectedCourse);
            } finally {
                refreshingCourseFilter = false;
            }
            loadMaterialData(selectedCourse);
        } catch (DatabaseException ex) {
            showMaterialError(ex);
        }
    }

    private void populateCourseDropdown() throws DatabaseException {
        refreshingCourseFilter = true;
        try {
            cmbCourseFilter.removeAllItems();
            cmbCourseFilter.addItem("All Courses");

            List<CourseMaterial> allMaterials = courseMaterialDAO.getAllMaterials();
            List<String> uniqueCourses = new ArrayList<>();
            for (CourseMaterial material : allMaterials) {
                if (!uniqueCourses.contains(material.getCourseId())) {
                    uniqueCourses.add(material.getCourseId());
                    cmbCourseFilter.addItem(material.getCourseId());
                }
            }
        } finally {
            refreshingCourseFilter = false;
        }
    }

    private boolean containsCourse(String courseId) {
        for (int i = 0; i < cmbCourseFilter.getItemCount(); i++) {
            if (cmbCourseFilter.getItemAt(i).equals(courseId)) {
                return true;
            }
        }
        return false;
    }

    private void loadMaterialData(String selectedCourse) throws DatabaseException {
        modelMaterial.setRowCount(0);
        List<CourseMaterial> list = courseMaterialDAO.getMaterialsByCourse(
                selectedCourse == null || selectedCourse.equals("All Courses") ? "ALL" : selectedCourse);

        for (CourseMaterial m : list) {
            Object[] row = {
                    m.getMaterialId(),
                    m.getCourseId(),
                    m.getTitle(),
                    m.getFilePath(),
                    m.getAvailableFrom(),
                    m.getAvailableTo(),
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

        int modelRow = tableMaterial.convertRowIndexToModel(selectedRow);
        int materialId = ((Number) modelMaterial.getValueAt(modelRow, 0)).intValue();
        try {
            if (!courseMaterialDAO.isMaterialAvailableNow(materialId)) {
                refreshMaterialList();
                JOptionPane.showMessageDialog(this,
                        "This material is outside its viewing period.",
                        "Material Unavailable", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DatabaseException ex) {
            showMaterialError(ex);
            return;
        }

        String filePath = (String) modelMaterial.getValueAt(modelRow, 3);
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

    private void showMaterialError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Course Materials Error", JOptionPane.ERROR_MESSAGE);
    }
}