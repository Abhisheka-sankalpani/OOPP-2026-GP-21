package model;

import java.sql.Timestamp;

public class Notice {

    // Attributes
    private int noticeId;
    private String title;
    private String content;
    private Timestamp postedDate;
    private String targetRole;

    // Default Constructor
    public Notice() {
        this.targetRole = "ALL";
        this.postedDate = new Timestamp(System.currentTimeMillis());
    }

    // Parameterized Constructor
    public Notice(String title, String content, String targetRole) {
        this();

        this.title = title;
        this.content = content;

        if (targetRole != null) {
            this.targetRole = targetRole;
        }
    }

    // Constructor for existing database notices
    public Notice(int noticeId, String title, String content,
                  Timestamp postedDate, String targetRole) {

        this(title, content, targetRole);

        this.noticeId = noticeId;

        if (postedDate != null) {
            this.postedDate = postedDate;
        }
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

    public Timestamp getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(Timestamp postedDate) {
        this.postedDate = postedDate;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    // Display Notice
    @Override
    public String toString() {
        return "[" + targetRole + "] " + title;
    }
}