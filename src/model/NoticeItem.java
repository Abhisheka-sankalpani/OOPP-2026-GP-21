package model;

/** One row of the notice table (title, content, who it is for). */
public class NoticeItem {
    private int noticeId;
    private String title;
    private String content;
    private String postedDate;
    private String targetRole;   // ALL, STUDENT, LECTURER or TECHNICAL_OFFICER

    public NoticeItem(int noticeId, String title, String content, String postedDate, String targetRole) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.targetRole = targetRole;
    }

    public int getNoticeId() { return noticeId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getPostedDate() { return postedDate; }
    public String getTargetRole() { return targetRole; }
}
