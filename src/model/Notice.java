package model;

public class Notice {
    private int noticeId;
    private String title;
    private String content;
    private String postedDate;
    private String targetRole;

    // Default constructor
    public Notice() {
    }

    // Parameterized constructor
    public Notice(int noticeId, String title, String content, String postedDate, String targetRole) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.postedDate = postedDate;
        this.targetRole = targetRole;
    }

    // Getters and Setters
    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
}
