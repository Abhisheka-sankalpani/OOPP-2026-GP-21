package gui;

import dao.TimetableDAO;
import model.Timetable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UndergraduateTimetableForm extends JPanel {

    private String department;
    private TimetableDAO timetableDAO;
    private JTable tableTimetable;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbDepartment;
    private JLabel lblSummary;

    public UndergraduateTimetableForm(String department) {
        this.department = (department != null && !department.isEmpty()) ? department : "ICT";
        this.timetableDAO = new TimetableDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadTimetableData();
    }

    private void initUI() {
        // 1. Header Bar with Department Filter & Refresh
        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setOpaque(false);

        JLabel lblDept = new JLabel("Department:");
        lblDept.setFont(UITheme.FONT_BODY_BOLD);
        lblDept.setForeground(UITheme.COLOR_TEXT_MAIN);

        cmbDepartment = new JComboBox<>(new String[]{"ICT", "BST", "ET"});
        cmbDepartment.setSelectedItem(department);
        cmbDepartment.setFont(UITheme.FONT_BODY);
        cmbDepartment.setPreferredSize(new Dimension(100, 32));
        cmbDepartment.addActionListener(e -> {
            if (cmbDepartment.getSelectedItem() != null) {
                department = cmbDepartment.getSelectedItem().toString();
                loadTimetableData();
            }
        });

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Schedule");
        btnRefresh.addActionListener(e -> loadTimetableData());

        topActions.add(lblDept);
        topActions.add(cmbDepartment);
        topActions.add(btnRefresh);

        JPanel header = UITheme.createTabHeader(
                "Course Timetable Schedule",
                "Weekly lecture timetable, laboratory practicals, and tutorial sessions.",
                topActions
        );
        add(header, BorderLayout.NORTH);

        // 2. Table Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel lblCardTitle = new JLabel("Class Schedule Matrix");
        lblCardTitle.setFont(UITheme.FONT_SECTION);
        lblCardTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        card.add(lblCardTitle, BorderLayout.NORTH);

        String[] columns = {"Day of Week", "Time Slot", "Course Module", "Session Type", "Assigned Venue"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tableTimetable = new JTable(tableModel);
        UITheme.styleTable(tableTimetable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        tableTimetable.getColumnModel().getColumn(0).setPreferredWidth(120);
        tableTimetable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableTimetable.getColumnModel().getColumn(1).setPreferredWidth(140);
        tableTimetable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableTimetable.getColumnModel().getColumn(2).setPreferredWidth(320);
        tableTimetable.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableTimetable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableTimetable.getColumnModel().getColumn(4).setPreferredWidth(140);
        tableTimetable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(tableTimetable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        // Footer Summary
        lblSummary = new JLabel("Total Scheduled Sessions: Calculating...");
        lblSummary.setFont(UITheme.FONT_BODY_BOLD);
        lblSummary.setForeground(UITheme.COLOR_TEXT_MAIN);
        card.add(lblSummary, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    public void loadTimetableData() {
        tableModel.setRowCount(0);
        List<Timetable> list = timetableDAO.getTimetableByDepartment(department);

        for (Timetable t : list) {
            tableModel.addRow(new Object[]{
                    t.getDayOfWeek(),
                    t.getTimeSlot(),
                    t.getCourseId() + " - " + t.getCourseName(),
                    t.getSessionType(),
                    t.getVenueId()
            });
        }

        if (lblSummary != null) {
            lblSummary.setText("Department: " + department + "  |  Total Scheduled Sessions: " + list.size());
        }
    }
}
