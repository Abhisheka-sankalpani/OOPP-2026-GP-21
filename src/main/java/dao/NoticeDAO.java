package dao;

import database.DBConnection;
import model.Notice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    // CREATE - Add New Notice
    public boolean addNotice(Notice notice) throws SQLException {

        validateNotice(notice);

        String sql = "INSERT INTO notice " +
                "(title, content, target_role) " +
                "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, notice.getTitle().trim());
            ps.setString(2, notice.getContent().trim());
            ps.setString(3, getValidTargetRole(notice.getTargetRole()));

            int rows = ps.executeUpdate();

            if (rows > 0) {

                // Get Auto-Generated Notice ID
                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {
                        notice.setNoticeId(rs.getInt(1));
                    }
                }

                return true;
            }
        }

        return false;
    }


    // READ - Get Notice by ID
    public Notice getNoticeById(int noticeId) throws SQLException {

        String sql = "SELECT notice_id, title, content, " +
                "posted_date, target_role " +
                "FROM notice WHERE notice_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, noticeId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapResultSetToNotice(rs);
                }
            }
        }

        return null;
    }


    // READ - Get All Notices
    public List<Notice> getAllNotices() throws SQLException {

        List<Notice> notices = new ArrayList<>();

        String sql = "SELECT notice_id, title, content, " +
                "posted_date, target_role " +
                "FROM notice " +
                "ORDER BY posted_date DESC, notice_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Notice notice = mapResultSetToNotice(rs);

                notices.add(notice);
            }
        }

        return notices;
    }


    // UPDATE - Update Existing Notice
    public boolean updateNotice(Notice notice) throws SQLException {

        validateNotice(notice);

        if (notice.getNoticeId() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Notice ID"
            );
        }

        String sql = "UPDATE notice SET " +
                "title = ?, " +
                "content = ?, " +
                "target_role = ? " +
                "WHERE notice_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, notice.getTitle().trim());
            ps.setString(2, notice.getContent().trim());
            ps.setString(3, getValidTargetRole(notice.getTargetRole()));
            ps.setInt(4, notice.getNoticeId());

            int rows = ps.executeUpdate();

            return rows > 0;
        }
    }


    // DELETE - Delete Notice
    public boolean deleteNotice(int noticeId) throws SQLException {

        if (noticeId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Notice ID"
            );
        }

        String sql = "DELETE FROM notice WHERE notice_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, noticeId);

            int rows = ps.executeUpdate();

            return rows > 0;
        }
    }


    // SEARCH - Search Notices by Keyword
    public List<Notice> searchNotices(String keyword)
            throws SQLException {

        List<Notice> notices = new ArrayList<>();

        String sql = "SELECT notice_id, title, content, " +
                "posted_date, target_role " +
                "FROM notice " +
                "WHERE LOWER(title) LIKE ? " +
                "OR LOWER(content) LIKE ? " +
                "ORDER BY posted_date DESC, notice_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String pattern = "%" +
                    (keyword == null ? "" :
                            keyword.trim().toLowerCase()) + "%";

            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    notices.add(mapResultSetToNotice(rs));
                }
            }
        }

        return notices;
    }


    // READ - Get Recent Notices
    public List<Notice> getRecentNotices(int limit)
            throws SQLException {

        List<Notice> notices = new ArrayList<>();

        if (limit <= 0) {
            return notices;
        }

        String sql = "SELECT notice_id, title, content, " +
                "posted_date, target_role " +
                "FROM notice " +
                "ORDER BY posted_date DESC, notice_id DESC " +
                "LIMIT ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    notices.add(mapResultSetToNotice(rs));
                }
            }
        }

        return notices;
    }


    // COUNT - Get Total Number of Notices
    public int getTotalNoticesCount() throws SQLException {

        String sql = "SELECT COUNT(*) FROM notice";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }


    // Convert ResultSet into Notice Object
    private Notice mapResultSetToNotice(ResultSet rs)
            throws SQLException {

        return new Notice(

                rs.getInt("notice_id"),

                rs.getString("title"),

                rs.getString("content"),

                rs.getTimestamp("posted_date"),

                rs.getString("target_role")
        );
    }


    // Validate Notice Details
    private void validateNotice(Notice notice) {

        if (notice == null) {
            throw new IllegalArgumentException(
                    "Notice cannot be null"
            );
        }

        if (notice.getTitle() == null ||
                notice.getTitle().isBlank()) {

            throw new IllegalArgumentException(
                    "Notice title is required"
            );
        }

        if (notice.getContent() == null ||
                notice.getContent().isBlank()) {

            throw new IllegalArgumentException(
                    "Notice content is required"
            );
        }

        if (notice.getTitle().trim().length() > 100) {

            throw new IllegalArgumentException(
                    "Notice title cannot exceed 100 characters"
            );
        }

        // Validate target_role VARCHAR(20)
        getValidTargetRole(notice.getTargetRole());
    }


    // Handle Default Target Role
    private String getValidTargetRole(String targetRole) {

        if (targetRole == null || targetRole.isBlank()) {
            return "ALL";
        }

        String role = targetRole.trim();

        if (role.length() > 20) {
            throw new IllegalArgumentException(
                    "Target role cannot exceed 20 characters"
            );
        }

        return role;
    }
}