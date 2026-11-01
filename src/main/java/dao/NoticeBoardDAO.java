package dao;

import exception.DatabaseException;
import model.NoticeItem;

import java.util.List;

public interface NoticeBoardDAO {
    /** Notices meant for everyone (ALL) or for the given role, newest first. */
    List<NoticeItem> getNoticesForRole(String role) throws DatabaseException;

    void addNotice(NoticeItem notice) throws DatabaseException;
}
