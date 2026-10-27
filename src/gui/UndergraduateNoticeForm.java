package gui;

import dao.NoticeDAO;
import model.Notice;

import javax.swing.*;
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

    public UndergraduateNoticeForm() {
        this.noticeDAO = new NoticeDAO();
        initUI();
        loadNotices();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblHeader = new JLabel("Faculty Notices & Announcements", JLabel.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 20));
        lblHeader.setForeground(new Color(30, 60, 114));
        add(lblHeader, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 10, 12));

        String[] columnNames = {"ID", "Title", "Posted Date", "Audience"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableNotices = new JTable(tableModel);
        tableNotices.setRowHeight(24);
        tableNotices.setFont(new Font("Arial", Font.PLAIN, 13));
        tableNotices.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        tableNotices.getTableHeader().setBackground(new Color(230, 235, 245));

        contentPanel.add(new JScrollPane(tableNotices));

        JPanel detailsPanel = new JPanel(new BorderLayout(8, 8));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Notice Details"));

        lblSelectedTitle = new JLabel("Select a notice above to view complete message");
        lblSelectedTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblSelectedTitle.setForeground(new Color(0, 102, 204));
        detailsPanel.add(lblSelectedTitle, BorderLayout.NORTH);

        txtNoticeContent = new JTextArea();
        txtNoticeContent.setEditable(false);
        txtNoticeContent.setLineWrap(true);
        txtNoticeContent.setWrapStyleWord(true);
        txtNoticeContent.setFont(new Font("Arial", Font.PLAIN, 13));
        txtNoticeContent.setBackground(new Color(248, 249, 250));

        detailsPanel.add(new JScrollPane(txtNoticeContent), BorderLayout.CENTER);
        contentPanel.add(detailsPanel);

        add(contentPanel, BorderLayout.CENTER);

        //Show details, Selected row
        tableNotices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableNotices.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < noticeList.size()) {
                    Notice selected = noticeList.get(selectedRow);
                    lblSelectedTitle.setText(selected.getTitle() + " (" + selected.getPostedDate() + ")");
                    txtNoticeContent.setText(selected.getContent());
                }
            }
        });
    }

    private void loadNotices() {
        tableModel.setRowCount(0);
        noticeList = noticeDAO.getStudentNotices();

        for (Notice n : noticeList) {
            tableModel.addRow(new Object[]{
                    n.getNoticeId(),
                    n.getTitle(),
                    n.getPostedDate(),
                    n.getTargetRole()
            });
        }

        if (!noticeList.isEmpty()) {
            tableNotices.setRowSelectionInterval(0, 0);
        }
    }
}
