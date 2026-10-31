package dao;

import db.DBConnection;
import model.Notice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public List<Notice> getStudentNotices() {
        List<Notice> list = new ArrayList<>();

        String sql = "SELECT notice_id, title, content, posted_date, target_role "
                + "FROM notice "
                + "WHERE target_role = 'STUDENT' OR target_role = 'ALL' "
                + "ORDER BY posted_date DESC, notice_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Notice notice = new Notice(
                        rs.getInt("notice_id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        String.valueOf(rs.getTimestamp("posted_date")),
                        rs.getString("target_role")
                );
                list.add(notice);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching student notices: " + e.getMessage());
        }
        return list;
    }
}
