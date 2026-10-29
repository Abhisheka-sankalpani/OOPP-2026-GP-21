package gui;

import dao.AssessmentDAO;
import dao.AssessmentDAOImpl;
import dao.CourseMaterialDAO;
import dao.CourseMaterialDAOImpl;
import dao.MarkDAO;
import dao.MarkDAOImpl;
import dao.NoticeBoardDAO;
import dao.NoticeBoardDAOImpl;
import dao.ResultDAO;
import dao.ResultDAOImpl;
import exception.DatabaseException;
import exception.InvalidMarkException;
import model.Assessment;
import model.LectureMaterial;
import model.Lecturer;
import model.Mark;
import model.NoticeItem;
import model.Result;
import util.CACalculator;
import util.EligibilityChecker;
import util.FileStorage;
import util.GPACalculator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LecturerDashboard extends JFrame {

    private final Lecturer lecturer;
    private final AssessmentDAO assessmentDAO = new AssessmentDAOImpl();
    private final MarkDAO markDAO = new MarkDAOImpl();
    private final ResultDAO resultDAO = new ResultDAOImpl();

    // Upload Marks tab
    private JTextField txtMarkCourse;
    private JComboBox<String> cmbAssessment;
    private JTextField txtMarkStudent;
    private JTextField txtScore;
    private List<Assessment> loadedAssessments = new ArrayList<>();
    private JComboBox<String> cmbType;
    private JTextField txtAssessNo;
    private JTextField txtWeight;
    private JTextField txtSetupCaWeight;
    private JLabel lblWeightTotal;

    // Final Results tab
    private JTextField txtResCourse;
    private JTextField txtResStudent;
    private JTextField txtFinalScore;
    private JTextField txtCaWeight;
    private JCheckBox chkRepeat;
    private JLabel lblResultSummary;

    // Notices tab
    private final NoticeBoardDAO noticeDAO = new NoticeBoardDAOImpl();
    private DefaultTableModel noticeTableModel;
    private JTable noticeTable;
    private JTextArea txtNoticeView;
    private JTextField txtNoticeTitle;
    private JComboBox<String> cmbNoticeTarget;
    private JTextArea txtNoticeContent;
    private List<NoticeItem> loadedNotices = new ArrayList<>();

    // Course Materials tab
    private final CourseMaterialDAO materialDAO = new CourseMaterialDAOImpl();
    private JTextField txtMatCourse;
    private JTextField txtMatTitle;
    private JTextField txtMatFile;
    private File selectedFile;
    private JTable materialTable;
    private DefaultTableModel materialTableModel;
    private List<LectureMaterial> loadedMaterials = new ArrayList<>();

    // Grades & GPA tab
    private JTextField txtGradeStudent;
    private DefaultTableModel gradeTableModel;
    private JLabel lblGpa;

    public LecturerDashboard(Lecturer lecturer) {
        this.lecturer = lecturer;

        setTitle("Lecturer Portal");
        setSize(900, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(createHeaderBar(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
        tabbedPane.addTab("My Profile", createProfilePanel());
        tabbedPane.addTab("Upload Marks", createUploadMarksPanel());
        tabbedPane.addTab("Final Results", createFinalResultPanel());
        tabbedPane.addTab("Grades & GPA", createGradesPanel());
        tabbedPane.addTab("Course Materials", createMaterialsPanel());

        JPanel noticesPanel = createNoticesPanel();
        tabbedPane.addTab("Notices", noticesPanel);
        // load the notices every time the Notices tab is opened
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == noticesPanel) {
                loadNotices();
            }
        });

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        add(rootPanel);
    }

    // ---------------------------------------------------------------- header

    private JPanel createHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 10));
        bar.setBackground(new Color(11, 102, 77));
        bar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblWelcome = new JLabel("Welcome, " + lecturer.getFirstName() + " " + lecturer.getLastName());
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        lblWelcome.setForeground(Color.WHITE);
        bar.add(lblWelcome, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(Color.RED);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
        bar.add(btnLogout, BorderLayout.EAST);
        return bar;
    }

    // ---------------------------------------------------------------- profile

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("My Profile Information");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(6, 2, 10, 10));
        center.add(new JLabel("Lecturer ID:"));
        center.add(new JLabel(nz(lecturer.getUserId())));
        center.add(new JLabel("Full Name:"));
        center.add(new JLabel(lecturer.getFirstName() + " " + lecturer.getLastName()));
        center.add(new JLabel("Department:"));
        center.add(new JLabel(nz(lecturer.getDepartment())));
        center.add(new JLabel("Email:"));
        center.add(new JLabel(nz(lecturer.getEmail())));
        center.add(new JLabel("Contact No:"));
        center.add(new JLabel(nz(lecturer.getContactNo())));
        center.add(new JLabel("Profile Picture:"));
        center.add(new JLabel(nz(lecturer.getProfilePic())));

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------- upload marks

    private JPanel createUploadMarksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Assessments and Marks (out of 100)");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel lblHint = new JLabel("Weightage = % of the whole course. Quizzes share one weightage "
                + "(e.g. 10) and the best " + CACalculator.BEST_QUIZZES_COUNTED + " quiz marks are counted.");
        JPanel north = new JPanel(new GridLayout(2, 1, 5, 5));
        north.add(lblTitle);
        north.add(lblHint);
        panel.add(north, BorderLayout.NORTH);

        // Part 1: create / load the assessments (Quiz, Mid Exam ...) of a course
        JPanel setup = new JPanel(new GridLayout(6, 2, 10, 10));
        setup.setBorder(BorderFactory.createTitledBorder("1. Assessments of the course"));

        txtMarkCourse = new JTextField();
        txtSetupCaWeight = new JTextField("30");
        cmbType = new JComboBox<>(new String[]{"Quiz", "Mid Exam", "Assignment", "Practical", "Presentation"});
        txtAssessNo = new JTextField();
        txtWeight = new JTextField();
        JButton btnLoad = new JButton("Load Assessments");
        JButton btnAdd = new JButton("Add Assessment");

        setup.add(new JLabel("Course ID (e.g. ICT2132):"));
        setup.add(txtMarkCourse);
        setup.add(new JLabel("CA weight of the course (%):"));
        setup.add(txtSetupCaWeight);
        setup.add(new JLabel("Assessment type:"));
        setup.add(cmbType);
        setup.add(new JLabel("Number (optional, e.g. 1):"));
        setup.add(txtAssessNo);
        setup.add(new JLabel("Weightage (% of the course):"));
        setup.add(txtWeight);
        setup.add(btnLoad);
        setup.add(btnAdd);

        // Part 2: enter marks for one assessment
        JPanel marks = new JPanel(new GridLayout(5, 2, 10, 10));
        marks.setBorder(BorderFactory.createTitledBorder("2. Enter marks"));

        cmbAssessment = new JComboBox<>();
        lblWeightTotal = new JLabel("-");
        txtMarkStudent = new JTextField();
        txtScore = new JTextField();
        JButton btnSave = new JButton("Save Mark");

        marks.add(new JLabel("Assessment:"));
        marks.add(cmbAssessment);
        marks.add(new JLabel("CA weightage added so far:"));
        marks.add(lblWeightTotal);
        marks.add(new JLabel("Student ID (e.g. tg1001):"));
        marks.add(txtMarkStudent);
        marks.add(new JLabel("Score (0 - 100):"));
        marks.add(txtScore);
        marks.add(new JLabel(""));
        marks.add(btnSave);

        btnLoad.addActionListener(e -> loadAssessments());
        btnAdd.addActionListener(e -> addAssessment());
        btnSave.addActionListener(e -> saveMark());

        JPanel wrapper = new JPanel(new GridLayout(2, 1, 10, 10));
        wrapper.add(setup);
        wrapper.add(marks);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private double getSetupCaWeight() {
        try {
            double w = Double.parseDouble(txtSetupCaWeight.getText().trim());
            return (w > 0 && w <= 100) ? w : 30;
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    private void loadAssessments() {
        String courseId = txtMarkCourse.getText().trim();
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a course ID first.");
            return;
        }
        try {
            refreshAssessments(courseId);
            if (loadedAssessments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No assessments yet for " + courseId + ". Add one using the form above.");
            }
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    /** Reloads the assessments of the course into the list, the combo box and the weightage total. */
    private void refreshAssessments(String courseId) throws DatabaseException {
        loadedAssessments = assessmentDAO.getAssessmentsByCourse(courseId);
        cmbAssessment.removeAllItems();
        for (Assessment a : loadedAssessments) {
            cmbAssessment.addItem(a.getName() + " (" + a.getWeightage() + "%)");
        }
        lblWeightTotal.setText(format(CACalculator.countedWeight(loadedAssessments))
                + " / " + format(getSetupCaWeight()));
    }

    private void addAssessment() {
        String courseId = txtMarkCourse.getText().trim();
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a course ID first.");
            return;
        }
        try {
            double weight = Double.parseDouble(txtWeight.getText().trim());
            double caWeight = getSetupCaWeight();
            if (weight <= 0 || weight > caWeight) {
                JOptionPane.showMessageDialog(this, "Weightage must be more than 0 and not more than the CA weight ("
                        + format(caWeight) + ").");
                return;
            }

            String name = (String) cmbType.getSelectedItem();
            String number = txtAssessNo.getText().trim();
            if (!number.isEmpty()) {
                name = name + " " + number;
            }
            Assessment candidate = new Assessment(0, courseId, name, weight);

            refreshAssessments(courseId);   // make sure we check against the latest data
            for (Assessment a : loadedAssessments) {
                if (a.getName().equalsIgnoreCase(name)) {
                    JOptionPane.showMessageDialog(this, name + " already exists for " + courseId);
                    return;
                }
                if (CACalculator.isQuiz(candidate) && CACalculator.isQuiz(a)
                        && Math.abs(a.getWeightage() - weight) > 0.001) {
                    JOptionPane.showMessageDialog(this, "All quizzes share one weightage ("
                            + a.getWeightage() + "). Use the same value.");
                    return;
                }
            }

            List<Assessment> after = new ArrayList<>(loadedAssessments);
            after.add(candidate);
            double newTotal = CACalculator.countedWeight(after);
            if (newTotal > caWeight + 0.001) {
                JOptionPane.showMessageDialog(this, "The CA weightage would become " + format(newTotal)
                        + ", which is more than the CA weight (" + format(caWeight) + ").");
                return;
            }

            assessmentDAO.addAssessment(candidate);
            refreshAssessments(courseId);
            txtAssessNo.setText("");
            txtWeight.setText("");
            JOptionPane.showMessageDialog(this, name + " added to " + courseId);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Weightage must be a number.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void saveMark() {
        int index = cmbAssessment.getSelectedIndex();
        if (index < 0 || index >= loadedAssessments.size()) {
            JOptionPane.showMessageDialog(this, "Load and select an assessment first.");
            return;
        }
        String studentId = txtMarkStudent.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a student ID.");
            return;
        }
        try {
            double score = Double.parseDouble(txtScore.getText().trim());
            Assessment assessment = loadedAssessments.get(index);
            Mark mark = new Mark(0, assessment.getAssessmentId(), studentId, score); // validates 0-100
            markDAO.addMark(mark);
            JOptionPane.showMessageDialog(this, "Mark saved for " + studentId);
            txtScore.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Score must be a number.", "Invalid input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (InvalidMarkException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid mark", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    // ---------------------------------------------------------------- final results

    private JPanel createFinalResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Calculate Final Result and Grade");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        txtResCourse = new JTextField();
        txtResStudent = new JTextField();
        txtFinalScore = new JTextField();
        txtCaWeight = new JTextField("30");
        chkRepeat = new JCheckBox("Repeat attempt (highest grade is C)");
        JButton btnCalc = new JButton("Calculate and Save");

        form.add(new JLabel("Course ID:"));
        form.add(txtResCourse);
        form.add(new JLabel("Student ID:"));
        form.add(txtResStudent);
        form.add(new JLabel("Final exam score (0 - 100):"));
        form.add(txtFinalScore);
        form.add(new JLabel("CA weight of the course (%):"));
        form.add(txtCaWeight);
        form.add(new JLabel(""));
        form.add(chkRepeat);
        form.add(new JLabel(""));
        form.add(btnCalc);

        btnCalc.addActionListener(e -> calculateAndSaveResult());

        lblResultSummary = new JLabel(" ");
        lblResultSummary.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.add(form, BorderLayout.NORTH);
        wrapper.add(lblResultSummary, BorderLayout.CENTER);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void calculateAndSaveResult() {
        String courseId = txtResCourse.getText().trim();
        String studentId = txtResStudent.getText().trim();
        if (courseId.isEmpty() || studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the course ID and the student ID.");
            return;
        }
        try {
            double finalScore = Double.parseDouble(txtFinalScore.getText().trim());
            double caWeight = Double.parseDouble(txtCaWeight.getText().trim());
            if (caWeight <= 0 || caWeight > 100) {
                JOptionPane.showMessageDialog(this, "CA weight must be more than 0 and not more than 100.");
                return;
            }

            List<Assessment> assessments = assessmentDAO.getAssessmentsByCourse(courseId);
            List<Mark> marks = markDAO.getMarksByStudent(studentId);

            double counted = CACalculator.countedWeight(assessments);
            if (Math.abs(counted - caWeight) > 0.001) {
                lblResultSummary.setText("Assessments of " + courseId + " add up to " + format(counted)
                        + " but the CA weight is " + format(caWeight) + ". Fix the assessments first. Nothing saved.");
                return;
            }

            // credits are read from the course table when results are loaded, so 0 is fine for saving
            Result result = new Result(studentId, courseId, 0, chkRepeat.isSelected());
            result.calculateCaTotal(assessments, marks, caWeight);   // best 2 quizzes are counted

            if (!EligibilityChecker.isCaEligible(result.getCaTotal())) {
                lblResultSummary.setText("CA = " + format(result.getCaTotal())
                        + " / 100 (below 40). Not eligible for the final exam. Nothing saved.");
                return;
            }

            result.setFinalExamScore(finalScore);
            result.calculateTotal(caWeight);   // also works out grade and grade point
            resultDAO.saveResult(result);

            lblResultSummary.setText("CA = " + format(result.getCaTotal()) + " / 100 ("
                    + format(result.getCaTotal() * caWeight / 100.0) + " / " + format(caWeight) + ")"
                    + "   Total = " + format(result.getTotalScore())
                    + "   Grade = " + result.getGrade().getLabel()
                    + "   GPV = " + result.getGradePoint() + "   (saved)");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Final score and CA weight must be numbers.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidMarkException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid mark", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    // ---------------------------------------------------------------- grades and GPA

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Student Grades and GPA");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        txtGradeStudent = new JTextField(12);
        JButton btnView = new JButton("View Grades");
        top.add(new JLabel("Student ID:"));
        top.add(txtGradeStudent);
        top.add(btnView);

        JPanel north = new JPanel(new BorderLayout());
        north.add(lblTitle, BorderLayout.NORTH);
        north.add(top, BorderLayout.CENTER);
        panel.add(north, BorderLayout.NORTH);

        String[] columns = {"Course", "Credits", "CA", "Final Exam", "Total", "Grade", "GPV"};
        gradeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(gradeTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        lblGpa = new JLabel(" ");
        lblGpa.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(lblGpa, BorderLayout.SOUTH);

        btnView.addActionListener(e -> viewGrades());
        return panel;
    }

    private void viewGrades() {
        String studentId = txtGradeStudent.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a student ID.");
            return;
        }
        try {
            List<Result> results = resultDAO.getResultsByStudent(studentId);
            gradeTableModel.setRowCount(0);
            for (Result r : results) {
                gradeTableModel.addRow(new Object[]{
                        r.getCourseId(),
                        r.getCredits(),
                        format(r.getCaTotal()),
                        format(r.getFinalExamScore()),
                        format(r.getTotalScore()),
                        r.getGrade() == null ? "-" : r.getGrade().getLabel(),
                        r.getGradePoint()
                });
            }
            if (results.isEmpty()) {
                lblGpa.setText("No results found for " + studentId);
            } else {
                lblGpa.setText("GPA (all results shown) = " + format(GPACalculator.calculateCGPA(results)));
            }
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    // ---------------------------------------------------------------- course materials

    private JPanel createMaterialsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Course Materials (lecture notes)");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle, BorderLayout.NORTH);

        txtMatCourse = new JTextField();
        txtMatTitle = new JTextField();
        txtMatFile = new JTextField();
        txtMatFile.setEditable(false);
        JButton btnBrowse = new JButton("Browse...");

        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.add(txtMatFile, BorderLayout.CENTER);
        filePanel.add(btnBrowse, BorderLayout.EAST);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.add(new JLabel("Course ID (e.g. ICT2132):"));
        form.add(txtMatCourse);
        form.add(new JLabel("Title:"));
        form.add(txtMatTitle);
        form.add(new JLabel("File:"));
        form.add(filePanel);

        String[] columns = {"ID", "Title", "File", "Uploaded"};
        materialTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        materialTable = new JTable(materialTableModel);
        materialTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton btnLoad = new JButton("Load Materials");
        JButton btnUpload = new JButton("Upload New");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnOpen = new JButton("Open File");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttons.add(btnLoad);
        buttons.add(btnUpload);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnOpen);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(form, BorderLayout.NORTH);
        center.add(new JScrollPane(materialTable), BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);

        btnBrowse.addActionListener(e -> browseFile());
        btnLoad.addActionListener(e -> loadMaterials());
        btnUpload.addActionListener(e -> uploadMaterial());
        btnUpdate.addActionListener(e -> updateMaterial());
        btnDelete.addActionListener(e -> deleteMaterial());
        btnOpen.addActionListener(e -> openMaterial());
        materialTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onMaterialSelected();
            }
        });
        return panel;
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            txtMatFile.setText(selectedFile.getAbsolutePath());
            if (txtMatTitle.getText().trim().isEmpty()) {
                txtMatTitle.setText(selectedFile.getName());
            }
        }
    }

    private void loadMaterials() {
        String courseId = txtMatCourse.getText().trim();
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a course ID first.");
            return;
        }
        try {
            refreshMaterials(courseId);
            if (loadedMaterials.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No materials yet for " + courseId);
            }
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void refreshMaterials(String courseId) throws DatabaseException {
        loadedMaterials = materialDAO.getMaterialsByCourse(courseId);
        materialTableModel.setRowCount(0);
        for (LectureMaterial m : loadedMaterials) {
            materialTableModel.addRow(new Object[]{
                    m.getMaterialId(),
                    m.getTitle(),
                    FileStorage.displayName(m.getFilePath()),
                    m.getUploadedDate()
            });
        }
    }

    private void onMaterialSelected() {
        int row = materialTable.getSelectedRow();
        if (row >= 0 && row < loadedMaterials.size()) {
            txtMatTitle.setText(loadedMaterials.get(row).getTitle());
            selectedFile = null;
            txtMatFile.setText("");
        }
    }

    private void clearMaterialForm() {
        txtMatTitle.setText("");
        txtMatFile.setText("");
        selectedFile = null;
    }

    private void uploadMaterial() {
        String courseId = txtMatCourse.getText().trim();
        String title = txtMatTitle.getText().trim();
        if (courseId.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the course ID and a title.");
            return;
        }
        if (selectedFile == null || !selectedFile.isFile()) {
            JOptionPane.showMessageDialog(this, "Choose a file with the Browse button first.");
            return;
        }
        try {
            String savedPath = FileStorage.saveCopy(selectedFile, courseId);
            try {
                materialDAO.addMaterial(new LectureMaterial(0, courseId, title, savedPath, null));
            } catch (DatabaseException ex) {
                FileStorage.delete(savedPath);   // do not keep a file that is not in the database
                throw ex;
            }
            refreshMaterials(courseId);
            clearMaterialForm();
            JOptionPane.showMessageDialog(this, "Material uploaded for " + courseId);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not copy the file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void updateMaterial() {
        int row = materialTable.getSelectedRow();
        if (row < 0 || row >= loadedMaterials.size()) {
            JOptionPane.showMessageDialog(this, "Select a material from the table first.");
            return;
        }
        String title = txtMatTitle.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The title cannot be empty.");
            return;
        }
        LectureMaterial old = loadedMaterials.get(row);
        String newPath = old.getFilePath();
        try {
            boolean replaceFile = selectedFile != null;
            if (replaceFile) {
                if (!selectedFile.isFile()) {
                    JOptionPane.showMessageDialog(this, "The chosen file does not exist.");
                    return;
                }
                newPath = FileStorage.saveCopy(selectedFile, old.getCourseId());
            }
            try {
                materialDAO.updateMaterial(old.getMaterialId(), title, newPath);
            } catch (DatabaseException ex) {
                if (replaceFile) {
                    FileStorage.delete(newPath);
                }
                throw ex;
            }
            if (replaceFile) {
                FileStorage.delete(old.getFilePath());   // remove the replaced file
            }
            refreshMaterials(old.getCourseId());
            clearMaterialForm();
            JOptionPane.showMessageDialog(this, "Material updated.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not copy the file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void deleteMaterial() {
        int row = materialTable.getSelectedRow();
        if (row < 0 || row >= loadedMaterials.size()) {
            JOptionPane.showMessageDialog(this, "Select a material from the table first.");
            return;
        }
        LectureMaterial material = loadedMaterials.get(row);
        int option = JOptionPane.showConfirmDialog(this,
                "Delete \"" + material.getTitle() + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            materialDAO.deleteMaterial(material.getMaterialId());
            FileStorage.delete(material.getFilePath());
            refreshMaterials(material.getCourseId());
            clearMaterialForm();
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void openMaterial() {
        int row = materialTable.getSelectedRow();
        if (row < 0 || row >= loadedMaterials.size()) {
            JOptionPane.showMessageDialog(this, "Select a material from the table first.");
            return;
        }
        File file = FileStorage.getFile(loadedMaterials.get(row).getFilePath());
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File not found: " + file.getPath());
            return;
        }
        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, "Opening files is not supported on this computer.");
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open the file: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------- notices

    private JPanel createNoticesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Notices");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Part 1: notices for lecturers (and for everyone)
        String[] columns = {"ID", "Title", "For", "Posted"};
        noticeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        noticeTable = new JTable(noticeTableModel);
        noticeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtNoticeView = new JTextArea(4, 20);
        txtNoticeView.setEditable(false);
        txtNoticeView.setLineWrap(true);
        txtNoticeView.setWrapStyleWord(true);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadNotices());
        noticeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onNoticeSelected();
            }
        });

        JPanel viewPanel = new JPanel(new BorderLayout(5, 5));
        viewPanel.setBorder(BorderFactory.createTitledBorder("1. Notices for lecturers"));
        viewPanel.add(new JScrollPane(noticeTable), BorderLayout.CENTER);
        JPanel viewBottom = new JPanel(new BorderLayout(5, 5));
        viewBottom.add(new JScrollPane(txtNoticeView), BorderLayout.CENTER);
        viewBottom.add(btnRefresh, BorderLayout.EAST);
        viewPanel.add(viewBottom, BorderLayout.SOUTH);

        // Part 2: post a new notice
        txtNoticeTitle = new JTextField();
        cmbNoticeTarget = new JComboBox<>(new String[]{"ALL", "STUDENT", "LECTURER", "TECHNICAL_OFFICER"});
        txtNoticeContent = new JTextArea(3, 20);
        txtNoticeContent.setLineWrap(true);
        txtNoticeContent.setWrapStyleWord(true);
        JButton btnPost = new JButton("Post Notice");
        btnPost.addActionListener(e -> postNotice());

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.add(new JLabel("Title:"));
        form.add(txtNoticeTitle);
        form.add(new JLabel("Send to:"));
        form.add(cmbNoticeTarget);

        JPanel postPanel = new JPanel(new BorderLayout(5, 5));
        postPanel.setBorder(BorderFactory.createTitledBorder("2. Post a notice"));
        postPanel.add(form, BorderLayout.NORTH);
        postPanel.add(new JScrollPane(txtNoticeContent), BorderLayout.CENTER);
        postPanel.add(btnPost, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new GridLayout(2, 1, 10, 10));
        wrapper.add(viewPanel);
        wrapper.add(postPanel);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void loadNotices() {
        try {
            loadedNotices = noticeDAO.getNoticesForRole(lecturer.getRole());
            noticeTableModel.setRowCount(0);
            for (NoticeItem n : loadedNotices) {
                noticeTableModel.addRow(new Object[]{
                        n.getNoticeId(), n.getTitle(), n.getTargetRole(), n.getPostedDate()
                });
            }
            txtNoticeView.setText("");
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    private void onNoticeSelected() {
        int row = noticeTable.getSelectedRow();
        if (row >= 0 && row < loadedNotices.size()) {
            txtNoticeView.setText(loadedNotices.get(row).getContent());
            txtNoticeView.setCaretPosition(0);
        }
    }

    private void postNotice() {
        String title = txtNoticeTitle.getText().trim();
        String body = txtNoticeContent.getText().trim();
        if (title.isEmpty() || body.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a title and the notice text.");
            return;
        }
        if (title.length() > 100) {
            JOptionPane.showMessageDialog(this, "The title can have at most 100 characters.");
            return;
        }
        String target = (String) cmbNoticeTarget.getSelectedItem();
        // the notice table has no "posted by" column, so the name is added to the text
        String content = body + "\n\n- Posted by " + lecturer.getFirstName() + " "
                + lecturer.getLastName() + " (Lecturer)";
        try {
            noticeDAO.addNotice(new NoticeItem(0, title, content, null, target));
            txtNoticeTitle.setText("");
            txtNoticeContent.setText("");
            loadNotices();
            JOptionPane.showMessageDialog(this, "Notice posted to " + target);
        } catch (DatabaseException ex) {
            showError(ex);
        }
    }

    // ---------------------------------------------------------------- helpers

    private String nz(String value) {
        return value == null ? "-" : value;
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }

    private void showError(Exception ex) {
        String message = ex.getMessage();
        if (ex.getCause() != null) {
            message += "\n" + ex.getCause().getMessage();
        }
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Temporary test lecturer - replace with the logged-in lecturer once the login screen is ready
            Lecturer lec = new Lecturer("lec1", "Kamal", "Silva",
                    "kamal@fot.ruh.ac.lk", null, null, "ICT");
            new LecturerDashboard(lec).setVisible(true);
        });
    }
}
