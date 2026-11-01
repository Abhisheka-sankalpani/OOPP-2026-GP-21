package gui;

import dao.NoticeDAO;
import model.Notice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UndergraduateNoticeForm extends JPanel {

    private NoticeDAO noticeDAO;
    private List<Notice> noticeList;

    private JTable tableNotices;
    private DefaultTableModel tableModel;
    private JTextArea txtNoticeContent;
    private JLabel lblSelectedTitle;
    private JLabel lblSelectedMeta;

    public UndergraduateNoticeForm() {
        this.noticeDAO = new NoticeDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadNotices();
    }

    private void initUI() {
        // 1. Header
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Notices");
        btnRefresh.addActionListener(e -> loadNotices());

        JPanel header = UITheme.createTabHeader(
                "Faculty Notices & Announcements",
                "Official circulars, semester schedules, and academic announcements.",
                btnRefresh
        );
        add(header, BorderLayout.NORTH);

        // 2. Center Content Split
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 16));
        centerPanel.setOpaque(false);

        // Top Card: Table
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel lblListTitle = new JLabel("All Published Notices");
        lblListTitle.setFont(UITheme.FONT_SECTION);
        lblListTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        tableCard.add(lblListTitle, BorderLayout.NORTH);

        String[] columnNames = {"Notice ID", "Announcement Title", "Target Audience", "Posted Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableNotices = new JTable(tableModel);
        UITheme.styleTable(tableNotices);
        tableNotices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        tableNotices.getColumnModel().getColumn(0).setPreferredWidth(90);
        tableNotices.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableNotices.getColumnModel().getColumn(1).setPreferredWidth(450);
        tableNotices.getColumnModel().getColumn(2).setPreferredWidth(130);
        tableNotices.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableNotices.getColumnModel().getColumn(3).setPreferredWidth(160);
        tableNotices.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(tableNotices);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        centerPanel.add(tableCard);

        //Notice Reader
        JPanel detailsCard = UITheme.createCardPanel();
        detailsCard.setLayout(new BorderLayout(0, 10));

        JPanel metaPanel = new JPanel(new BorderLayout());
        metaPanel.setOpaque(false);

        lblSelectedTitle = new JLabel("Select an announcement from the list above to read");
        lblSelectedTitle.setFont(UITheme.FONT_SECTION);
        lblSelectedTitle.setForeground(UITheme.COLOR_PRIMARY);

        lblSelectedMeta = new JLabel("");
        lblSelectedMeta.setFont(UITheme.FONT_SMALL);
        lblSelectedMeta.setForeground(UITheme.COLOR_TEXT_MUTED);

        metaPanel.add(lblSelectedTitle, BorderLayout.WEST);
        metaPanel.add(lblSelectedMeta, BorderLayout.EAST);
        detailsCard.add(metaPanel, BorderLayout.NORTH);

        txtNoticeContent = new JTextArea();
        txtNoticeContent.setEditable(false);
        txtNoticeContent.setLineWrap(true);
        txtNoticeContent.setWrapStyleWord(true);
        txtNoticeContent.setFont(UITheme.FONT_BODY);
        txtNoticeContent.setBackground(new Color(250, 252, 255));
        txtNoticeContent.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane contentScroll = new JScrollPane(txtNoticeContent);
        contentScroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_CARD_BORDER, 1));
        detailsCard.add(contentScroll, BorderLayout.CENTER);

        centerPanel.add(detailsCard);
        add(centerPanel, BorderLayout.CENTER);

        // Table Selection Listener
        tableNotices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableNotices.getSelectedRow();
                if (selectedRow >= 0 && noticeList != null && selectedRow < noticeList.size()) {
                    Notice selected = noticeList.get(selectedRow);
                    lblSelectedTitle.setText(selected.getTitle());
                    lblSelectedMeta.setText("Posted: " + selected.getPostedDate() + "  |  Audience: " + selected.getTargetRole());
                    txtNoticeContent.setText(selected.getContent());
                }
            }
        });
    }

    public void loadNotices() {
        tableModel.setRowCount(0);
        noticeList = noticeDAO.getStudentNotices();

        for (Notice n : noticeList) {
            tableModel.addRow(new Object[]{
                    n.getNoticeId(),
                    n.getTitle(),
                    n.getTargetRole(),
                    n.getPostedDate()
            });
        }

        if (!noticeList.isEmpty()) {
            tableNotices.setRowSelectionInterval(0, 0);
        } else {
            lblSelectedTitle.setText("No faculty notices available at this time.");
            lblSelectedMeta.setText("");
            txtNoticeContent.setText("");
        }
    }
}
