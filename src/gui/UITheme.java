package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UITheme {

    // Color Palette
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color COLOR_SIDEBAR = new Color(15, 23, 42);
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(37, 99, 235);
    public static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);
    public static final Color COLOR_SIDEBAR_TEXT = new Color(148, 163, 184);
    public static final Color COLOR_HEADER = new Color(15, 23, 42);
    public static final Color COLOR_BG = new Color(248, 250, 252);
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_CARD_BORDER = new Color(226, 232, 240);
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    public static final Color COLOR_DANGER = new Color(220, 38, 38);
    public static final Color COLOR_DANGER_HOVER = new Color(185, 28, 28);
    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);

    // Accent colors for Stat Cards
    public static final Color ACCENT_BLUE = new Color(37, 99, 235);
    public static final Color ACCENT_TEAL = new Color(13, 148, 136);
    public static final Color ACCENT_AMBER = new Color(245, 158, 11);
    public static final Color ACCENT_PURPLE = new Color(139, 92, 246);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_NAV_LABEL = new Font("Segoe UI", Font.BOLD, 11);

    // Card Panel with custom border and background
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    public static JPanel createStatCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(accentColor);
                g2.fillRect(0, 0, 5, getHeight());
                g2.dispose();
            }
        };
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 16)
        ));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(COLOR_CARD_BG);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_BODY_BOLD);
        lblTitle.setForeground(COLOR_TEXT_MUTED);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(FONT_STAT_NUM);
        lblValue.setForeground(COLOR_TEXT_MAIN);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(COLOR_TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(lblValue);
        content.add(Box.createVerticalStrut(4));
        content.add(lblSub);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // Modern Primary Button
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(COLOR_PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_PRIMARY);
            }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(Color.WHITE);
        btn.setForeground(COLOR_TEXT_MAIN);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(COLOR_CARD_BORDER, 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(241, 245, 249));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    // Sign Out
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(COLOR_DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(COLOR_DANGER_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_DANGER);
            }
        });
        return btn;
    }

    // Top Header Banner for tabs
    public static JPanel createTabHeader(String title, String subtitle, JComponent rightComponent) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_SUBTITLE);
        lblSub.setForeground(COLOR_TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(3));
        left.add(lblSub);

        header.add(left, BorderLayout.WEST);
        if (rightComponent != null) {
            header.add(rightComponent, BorderLayout.EAST);
        }
        return header;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setFillsViewportHeight(true);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(new Color(30, 41, 59));
        th.setPreferredSize(new Dimension(th.getPreferredSize().width, 36));
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_CARD_BORDER));

        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, val, isSel, hasFoc, r, c);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(250, 252, 255));
                }
                return comp;
            }
        };
        table.setDefaultRenderer(Object.class, defaultRenderer);
    }

    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);

            String text = (value != null) ? value.toString().trim() : "";
            label.setText(text);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            if ("Eligible".equalsIgnoreCase(text) || "Approved".equalsIgnoreCase(text) || "Pass".equalsIgnoreCase(text) || "Active".equalsIgnoreCase(text)) {
                label.setBackground(new Color(220, 252, 231));
                label.setForeground(new Color(22, 101, 52));
            } else if ("Not Eligible".equalsIgnoreCase(text) || "Rejected".equalsIgnoreCase(text) || "Fail".equalsIgnoreCase(text)) {
                label.setBackground(new Color(254, 226, 226));
                label.setForeground(new Color(153, 27, 27));
            } else if ("Pending".equalsIgnoreCase(text)) {
                label.setBackground(new Color(254, 243, 199));
                label.setForeground(new Color(146, 64, 14));
            } else {
                label.setBackground(new Color(241, 245, 249));
                label.setForeground(COLOR_TEXT_MAIN);
            }

            label.setBorder(new EmptyBorder(2, 8, 2, 8));
            return label;
        }
    }
}
