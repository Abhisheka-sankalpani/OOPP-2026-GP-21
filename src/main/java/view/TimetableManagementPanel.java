package view;

import dao.CourseDAO;
import dao.TimetableDAO;

import model.Course;
import model.Timetable;

import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class TimetableManagementPanel extends JPanel {

    // DAO Objects
    private final TimetableDAO timetableDAO = new TimetableDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    // Store Timetable Objects
    private final Map<Integer, Timetable> timetables =
            new LinkedHashMap<>();

    // Time Format
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    // Form Fields
    private JComboBox<CourseOption> courseCombo;
    private JComboBox<String> sessionTypeCombo;
    private JComboBox<String> dayCombo;

    private JTextField departmentField;
    private JTextField venueField;
    private JTextField startTimeField;
    private JTextField endTimeField;

    // Search Fields
    private JTextField searchField;
    private JTextField departmentFilterField;
    private JComboBox<String> dayFilterCombo;

    // Table
    private JTable timetableTable;
    private DefaultTableModel tableModel;

    // Buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton conflictButton;

    // Status
    private JLabel statusLabel;

    // Selected Timetable ID
    private Integer selectedTimetableId = null;


    // =====================================
    // CONSTRUCTOR
    // =====================================

    public TimetableManagementPanel() {

        initComponents();

        loadCoursesIntoCombo();

        loadTimetable();
    }


    // =====================================
    // COURSE OPTION CLASS
    // =====================================

    private static class CourseOption {

        private final String courseId;
        private final String courseName;

        public CourseOption(String courseId, String courseName) {
            this.courseId = courseId;
            this.courseName = courseName;
        }

        public String getCourseId() {
            return courseId;
        }

        @Override
        public String toString() {
            return courseId + " - " + courseName;
        }
    }


    // =====================================
    // INITIALIZE GUI
    // =====================================

    private void initComponents() {

        setLayout(new BorderLayout(15, 15));

        setBackground(UIStyle.COLOR_BG);

        setBorder(new EmptyBorder(20, 20, 20, 20));


        // HEADER
        JLabel titleLabel = new JLabel(
                "Timetable Management"
        );

        titleLabel.setFont(UIStyle.FONT_TITLE);

        titleLabel.setForeground(UIStyle.COLOR_PRIMARY);

        add(titleLabel, BorderLayout.NORTH);


        // MAIN CONTENT
        JPanel contentPanel = new JPanel(
                new BorderLayout(15, 0)
        );

        contentPanel.setOpaque(false);


        // =====================================
        // LEFT SIDE - TIMETABLE FORM
        // =====================================

        JPanel formCard = new JPanel(
                new BorderLayout(0, 12)
        );

        formCard.setBackground(Color.WHITE);

        formCard.setBorder(UIStyle.createCardBorder());

        formCard.setPreferredSize(
                new Dimension(350, 550)
        );


        JLabel formTitle = new JLabel(
                "Timetable Details"
        );

        formTitle.setFont(UIStyle.FONT_SUBTITLE);

        formTitle.setForeground(UIStyle.COLOR_PRIMARY);

        formCard.add(formTitle, BorderLayout.NORTH);


        JPanel formBody = new JPanel();

        formBody.setLayout(
                new BoxLayout(formBody, BoxLayout.Y_AXIS)
        );

        formBody.setBackground(Color.WHITE);


        // COURSE
        courseCombo = new JComboBox<>();

        courseCombo.setFont(UIStyle.FONT_BODY);

        formBody.add(
                createFormField("Course *", courseCombo)
        );


        // DEPARTMENT
        departmentField = UIStyle.createTextField(20);

        departmentField.setText("ICT");

        formBody.add(
                createFormField("Department *", departmentField)
        );


        // SESSION TYPE
        sessionTypeCombo = new JComboBox<>(
                new String[]{
                        "THEORY",
                        "PRACTICAL"
                }
        );

        sessionTypeCombo.setFont(UIStyle.FONT_BODY);

        formBody.add(
                createFormField("Session Type *", sessionTypeCombo)
        );


        // DAY OF WEEK
        dayCombo = new JComboBox<>(
                new String[]{
                        "MONDAY",
                        "TUESDAY",
                        "WEDNESDAY",
                        "THURSDAY",
                        "FRIDAY"
                }
        );

        dayCombo.setFont(UIStyle.FONT_BODY);

        formBody.add(
                createFormField("Day of Week *", dayCombo)
        );


        // START TIME
        startTimeField = UIStyle.createTextField(20);

        startTimeField.setText("09:00");

        startTimeField.setToolTipText(
                "Time format: HH:mm (Example: 09:00)"
        );

        formBody.add(
                createFormField(
                        "Start Time (HH:mm) *",
                        startTimeField
                )
        );


        // END TIME
        endTimeField = UIStyle.createTextField(20);

        endTimeField.setText("11:00");

        endTimeField.setToolTipText(
                "Time format: HH:mm (Example: 11:00)"
        );

        formBody.add(
                createFormField(
                        "End Time (HH:mm) *",
                        endTimeField
                )
        );


        // VENUE
        venueField = UIStyle.createTextField(20);

        venueField.setToolTipText(
                "Example: LAB-12"
        );

        formBody.add(
                createFormField("Venue", venueField)
        );


        // FORM SCROLL
        JScrollPane formScroll = new JScrollPane(formBody);

        formScroll.setBorder(null);

        formScroll.getVerticalScrollBar()
                .setUnitIncrement(16);

        formCard.add(formScroll, BorderLayout.CENTER);


        // =====================================
        // FORM BUTTONS
        // =====================================

        JPanel buttonPanel = new JPanel(
                new GridLayout(3, 2, 8, 8)
        );


        // ADD
        addButton = UIStyle.createPrimaryButton(
                "Add Timetable"
        );

        addButton.addActionListener(
                e -> handleAddTimetable()
        );


        // UPDATE
        updateButton = UIStyle.createPrimaryButton(
                "Update"
        );

        updateButton.setEnabled(false);

        updateButton.addActionListener(
                e -> handleUpdateTimetable()
        );


        // DELETE
        deleteButton = UIStyle.createPrimaryButton(
                "Delete"
        );

        deleteButton.setBackground(
                new Color(220, 38, 38)
        );

        deleteButton.setEnabled(false);

        deleteButton.addActionListener(
                e -> handleDeleteTimetable()
        );


        // CHECK CONFLICT
        conflictButton = UIStyle.createSecondaryButton(
                "Check Conflict"
        );

        conflictButton.addActionListener(
                e -> handleCheckConflict()
        );


        // CLEAR
        clearButton = UIStyle.createSecondaryButton(
                "Clear"
        );

        clearButton.addActionListener(
                e -> clearForm()
        );


        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(conflictButton);
        buttonPanel.add(clearButton);

        formCard.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.add(formCard, BorderLayout.WEST);


        // =====================================
        // RIGHT SIDE - TIMETABLE TABLE
        // =====================================

        JPanel tableCard = new JPanel(
                new BorderLayout(0, 12)
        );

        tableCard.setBackground(Color.WHITE);

        tableCard.setBorder(UIStyle.createCardBorder());


        // SEARCH PANEL
        JPanel searchPanel = new JPanel(
                new GridLayout(2, 1, 5, 5)
        );

        searchPanel.setOpaque(false);


        // Keyword Search
        JPanel keywordPanel = new JPanel(
                new BorderLayout(8, 0)
        );

        keywordPanel.setOpaque(false);

        searchField = UIStyle.createTextField(15);

        searchField.setToolTipText(
                "Search Course ID, Course Name or Venue"
        );

        searchField.addActionListener(
                e -> loadTimetable()
        );

        JButton searchButton =
                UIStyle.createPrimaryButton("Search");

        searchButton.addActionListener(
                e -> loadTimetable()
        );

        keywordPanel.add(searchField, BorderLayout.CENTER);

        keywordPanel.add(searchButton, BorderLayout.EAST);

        searchPanel.add(keywordPanel);


        // Filters
        JPanel filterPanel = new JPanel(
                new GridLayout(1, 3, 6, 0)
        );

        filterPanel.setOpaque(false);


        dayFilterCombo = new JComboBox<>(
                new String[]{
                        "ALL",
                        "MONDAY",
                        "TUESDAY",
                        "WEDNESDAY",
                        "THURSDAY",
                        "FRIDAY"
                }
        );

        departmentFilterField = UIStyle.createTextField(10);

        departmentFilterField.setToolTipText(
                "Department Filter"
        );

        JButton refreshButton =
                UIStyle.createSecondaryButton("Refresh");

        refreshButton.addActionListener(e -> {

            searchField.setText("");

            departmentFilterField.setText("");

            dayFilterCombo.setSelectedItem("ALL");

            loadTimetable();
        });


        filterPanel.add(dayFilterCombo);

        filterPanel.add(departmentFilterField);

        filterPanel.add(refreshButton);

        searchPanel.add(filterPanel);

        tableCard.add(searchPanel, BorderLayout.NORTH);


        // =====================================
        // TIMETABLE TABLE
        // =====================================

        String[] columns = {
                "ID",
                "Course",
                "Department",
                "Type",
                "Day",
                "Start",
                "End",
                "Venue"
        };


        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };


        timetableTable = new JTable(tableModel);

        timetableTable.setFont(UIStyle.FONT_BODY);

        timetableTable.setRowHeight(30);

        timetableTable.getTableHeader().setFont(
                UIStyle.FONT_BODY_BOLD
        );

        timetableTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        timetableTable.setAutoCreateRowSorter(true);

        timetableTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF
        );


        // Column Widths
        int[] widths = {
                55, 100, 120, 100,
                120, 75, 75, 120
        };

        for (int i = 0; i < widths.length; i++) {

            timetableTable.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }


        // Select Timetable Entry
        timetableTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        populateSelectedTimetable();
                    }
                });


        tableCard.add(
                new JScrollPane(timetableTable),
                BorderLayout.CENTER
        );


        // STATUS LABEL
        statusLabel = new JLabel("Ready");

        statusLabel.setFont(UIStyle.FONT_SMALL);

        statusLabel.setForeground(
                UIStyle.COLOR_TEXT_MUTED
        );

        tableCard.add(statusLabel, BorderLayout.SOUTH);


        contentPanel.add(tableCard, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }


    // =====================================
    // CREATE FORM FIELD
    // =====================================

    private JPanel createFormField(
            String label,
            JComponent component) {

        JPanel panel = new JPanel(
                new BorderLayout(0, 5)
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(5, 0, 8, 0)
        );

        JLabel fieldLabel = new JLabel(label);

        fieldLabel.setFont(UIStyle.FONT_BODY_BOLD);

        panel.add(fieldLabel, BorderLayout.NORTH);

        panel.add(component, BorderLayout.CENTER);

        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 80)
        );

        return panel;
    }


    // =====================================
    // LOAD COURSES INTO COMBOBOX
    // =====================================

    public void loadCoursesIntoCombo() {

        SwingWorker<List<Course>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<Course> doInBackground()
                            throws Exception {

                        return courseDAO.getAllCourses();
                    }


                    @Override
                    protected void done() {

                        try {

                            List<Course> courses = get();

                            String previousId = null;

                            CourseOption current =
                                    (CourseOption) courseCombo.getSelectedItem();

                            if (current != null) {
                                previousId = current.getCourseId();
                            }

                            courseCombo.removeAllItems();

                            int selectedIndex = -1;

                            for (Course course : courses) {

                                CourseOption option = new CourseOption(
                                        course.getCourseId(),
                                        course.getCourseName()
                                );

                                courseCombo.addItem(option);

                                if (course.getCourseId().equals(previousId)) {
                                    selectedIndex = courseCombo.getItemCount() - 1;
                                }
                            }

                            courseCombo.setSelectedIndex(selectedIndex);

                        } catch (Exception ex) {

                            showError("Unable to load courses.");
                        }
                    }
                };

        worker.execute();
    }


    // =====================================
    // LOAD TIMETABLE
    // =====================================

    public void loadTimetable() {

        String keyword = searchField.getText().trim();

        String dayFilter =
                (String) dayFilterCombo.getSelectedItem();

        String departmentFilter =
                departmentFilterField.getText().trim();

        statusLabel.setText("Loading timetable...");


        SwingWorker<List<Timetable>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<Timetable> doInBackground()
                            throws Exception {

                        return timetableDAO.searchTimetable(
                                keyword,
                                dayFilter,
                                departmentFilter
                        );
                    }


                    @Override
                    protected void done() {

                        try {

                            List<Timetable> result = get();

                            clearForm();

                            timetables.clear();

                            tableModel.setRowCount(0);


                            for (Timetable timetable : result) {

                                timetables.put(
                                        timetable.getTimetableId(),
                                        timetable
                                );

                                tableModel.addRow(
                                        new Object[]{
                                                timetable.getTimetableId(),
                                                timetable.getCourseId(),
                                                timetable.getDepartment(),
                                                timetable.getSessionType(),
                                                timetable.getDayOfWeek(),
                                                formatTime(timetable.getStartTime()),
                                                formatTime(timetable.getEndTime()),
                                                timetable.getVenueId()
                                        }
                                );
                            }

                            statusLabel.setText(
                                    "Timetable entries: " + result.size()
                            );

                        } catch (Exception ex) {

                            showError("Unable to load timetable.");

                            statusLabel.setText("Failed to load timetable.");
                        }
                    }
                };

        worker.execute();
    }


    // =====================================
    // SELECT TIMETABLE ENTRY
    // =====================================

    private void populateSelectedTimetable() {

        int selectedRow = timetableTable.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }

        int modelRow =
                timetableTable.convertRowIndexToModel(selectedRow);

        int timetableId =
                (Integer) tableModel.getValueAt(modelRow, 0);

        Timetable timetable = timetables.get(timetableId);

        if (timetable == null) {
            return;
        }

        selectedTimetableId = timetable.getTimetableId();


        // Select Course
        selectCourseById(timetable.getCourseId());

        departmentField.setText(timetable.getDepartment());

        sessionTypeCombo.setSelectedItem(
                timetable.getSessionType()
        );

        dayCombo.setSelectedItem(
                timetable.getDayOfWeek()
        );

        startTimeField.setText(
                formatTime(timetable.getStartTime())
        );

        endTimeField.setText(
                formatTime(timetable.getEndTime())
        );

        venueField.setText(
                timetable.getVenueId() == null ?
                        "" : timetable.getVenueId()
        );


        addButton.setEnabled(false);

        updateButton.setEnabled(true);

        deleteButton.setEnabled(true);
    }


    // =====================================
    // SELECT COURSE BY ID
    // =====================================

    private void selectCourseById(String courseId) {

        for (int i = 0; i < courseCombo.getItemCount(); i++) {

            CourseOption option = courseCombo.getItemAt(i);

            if (option.getCourseId().equals(courseId)) {

                courseCombo.setSelectedIndex(i);

                return;
            }
        }

        courseCombo.setSelectedIndex(-1);
    }


    // =====================================
    // CONVERT TIME TO HH:mm
    // =====================================

    private String formatTime(Time time) {

        if (time == null) {
            return "";
        }

        return time.toLocalTime().format(TIME_FORMAT);
    }


    // =====================================
    // READ TIMETABLE FROM FORM
    // =====================================

    private Timetable getTimetableFromForm() {

        CourseOption selectedCourse =
                (CourseOption) courseCombo.getSelectedItem();

        if (selectedCourse == null) {

            throw new IllegalArgumentException(
                    "Please select a course."
            );
        }


        String department =
                departmentField.getText().trim();

        String sessionType =
                (String) sessionTypeCombo.getSelectedItem();

        String day =
                (String) dayCombo.getSelectedItem();

        String venue =
                venueField.getText().trim();


        // Validate Department
        if (department.isBlank() || department.length() > 50) {

            throw new IllegalArgumentException(
                    "Department must contain 1-50 characters."
            );
        }


        // Validate Venue
        if (venue.length() > 50) {

            throw new IllegalArgumentException(
                    "Venue cannot exceed 50 characters."
            );
        }


        // Parse Time
        LocalTime start;
        LocalTime end;

        try {

            start = LocalTime.parse(
                    startTimeField.getText().trim(),
                    TIME_FORMAT
            );

            end = LocalTime.parse(
                    endTimeField.getText().trim(),
                    TIME_FORMAT
            );

        } catch (DateTimeParseException ex) {

            throw new IllegalArgumentException(
                    "Invalid time. Use HH:mm format (Example: 09:00)."
            );
        }


        Timetable timetable = new Timetable(
                selectedCourse.getCourseId(),
                department,
                sessionType,
                day,
                Time.valueOf(start),
                Time.valueOf(end),
                venue.isEmpty() ? null : venue
        );


        // Keep ID for UPDATE or Conflict Checking
        if (selectedTimetableId != null) {

            timetable.setTimetableId(selectedTimetableId);
        }


        if (!timetable.isTimeWindowValid()) {

            throw new IllegalArgumentException(
                    "End Time must be later than Start Time."
            );
        }

        return timetable;
    }


    // =====================================
    // CHECK ADMIN PERMISSION
    // =====================================

    private void requireAdmin() {

        if (!SessionManager.getInstance().isAdmin()) {

            throw new SecurityException(
                    "Administrator access required."
            );
        }
    }


    // =====================================
    // ADD TIMETABLE
    // =====================================

    private void handleAddTimetable() {

        try {

            requireAdmin();

            if (selectedTimetableId != null) {

                showError("Clear the form before adding a new entry.");

                return;
            }

            Timetable timetable = getTimetableFromForm();

            executeChange(
                    () -> timetableDAO.addTimetableEntry(timetable),
                    "Timetable Added Successfully!"
            );

        } catch (RuntimeException ex) {

            showError(ex.getMessage());
        }
    }


    // =====================================
    // UPDATE TIMETABLE
    // =====================================

    private void handleUpdateTimetable() {

        if (selectedTimetableId == null) {

            showError("Please select a timetable entry first.");

            return;
        }

        try {

            requireAdmin();

            Timetable timetable = getTimetableFromForm();

            executeChange(
                    () -> timetableDAO.updateTimetableEntry(timetable),
                    "Timetable Updated Successfully!"
            );

        } catch (RuntimeException ex) {

            showError(ex.getMessage());
        }
    }


    // =====================================
    // DELETE TIMETABLE
    // =====================================

    private void handleDeleteTimetable() {

        if (selectedTimetableId == null) {

            showError("Please select an entry first.");

            return;
        }

        try {

            requireAdmin();

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete Timetable Entry ID: " +
                            selectedTimetableId + "?",
                    "Confirm Timetable Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            int timetableId = selectedTimetableId;

            executeChange(
                    () -> timetableDAO.deleteTimetableEntry(timetableId),
                    "Timetable Deleted Successfully!"
            );

        } catch (RuntimeException ex) {

            showError(ex.getMessage());
        }
    }


    // =====================================
    // CHECK SCHEDULING CONFLICT
    // =====================================

    private void handleCheckConflict() {

        try {

            requireAdmin();

            Timetable timetable = getTimetableFromForm();

            conflictButton.setEnabled(false);

            statusLabel.setText("Checking schedule...");

            SwingWorker<String, Void> worker =
                    new SwingWorker<>() {

                        @Override
                        protected String doInBackground()
                                throws Exception {

                            return timetableDAO
                                    .checkSchedulingConflict(timetable);
                        }

                        @Override
                        protected void done() {

                            try {

                                String conflict = get();

                                if (conflict == null) {

                                    JOptionPane.showMessageDialog(
                                            TimetableManagementPanel.this,
                                            "No Scheduling Conflict Found!",
                                            "Schedule Check",
                                            JOptionPane.INFORMATION_MESSAGE
                                    );

                                } else {

                                    JOptionPane.showMessageDialog(
                                            TimetableManagementPanel.this,
                                            conflict,
                                            "Scheduling Conflict",
                                            JOptionPane.WARNING_MESSAGE
                                    );
                                }

                            } catch (Exception ex) {

                                showError("Unable to check scheduling conflict.");

                            } finally {

                                conflictButton.setEnabled(true);

                                statusLabel.setText("Ready");
                            }
                        }
                    };

            worker.execute();

        } catch (RuntimeException ex) {

            showError(ex.getMessage());
        }
    }


    // =====================================
    // EXECUTE DATABASE CHANGES
    // =====================================

    private void executeChange(
            Callable<Boolean> operation,
            String successMessage) {

        setButtonsEnabled(false);

        statusLabel.setText("Saving changes...");


        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws Exception {

                        requireAdmin();

                        return operation.call();
                    }


                    @Override
                    protected void done() {

                        try {

                            boolean success = get();

                            if (success) {

                                JOptionPane.showMessageDialog(
                                        TimetableManagementPanel.this,
                                        successMessage
                                );

                                clearForm();

                                loadTimetable();

                            } else {

                                showError(
                                        "Operation failed or entry not found."
                                );
                            }

                        } catch (InterruptedException ex) {

                            Thread.currentThread().interrupt();

                            showError("Operation interrupted.");

                        } catch (ExecutionException ex) {

                            Throwable cause = ex.getCause();

                            showError(
                                    cause != null ?
                                            cause.getMessage() :
                                            "Database operation failed."
                            );

                        } finally {

                            setButtonsEnabled(true);
                        }
                    }
                };

        worker.execute();
    }


    // =====================================
    // ENABLE / DISABLE BUTTONS
    // =====================================

    private void setButtonsEnabled(boolean enabled) {

        addButton.setEnabled(
                enabled && selectedTimetableId == null
        );

        updateButton.setEnabled(
                enabled && selectedTimetableId != null
        );

        deleteButton.setEnabled(
                enabled && selectedTimetableId != null
        );

        clearButton.setEnabled(enabled);

        conflictButton.setEnabled(enabled);
    }


    // =====================================
    // CLEAR FORM
    // =====================================

    private void clearForm() {

        selectedTimetableId = null;

        courseCombo.setSelectedIndex(-1);

        departmentField.setText("ICT");

        sessionTypeCombo.setSelectedItem("THEORY");

        dayCombo.setSelectedItem("MONDAY");

        startTimeField.setText("09:00");

        endTimeField.setText("11:00");

        venueField.setText("");

        timetableTable.clearSelection();

        setButtonsEnabled(true);
    }


    // =====================================
    // SHOW ERROR
    // =====================================

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Timetable Management",
                JOptionPane.ERROR_MESSAGE
        );
    }
}