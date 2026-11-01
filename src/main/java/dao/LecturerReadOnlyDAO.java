package dao;

import database.DBConnection;
import exception.DatabaseException;
import model.Assessment;
import model.Mark;
import util.CACalculator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/** Read-only data access for the lecturer's three additional dashboard pages.
 *  All SQL statements here are SELECTs. Existing DAOs continue to own writes.
 */
public class LecturerReadOnlyDAO {

    public record StudentInfo(String id, String name, String regNo, String department,
                              String batch, boolean repeat, String email,
                              String contact, String photo) {
        @Override public String toString() { return name + " (" + id + ")"; }
    }

    public record CourseInfo(String id, String title) {
        @Override public String toString() { return id + " - " + title; }
    }

    public record AttendanceRow(String type, int session, java.sql.Date date,
                                String status, boolean approvedMedical) { }

    public record MedicalRow(int id, String type, int session, java.sql.Date medicalDate,
                             java.sql.Date submitted, String description, String status) { }

    public record AttendanceSummary(int theoryPresent, int theoryTotal, int practicalPresent,
                                    int practicalTotal, int theoryMed, int practicalMed,
                                    double theoryPct, double practicalPct,
                                    double theoryAdjusted, double practicalAdjusted,
                                    double combined, double combinedAdjusted,
                                    List<AttendanceRow> rows) {
        public boolean hasData() { return theoryTotal + practicalTotal > 0; }
    }

    public record EligibilityRow(StudentInfo student, String courseId,
                                 Double caPct, Double attendancePct, Double adjustedPct,
                                 String caStatus, String attendanceStatus,
                                 String finalStatus, String note) { }

    public List<StudentInfo> getStudents() throws SQLException {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, " +
                "u.contact_no, u.profile_pic, s.reg_no, s.department, s.batch, s.is_repeat " +
                "FROM users u JOIN undergraduate s ON u.user_id = s.student_id " +
                "WHERE u.role = 'STUDENT' ORDER BY s.batch, s.reg_no, u.user_id";
        List<StudentInfo> students = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                students.add(new StudentInfo(
                        rs.getString("user_id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("reg_no"), rs.getString("department"),
                        rs.getString("batch"), rs.getBoolean("is_repeat"),
                        rs.getString("email"), rs.getString("contact_no"),
                        rs.getString("profile_pic")));
            }
        }
        return students;
    }

    public List<CourseInfo> getCourses() throws SQLException {
        List<CourseInfo> courses = new ArrayList<>();
        String sql = "SELECT course_id, course_name FROM course ORDER BY course_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courses.add(new CourseInfo(rs.getString(1), rs.getString(2)));
            }
        }
        return courses;
    }

    /** Each distinct session counts once; the most recent attendance_id takes priority.
     *  An APPROVED medical counts only for an ABSENT session. Never counts twice.
     */
    public AttendanceSummary getAttendance(String studentId, String courseId) throws SQLException {
        String sql = "SELECT a.attendance_id, a.session_type, a.session_no, a.session_date, a.status, " +
                "EXISTS (SELECT 1 FROM medical m WHERE m.student_id = a.student_id " +
                "AND m.course_id = a.course_id AND m.session_type = a.session_type " +
                "AND m.session_no = a.session_no AND m.approval_status = 'APPROVED') AS has_medical " +
                "FROM attendance a WHERE a.student_id = ? AND a.course_id = ? " +
                "ORDER BY a.attendance_id DESC";
        Map<String, AttendanceRow> distinct = new LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("session_type");
                    int session = rs.getInt("session_no");
                    String key = type + ":" + session;
                    distinct.putIfAbsent(key, new AttendanceRow(type, session,
                            rs.getDate("session_date"), rs.getString("status"),
                            rs.getBoolean("has_medical")));
                }
            }
        }

        int tp = 0, tt = 0, pp = 0, pt = 0, tm = 0, pm = 0;
        for (AttendanceRow row : distinct.values()) {
            boolean present = "PRESENT".equalsIgnoreCase(row.status());
            boolean covered = !present && row.approvedMedical();
            if ("THEORY".equalsIgnoreCase(row.type())) {
                tt++;
                if (present) tp++;
                if (covered) tm++;
            } else if ("PRACTICAL".equalsIgnoreCase(row.type())) {
                pt++;
                if (present) pp++;
                if (covered) pm++;
            }
        }
        double t = percent(tp, tt), p = percent(pp, pt);
        double tMed = percent(tp + tm, tt), pMed = percent(pp + pm, pt);
        double combined = combine(t, tt, p, pt);
        double combinedMed = combine(tMed, tt, pMed, pt);
        return new AttendanceSummary(tp, tt, pp, pt, tm, pm, t, p, tMed, pMed,
                combined, combinedMed, new ArrayList<>(distinct.values()));
    }

    private static double percent(int count, int total) {
        return total == 0 ? 0 : Math.min(100.0, (100.0 * count / total));
    }

    /** Match the existing CombinedAttendanceSummary's equal-component averaging. */
    private static double combine(double theory, int tCount, double practical, int pCount) {
        if (tCount > 0 && pCount > 0) return (theory + practical) / 2.0;
        if (tCount > 0) return theory;
        if (pCount > 0) return practical;
        return 0.0;
    }

    public List<MedicalRow> getMedicals(String studentId, String courseId) throws SQLException {
        List<MedicalRow> rows = new ArrayList<>();
        String sql = "SELECT medical_id, session_type, session_no, medical_date, " +
                "submission_date, description, approval_status FROM medical " +
                "WHERE student_id = ? AND course_id = ? ORDER BY medical_date DESC, medical_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new MedicalRow(rs.getInt("medical_id"),
                            rs.getString("session_type"), rs.getInt("session_no"),
                            rs.getDate("medical_date"), rs.getDate("submission_date"),
                            rs.getString("description"), rs.getString("approval_status")));
                }
            }
        }
        return rows;
    }

    /** CA is computed with the EXISTING CACalculator (best two quizzes, weighted marks).
     *  A course's total CA weight is entered in the UI because that value is not
     *  persisted separately in the supplied schema. No marks/assessments -> PENDING.
     */
    public List<EligibilityRow> getEligibility(List<StudentInfo> students, CourseInfo course,
                                                double caWeight) throws SQLException {
        List<Assessment> assessments;
        try {
            assessments = new AssessmentDAOImpl().getAssessmentsByCourse(course.id());
        } catch (DatabaseException e) {
            throw new SQLException("Failed to read course assessments", e);
        }
        double configured = CACalculator.countedWeight(assessments);
        boolean setupReady = !assessments.isEmpty() && Math.abs(configured - caWeight) < 0.001;
        Set<Integer> assessmentIds = new HashSet<>();
        for (Assessment a : assessments) assessmentIds.add(a.getAssessmentId());

        List<EligibilityRow> out = new ArrayList<>();
        MarkDAO marksDao = new MarkDAOImpl();
        for (StudentInfo student : students) {
            Double ca = null;
            String caStatus;
            String note = "Current records only; eligibility may change as new sessions/marks are recorded.";
            if (!setupReady) {
                caStatus = "PENDING";
                note = String.format("CA setup incomplete: weightages total %.1f%%; expected %.1f%%.",
                        configured, caWeight);
            } else {
                List<Mark> marks;
                try {
                    marks = marksDao.getMarksByStudent(student.id());
                } catch (DatabaseException e) {
                    throw new SQLException("Failed to read marks for " + student.id(), e);
                }
                boolean anyMark = false;
                for (Mark m : marks) {
                    if (assessmentIds.contains(m.getAssessmentId())) {
                        anyMark = true;
                        break;
                    }
                }
                if (!anyMark) {
                    caStatus = "PENDING";
                    note = "No CA marks available for this student and course.";
                } else {
                    ca = Math.min(100.0, 100 * CACalculator.earned(
                            student.id(), assessments, marks) / caWeight);
                    caStatus = ca >= 40.0 ? "ELIGIBLE" : "NOT ELIGIBLE";
                }
            }

            AttendanceSummary summary = getAttendance(student.id(), course.id());
            Double raw = summary.hasData() ? summary.combined() : null;
            Double adjusted = summary.hasData() ? summary.combinedAdjusted() : null;
            String attStatus = adjusted == null ? "PENDING" :
                    adjusted >= 80.0 ? "ELIGIBLE" : "NOT ELIGIBLE";
            String finalStatus = ca == null || adjusted == null ? "PENDING" :
                    (ca >= 40.0 && adjusted >= 80.0 ? "ELIGIBLE" : "NOT ELIGIBLE");
            out.add(new EligibilityRow(student, course.id(), ca, raw, adjusted,
                    caStatus, attStatus, finalStatus, note));
        }
        return out;
    }
}
