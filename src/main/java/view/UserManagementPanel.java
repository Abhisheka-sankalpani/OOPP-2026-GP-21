package view;

import dao.UserDAO;

import model.User;
import model.Admin;
import model.Lecturer;
import model.TechnicalOfficer;
import model.Student;

import service.PasswordService;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class UserManagementPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();

    // Store loaded users
    private final Map<String, User> users =
            new LinkedHashMap<>();

    // Common User Fields
    private JTextField userIdField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField contactField;
    private JTextField profilePicField;

    private JPasswordField passwordField;

    private JComboBox<String> roleCombo;

    // Role-Specific Fields
    private JTextField departmentField;
    private JTextField regNoField;
    private JTextField batchField;

    private JCheckBox repeatCheck;

    // Form Panels
    private JPanel passwordGroup;
    private JPanel departmentGroup;
    private JPanel regNoGroup;
    private JPanel batchGroup;
    private JPanel repeatGroup;

    // Search and Table
    private JTextField searchField;
    private JTable userTable;
    private DefaultTableModel tableModel;

    // Buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    // Selected User
    private String selectedUserId;

    // Constructor
    public UserManagementPanel() {
        initComponents();
        loadUsers();
    }

    // ===================================
    // INITIALIZE GUI
    // ===================================

    private void initComponents() {

        setLayout(new BorderLayout(15, 15));

        setBackground(UIStyle.COLOR_BG);

        setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        // Header
        JLabel titleLabel =
                new JLabel("User Management");

        titleLabel.setFont(UIStyle.FONT_TITLE);

        titleLabel.setForeground(
                UIStyle.COLOR_PRIMARY
        );

        add(titleLabel, BorderLayout.NORTH);

        // Main Content
        JPanel contentPanel =
                new JPanel(new BorderLayout(15, 0));

        contentPanel.setOpaque(false);

        // ===================================
        // LEFT SIDE - USER FORM
        // ===================================

        JPanel formCard =
                new JPanel(new BorderLayout(0, 12));

        formCard.setBackground(Color.WHITE);

        formCard.setBorder(
                UIStyle.createCardBorder()
        );

        formCard.setPreferredSize(
                new Dimension(350, 550)
        );

        JLabel formTitle =
                new JLabel("User Details");

        formTitle.setFont(UIStyle.FONT_SUBTITLE);

        formTitle.setForeground(
                UIStyle.COLOR_PRIMARY
        );

        formCard.add(
                formTitle,
                BorderLayout.NORTH
        );

        JPanel formBody = new JPanel();

        formBody.setLayout(
                new BoxLayout(formBody, BoxLayout.Y_AXIS)
        );

        formBody.setBackground(Color.WHITE);

        // User ID
        userIdField = UIStyle.createTextField(20);

        formBody.add(
                createFormField("User ID *", userIdField)
        );

        // First Name
        firstNameField = UIStyle.createTextField(20);

        formBody.add(
                createFormField("First Name *", firstNameField)
        );

        // Last Name
        lastNameField = UIStyle.createTextField(20);

        formBody.add(
                createFormField("Last Name *", lastNameField)
        );

        // Email
        emailField = UIStyle.createTextField(20);

        formBody.add(
                createFormField("Email *", emailField)
        );

        // Contact
        contactField = UIStyle.createTextField(20);

        formBody.add(
                createFormField("Contact Number", contactField)
        );

        // Profile Picture
        profilePicField = UIStyle.createTextField(20);

        formBody.add(
                createFormField(
                        "Profile Picture Path",
                        profilePicField
                )
        );

        // Role
        String[] roles = {
                "ADMIN",
                "LECTURER",
                "TECHNICAL_OFFICER",
                "STUDENT"
        };

        roleCombo = new JComboBox<>(roles);

        formBody.add(
                createFormField("User Role *", roleCombo)
        );

        // Password
        passwordField =
                UIStyle.createPasswordField(20);

        passwordGroup = createFormField(
                "Password * (New User Only)",
                passwordField
        );

        formBody.add(passwordGroup);

        // Department
        departmentField = UIStyle.createTextField(20);

        departmentGroup = createFormField(
                "Department *",
                departmentField
        );

        formBody.add(departmentGroup);

        // Registration Number
        regNoField = UIStyle.createTextField(20);

        regNoGroup = createFormField(
                "Registration Number *",
                regNoField
        );

        formBody.add(regNoGroup);

        // Batch
        batchField = UIStyle.createTextField(20);

        batchGroup = createFormField(
                "Batch *",
                batchField
        );

        formBody.add(batchGroup);

        // Repeat Student
        repeatCheck = new JCheckBox(
                "Repeat Student"
        );

        repeatCheck.setBackground(Color.WHITE);

        repeatGroup = new JPanel(new BorderLayout());

        repeatGroup.setBackground(Color.WHITE);

        repeatGroup.add(
                repeatCheck,
                BorderLayout.CENTER
        );

        formBody.add(repeatGroup);

        // Update fields when role changes
        roleCombo.addActionListener(
                e -> updateRoleFields()
        );

        // Form Scroll Pane
        JScrollPane formScroll =
                new JScrollPane(formBody);

        formScroll.setBorder(null);

        formScroll.getVerticalScrollBar()
                .setUnitIncrement(16);

        formCard.add(
                formScroll,
                BorderLayout.CENTER
        );

        // ===================================
        // FORM BUTTONS
        // ===================================

        JPanel buttonPanel =
                new JPanel(new GridLayout(2, 2, 8, 8));

        addButton =
                UIStyle.createPrimaryButton("Add User");

        addButton.addActionListener(
                e -> handleAddUser()
        );

        updateButton =
                UIStyle.createPrimaryButton("Update");

        updateButton.setEnabled(false);

        updateButton.addActionListener(
                e -> handleUpdateUser()
        );

        deleteButton =
                UIStyle.createPrimaryButton("Delete");

        deleteButton.setBackground(
                new Color(220, 38, 38)
        );

        deleteButton.setEnabled(false);

        deleteButton.addActionListener(
                e -> handleDeleteUser()
        );

        clearButton =
                UIStyle.createSecondaryButton("Clear");

        clearButton.addActionListener(
                e -> clearForm()
        );

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        formCard.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        contentPanel.add(
                formCard,
                BorderLayout.WEST
        );

        // ===================================
        // RIGHT SIDE - USER TABLE
        // ===================================

        JPanel tableCard =
                new JPanel(new BorderLayout(0, 12));

        tableCard.setBackground(Color.WHITE);

        tableCard.setBorder(
                UIStyle.createCardBorder()
        );

        // Search Panel
        JPanel searchPanel =
                new JPanel(new BorderLayout(8, 0));

        searchPanel.setOpaque(false);

        searchField = UIStyle.createTextField(20);

        searchField.setToolTipText(
                "Search User ID, Name, Email or Role"
        );

        searchField.addActionListener(
                e -> applyFilter()
        );

        JButton searchButton =
                UIStyle.createPrimaryButton("Search");

        searchButton.addActionListener(
                e -> applyFilter()
        );

        JButton refreshButton =
                UIStyle.createSecondaryButton("Refresh");

        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadUsers();
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

        tableCard.add(
                searchPanel,
                BorderLayout.NORTH
        );

        // Table Columns
        String[] columns = {
                "User ID",
                "Full Name",
                "Email",
                "Contact",
                "Role"
        };

        tableModel = new DefaultTableModel(
                columns, 0
        ) {
            @Override
            public boolean isCellEditable(
                    int row, int column) {

                return false;
            }
        };

        userTable = new JTable(tableModel);

        userTable.setFont(UIStyle.FONT_BODY);

        userTable.setRowHeight(30);

        userTable.getTableHeader().setFont(
                UIStyle.FONT_BODY_BOLD
        );

        userTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        userTable.setAutoCreateRowSorter(true);

        // Select User from JTable
        userTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        populateSelectedUser();
                    }
                });

        tableCard.add(
                new JScrollPane(userTable),
                BorderLayout.CENTER
        );

        contentPanel.add(
                tableCard,
                BorderLayout.CENTER
        );

        add(contentPanel, BorderLayout.CENTER);

        updateRoleFields();
    }

    // ===================================
    // CREATE FORM FIELD
    // ===================================

    private JPanel createFormField(
            String label,
            JComponent component) {

        JPanel panel =
                new JPanel(new BorderLayout(0, 5));

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(5, 0, 8, 0)
        );

        JLabel fieldLabel = new JLabel(label);

        fieldLabel.setFont(
                UIStyle.FONT_BODY_BOLD
        );

        panel.add(
                fieldLabel,
                BorderLayout.NORTH
        );

        panel.add(
                component,
                BorderLayout.CENTER
        );

        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 75)
        );

        return panel;
    }

    // ===================================
    // SHOW ROLE-SPECIFIC FIELDS
    // ===================================

    private void updateRoleFields() {

        String role =
                (String) roleCombo.getSelectedItem();

        boolean isLecturer =
                "LECTURER".equals(role);

        boolean isTO =
                "TECHNICAL_OFFICER".equals(role);

        boolean isStudent =
                "STUDENT".equals(role);

        departmentGroup.setVisible(
                isLecturer || isTO || isStudent
        );

        regNoGroup.setVisible(isStudent);

        batchGroup.setVisible(isStudent);

        repeatGroup.setVisible(isStudent);

        revalidate();
        repaint();
    }

    // ===================================
    // LOAD ALL USERS
    // ===================================

    public void loadUsers() {

        SwingWorker<List<User>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<User> doInBackground()
                            throws Exception {

                        return userDAO.getAllUsers();
                    }

                    @Override
                    protected void done() {

                        try {

                            List<User> result = get();

                            users.clear();

                            for (User user : result) {
                                users.put(
                                        user.getUserId(),
                                        user
                                );
                            }

                            applyFilter();

                        } catch (Exception ex) {

                            showError(
                                    "Unable to load users."
                            );
                        }
                    }
                };

        worker.execute();
    }

    // ===================================
    // SEARCH USERS
    // ===================================

    private void applyFilter() {

        String keyword = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        tableModel.setRowCount(0);

        for (User user : users.values()) {

            String text = (
                    user.getUserId() + " " +
                            user.getFullname() + " " +
                            user.getEmail() + " " +
                            user.getRole()
            ).toLowerCase(Locale.ROOT);

            if (text.contains(keyword)) {

                tableModel.addRow(new Object[]{
                        user.getUserId(),
                        user.getFullname(),
                        user.getEmail(),
                        user.getContactNo(),
                        user.getRole()
                });
            }
        }
    }

    // ===================================
    // SELECT USER
    // ===================================

    private void populateSelectedUser() {

        int selectedRow =
                userTable.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }

        int modelRow =
                userTable.convertRowIndexToModel(
                        selectedRow
                );

        String userId = (String)
                tableModel.getValueAt(modelRow, 0);

        User user = users.get(userId);

        if (user == null) {
            return;
        }

        selectedUserId = user.getUserId();

        userIdField.setText(user.getUserId());

        userIdField.setEditable(false);

        firstNameField.setText(user.getFirstName());

        lastNameField.setText(user.getLastName());

        emailField.setText(user.getEmail());

        contactField.setText(user.getContactNo());

        profilePicField.setText(user.getProfilePic());

        roleCombo.setSelectedItem(user.getRole());

        roleCombo.setEnabled(false);

        passwordField.setText("");

        passwordGroup.setVisible(false);

        departmentField.setText("");
        regNoField.setText("");
        batchField.setText("");
        repeatCheck.setSelected(false);

        // Lecturer Details
        if (user instanceof Lecturer) {

            Lecturer lecturer = (Lecturer) user;

            departmentField.setText(
                    lecturer.getDepartment()
            );
        }

        // Technical Officer Details
        if (user instanceof TechnicalOfficer) {

            TechnicalOfficer to =
                    (TechnicalOfficer) user;

            departmentField.setText(
                    to.getDepartment()
            );
        }

        // Student Details
        if (user instanceof Student) {

            Student student = (Student) user;

            departmentField.setText(
                    student.getDepartment()
            );

            regNoField.setText(
                    student.getRegNo()
            );

            batchField.setText(
                    student.getBatch()
            );

            repeatCheck.setSelected(
                    student.isRepeat()
            );
        }

        addButton.setEnabled(false);

        updateButton.setEnabled(true);

        deleteButton.setEnabled(true);

        updateRoleFields();
    }

    // ===================================
    // CREATE USER OBJECT FROM FORM
    // ===================================

    private User getUserFromForm() {

        String userId = userIdField.getText().trim();

        String firstName =
                firstNameField.getText().trim();

        String lastName =
                lastNameField.getText().trim();

        String email =
                emailField.getText().trim();

        String role =
                (String) roleCombo.getSelectedItem();

        // Validate Common Details
        if (userId.isBlank() || userId.length() > 50) {
            throw new IllegalArgumentException(
                    "Valid User ID is required."
            );
        }

        if (firstName.isBlank() ||
                firstName.length() > 50 ||
                lastName.isBlank() ||
                lastName.length() > 50) {

            throw new IllegalArgumentException(
                    "Valid First Name and Last Name are required."
            );
        }

        if (email.isBlank() ||
                email.length() > 100 ||
                !email.contains("@")) {

            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        User user;

        switch (role) {

            case "ADMIN":
                user = new Admin();
                break;

            case "LECTURER":

                Lecturer lecturer = new Lecturer();

                String lecturerDept =
                        departmentField.getText().trim();

                if (lecturerDept.isBlank() ||
                        lecturerDept.length() > 50) {

                    throw new IllegalArgumentException(
                            "Valid Lecturer Department is required."
                    );
                }

                lecturer.setDepartment(lecturerDept);

                user = lecturer;
                break;

            case "TECHNICAL_OFFICER":

                TechnicalOfficer to =
                        new TechnicalOfficer();

                String toDept =
                        departmentField.getText().trim();

                if (toDept.isBlank() ||
                        toDept.length() > 50) {

                    throw new IllegalArgumentException(
                            "Valid Technical Officer Department is required."
                    );
                }

                to.setDepartment(toDept);

                user = to;
                break;

            case "STUDENT":

                Student student = new Student();

                String regNo =
                        regNoField.getText().trim();

                String studentDept =
                        departmentField.getText().trim();

                String batch =
                        batchField.getText().trim();

                if (regNo.isBlank() ||
                        regNo.length() > 20 ||
                        studentDept.isBlank() ||
                        studentDept.length() > 50 ||
                        batch.isBlank() ||
                        batch.length() > 10) {

                    throw new IllegalArgumentException(
                            "Valid Student Registration, Department and Batch are required."
                    );
                }

                student.setRegNo(regNo);
                student.setDepartment(studentDept);
                student.setBatch(batch);

                student.setRepeat(
                        repeatCheck.isSelected()
                );

                user = student;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid User Role."
                );
        }

        user.setUserId(userId);

        user.setFirstName(firstName);

        user.setLastName(lastName);

        user.setEmail(email);

        user.setContactNo(
                contactField.getText().trim()
        );

        user.setProfilePic(
                profilePicField.getText().trim()
        );

        user.setRole(role);

        return user;
    }

    // ===================================
    // CHECK ADMIN PERMISSION
    // ===================================

    private boolean requireAdmin() {

        if (!SessionManager.getInstance().isAdmin()) {

            showError(
                    "Administrator access required."
            );

            return false;
        }

        return true;
    }

    // ===================================
    // ADD USER
    // ===================================

    private void handleAddUser() {

        if (!requireAdmin()) {
            return;
        }

        char[] password = passwordField.getPassword();

        try {

            User user = getUserFromForm();

            if (password.length < 8) {

                throw new IllegalArgumentException(
                        "Password must contain at least 8 characters."
                );
            }

            passwordField.setText("");

            executeChange(() -> {

                try {

                    String hash =
                            PasswordService.hashPassword(
                                    new String(password)
                            );

                    user.setPasswordHash(hash);

                    return userDAO.addUser(user);

                } finally {

                    Arrays.fill(password, '\0');
                }

            }, "User Added Successfully!");

        } catch (RuntimeException ex) {

            Arrays.fill(password, '\0');

            showError(ex.getMessage());
        }
    }

    // ===================================
    // UPDATE USER
    // ===================================

    private void handleUpdateUser() {

        if (!requireAdmin()) {
            return;
        }

        if (selectedUserId == null) {
            showError("Select a user first.");
            return;
        }

        try {

            User user = getUserFromForm();

            // User ID and Role cannot change
            user.setUserId(selectedUserId);

            executeChange(
                    () -> userDAO.updateUser(user),
                    "User Updated Successfully!"
            );

        } catch (RuntimeException ex) {

            showError(ex.getMessage());
        }
    }

    // ===================================
    // DELETE USER
    // ===================================

    private void handleDeleteUser() {

        if (!requireAdmin()) {
            return;
        }

        if (selectedUserId == null) {
            showError("Select a user first.");
            return;
        }

        User currentUser =
                SessionManager.getInstance().getCurrentUser();

        // Prevent deleting own account
        if (currentUser != null &&
                selectedUserId.equals(
                        currentUser.getUserId())) {

            showError(
                    "You cannot delete your own logged-in account."
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete User: " + selectedUserId +
                        "?\nRelated records may be affected.",
                "Confirm User Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String confirmation = JOptionPane.showInputDialog(
                this,
                "Type the User ID to confirm deletion:"
        );

        if (confirmation == null ||
                !confirmation.trim().equals(
                        selectedUserId)) {

            return;
        }

        String userId = selectedUserId;

        executeChange(
                () -> userDAO.deleteUser(userId),
                "User Deleted Successfully!"
        );
    }

    // ===================================
    // EXECUTE DATABASE CHANGES
    // ===================================

    private void executeChange(
            Callable<Boolean> operation,
            String successMessage) {

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws Exception {

                        return operation.call();
                    }

                    @Override
                    protected void done() {

                        try {

                            boolean success = get();

                            if (success) {

                                JOptionPane.showMessageDialog(
                                        UserManagementPanel.this,
                                        successMessage
                                );

                                clearForm();
                                loadUsers();

                            } else {

                                showError(
                                        "Operation failed or record not found."
                                );
                            }

                        } catch (InterruptedException ex) {

                            Thread.currentThread().interrupt();

                            showError("Operation interrupted.");

                        } catch (ExecutionException ex) {

                            Throwable cause = ex.getCause();

                            showError(
                                    cause == null ?
                                            "Database operation failed." :
                                            cause.getMessage()
                            );

                        } finally {

                            setButtonsEnabled(true);
                        }
                    }
                };

        worker.execute();
    }

    // ===================================
    // ENABLE / DISABLE BUTTONS
    // ===================================

    private void setButtonsEnabled(boolean enabled) {

        addButton.setEnabled(
                enabled && selectedUserId == null
        );

        updateButton.setEnabled(
                enabled && selectedUserId != null
        );

        deleteButton.setEnabled(
                enabled && selectedUserId != null
        );

        clearButton.setEnabled(enabled);
    }

    // ===================================
    // CLEAR FORM
    // ===================================

    private void clearForm() {

        selectedUserId = null;

        userIdField.setText("");
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        contactField.setText("");
        profilePicField.setText("");

        passwordField.setText("");

        departmentField.setText("");
        regNoField.setText("");
        batchField.setText("");

        repeatCheck.setSelected(false);

        roleCombo.setSelectedItem("ADMIN");

        roleCombo.setEnabled(true);

        userIdField.setEditable(true);

        passwordGroup.setVisible(true);

        userTable.clearSelection();

        addButton.setEnabled(true);
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        updateRoleFields();
    }

    // ===================================
    // SHOW ERROR
    // ===================================

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "User Management",
                JOptionPane.ERROR_MESSAGE
        );
    }
}