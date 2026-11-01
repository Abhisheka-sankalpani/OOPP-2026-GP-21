package gui;

import model.Lecturer;
import service.ProfileService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/** Blue-theme lecturer self-service profile. The existing ProfileService handles DB updates. */
public class LecturerProfilePanel extends JPanel {
    private static final Color NAVY = new Color(27, 54, 91);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color LIGHT_BLUE = new Color(239, 246, 255);
    private static final Color PALE_BLUE = new Color(219, 234, 254);
    private static final Color BORDER = new Color(203, 213, 225);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Font PLAIN = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024;

    private final Lecturer lecturer;
    private final ProfileService profileService = new ProfileService();

    private JTextField emailField;
    private JTextField contactField;
    private JTextField selectedFileField;
    private JButton saveButton;
    private JLabel photoLabel;
    private JLabel detailsEmail;
    private JLabel detailsContact;
    private JLabel statusLabel;

    private File pendingPhoto;
    private boolean removePhotoRequested = false;

    public LecturerProfilePanel(Lecturer lecturer) {
        this.lecturer = lecturer;
        buildUI();
        refreshFromLecturer();
    }

    private void buildUI() {
        setLayout(new BorderLayout(20, 22));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(28, 30, 34, 30));

        JPanel heading = new JPanel();
        heading.setBackground(Color.WHITE);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Update your contact details and profile picture.");
        subtitle.setFont(PLAIN);
        subtitle.setForeground(MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(8));
        heading.add(subtitle);
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(24, 0));
        center.setBackground(Color.WHITE);

        JPanel editCard = new JPanel(new BorderLayout(0, 25));
        editCard.setBackground(Color.WHITE);
        editCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(24, 24, 24, 24)));

        JPanel editContent = new JPanel();
        editContent.setBackground(Color.WHITE);
        editContent.setLayout(new BoxLayout(editContent, BoxLayout.Y_AXIS));

        JLabel formTitle = new JLabel("Edit my details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(NAVY);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        editContent.add(formTitle);
        editContent.add(Box.createVerticalStrut(28));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Color.WHITE);
        fields.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField = inputField();
        contactField = inputField();
        selectedFileField = inputField();
        selectedFileField.setEditable(false);
        selectedFileField.setBackground(new Color(248, 250, 252));

        addFormRow(fields, 0, "Email:", emailField);
        addFormRow(fields, 1, "Contact No:", contactField);

        JPanel photoChooser = new JPanel(new BorderLayout(8, 0));
        photoChooser.setBackground(Color.WHITE);
        photoChooser.add(selectedFileField, BorderLayout.CENTER);
        JButton browse = blueButton("Browse...");
        browse.addActionListener(e -> choosePhoto());
        photoChooser.add(browse, BorderLayout.EAST);
        addFormRow(fields, 2, "Profile Picture:", photoChooser);
        editContent.add(fields);
        editContent.add(Box.createVerticalStrut(24));

        JLabel hint = new JLabel("Username, name, role and password cannot be changed here.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        editContent.add(hint);
        editContent.add(Box.createVerticalStrut(22));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonRow.setBackground(Color.WHITE);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveButton = blueButton("Save Changes");
        saveButton.addActionListener(e -> saveChanges());
        JButton remove = secondaryButton("Remove Photo");
        remove.addActionListener(e -> removePhoto());
        buttonRow.add(saveButton);
        buttonRow.add(remove);
        editContent.add(buttonRow);
        editContent.add(Box.createVerticalStrut(18));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        editContent.add(statusLabel);

        editCard.add(editContent, BorderLayout.NORTH);
        center.add(editCard, BorderLayout.CENTER);

        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.setBackground(LIGHT_BLUE);
        infoCard.setPreferredSize(new Dimension(340, 560));
        infoCard.setBorder(BorderFactory.createLineBorder(new Color(147, 197, 253)));

        JLabel infoTitle = new JLabel("Lecturer Details", SwingConstants.CENTER);
        infoTitle.setOpaque(true);
        infoTitle.setBackground(NAVY);
        infoTitle.setForeground(Color.WHITE);
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        infoTitle.setBorder(new EmptyBorder(17, 10, 17, 10));
        infoCard.add(infoTitle, BorderLayout.NORTH);

        JPanel infoBody = new JPanel();
        infoBody.setLayout(new BoxLayout(infoBody, BoxLayout.Y_AXIS));
        infoBody.setBackground(LIGHT_BLUE);
        infoBody.setBorder(new EmptyBorder(24, 20, 24, 20));

        JPanel portraitHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        portraitHolder.setBackground(LIGHT_BLUE);
        portraitHolder.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoLabel = new JLabel("No photo", SwingConstants.CENTER);
        photoLabel.setOpaque(true);
        photoLabel.setBackground(Color.WHITE);
        photoLabel.setForeground(MUTED);
        photoLabel.setFont(PLAIN);
        photoLabel.setPreferredSize(new Dimension(175, 190));
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(148, 163, 184)));
        portraitHolder.add(photoLabel);
        infoBody.add(portraitHolder);
        infoBody.add(Box.createVerticalStrut(28));

        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setBackground(LIGHT_BLUE);
        addDetailRow(detailsGrid, 0, "Lecturer ID", lecturer.getUserId());
        addDetailRow(detailsGrid, 1, "Name", lecturer.getFirstName() + " " + lecturer.getLastName());
        addDetailRow(detailsGrid, 2, "Designation", "Lecturer");
        addDetailRow(detailsGrid, 3, "Department", lecturer.getDepartment());
        detailsEmail = addDetailRow(detailsGrid, 4, "E-mail", "");
        detailsContact = addDetailRow(detailsGrid, 5, "Contact No", "");
        detailsGrid.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoBody.add(detailsGrid);
        infoCard.add(infoBody, BorderLayout.CENTER);
        center.add(infoCard, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);
    }

    private JTextField inputField() {
        JTextField input = new JTextField();
        input.setFont(PLAIN);
        input.setForeground(TEXT);
        input.setPreferredSize(new Dimension(240, 40));
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(8, 10, 8, 10)));
        return input;
    }

    private JButton blueButton(String title) {
        JButton button = new JButton(title);
        button.setFont(BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(BLUE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 19, 11, 19));
        return button;
    }

    private JButton secondaryButton(String title) {
        JButton button = new JButton(title);
        button.setFont(BOLD);
        button.setForeground(NAVY);
        button.setBackground(PALE_BLUE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 17, 11, 17));
        return button;
    }

    private void addFormRow(JPanel grid, int row, String title, JComponent value) {
        GridBagConstraints label = new GridBagConstraints();
        label.gridx = 0;
        label.gridy = row;
        label.anchor = GridBagConstraints.WEST;
        label.insets = new Insets(0, 0, 20, 18);
        JLabel text = new JLabel(title);
        text.setFont(BOLD);
        text.setForeground(TEXT);
        grid.add(text, label);

        GridBagConstraints input = new GridBagConstraints();
        input.gridx = 1;
        input.gridy = row;
        input.weightx = 1;
        input.fill = GridBagConstraints.HORIZONTAL;
        input.insets = new Insets(0, 0, 20, 0);
        grid.add(value, input);
    }

    private JLabel addDetailRow(JPanel grid, int row, String title, String value) {
        GridBagConstraints a = new GridBagConstraints();
        a.gridx = 0; a.gridy = row;
        a.anchor = GridBagConstraints.NORTHWEST;
        a.insets = new Insets(0, 0, 16, 14);
        JLabel key = new JLabel(title);
        key.setFont(BOLD);
        key.setForeground(NAVY);
        grid.add(key, a);

        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 1; b.gridy = row;
        b.weightx = 1;
        b.anchor = GridBagConstraints.NORTHWEST;
        b.fill = GridBagConstraints.HORIZONTAL;
        b.insets = new Insets(0, 0, 16, 0);
        JLabel label = new JLabel(value == null ? "-" : value);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT);
        grid.add(label, b);
        return label;
    }

    private void refreshFromLecturer() {
        emailField.setText(nullToEmpty(lecturer.getEmail()));
        contactField.setText(nullToEmpty(lecturer.getContactNo()));
        String path = lecturer.getProfilePic();
        selectedFileField.setText(path == null || path.isBlank() ? "" : new File(path).getName());
        renderPhoto(path);
        detailsEmail.setText(display(lecturer.getEmail()));
        detailsContact.setText(display(lecturer.getContactNo()));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Lecturer Profile Picture");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Photos (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File candidate = chooser.getSelectedFile();
        try {
            validatePhoto(candidate);
            pendingPhoto = candidate;
            removePhotoRequested = false;
            selectedFileField.setText(candidate.getName());
            renderPhoto(candidate.getAbsolutePath());
            statusLabel.setText("Photo selected. Click Save Changes to keep it.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Photo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removePhoto() {
        pendingPhoto = null;
        removePhotoRequested = true;
        selectedFileField.setText("");
        renderPhoto(null);
        statusLabel.setText("Photo removed from preview. Click Save Changes to confirm.");
    }

    private void validatePhoto(File candidate) throws IOException {
        if (!candidate.isFile()) throw new IOException("Please select an existing image file.");
        String lower = candidate.getName().toLowerCase(java.util.Locale.ROOT);
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png"))) {
            throw new IOException("Only JPG and PNG photos are supported.");
        }
        if (Files.size(candidate.toPath()) > MAX_PHOTO_BYTES) {
            throw new IOException("The photo must be 5 MB or smaller.");
        }
        if (ImageIO.read(candidate) == null) throw new IOException("The selected file is not a valid image.");
    }

    private void renderPhoto(String path) {
        photoLabel.setIcon(null);
        photoLabel.setText("No photo");
        if (path == null || path.isBlank()) return;
        try {
            BufferedImage source = ImageIO.read(new File(path));
            if (source == null) {
                photoLabel.setText("Photo unavailable");
                return;
            }
            // Contain image inside the portrait area while keeping its aspect ratio.
            double scale = Math.min(167.0 / source.getWidth(), 182.0 / source.getHeight());
            int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
            Image scaled = source.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(scaled));
            photoLabel.setText("");
        } catch (IOException | RuntimeException ex) {
            photoLabel.setText("Photo unavailable");
        }
    }

    private Path storePhoto(File selected, String userId) throws IOException {
        validatePhoto(selected);
        String lower = selected.getName().toLowerCase(java.util.Locale.ROOT);
        String ext = lower.endsWith(".png") ? ".png" : ".jpg";
        String safeId = userId.replaceAll("[^A-Za-z0-9_-]", "_");
        Path folder = Path.of(System.getProperty("user.home"), ".fot-mis", "profile-photos");
        Files.createDirectories(folder);
        Path destination = folder.resolve(safeId + "_" + UUID.randomUUID() + ext).toAbsolutePath();
        if (destination.toString().length() > 255) {
            throw new IOException("Profile photo path is too long for the database.");
        }
        Files.copy(selected.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination;
    }

    private void saveChanges() {
        final String email = emailField.getText().trim();
        final String contact = contactField.getText().trim();
        final File photo = pendingPhoto;
        final boolean remove = removePhotoRequested;
        if (email.isEmpty() || email.length() > 100 || !email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (contact.length() > 15) {
            JOptionPane.showMessageDialog(this, "Contact number must be 15 characters or fewer.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        saveButton.setEnabled(false);
        statusLabel.setForeground(MUTED);
        statusLabel.setText("Saving profile...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                Path savedPhoto = null;
                String picturePath = remove ? null : lecturer.getProfilePic();
                try {
                    if (photo != null) {
                        savedPhoto = storePhoto(photo, lecturer.getUserId());
                        picturePath = savedPhoto.toString();
                    }
                    boolean saved = profileService.updateMyProfile(
                            lecturer.getFirstName(), lecturer.getLastName(),
                            email, contact, picturePath);
                    if (!saved) throw new IOException("Profile was not updated. Please try again.");
                    return picturePath;
                } catch (Exception ex) {
                    if (savedPhoto != null) {
                        try { Files.deleteIfExists(savedPhoto); }
                        catch (IOException cleanup) { ex.addSuppressed(cleanup); }
                    }
                    throw ex;
                }
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                try {
                    String savedPath = get();
                    lecturer.setEmail(email);
                    lecturer.setContactNo(contact);
                    lecturer.setProfilePic(savedPath);
                    pendingPhoto = null;
                    removePhotoRequested = false;
                    refreshFromLecturer();
                    Window parentWindow = SwingUtilities.getWindowAncestor(LecturerProfilePanel.this);
                    if (parentWindow instanceof LecturerDashboard dashboard) {
                        dashboard.refreshHeaderPhoto();
                    }
                    statusLabel.setForeground(new Color(21, 128, 61));
                    statusLabel.setText("Profile saved successfully.");
                    JOptionPane.showMessageDialog(LecturerProfilePanel.this,
                            "Profile updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    statusLabel.setForeground(new Color(185, 28, 28));
                    statusLabel.setText("Saving interrupted.");
                } catch (ExecutionException ex) {
                    Throwable error = ex.getCause() == null ? ex : ex.getCause();
                    statusLabel.setForeground(new Color(185, 28, 28));
                    statusLabel.setText("Changes could not be saved.");
                    JOptionPane.showMessageDialog(LecturerProfilePanel.this,
                            error.getMessage(), "Save Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
