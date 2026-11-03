package view;

import dao.CourseDAO;
import model.Course;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseManagementPanel extends JPanel {

    // DAO
    private final CourseDAO courseDAO = new CourseDAO();

    // Form Fields
    private JTextField courseIdField;
    private JTextField courseNameField;

    private JSpinner creditSpinner;
    private JSpinner theoryCreditSpinner;
    private JSpinner practicalCreditSpinner;

    private JComboBox<String> typeCombo;

    private JCheckBox hasTheoryCheck;
    private JCheckBox hasPracticalCheck;

    // Search
    private JTextField searchField;

    // Table
    private JTable courseTable;
    private DefaultTableModel tableModel;

    private final List<Course> displayedCourses =
            new ArrayList<>();

    // Buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    // Currently Selected Course
    private String selectedCourseId;

    // Constructor
    public CourseManagementPanel() {
        initComponents();
        loadCourses();
    }


    // ======================================
    // INITIALIZE COMPONENTS
    // ======================================

    private void initComponents() {

        setLayout(new BorderLayout(15, 15));

        setBackground(UIStyle.COLOR_BG);

        setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );


        // HEADER
        JLabel titleLabel = new JLabel(
                "Course & Curriculum Management"
        );

        titleLabel.setFont(UIStyle.FONT_TITLE);

        titleLabel.setForeground(UIStyle.COLOR_PRIMARY);

        add(titleLabel, BorderLayout.NORTH);


        // MAIN CONTENT
        JPanel contentPanel = new JPanel(
                new BorderLayout(15, 0)
        );

        contentPanel.setOpaque(false);


        // ======================================
        // LEFT FORM PANEL
        // ======================================

        JPanel formCard = new JPanel(
                new BorderLayout(0, 12)
        );

        formCard.setBackground(Color.WHITE);

        formCard.setBorder(
                UIStyle.createCardBorder()
        );

        formCard.setPreferredSize(
                new Dimension(350, 500)
        );


        JLabel formTitle = new JLabel("Course Details");

        formTitle.setFont(UIStyle.FONT_SUBTITLE);

        formTitle.setForeground(UIStyle.COLOR_PRIMARY);

        formCard.add(formTitle, BorderLayout.NORTH);


        // Form Body
        JPanel formBody = new JPanel();

        formBody.setLayout(
                new BoxLayout(formBody, BoxLayout.Y_AXIS)
        );

        formBody.setBackground(Color.WHITE);


        // Course ID
        courseIdField = UIStyle.createTextField(15);

        formBody.add(
                createFormField("Course ID *", courseIdField)
        );


        // Course Name
        courseNameField = UIStyle.createTextField(20);

        formBody.add(
                createFormField("Course Name *", courseNameField)
        );


        // Total Credits
        creditSpinner = new JSpinner(
                new SpinnerNumberModel(2, 1, 30, 1)
        );

        formBody.add(
                createFormField("Total Credits", creditSpinner)
        );


        // Course Type
        String[] courseTypes = {
                "Core",
                "Optional",
                "Compulsory"
        };

        typeCombo = new JComboBox<>(courseTypes);

        formBody.add(
                createFormField("Course Type", typeCombo)
        );


        // Theory Credits
        theoryCreditSpinner = new JSpinner(
                new SpinnerNumberModel(1, 0, 30, 1)
        );

        formBody.add(
                createFormField(
                        "Theory Credits",
                        theoryCreditSpinner
                )
        );


        // Practical Credits
        practicalCreditSpinner = new JSpinner(
                new SpinnerNumberModel(1, 0, 30, 1)
        );

        formBody.add(
                createFormField(
                        "Practical Credits",
                        practicalCreditSpinner
                )
        );


        // Checkboxes
        hasTheoryCheck = new JCheckBox(
                "Has Theory Component",
                true
        );

        hasTheoryCheck.setBackground(Color.WHITE);

        hasTheoryCheck.addActionListener(e -> {

            theoryCreditSpinner.setEnabled(
                    hasTheoryCheck.isSelected()
            );
        });


        hasPracticalCheck = new JCheckBox(
                "Has Practical Component",
                true
        );

        hasPracticalCheck.setBackground(Color.WHITE);

        hasPracticalCheck.addActionListener(e -> {

            practicalCreditSpinner.setEnabled(
                    hasPracticalCheck.isSelected()
            );
        });


        formBody.add(hasTheoryCheck);
        formBody.add(hasPracticalCheck);


        JScrollPane formScroll = new JScrollPane(formBody);

        formScroll.setBorder(null);

        formCard.add(formScroll, BorderLayout.CENTER);


        // ======================================
        // BUTTONS
        // ======================================

        JPanel buttonPanel = new JPanel(
                new GridLayout(2, 2, 8, 8)
        );


        addButton = UIStyle.createPrimaryButton(
                "Add Course"
        );

        addButton.addActionListener(e -> handleAddCourse());


        updateButton = UIStyle.createPrimaryButton(
                "Update"
        );

        updateButton.setEnabled(false);

        updateButton.addActionListener(
                e -> handleUpdateCourse()
        );


        deleteButton = UIStyle.createPrimaryButton(
                "Delete"
        );

        deleteButton.setBackground(
                new Color(220, 38, 38)
        );

        deleteButton.setEnabled(false);

        deleteButton.addActionListener(
                e -> handleDeleteCourse()
        );


        clearButton = UIStyle.createSecondaryButton(
                "Clear"
        );

        clearButton.addActionListener(e -> clearForm());


        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);


        formCard.add(buttonPanel, BorderLayout.SOUTH);

        contentPanel.add(formCard, BorderLayout.WEST);


        // ======================================
        // RIGHT TABLE PANEL
        // ======================================

        JPanel tableCard = new JPanel(
                new BorderLayout(0, 12)
        );

        tableCard.setBackground(Color.WHITE);

        tableCard.setBorder(
                UIStyle.createCardBorder()
        );


        // Search Panel
        JPanel searchPanel = new JPanel(
                new BorderLayout(8, 0)
        );

        searchPanel.setOpaque(false);


        searchField = UIStyle.createTextField(18);

        searchField.addActionListener(
                e -> loadCourses()
        );


        JButton searchButton =
                UIStyle.createPrimaryButton("Search");

        searchButton.addActionListener(
                e -> loadCourses()
        );


        JButton refreshButton =
                UIStyle.createSecondaryButton("Refresh");

        refreshButton.addActionListener(e -> {

            searchField.setText("");

            loadCourses();
        });


        JPanel searchButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 5, 0)
        );

        searchButtons.setOpaque(false);

        searchButtons.add(searchButton);
        searchButtons.add(refreshButton);


        searchPanel.add(
                searchField,
                BorderLayout.CENTER
        );

        searchPanel.add(
                searchButtons,
                BorderLayout.EAST
        );

        tableCard.add(searchPanel, BorderLayout.NORTH);


        // ======================================
        // TABLE
        // ======================================

        String[] columns = {

                "Course ID",
                "Course Name",
                "Credits",
                "Type",
                "Theory",
                "Practical",
                "Components"
        };


        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };


        courseTable = new JTable(tableModel);

        courseTable.setRowHeight(30);

        courseTable.setFont(UIStyle.FONT_BODY);

        courseTable.getTableHeader().setFont(
                UIStyle.FONT_BODY_BOLD
        );

        courseTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );


        // Select Course From Table
        courseTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        populateSelectedCourse();
                    }
                });


        JScrollPane tableScroll =
                new JScrollPane(courseTable);

        tableCard.add(
                tableScroll,
                BorderLayout.CENTER
        );


        contentPanel.add(
                tableCard,
                BorderLayout.CENTER
        );

        add(contentPanel, BorderLayout.CENTER);
    }


    // ======================================
    // CREATE FORM FIELD
    // ======================================

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
                new Dimension(Integer.MAX_VALUE, 75)
        );

        return panel;
    }


    // ======================================
    // LOAD COURSES
    // ======================================

    public void loadCourses() {

        String keyword = searchField.getText().trim();

        SwingWorker<List<Course>, Void> worker =
                new SwingWorker<List<Course>, Void>() {

                    @Override
                    protected List<Course> doInBackground()
                            throws Exception {

                        return courseDAO.searchCourses(keyword);
                    }


                    @Override
                    protected void done() {

                        try {

                            List<Course> courses = get();

                            displayedCourses.clear();
                            displayedCourses.addAll(courses);

                            tableModel.setRowCount(0);

                            for (Course course : courses) {

                                String components = "";

                                if (course.isHasTheory()) {
                                    components += "Theory ";
                                }

                                if (course.isHasPractical()) {
                                    components += "Practical";
                                }

                                tableModel.addRow(new Object[]{

                                        course.getCourseId(),
                                        course.getCourseName(),
                                        course.getCredit(),
                                        course.getType(),
                                        course.getTheoryCredits(),
                                        course.getPracticalCredits(),
                                        components.trim()
                                });
                            }

                        } catch (Exception ex) {

                            showError(
                                    "Failed to load courses."
                            );
                        }
                    }
                };

        worker.execute();
    }


    // ======================================
    // POPULATE SELECTED COURSE
    // ======================================

    private void populateSelectedCourse() {

        int row = courseTable.getSelectedRow();

        if (row < 0 || row >= displayedCourses.size()) {
            return;
        }

        Course course = displayedCourses.get(row);

        selectedCourseId = course.getCourseId();

        courseIdField.setText(course.getCourseId());

        courseIdField.setEditable(false);

        courseIdField.setBackground(
                new Color(241, 245, 249)
        );

        courseNameField.setText(course.getCourseName());

        creditSpinner.setValue(course.getCredit());

        typeCombo.setSelectedItem(course.getType());

        theoryCreditSpinner.setValue(
                course.getTheoryCredits()
        );

        practicalCreditSpinner.setValue(
                course.getPracticalCredits()
        );

        hasTheoryCheck.setSelected(
                course.isHasTheory()
        );

        hasPracticalCheck.setSelected(
                course.isHasPractical()
        );

        theoryCreditSpinner.setEnabled(
                course.isHasTheory()
        );

        practicalCreditSpinner.setEnabled(
                course.isHasPractical()
        );

        addButton.setEnabled(false);
        updateButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }


    // ======================================
    // GET COURSE DETAILS FROM FORM
    // ======================================

    private Course getCourseFromForm() {

        String id = courseIdField.getText()
                .trim()
                .toUpperCase(Locale.ROOT);

        String name = courseNameField.getText().trim();

        int credit = (Integer) creditSpinner.getValue();

        String type = (String) typeCombo.getSelectedItem();

        boolean hasTheory = hasTheoryCheck.isSelected();

        boolean hasPractical = hasPracticalCheck.isSelected();

        int theoryCredits = hasTheory ?
                (Integer) theoryCreditSpinner.getValue() : 0;

        int practicalCredits = hasPractical ?
                (Integer) practicalCreditSpinner.getValue() : 0;


        // Validate
        if (id.isEmpty() || id.length() > 10) {

            throw new IllegalArgumentException(
                    "Course ID must contain 1-10 characters."
            );
        }

        if (name.isEmpty() || name.length() > 100) {

            throw new IllegalArgumentException(
                    "Course Name must contain 1-100 characters."
            );
        }

        if (!hasTheory && !hasPractical) {

            throw new IllegalArgumentException(
                    "Select Theory or Practical component."
            );
        }

        if (credit != theoryCredits + practicalCredits) {

            throw new IllegalArgumentException(
                    "Total Credits must equal Theory + Practical Credits."
            );
        }


        // Create Course Object
        Course course = new Course();

        course.setCourseId(id);
        course.setCourseName(name);
        course.setCredit(credit);
        course.setType(type);

        course.setTheoryCredits(theoryCredits);
        course.setPracticalCredits(practicalCredits);

        course.setHasTheory(hasTheory);
        course.setHasPractical(hasPractical);

        return course;
    }


    // ======================================
    // CHECK ADMIN PERMISSION
    // ======================================

    private void requireAdmin() {

        if (!SessionManager.getInstance().isAdmin()) {

            throw new SecurityException(
                    "Administrator access required."
            );
        }
    }


    // ======================================
    // ADD COURSE
    // ======================================

    private void handleAddCourse() {

        try {

            requireAdmin();

            Course course = getCourseFromForm();

            if (courseDAO.courseExists(
                    course.getCourseId())) {

                showError(
                        "Course ID already exists."
                );

                return;
            }

            boolean added = courseDAO.addCourse(course);

            if (added) {

                JOptionPane.showMessageDialog(
                        this,
                        "Course Added Successfully!"
                );

                clearForm();

                loadCourses();
            }

        } catch (Exception ex) {

            showError(ex.getMessage());
        }
    }


    // ======================================
    // UPDATE COURSE
    // ======================================

    private void handleUpdateCourse() {

        if (selectedCourseId == null) {
            showError("Select a course first.");
            return;
        }

        try {

            requireAdmin();

            Course course = getCourseFromForm();

            course.setCourseId(selectedCourseId);

            boolean updated =
                    courseDAO.updateCourse(course);

            if (updated) {

                JOptionPane.showMessageDialog(
                        this,
                        "Course Updated Successfully!"
                );

                clearForm();

                loadCourses();

            } else {

                showError("Course was not updated.");
            }

        } catch (Exception ex) {

            showError(ex.getMessage());
        }
    }


    // ======================================
    // DELETE COURSE
    // ======================================

    private void handleDeleteCourse() {

        if (selectedCourseId == null) {
            showError("Select a course first.");
            return;
        }

        try {

            requireAdmin();

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete Course: " + selectedCourseId +
                            "?\nRelated academic records may be affected.",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Require the course ID as an extra confirmation
            String typedId = JOptionPane.showInputDialog(
                    this,
                    "Type the Course ID to confirm deletion:"
            );

            if (typedId == null ||
                    !typedId.trim().equals(selectedCourseId)) {
                return;
            }

            boolean deleted =
                    courseDAO.deleteCourse(selectedCourseId);

            if (deleted) {

                JOptionPane.showMessageDialog(
                        this,
                        "Course Deleted Successfully!"
                );

                clearForm();

                loadCourses();

            } else {

                showError("Course was not found.");
            }

        } catch (Exception ex) {

            showError(ex.getMessage());
        }
    }


    // ======================================
    // CLEAR FORM
    // ======================================

    private void clearForm() {

        selectedCourseId = null;

        courseIdField.setText("");
        courseNameField.setText("");

        courseIdField.setEditable(true);
        courseIdField.setBackground(Color.WHITE);

        creditSpinner.setValue(2);

        typeCombo.setSelectedIndex(0);

        theoryCreditSpinner.setValue(1);
        practicalCreditSpinner.setValue(1);

        hasTheoryCheck.setSelected(true);
        hasPracticalCheck.setSelected(true);

        theoryCreditSpinner.setEnabled(true);
        practicalCreditSpinner.setEnabled(true);

        courseTable.clearSelection();

        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }


    // ======================================
    // ERROR MESSAGE
    // ======================================

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Course Management",
                JOptionPane.ERROR_MESSAGE
        );
    }
}