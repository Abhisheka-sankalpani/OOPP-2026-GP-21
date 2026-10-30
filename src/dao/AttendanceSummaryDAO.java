package dao;

import db.DBConnection;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds AttendanceSummary objects for students.
 *
 * Demonstrates Polymorphism in action:
 *   - TheoryAttendanceSummary.calculatePercentage()
 *   - PracticalAttendanceSummary.calculatePercentage()
 *   - CombinedAttendanceSummary.calculatePercentage()
 * All share the same interface (AttendanceSummary) and are resolved at runtime.
 */
public class AttendanceSummaryDAO {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MedicalDAO    medicalDAO    = new MedicalDAO();

    // ─── Summary builders ─────────────────────────────────────────────────

    /**
     * Builds a TheoryAttendanceSummary for one student in one course.
     * (Polymorphism: calculatePercentage() will use the THEORY override)
     */
    public TheoryAttendanceSummary buildTheorySummary(String studentId, String courseId) {
        String courseName = getCourseName(courseId);
        int attended = attendanceDAO.countAttended(studentId, courseId, "THEORY");
        int total    = attendanceDAO.countTotalSessions(studentId, courseId, "THEORY");
        int medApp   = medicalDAO.countApprovedMedicals(studentId, courseId, "THEORY");

        return new TheoryAttendanceSummary(studentId, courseId, courseName,
                                          attended, total, medApp);
    }

    /**
     * Builds a PracticalAttendanceSummary for one student in one course.
     * (Polymorphism: calculatePercentage() will use the PRACTICAL override)
     */
    public PracticalAttendanceSummary buildPracticalSummary(String studentId, String courseId) {
        String courseName = getCourseName(courseId);
        int attended = attendanceDAO.countAttended(studentId, courseId, "PRACTICAL");
        int total    = attendanceDAO.countTotalSessions(studentId, courseId, "PRACTICAL");
        int medApp   = medicalDAO.countApprovedMedicals(studentId, courseId, "PRACTICAL");

        return new PracticalAttendanceSummary(studentId, courseId, courseName,
                                              attended, total, medApp);
    }

    /**
     * Builds a CombinedAttendanceSummary for one student in one course.
     * (Polymorphism: calculatePercentage() will use the COMBINED override)
     */
    public CombinedAttendanceSummary buildCombinedSummary(String studentId, String courseId) {
        String courseName = getCourseName(courseId);
        TheoryAttendanceSummary    ts = buildTheorySummary(studentId, courseId);
        PracticalAttendanceSummary ps = buildPracticalSummary(studentId, courseId);
        return new CombinedAttendanceSummary(studentId, courseId, courseName, ts, ps);
    }

    // ─── Class-wide summary lists (for TO dashboard) ──────────────────────

    /**
     * Returns a CombinedAttendanceSummary for EVERY student enrolled in a course.
     * The returned list is used to populate the summary table and category views.
     */
    public List<CombinedAttendanceSummary> buildClassSummary(String courseId) {
        List<String>                  studentIds = attendanceDAO.getStudentIdsByCourse(courseId);
        List<CombinedAttendanceSummary> results  = new ArrayList<>();
        for (String sid : studentIds) {
            results.add(buildCombinedSummary(sid, courseId));
        }
        return results;
    }

    /**
     * Filters a list of summaries into three buckets:
     *   > 80 %,  = 80 %,  < 80 %
     * Uses the raw (without medical) percentage for each bucket.
     *
     * @param useWithMedical when true, uses calculatePercentageWithMedical()
     */
    public List<CombinedAttendanceSummary> filterByCategory(
            List<CombinedAttendanceSummary> all, String category, boolean useWithMedical) {

        List<CombinedAttendanceSummary> filtered = new ArrayList<>();
        for (CombinedAttendanceSummary s : all) {
            double pct = useWithMedical ? s.calculatePercentageWithMedical()
                                       : s.calculatePercentage();
            boolean match = switch (category) {
                case "> 80%" -> pct > 80.0;
                case "= 80%" -> pct == 80.0;
                case "< 80%" -> pct < 80.0;
                default      -> false;
            };
            if (match) filtered.add(s);
        }
        return filtered;
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private String getCourseName(String courseId) {
        String sql = "SELECT course_name FROM course WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("course_name");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course name: " + e.getMessage());
        }
        return courseId;
    }
}
