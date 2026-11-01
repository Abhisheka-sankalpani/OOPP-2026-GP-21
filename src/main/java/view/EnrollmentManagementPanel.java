package view;

import dao.CourseEnrollmentDAO;
import dao.CourseEnrollmentDAO.CourseInfo;
import dao.CourseEnrollmentDAO.StudentInfo;
import service.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Admin-only enrollment tab. Uses the existing student_course table. */
public class EnrollmentManagementPanel extends JPanel {
    private static final Color NAVY = new Color(27, 54, 91);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BG = new Color(245, 247, 250);
    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final CourseEnrollmentDAO dao = new CourseEnrollmentDAO();
    private final JComboBox<CourseInfo> courseBox = new JComboBox<>();
    private final JComboBox<String> departmentBox = new JComboBox<>();
    private final JComboBox<String> batchBox = new JComboBox<>();
    private final JTextField studentIdField = new JTextField(15);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Student ID", "Full Name", "Department", "Batch"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JLabel status = new JLabel(" ");
    private final JButton individualButton = button("Enroll Student");
    private final JButton batchButton = button("Enroll Batch");
    private final JButton refreshButton = button("Refresh");

    public EnrollmentManagementPanel() {
        if (!SessionManager.getInstance().isAdmin()) {
            throw new SecurityException("Administrator access required.");
        }
        buildUI();
        reload();
    }

    private static JButton button(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(11, 16, 11, 16));
        return button;
    }

    private static JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 227, 236)),
                new EmptyBorder(18, 20, 18, 20)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        heading.setForeground(NAVY);
        p.add(heading, BorderLayout.NORTH);
        return p;
    }

    private void buildUI() {
        setLayout(new BorderLayout(16, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel heading = new JPanel(new BorderLayout(10, 8));
        heading.setOpaque(false);
        JLabel title = new JLabel("Student Course Enrollment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        title.setForeground(NAVY);
        heading.add(title, BorderLayout.NORTH);
        JLabel intro = new JLabel("Enroll individual students or an entire department/batch in an existing course.");
        intro.setFont(FONT);
        heading.add(intro, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);

        JPanel forms = new JPanel(new GridLayout(1, 2, 16, 0));
        forms.setOpaque(false);

        JPanel individualCard = card("Individual Enrollment");
        JPanel individualInputs = new JPanel(new GridBagLayout());
        individualInputs.setOpaque(false);
        addField(individualInputs, 0, "Course:", courseBox);
        addField(individualInputs, 1, "Student User ID:", studentIdField);
        JPanel individualFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        individualFooter.setOpaque(false);
        individualFooter.add(individualButton);
        individualCard.add(individualInputs, BorderLayout.CENTER);
        individualCard.add(individualFooter, BorderLayout.SOUTH);
        forms.add(individualCard);

        JPanel batchCard = card("Batch Enrollment");
        JPanel batchInputs = new JPanel(new GridBagLayout());
        batchInputs.setOpaque(false);
        addField(batchInputs, 0, "Department:", departmentBox);
        addField(batchInputs, 1, "Batch:", batchBox);
        JPanel batchFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        batchFooter.setOpaque(false);
        batchFooter.add(batchButton);
        batchCard.add(batchInputs, BorderLayout.CENTER);
        batchCard.add(batchFooter, BorderLayout.SOUTH);
        forms.add(batchCard);

        JPanel center = new JPanel(new BorderLayout(16, 16));
        center.setOpaque(false);
        center.add(forms, BorderLayout.NORTH);

        JPanel listing = card("Enrolled Students for Selected Course");
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(FONT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(228, 235, 249));
        listing.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        status.setFont(FONT);
        footer.add(status, BorderLayout.CENTER);
        footer.add(refreshButton, BorderLayout.EAST);
        listing.add(footer, BorderLayout.SOUTH);
        center.add(listing, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        courseBox.addActionListener(e -> refreshEnrolled());
        departmentBox.addActionListener(e -> refreshBatches());
        individualButton.addActionListener(e -> enrollIndividual());
        batchButton.addActionListener(e -> enrollBatch());
        refreshButton.addActionListener(e -> reload());
    }

    private static void addField(JPanel parent, int row, String label, JComponent field) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = row; g.insets = new Insets(4, 0, 12, 12);
        g.anchor = GridBagConstraints.WEST;
        parent.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1.0; g.fill = GridBagConstraints.HORIZONTAL;
        parent.add(field, g);
    }

    private CourseInfo selectedCourse() {
        return (CourseInfo) courseBox.getSelectedItem();
    }

    private void setBusy(boolean busy) {
        individualButton.setEnabled(!busy);
        batchButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
    }

    private <T> void runDB(String message, Callable<T> work, Consumer<T> onSuccess) {
        setBusy(true);
        status.setText(message);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return work.call(); }
            @Override protected void done() {
                setBusy(false);
                try { onSuccess.accept(get()); }
                catch (Exception e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    status.setText("Operation failed.");
                    JOptionPane.showMessageDialog(EnrollmentManagementPanel.this,
                            cause.getMessage(), "Enrollment Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /** Call this after adding courses or switching to this tab. */
    public void reload() {
        runDB("Loading courses and departments...", () -> {
            List<CourseInfo> courses = dao.getCourses();
            List<String> departments = dao.getDepartments();
            return new Object[]{courses, departments};
        }, data -> {
            Object[] pair = (Object[]) data;
            String oldCourse = selectedCourse() == null ? null : selectedCourse().id;
            String oldDepartment = (String) departmentBox.getSelectedItem();
            courseBox.removeAllItems();
            @SuppressWarnings("unchecked") List<CourseInfo> courses = (List<CourseInfo>) pair[0];
            for (CourseInfo c : courses) courseBox.addItem(c);
            if (oldCourse != null) {
                for (int i = 0; i < courseBox.getItemCount(); i++) {
                    if (oldCourse.equals(courseBox.getItemAt(i).id)) { courseBox.setSelectedIndex(i); break; }
                }
            }
            departmentBox.removeAllItems();
            @SuppressWarnings("unchecked") List<String> departments = (List<String>) pair[1];
            for (String d : departments) departmentBox.addItem(d);
            if (oldDepartment != null) departmentBox.setSelectedItem(oldDepartment);
            status.setText(courses.isEmpty() ? "No courses found. Add a course first." : "Select course and enrollment options.");
            refreshEnrolled();
            refreshBatches();
        });
    }

    private void refreshBatches() {
        String dept = (String) departmentBox.getSelectedItem();
        if (dept == null) { batchBox.removeAllItems(); return; }
        runDB("Loading batches...", () -> dao.getBatches(dept), batches -> {
            String previous = (String) batchBox.getSelectedItem();
            batchBox.removeAllItems();
            for (String b : batches) batchBox.addItem(b);
            if (previous != null) batchBox.setSelectedItem(previous);
            status.setText("Choose a batch or enroll an individual student.");
        });
    }

    private void refreshEnrolled() {
        CourseInfo selected = selectedCourse();
        if (selected == null) { tableModel.setRowCount(0); return; }
        runDB("Loading enrolled students...", () -> dao.getEnrolledStudents(selected.id), students -> {
            if (selectedCourse() == null || !selected.id.equals(selectedCourse().id)) return;
            tableModel.setRowCount(0);
            for (StudentInfo s : students) {
                tableModel.addRow(new Object[]{s.id, s.fullName, s.department, s.batch});
            }
            status.setText(students.size() + " enrolled student(s) in " + selected.id);
        });
    }

    private void enrollIndividual() {
        CourseInfo selected = selectedCourse();
        String studentId = studentIdField.getText().trim();
        if (selected == null || studentId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Select a course and enter a Student User ID.");
            return;
        }
        runDB("Enrolling student...", () -> dao.enrollIndividual(studentId, selected.id), rows -> {
            status.setText(rows == 1 ? "Student enrolled successfully." :
                    "No new enrollment. Student ID may be invalid or already enrolled.");
            if (rows == 1) studentIdField.setText("");
            refreshEnrolled();
        });
    }

    private void enrollBatch() {
        CourseInfo selected = selectedCourse();
        String department = (String) departmentBox.getSelectedItem();
        String batch = (String) batchBox.getSelectedItem();
        if (selected == null || department == null || batch == null) {
            JOptionPane.showMessageDialog(this, "Select a course, department and batch.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Enroll all students in " + department + " / Batch " + batch + " into " + selected.id + "?",
                "Confirm Batch Enrollment", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        runDB("Enrolling batch...", () -> dao.enrollBatch(department, batch, selected.id), rows -> {
            status.setText(rows + " new student(s) enrolled. Existing enrollments were skipped.");
            JOptionPane.showMessageDialog(this, rows + " new student(s) enrolled into " + selected.id + ".");
            refreshEnrolled();
        });
    }
}
