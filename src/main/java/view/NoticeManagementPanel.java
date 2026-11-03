package view;

import dao.NoticeDAO;
import model.Notice;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class NoticeManagementPanel extends JPanel {

    // DAO Object
    private final NoticeDAO noticeDAO = new NoticeDAO();

    // Store Notice Objects
    private final Map<Integer, Notice> notices = new HashMap<>();

    // Form Fields
    private JTextField titleField;
    private JTextArea contentArea;
    private JComboBox<String> targetRoleCombo;

    private JLabel postedDateLabel;
    private JLabel statusLabel;

    // Search
    private JTextField searchField;

    // Table
    private JTable noticeTable;
    private DefaultTableModel tableModel;

    // Buttons
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;

    // Selected Notice
    private Integer selectedNoticeId = null;


    // =====================================
    // CONSTRUCTOR
    // =====================================

    public NoticeManagementPanel() {

        initComponents();

        loadNotices();
    }


    // =====================================
    // INITIALIZE GUI
    // =====================================

    private void initComponents() {

        setLayout(new BorderLayout(15, 15));

        setBackground(UIStyle.COLOR_BG);

        setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );


        // HEADER
        JLabel titleLabel = new JLabel(
                "Notice Management"
        );

        titleLabel.setFont(UIStyle.FONT_TITLE);

        titleLabel.setForeground(
                UIStyle.COLOR_PRIMARY
        );

        add(titleLabel, BorderLayout.NORTH);


        // MAIN CONTENT
        JPanel contentPanel = new JPanel(
                new BorderLayout(15, 0)
        );

        contentPanel.setOpaque(false);


        // =====================================
        // LEFT SIDE - NOTICE FORM
        // =====================================

        JPanel formCard = new JPanel(
                new BorderLayout(0, 12)
        );

        formCard.setBackground(Color.WHITE);

        formCard.setBorder(
                UIStyle.createCardBorder()
        );

        formCard.setPreferredSize(
                new Dimension(370, 500)
        );


        JLabel formTitle = new JLabel(
                "Notice Details"
        );

        formTitle.setFont(
                UIStyle.FONT_SUBTITLE
        );

        formTitle.setForeground(
                UIStyle.COLOR_PRIMARY
        );

        formCard.add(
                formTitle,
                BorderLayout.NORTH
        );


        // Form Body
        JPanel formBody = new JPanel();

        formBody.setLayout(
                new BoxLayout(formBody, BoxLayout.Y_AXIS)
        );

        formBody.setBackground(Color.WHITE);


        // =====================================
        // NOTICE TITLE
        // =====================================

        titleField = UIStyle.createTextField(20);

        formBody.add(
                createFormField(
                        "Notice Title *",
                        titleField,
                        75
                )
        );


        // =====================================
        // NOTICE CONTENT
        // =====================================

        contentArea = new JTextArea(6, 20);

        contentArea.setFont(
                UIStyle.FONT_BODY
        );

        contentArea.setLineWrap(true);

        contentArea.setWrapStyleWord(true);

        contentArea.setBorder(
                new EmptyBorder(8, 8, 8, 8)
        );

        JScrollPane contentScroll =
                new JScrollPane(contentArea);

        contentScroll.setPreferredSize(
                new Dimension(300, 140)
        );

        formBody.add(
                createFormField(
                        "Notice Content *",
                        contentScroll,
                        180
                )
        );


        // =====================================
        // TARGET ROLE
        // =====================================

        String[] roles = {
                "ALL",
                "ADMIN",
                "LECTURER",
                "TECHNICAL_OFFICER",
                "STUDENT"
        };

        targetRoleCombo = new JComboBox<>(roles);

        targetRoleCombo.setFont(
                UIStyle.FONT_BODY
        );

        targetRoleCombo.setBackground(
                Color.WHITE
        );

        formBody.add(
                createFormField(
                        "Target Role",
                        targetRoleCombo,
                        75
                )
        );


        // =====================================
        // POSTED DATE
        // =====================================

        postedDateLabel = new JLabel(
                "Automatically generated"
        );

        postedDateLabel.setFont(
                UIStyle.FONT_BODY
        );

        postedDateLabel.setForeground(
                UIStyle.COLOR_TEXT_MUTED
        );

        formBody.add(
                createFormField(
                        "Posted Date",
                        postedDateLabel,
                        65
                )
        );


        // FORM SCROLL
        JScrollPane formScroll =
                new JScrollPane(formBody);

        formScroll.setBorder(null);

        formScroll.getVerticalScrollBar()
                .setUnitIncrement(16);

        formCard.add(
                formScroll,
                BorderLayout.CENTER
        );


        // =====================================
        // ACTION BUTTONS
        // =====================================

        JPanel buttonPanel = new JPanel(
                new GridLayout(2, 2, 8, 8)
        );


        // Add Button
        addButton = UIStyle.createPrimaryButton(
                "Add Notice"
        );

        addButton.addActionListener(
                e -> handleAddNotice()
        );


        // Update Button
        updateButton = UIStyle.createPrimaryButton(
                "Update"
        );

        updateButton.setEnabled(false);

        updateButton.addActionListener(
                e -> handleUpdateNotice()
        );


        // Delete Button
        deleteButton = UIStyle.createPrimaryButton(
                "Delete"
        );

        deleteButton.setBackground(
                new Color(220, 38, 38)
        );

        deleteButton.setEnabled(false);

        deleteButton.addActionListener(
                e -> handleDeleteNotice()
        );


        // Clear Button
        clearButton = UIStyle.createSecondaryButton(
                "Clear"
        );

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


        // =====================================
        // RIGHT SIDE - NOTICE TABLE
        // =====================================

        JPanel tableCard = new JPanel(
                new BorderLayout(0, 12)
        );

        tableCard.setBackground(Color.WHITE);

        tableCard.setBorder(
                UIStyle.createCardBorder()
        );


        // =====================================
        // SEARCH PANEL
        // =====================================

        JPanel searchPanel = new JPanel(
                new BorderLayout(8, 0)
        );

        searchPanel.setOpaque(false);


        searchField = UIStyle.createTextField(20);

        searchField.setToolTipText(
                "Search by notice title or content"
        );

        searchField.addActionListener(
                e -> loadNotices()
        );


        JButton searchButton =
                UIStyle.createPrimaryButton("Search");

        searchButton.addActionListener(
                e -> loadNotices()
        );


        JButton refreshButton =
                UIStyle.createSecondaryButton("Refresh");

        refreshButton.addActionListener(e -> {

            searchField.setText("");

            loadNotices();
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


        // =====================================
        // NOTICE TABLE
        // =====================================

        String[] columns = {
                "Notice ID",
                "Title",
                "Target Role",
                "Posted Date"
        };


        tableModel = new DefaultTableModel(
                columns, 0
        ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };


        noticeTable = new JTable(tableModel);

        noticeTable.setFont(
                UIStyle.FONT_BODY
        );

        noticeTable.setRowHeight(32);

        noticeTable.getTableHeader().setFont(
                UIStyle.FONT_BODY_BOLD
        );

        noticeTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        noticeTable.setAutoCreateRowSorter(true);


        // Select Notice From Table
        noticeTable.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        populateSelectedNotice();
                    }
                });


        JScrollPane tableScroll =
                new JScrollPane(noticeTable);

        tableCard.add(
                tableScroll,
                BorderLayout.CENTER
        );


        // STATUS LABEL
        statusLabel = new JLabel("Ready");

        statusLabel.setFont(
                UIStyle.FONT_SMALL
        );

        statusLabel.setForeground(
                UIStyle.COLOR_TEXT_MUTED
        );

        tableCard.add(
                statusLabel,
                BorderLayout.SOUTH
        );


        contentPanel.add(
                tableCard,
                BorderLayout.CENTER
        );


        add(
                contentPanel,
                BorderLayout.CENTER
        );
    }


    // =====================================
    // CREATE FORM FIELD
    // =====================================

    private JPanel createFormField(
            String label,
            JComponent component,
            int height) {

        JPanel panel = new JPanel(
                new BorderLayout(0, 5)
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(5, 0, 8, 0)
        );


        JLabel fieldLabel = new JLabel(label);

        fieldLabel.setFont(
                UIStyle.FONT_BODY_BOLD
        );

        fieldLabel.setForeground(
                UIStyle.COLOR_TEXT_MAIN
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
                new Dimension(
                        Integer.MAX_VALUE,
                        height
                )
        );

        return panel;
    }


    // =====================================
    // LOAD NOTICES
    // =====================================

    public void loadNotices() {

        String keyword =
                searchField.getText().trim();

        statusLabel.setText(
                "Loading notices..."
        );


        SwingWorker<List<Notice>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<Notice> doInBackground()
                            throws Exception {

                        if (keyword.isBlank()) {

                            return noticeDAO.getAllNotices();

                        } else {

                            return noticeDAO.searchNotices(
                                    keyword
                            );
                        }
                    }


                    @Override
                    protected void done() {

                        try {

                            List<Notice> result = get();

                            clearForm();

                            notices.clear();

                            tableModel.setRowCount(0);


                            for (Notice notice : result) {

                                notices.put(
                                        notice.getNoticeId(),
                                        notice
                                );

                                tableModel.addRow(
                                        new Object[]{

                                                notice.getNoticeId(),

                                                notice.getTitle(),

                                                notice.getTargetRole(),

                                                notice.getPostedDate()
                                        }
                                );
                            }


                            statusLabel.setText(
                                    "Notices loaded: " +
                                            result.size()
                            );

                        } catch (Exception ex) {

                            showError(
                                    "Unable to load notices."
                            );

                            statusLabel.setText(
                                    "Failed to load notices."
                            );
                        }
                    }
                };


        worker.execute();
    }


    // =====================================
    // SELECT NOTICE
    // =====================================

    private void populateSelectedNotice() {

        int selectedRow =
                noticeTable.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }


        int modelRow =
                noticeTable.convertRowIndexToModel(
                        selectedRow
                );


        int noticeId = (Integer)
                tableModel.getValueAt(modelRow, 0);


        Notice notice = notices.get(noticeId);

        if (notice == null) {
            return;
        }


        selectedNoticeId = notice.getNoticeId();

        titleField.setText(
                notice.getTitle()
        );

        contentArea.setText(
                notice.getContent()
        );

        targetRoleCombo.setSelectedItem(
                notice.getTargetRole()
        );

        postedDateLabel.setText(
                String.valueOf(
                        notice.getPostedDate()
                )
        );


        addButton.setEnabled(false);

        updateButton.setEnabled(true);

        deleteButton.setEnabled(true);
    }


    // =====================================
    // VALIDATE NOTICE
    // =====================================

    private Notice getNoticeFromForm() {

        String title =
                titleField.getText().trim();

        String content =
                contentArea.getText().trim();

        String targetRole =
                (String) targetRoleCombo.getSelectedItem();


        // Validate Title
        if (title.isBlank()) {

            throw new IllegalArgumentException(
                    "Notice title is required."
            );
        }


        if (title.length() > 100) {

            throw new IllegalArgumentException(
                    "Notice title cannot exceed 100 characters."
            );
        }


        // Validate Content
        if (content.isBlank()) {

            throw new IllegalArgumentException(
                    "Notice content is required."
            );
        }


        // Create Notice Object
        return new Notice(
                title,
                content,
                targetRole
        );
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
    // ADD NOTICE
    // =====================================

    private void handleAddNotice() {

        try {

            requireAdmin();

            Notice notice = getNoticeFromForm();


            executeChange(
                    () -> noticeDAO.addNotice(notice),
                    "Notice Added Successfully!"
            );

        } catch (RuntimeException ex) {

            showError(ex.getMessage());
        }
    }


    // =====================================
    // UPDATE NOTICE
    // =====================================

    private void handleUpdateNotice() {

        if (selectedNoticeId == null) {

            showError(
                    "Please select a notice first."
            );

            return;
        }


        try {

            requireAdmin();

            Notice notice = getNoticeFromForm();

            notice.setNoticeId(
                    selectedNoticeId
            );


            executeChange(
                    () -> noticeDAO.updateNotice(notice),
                    "Notice Updated Successfully!"
            );

        } catch (RuntimeException ex) {

            showError(ex.getMessage());
        }
    }


    // =====================================
    // DELETE NOTICE
    // =====================================

    private void handleDeleteNotice() {

        if (selectedNoticeId == null) {

            showError(
                    "Please select a notice first."
            );

            return;
        }


        try {

            requireAdmin();


            int confirm =
                    JOptionPane.showConfirmDialog(

                            this,

                            "Are you sure you want to delete Notice ID " +
                                    selectedNoticeId + "?",

                            "Confirm Notice Deletion",

                            JOptionPane.YES_NO_OPTION,

                            JOptionPane.WARNING_MESSAGE
                    );


            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }


            int noticeId = selectedNoticeId;


            executeChange(
                    () -> noticeDAO.deleteNotice(noticeId),
                    "Notice Deleted Successfully!"
            );

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

        statusLabel.setText(
                "Saving changes..."
        );


        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws Exception {

                        if (!SessionManager.getInstance().isAdmin()) {

                            throw new SecurityException(
                                    "Administrator access required."
                            );
                        }

                        return operation.call();
                    }


                    @Override
                    protected void done() {

                        try {

                            boolean success = get();


                            if (success) {

                                JOptionPane.showMessageDialog(
                                        NoticeManagementPanel.this,
                                        successMessage
                                );

                                clearForm();

                                loadNotices();

                            } else {

                                showError(
                                        "Operation failed or notice not found."
                                );
                            }

                        } catch (InterruptedException ex) {

                            Thread.currentThread().interrupt();

                            showError(
                                    "Operation interrupted."
                            );

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
                enabled && selectedNoticeId == null
        );

        updateButton.setEnabled(
                enabled && selectedNoticeId != null
        );

        deleteButton.setEnabled(
                enabled && selectedNoticeId != null
        );

        clearButton.setEnabled(enabled);
    }


    // =====================================
    // CLEAR FORM
    // =====================================

    private void clearForm() {

        selectedNoticeId = null;

        titleField.setText("");

        contentArea.setText("");

        targetRoleCombo.setSelectedItem(
                "ALL"
        );

        postedDateLabel.setText(
                "Automatically generated"
        );

        noticeTable.clearSelection();

        addButton.setEnabled(true);

        updateButton.setEnabled(false);

        deleteButton.setEnabled(false);
    }


    // =====================================
    // SHOW ERROR
    // =====================================

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Notice Management",
                JOptionPane.ERROR_MESSAGE
        );
    }
}