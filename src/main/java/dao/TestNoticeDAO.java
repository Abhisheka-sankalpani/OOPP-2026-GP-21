package dao;

import model.Notice;

import java.sql.SQLException;
import java.util.List;

public class TestNoticeDAO {

    public static void main(String[] args) {

        NoticeDAO dao = new NoticeDAO();

        int testNoticeId = 0;

        try {

            // 1. CREATE
            Notice notice = new Notice(
                    "Java Test Notice",
                    "This is a notice created using Java JDBC.",
                    "ALL"
            );

            boolean added = dao.addNotice(notice);

            if (!added) {
                System.out.println("Notice Add Failed!");
                return;
            }

            testNoticeId = notice.getNoticeId();

            System.out.println("Notice Added Successfully!");
            System.out.println("Notice ID: " + testNoticeId);


            // 2. READ BY ID
            Notice savedNotice = dao.getNoticeById(testNoticeId);

            if (savedNotice != null) {

                System.out.println("\nNotice Details:");

                System.out.println("ID: " + savedNotice.getNoticeId());
                System.out.println("Title: " + savedNotice.getTitle());
                System.out.println("Content: " + savedNotice.getContent());
                System.out.println("Date: " + savedNotice.getPostedDate());
                System.out.println("Role: " + savedNotice.getTargetRole());

            } else {
                System.out.println("Notice Not Found!");
                return;
            }


            // 3. UPDATE
            savedNotice.setTitle("Updated Java Notice");
            savedNotice.setContent("This notice has been updated.");

            boolean updated = dao.updateNotice(savedNotice);

            if (updated) {
                System.out.println("\nNotice Updated Successfully!");
            }


            // 4. READ ALL
            System.out.println("\nAll Notices:");

            List<Notice> notices = dao.getAllNotices();

            for (Notice n : notices) {

                System.out.println(
                        n.getNoticeId() + " | " +
                                n.getTitle() + " | " +
                                n.getTargetRole()
                );
            }


            // 5. SEARCH
            System.out.println("\nSearch Results:");

            List<Notice> searchResults =
                    dao.searchNotices("Updated Java Notice");

            for (Notice n : searchResults) {
                System.out.println(n.getTitle());
            }


            // 6. RECENT NOTICES
            System.out.println("\nRecent Notices:");

            List<Notice> recentNotices =
                    dao.getRecentNotices(3);

            for (Notice n : recentNotices) {
                System.out.println(n.getTitle());
            }


            // 7. TOTAL COUNT
            System.out.println(
                    "\nTotal Notices: " +
                            dao.getTotalNoticesCount()
            );


            // 8. DELETE
            boolean deleted = dao.deleteNotice(testNoticeId);

            if (deleted) {
                System.out.println("\nNotice Deleted Successfully!");
                testNoticeId = 0;
            }

        } catch (SQLException | IllegalArgumentException e) {

            System.out.println("Error: " + e.getMessage());

        } finally {

            // Clean up only the newly created test notice
            if (testNoticeId > 0) {

                try {
                    dao.deleteNotice(testNoticeId);
                    System.out.println("Test notice cleanup completed.");

                } catch (SQLException e) {
                    System.out.println(
                            "Cleanup failed for Notice ID " +
                                    testNoticeId + ": " + e.getMessage()
                    );
                }
            }
        }
    }
}