package view;

import model.Admin;
import model.User;
import service.SessionManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import java.io.File;

import java.util.LinkedHashMap;
import java.util.Map;


// =====================================================
// ADMIN DASHBOARD
// =====================================================

public class AdminDashboard extends JFrame {

    // =====================================================
    // COLORS
    // =====================================================

    private static final Color HEADER_COLOR =
            new Color(27, 54, 91);

    private static final Color SIDEBAR_COLOR =
            new Color(15, 23, 42);

    private static final Color ACTIVE_COLOR =
            new Color(37, 99, 235);

    private static final Color BACKGROUND_COLOR =
            new Color(245, 247, 250);

    private static final Color TEXT_LIGHT =
            new Color(210, 220, 235);

    private static final Color LOGOUT_COLOR =
            new Color(239, 68, 68);

    // =====================================================
    // FONTS
    // =====================================================

    private static final Font BODY_FONT =
            new Font("Segoe UI", Font.BOLD, 13);

    private static final Font TITLE_FONT =
            new Font("Segoe UI", Font.BOLD, 20);


    // =====================================================
    // ATTRIBUTES
    // =====================================================

    private final Admin admin;

    private final CardLayout cardLayout =
            new CardLayout();

    private final JPanel contentPanel =
            new JPanel(cardLayout);

    private final Map<String, JButton> navButtons =
            new LinkedHashMap<>();


    // =====================================================
    // MANAGEMENT PANELS
    // =====================================================

    private DashboardHomePanel homePanel;

    private UserManagementPanel userPanel;

    private CourseManagementPanel coursePanel;

    private NoticeManagementPanel noticePanel;

    private TimetableManagementPanel timetablePanel;

    private ProfilePanel profilePanel;


    // =====================================================
    // HEADER COMPONENTS
    // =====================================================

    private JLabel adminNameLabel;

    private JLabel profilePhotoLabel;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AdminDashboard(Admin admin) {

        SessionManager session =
                SessionManager.getInstance();

        User currentUser =
                session.getCurrentUser();

        // Check Admin Permission
        if (admin == null ||
                currentUser == null ||
                !session.isAdmin() ||
                !admin.getUserId().equals(currentUser.getUserId())) {

            throw new SecurityException(
                    "Administrator access required."
            );
        }

        this.admin = admin;

        initializeUI();

        switchCard("DASHBOARD");
    }


    // =====================================================
    // INITIALIZE USER INTERFACE
    // =====================================================

    private void initializeUI() {

        setTitle(
                "FOT-MIS | Administrator Dashboard"
        );

        setSize(1360, 900);

        setMinimumSize(
                new Dimension(1080, 700)
        );

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.DO_NOTHING_ON_CLOSE
        );


        // Window Closing Event
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                confirmLogout();
            }
        });


        // Root Panel
        JPanel root = new JPanel(
                new BorderLayout()
        );

        root.setBackground(BACKGROUND_COLOR);


        // Header
        root.add(
                createHeader(),
                BorderLayout.NORTH
        );


        // Sidebar
        root.add(
                createSidebar(),
                BorderLayout.WEST
        );


        // =================================================
        // INITIALIZE MANAGEMENT PANELS
        // =================================================

        contentPanel.setBackground(BACKGROUND_COLOR);


        // Dashboard Overview
        homePanel = new DashboardHomePanel(
                admin,
                this::switchCard
        );


        // User Management
        userPanel = new UserManagementPanel();


        // Course Management
        coursePanel = new CourseManagementPanel();


        // Notice Management
        noticePanel = new NoticeManagementPanel();


        // Timetable Management
        timetablePanel = new TimetableManagementPanel();


        // My Profile
        profilePanel = new ProfilePanel();


        // =================================================
        // ADD PANELS TO CARDLAYOUT
        // =================================================

        contentPanel.add(
                homePanel,
                "DASHBOARD"
        );

        contentPanel.add(
                userPanel,
                "USERS"
        );

        contentPanel.add(
                coursePanel,
                "COURSES"
        );

        contentPanel.add(
                noticePanel,
                "NOTICES"
        );

        contentPanel.add(
                timetablePanel,
                "TIMETABLE"
        );

        contentPanel.add(
                profilePanel,
                "PROFILE"
        );


        root.add(
                contentPanel,
                BorderLayout.CENTER
        );

        setContentPane(root);
    }


    // =====================================================
    // CREATE HEADER
    // =====================================================

    private JPanel createHeader() {

        JPanel header = new JPanel(
                new BorderLayout()
        );

        header.setBackground(HEADER_COLOR);

        header.setBorder(
                new EmptyBorder(12, 30, 12, 28)
        );

        header.setPreferredSize(
                new Dimension(0, 74)
        );


        // =================================================
        // LEFT SIDE - UNIVERSITY BRANDING
        // =================================================

        JPanel branding = new JPanel();

        branding.setLayout(
                new BoxLayout(
                        branding,
                        BoxLayout.Y_AXIS
                )
        );

        branding.setOpaque(false);


        JLabel title = new JLabel(
                "FACULTY OF TECHNOLOGY MIS"
        );

        title.setFont(TITLE_FONT);

        title.setForeground(Color.WHITE);


        JLabel subtitle = new JLabel(
                "University of Ruhuna, Sri Lanka | Admin Workspace"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(
                new Color(222, 233, 248)
        );


        branding.add(title);

        branding.add(
                Box.createVerticalStrut(3)
        );

        branding.add(subtitle);


        header.add(
                branding,
                BorderLayout.WEST
        );


        // =================================================
        // RIGHT SIDE - ADMIN PROFILE
        // =================================================

        JPanel right = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        15,
                        0
                )
        );

        right.setOpaque(false);


        // =================================================
        // PROFILE PICTURE
        // =================================================

        profilePhotoLabel = new JLabel();

        profilePhotoLabel.setPreferredSize(
                new Dimension(42, 42)
        );

        profilePhotoLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        profilePhotoLabel.setVerticalAlignment(
                SwingConstants.CENTER
        );

        profilePhotoLabel.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        profilePhotoLabel.setToolTipText(
                "Click to view My Profile"
        );


        // Click Profile Picture to Open Profile
        profilePhotoLabel.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        switchCard("PROFILE");
                    }
                }
        );


        // =================================================
        // ADMIN NAME
        // =================================================

        adminNameLabel = new JLabel();

        adminNameLabel.setFont(BODY_FONT);

        adminNameLabel.setForeground(Color.WHITE);

        adminNameLabel.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        adminNameLabel.setToolTipText(
                "Click to view My Profile"
        );


        adminNameLabel.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        switchCard("PROFILE");
                    }
                }
        );


        // =================================================
        // SIGN OUT BUTTON
        // =================================================

        JButton signOutButton =
                new JButton("Sign Out");

        styleButton(
                signOutButton,
                LOGOUT_COLOR,
                Color.WHITE
        );

        signOutButton.setBorder(
                new EmptyBorder(12, 22, 12, 22)
        );

        signOutButton.addActionListener(
                e -> confirmLogout()
        );


        // Add Components
        right.add(profilePhotoLabel);

        right.add(adminNameLabel);

        right.add(signOutButton);


        header.add(
                right,
                BorderLayout.EAST
        );


        // Load Logged-in User Details
        refreshHeaderProfile();

        return header;
    }


    // =====================================================
    // REFRESH HEADER PROFILE
    // =====================================================

    public void refreshHeaderProfile() {

        User currentUser =
                SessionManager
                        .getInstance()
                        .getCurrentUser();

        if (currentUser == null ||
                profilePhotoLabel == null ||
                adminNameLabel == null) {

            return;
        }


        // =================================================
        // UPDATE ADMIN NAME
        // =================================================

        String fullName =
                currentUser.getFullname();

        if (fullName == null || fullName.isBlank()) {

            fullName = currentUser.getUserId();
        }

        adminNameLabel.setText(
                fullName +
                        " (" +
                        currentUser.getUserId() +
                        ")"
        );


        // =================================================
        // CREATE CIRCULAR AVATAR
        // =================================================

        int size = 40;

        BufferedImage avatar =
                new BufferedImage(
                        size,
                        size,
                        BufferedImage.TYPE_INT_ARGB
                );

        Graphics2D g = avatar.createGraphics();


        // Smooth Rendering
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );


        // Circular Clip
        g.setClip(
                new Ellipse2D.Double(
                        0,
                        0,
                        size,
                        size
                )
        );


        boolean photoLoaded = false;


        // =================================================
        // LOAD PROFILE PHOTO
        // =================================================

        String photoPath =
                currentUser.getProfilePic();

        if (photoPath != null &&
                !photoPath.isBlank()) {

            try {

                File photoFile =
                        new File(photoPath);

                if (photoFile.exists() &&
                        photoFile.isFile()) {

                    BufferedImage image =
                            ImageIO.read(photoFile);

                    if (image != null) {

                        // Center Crop
                        int cropSize = Math.min(
                                image.getWidth(),
                                image.getHeight()
                        );

                        int cropX =
                                (image.getWidth() - cropSize) / 2;

                        int cropY =
                                (image.getHeight() - cropSize) / 2;


                        // Draw Image Inside Circle
                        g.drawImage(
                                image,

                                0,
                                0,
                                size,
                                size,

                                cropX,
                                cropY,
                                cropX + cropSize,
                                cropY + cropSize,

                                null
                        );

                        photoLoaded = true;
                    }
                }

            } catch (Exception ex) {

                System.out.println(
                        "Unable to load profile image."
                );
            }
        }


        // =================================================
        // DEFAULT AVATAR
        // =================================================

        if (!photoLoaded) {

            // Blue Background
            g.setColor(
                    new Color(37, 99, 235)
            );

            g.fillOval(
                    0,
                    0,
                    size,
                    size
            );


            // White Initial
            g.setColor(Color.WHITE);

            g.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            18
                    )
            );


            String firstName =
                    currentUser.getFirstName();

            String initials = "U";

            if (firstName != null &&
                    !firstName.isBlank()) {

                initials =
                        firstName.substring(0, 1)
                                .toUpperCase();
            }


            FontMetrics fm =
                    g.getFontMetrics();

            int textX =
                    (size - fm.stringWidth(initials)) / 2;

            int textY =
                    (size - fm.getHeight()) / 2
                            + fm.getAscent();


            g.drawString(
                    initials,
                    textX,
                    textY
            );
        }


        g.dispose();


        // =================================================
        // DISPLAY PROFILE PICTURE
        // =================================================

        profilePhotoLabel.setIcon(
                new ImageIcon(avatar)
        );

        profilePhotoLabel.setText("");

        profilePhotoLabel.repaint();

        adminNameLabel.repaint();
    }


    // =====================================================
    // CREATE SIDEBAR
    // =====================================================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(
                new BorderLayout()
        );

        sidebar.setPreferredSize(
                new Dimension(265, 0)
        );

        sidebar.setBackground(
                SIDEBAR_COLOR
        );


        // =================================================
        // NAVIGATION PANEL
        // =================================================

        JPanel nav = new JPanel();

        nav.setLayout(
                new BoxLayout(
                        nav,
                        BoxLayout.Y_AXIS
                )
        );

        nav.setOpaque(false);

        nav.setBorder(
                new EmptyBorder(24, 10, 10, 10)
        );


        // Navigation Heading
        JLabel heading = new JLabel(
                "  NAVIGATION MENU"
        );

        heading.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        heading.setForeground(
                new Color(148, 163, 184)
        );

        heading.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        nav.add(heading);

        nav.add(
                Box.createVerticalStrut(16)
        );


        // =================================================
        // NAVIGATION BUTTONS
        // =================================================

        addNavButton(
                nav,
                "Dashboard Overview",
                "DASHBOARD"
        );

        addNavButton(
                nav,
                "User Management",
                "USERS"
        );

        addNavButton(
                nav,
                "Course Management",
                "COURSES"
        );

        addNavButton(
                nav,
                "Notice Board",
                "NOTICES"
        );

        addNavButton(
                nav,
                "Timetable Schedule",
                "TIMETABLE"
        );

        addNavButton(
                nav,
                "My Profile",
                "PROFILE"
        );


        sidebar.add(
                nav,
                BorderLayout.NORTH
        );


        // =================================================
        // SIDEBAR SIGN OUT
        // =================================================

        JPanel bottom = new JPanel(
                new BorderLayout()
        );

        bottom.setOpaque(false);

        bottom.setBorder(
                new EmptyBorder(10, 12, 15, 12)
        );


        JButton bottomSignOut =
                new JButton("Sign Out");

        styleButton(
                bottomSignOut,
                new Color(31, 41, 59),
                new Color(252, 95, 95)
        );

        bottomSignOut.setBorder(
                new EmptyBorder(12, 14, 12, 14)
        );

        bottomSignOut.addActionListener(
                e -> confirmLogout()
        );


        bottom.add(
                bottomSignOut,
                BorderLayout.CENTER
        );


        sidebar.add(
                bottom,
                BorderLayout.SOUTH
        );

        return sidebar;
    }


    // =====================================================
    // ADD NAVIGATION BUTTON
    // =====================================================

    private void addNavButton(
            JPanel parent,
            String text,
            String key) {

        JButton button = new JButton(
                "   " + text
        );

        styleButton(
                button,
                SIDEBAR_COLOR,
                TEXT_LIGHT
        );

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        50
                )
        );

        button.setPreferredSize(
                new Dimension(240, 50)
        );

        button.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );


        // Open Selected Page
        button.addActionListener(
                e -> switchCard(key)
        );


        navButtons.put(
                key,
                button
        );


        parent.add(button);

        parent.add(
                Box.createVerticalStrut(7)
        );
    }


    // =====================================================
    // BUTTON STYLING
    // =====================================================

    private void styleButton(
            JButton button,
            Color background,
            Color foreground) {

        // Prevent Windows Look and Feel
        // from overriding custom button colors
        button.setUI(
                new BasicButtonUI()
        );

        button.setFont(BODY_FONT);

        button.setBackground(background);

        button.setForeground(foreground);

        button.setOpaque(true);

        button.setContentAreaFilled(true);

        button.setBorderPainted(false);

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setBorder(
                new EmptyBorder(12, 15, 12, 15)
        );
    }


    // =====================================================
    // SWITCH BETWEEN PAGES
    // =====================================================

    public void switchCard(String cardName) {

        // Check Admin Session
        if (!SessionManager
                .getInstance()
                .isAdmin()) {

            throw new SecurityException(
                    "Administrator session required."
            );
        }


        // Validate Card
        if (!navButtons.containsKey(cardName)) {

            return;
        }


        // Refresh Header Profile
        refreshHeaderProfile();


        // Show Selected Card
        cardLayout.show(
                contentPanel,
                cardName
        );


        // =================================================
        // UPDATE SIDEBAR SELECTED BUTTON
        // =================================================

        for (Map.Entry<String, JButton> item :
                navButtons.entrySet()) {

            boolean active =
                    item.getKey().equals(cardName);

            JButton button =
                    item.getValue();

            button.setBackground(
                    active
                            ? ACTIVE_COLOR
                            : SIDEBAR_COLOR
            );

            button.setForeground(
                    active
                            ? Color.WHITE
                            : TEXT_LIGHT
            );
        }


        // =================================================
        // LOAD PAGE DATA
        // =================================================

        switch (cardName) {

            // Dashboard Overview
            case "DASHBOARD":

                homePanel.refreshData();

                break;


            // User Management
            case "USERS":

                userPanel.loadUsers();

                break;


            // Course Management
            case "COURSES":

                coursePanel.loadCourses();

                break;


            // Notice Management
            case "NOTICES":

                noticePanel.loadNotices();

                break;


            // Timetable Management
            case "TIMETABLE":

                timetablePanel.loadCoursesIntoCombo();

                timetablePanel.loadTimetable();

                break;


            // My Profile
            case "PROFILE":

                profilePanel.refreshProfile();

                break;


            default:

                break;
        }
    }


    // =====================================================
    // LOGOUT CONFIRMATION
    // =====================================================

    private void confirmLogout() {

        int choice =
                JOptionPane.showConfirmDialog(
                        this,

                        "Are you sure you want to sign out?",

                        "Confirm Sign Out",

                        JOptionPane.YES_NO_OPTION,

                        JOptionPane.QUESTION_MESSAGE
                );


        if (choice == JOptionPane.YES_OPTION) {

            // Clear User Session
            SessionManager
                    .getInstance()
                    .logout();


            // Close Dashboard
            dispose();


            // Open Login Screen
            new LoginFrame().setVisible(true);
        }
    }
}