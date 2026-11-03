package view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class UIStyle {

    // Colors
    public static final Color COLOR_PRIMARY =
            new Color(30, 64, 175);

    public static final Color COLOR_PRIMARY_LIGHT =
            new Color(59, 130, 246);

    public static final Color COLOR_BG =
            new Color(243, 246, 251);

    public static final Color COLOR_TEXT_MAIN =
            new Color(30, 41, 59);

    public static final Color COLOR_TEXT_MUTED =
            new Color(100, 116, 139);

    public static final Color COLOR_TEXT_LIGHT =
            new Color(219, 234, 254);

    public static final Color COLOR_DANGER =
            new Color(220, 38, 38);

    public static final Color COLOR_WHITE =
            Color.WHITE;

    // Fonts
    public static final Font FONT_TITLE =
            new Font("Segoe UI", Font.BOLD, 24);

    public static final Font FONT_SUBTITLE =
            new Font("Segoe UI", Font.BOLD, 20);

    public static final Font FONT_BODY =
            new Font("Segoe UI", Font.PLAIN, 13);

    public static final Font FONT_BODY_BOLD =
            new Font("Segoe UI", Font.BOLD, 13);

    public static final Font FONT_SMALL =
            new Font("Segoe UI", Font.PLAIN, 12);

    // Private Constructor
    private UIStyle() {
    }

    // Create Card Border
    public static Border createCardBorder() {

        return BorderFactory.createCompoundBorder(

                BorderFactory.createLineBorder(
                        new Color(226, 232, 240),
                        1,
                        true
                ),

                new EmptyBorder(18, 20, 18, 20)
        );
    }

    // Create Text Field
    public static JTextField createTextField(int columns) {

        JTextField field = new JTextField(columns);

        styleTextField(field);

        return field;
    }

    // Create Password Field
    public static JPasswordField createPasswordField(int columns) {

        JPasswordField field = new JPasswordField(columns);

        styleTextField(field);

        return field;
    }

    // Common Text Field Design
    private static void styleTextField(JTextField field) {

        field.setFont(FONT_BODY);

        field.setForeground(COLOR_TEXT_MAIN);

        field.setBackground(Color.WHITE);

        field.setPreferredSize(new Dimension(300, 42));

        field.setBorder(
                BorderFactory.createCompoundBorder(

                        BorderFactory.createLineBorder(
                                new Color(203, 213, 225)
                        ),

                        new EmptyBorder(8, 12, 8, 12)
                )
        );
    }

    // Primary Button
    public static JButton createPrimaryButton(String text) {

        JButton button = new JButton(text);

        styleButton(button);

        button.setBackground(COLOR_PRIMARY);

        button.setForeground(Color.WHITE);

        return button;
    }

    // Secondary Button
    public static JButton createSecondaryButton(String text) {

        JButton button = new JButton(text);

        styleButton(button);

        button.setBackground(new Color(226, 232, 240));

        button.setForeground(COLOR_TEXT_MAIN);

        return button;
    }

    // Common Button Design
    private static void styleButton(JButton button) {

        button.setFont(FONT_BODY_BOLD);

        button.setFocusPainted(false);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(
                new EmptyBorder(12, 18, 12, 18)
        );
    }
}