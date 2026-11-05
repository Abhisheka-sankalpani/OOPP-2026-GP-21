package model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Shared notice model for Admin, Lecturer, Technical Officer and Student modules.
 * postedDate remains a Timestamp for compatibility with the Admin DAO/UI.
 */
public class Notice {
    private int noticeId;
    private String title;
    private String content;
    private Timestamp postedDate;
    private String targetRole;
    private LocalDate fromDate;
    private LocalDate toDate;

    /** Defaults used when the Admin creates a new notice. */
    public Notice() {
        this.targetRole = "ALL";
        this.postedDate = new Timestamp(System.currentTimeMillis());
    }

    /** Creates a new notice for saving via the Admin DAO. */
    public Notice(String title, String content, String targetRole) {
        this();
        this.title = title;
        this.content = content;
        if (targetRole != null) {
            this.targetRole = targetRole;
        }
    }

    /** For Admin DAO records read from a JDBC ResultSet. */
    public Notice(int noticeId, String title, String content,
                  Timestamp postedDate, String targetRole) {
        this(title, content, targetRole);
        this.noticeId = noticeId;
        if (postedDate != null) {
            this.postedDate = postedDate;
        }
    }

    /** For the original Student DAO, which passes the date as a String. */
    public Notice(int noticeId, String title, String content,
                  String postedDate, String targetRole) {
        this(title, content, targetRole);
        this.noticeId = noticeId;
        if (postedDate != null && !postedDate.isBlank()
                && !"null".equalsIgnoreCase(postedDate.trim())) {
            this.postedDate = parsePostedDate(postedDate);
        }
    }

    private static Timestamp parsePostedDate(String text) {
        String value = text.trim();
        try {
            // SQL timestamps: 2026-11-01 09:30:00.0
            return Timestamp.valueOf(value);
        } catch (IllegalArgumentException e) {
            try {
                // ISO date-time: 2026-11-01T09:30:00
                return Timestamp.valueOf(LocalDateTime.parse(value));
            } catch (DateTimeParseException ignored) {
                try {
                    // Date-only: 2026-11-01
                    return Timestamp.valueOf(LocalDate.parse(value).atStartOfDay());
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("Invalid notice posted date: " + text, ex);
                }
            }
        }
    }

    public int getNoticeId() { return noticeId; }
    public void setNoticeId(int noticeId) { this.noticeId = noticeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    /** Timestamp getter retained for Admin's date formatting code. */
    public Timestamp getPostedDate() { return postedDate; }
    public void setPostedDate(Timestamp postedDate) { this.postedDate = postedDate; }

    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }

    /** First day the notice is visible (inclusive). Null means no start restriction. */
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    /** Last day the notice is visible (inclusive). Null means no end restriction. */
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    @Override
    public String toString() {
        return "[" + targetRole + "] " + title;
    }
}
