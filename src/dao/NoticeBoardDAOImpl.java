package dao;

import db.DBConnection;
import exception.DatabaseException;
import model.NoticeItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NoticeBoardDAOImpl implements NoticeBoardDAO {

    @Override
    public List<NoticeItem> getNoticesForRole(String role) throws DatabaseException {
        List<NoticeItem> list = new ArrayList<>();
        String sql = "SELECT * FROM notice WHERE target_role = 'ALL' OR target_role = ? "
                + "ORDER BY posted_date DESC, notice_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp time = rs.getTimestamp("posted_date");
                    String date = (time == null) ? "" : time.toString().substring(0, 16);
                    list.add(new NoticeItem(rs.getInt("notice_id"), rs.getString("title"),
                            rs.getString("content"), date, rs.getString("target_role")));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not read the notices", e);
        }
        return list;
    }

    @Override
    public void addNotice(NoticeItem notice) throws DatabaseException {
        String sql = "INSERT INTO notice (title, content, target_role) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, notice.getTitle());
            ps.setString(2, notice.getContent());
            ps.setString(3, notice.getTargetRole());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not post the notice", e);
        }
    }
}
