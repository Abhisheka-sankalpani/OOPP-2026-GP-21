package view;

import dao.CourseDAO;
import dao.NoticeDAO;
import dao.TimetableDAO;
import dao.UserDAO;
import model.Admin;
import model.Notice;
import model.User;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class DashboardHomePanel extends JPanel {

    private static final Color BG = new Color(245, 247, 250);
    private static final Color TEXT = new Color(18, 30, 52);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color GREEN = new Color(16, 185, 129);
    private static final Color ORANGE = new Color(245, 158, 11);
    private static final Color PURPLE = new Color(124, 58, 237);
    private static final Border CARD_BORDER = new LineBorder(new Color(217, 225, 235));
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Consumer<String> navigate;
    private final UserDAO userDAO = new UserDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final NoticeDAO noticeDAO = new NoticeDAO();
    private final TimetableDAO timetableDAO = new TimetableDAO();

    private final JLabel usersValue = statValueLabel();
    private final JLabel coursesValue = statValueLabel();
    private final JLabel noticesValue = statValueLabel();
    private final JLabel timetablesValue = statValueLabel();
    private final JLabel userBreakdown = mutedLabel("Lecturers | TO | Students");
    private final JLabel databaseStatus = new JLabel("Checking database connection...");
    private final JButton refreshButton = secondaryButton("Refresh Stats");

    private final DefaultTableModel noticeTableModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Target Audience", "Posted Date"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };

    public DashboardHomePanel(Admin admin, Consumer<String> navigate) {
        this.navigate = navigate;
        setLayout(new BorderLayout(0, 18));
        setBackground(BG);
        setBorder(new EmptyBorder(26, 32, 24, 32));

        JPanel upper = new JPanel();
        upper.setOpaque(false);
        upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
        upper.add(createWelcome(admin));
        upper.add(Box.createVerticalStrut(22));
        upper.add(createStatsRow());
        upper.add(Box.createVerticalStrut(24));
        upper.add(createInfoRow());
        add(upper, BorderLayout.NORTH);
        add(createLatestNotices(), BorderLayout.CENTER);
    }

    private JPanel createWelcome(Admin admin) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel heading = new JLabel("Welcome back, " + admin.getFirstName());
        heading.setFont(new Font("Segoe UI", Font.BOLD, 27));
        heading.setForeground(TEXT);
        JLabel sub = mutedLabel("Faculty of Technology Management Information System | Admin Control Center");
        text.add(heading);
        text.add(Box.createVerticalStrut(5));
        text.add(sub);

        refreshButton.addActionListener(e -> refreshData());
        panel.add(text, BorderLayout.WEST);
        panel.add(refreshButton, BorderLayout.EAST);
        return panel;
    }

    private JPanel createStatsRow() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 18, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 153));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(createStatCard("Total Users", usersValue, userBreakdown, BLUE));
        panel.add(createStatCard("Total Courses", coursesValue, mutedLabel("Modules in syllabus"), GREEN));
        panel.add(createStatCard("Notices", noticesValue, mutedLabel("Active faculty notices"), ORANGE));
        panel.add(createStatCard("Timetable Sessions", timetablesValue, mutedLabel("Scheduled lab / lectures"), PURPLE));
        return panel;
    }

    private JPanel createStatCard(String title, JLabel number, JLabel detail, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(CARD_BORDER);

        JPanel strip = new JPanel();
        strip.setBackground(accent);
        strip.setPreferredSize(new Dimension(5, 0));
        card.add(strip, BorderLayout.WEST);

        JPanel inside = new JPanel();
        inside.setLayout(new BoxLayout(inside, BoxLayout.Y_AXIS));
        inside.setBackground(Color.WHITE);
        inside.setBorder(new EmptyBorder(20, 20, 14, 12));
        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(MUTED);
        inside.add(header);
        inside.add(Box.createVerticalStrut(12));
        inside.add(number);
        inside.add(Box.createVerticalStrut(10));
        inside.add(detail);
        card.add(inside, BorderLayout.CENTER);
        return card;
    }

    private JPanel createInfoRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 18, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));

        JPanel health = panelCard();
        health.setLayout(new BoxLayout(health, BoxLayout.Y_AXIS));
        JLabel healthTitle = sectionLabel("System & Database Health");
        health.add(healthTitle);
        health.add(Box.createVerticalStrut(22));
        databaseStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        databaseStatus.setForeground(MUTED);
        health.add(databaseStatus);
        row.add(health);

        JPanel actions = panelCard();
        actions.setLayout(new BorderLayout(0, 16));
        actions.add(sectionLabel("Quick Management Shortcuts"), BorderLayout.NORTH);
        JPanel shortcuts = new JPanel(new GridLayout(1, 4, 10, 0));
        shortcuts.setOpaque(false);
        shortcuts.add(shortcut("+ User", "USERS"));
        shortcuts.add(shortcut("+ Course", "COURSES"));
        shortcuts.add(shortcut("+ Notice", "NOTICES"));
        shortcuts.add(shortcut("+ Schedule", "TIMETABLE"));
        actions.add(shortcuts, BorderLayout.CENTER);
        row.add(actions);
        return row;
    }

    private JPanel createLatestNotices() {
        JPanel panel = panelCard();
        panel.setLayout(new BorderLayout(0, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Latest Notices & Announcements"), BorderLayout.WEST);
        JButton viewAll = secondaryButton("View All Notices  →");
        viewAll.addActionListener(e -> navigate.accept("NOTICES"));
        header.add(viewAll, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JTable table = new JTable(noticeTableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(39);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(239, 243, 248));
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));

        DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
        centered.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centered);
        table.getColumnModel().getColumn(2).setCellRenderer(centered);
        table.getColumnModel().getColumn(3).setCellRenderer(centered);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(310);
        table.getColumnModel().getColumn(2).setPreferredWidth(155);
        table.getColumnModel().getColumn(3).setPreferredWidth(170);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(217, 225, 235)));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        if (!refreshButton.isEnabled()) return;
        refreshButton.setEnabled(false);
        databaseStatus.setForeground(MUTED);
        databaseStatus.setText("Refreshing data from MySQL...");

        SwingWorker<Snapshot, Void> worker = new SwingWorker<>() {
            @Override
            protected Snapshot doInBackground() throws Exception {
                Snapshot snapshot = new Snapshot();
                List<User> allUsers = userDAO.getAllUsers();
                snapshot.users = allUsers.size();
                for (User user : allUsers) {
                    String role = user.getRole();
                    if ("LECTURER".equals(role)) snapshot.lecturers++;
                    if ("TECHNICAL_OFFICER".equals(role)) snapshot.officers++;
                    if ("STUDENT".equals(role)) snapshot.students++;
                }
                snapshot.courses = courseDAO.getAllCourses().size();
                snapshot.notices = noticeDAO.getTotalNoticesCount();
                snapshot.timetables = timetableDAO.getTotalTimetableCount();
                snapshot.recent = noticeDAO.getRecentNotices(5);
                return snapshot;
            }

            @Override
            protected void done() {
                try {
                    Snapshot data = get();
                    usersValue.setText(String.valueOf(data.users));
                    coursesValue.setText(String.valueOf(data.courses));
                    noticesValue.setText(String.valueOf(data.notices));
                    timetablesValue.setText(String.valueOf(data.timetables));
                    userBreakdown.setText("Lec: " + data.lecturers + " | TO: " + data.officers
                            + " | Stu: " + data.students);
                    databaseStatus.setForeground(new Color(0, 145, 90));
                    databaseStatus.setText("● Online — Connected to MySQL database");
                    noticeTableModel.setRowCount(0);
                    for (Notice n : data.recent) {
                        noticeTableModel.addRow(new Object[]{
                                n.getNoticeId(), n.getTitle(), n.getTargetRole(), formatDate(n.getPostedDate())
                        });
                    }
                } catch (Exception ex) {
                    databaseStatus.setForeground(new Color(190, 40, 50));
                    databaseStatus.setText("Unable to load data. Check MySQL connection.");
                    JOptionPane.showMessageDialog(DashboardHomePanel.this,
                            "Could not refresh the dashboard. Please check the database and try again.",
                            "Dashboard Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private static String formatDate(Timestamp stamp) {
        return stamp == null ? "" : stamp.toLocalDateTime().format(DATE_FORMAT);
    }

    private static JLabel statValueLabel() {
        JLabel label = new JLabel("—");
        label.setFont(new Font("Segoe UI", Font.BOLD, 34));
        label.setForeground(TEXT);
        return label;
    }

    private static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(MUTED);
        return label;
    }

    private static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(TEXT);
        return label;
    }

    private static JPanel panelCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(CARD_BORDER, new EmptyBorder(19, 20, 19, 20)));
        return panel;
    }

    private JButton shortcut(String text, String target) {

        JButton button = new JButton(text);

        // Ensure custom button colors work on Windows
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        // Font
        button.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        // Background and Text Colors
        button.setBackground(new Color(37, 99, 235));
        button.setForeground(Color.WHITE);

        // Button Appearance
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        button.setBorder(
                new EmptyBorder(10, 8, 10, 8)
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        // Navigation
        button.addActionListener(
                e -> navigate.accept(target)
        );

        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT);
        button.setBackground(new Color(237, 242, 248));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 16, 11, 16));
        return button;
    }

    private static class Snapshot {
        int users;
        int lecturers;
        int officers;
        int students;
        int courses;
        int notices;
        int timetables;
        List<Notice> recent;
    }
}
