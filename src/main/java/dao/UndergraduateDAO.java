package dao;

import database.DBConnection;
import exception.DatabaseException;
import model.Assessment;
import model.Mark;
import model.Result;
import model.Undergraduate;
import util.CACalculator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class UndergraduateDAO {

    // Read the student's basic profile.
    public Undergraduate getStudentById(String studentId) {
        String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, "
                + "u.contact_no, u.profile_pic, s.reg_no, s.department, "
                + "s.batch, s.is_repeat FROM users u "
                + "JOIN undergraduate s ON u.user_id = s.student_id "
                + "WHERE u.user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Undergraduate(
                            rs.getString("user_id"),
                            rs.getString("reg_no"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("contact_no"),
                            rs.getString("department"),
                            rs.getString("batch"),
                            rs.getString("profile_pic"),
                            rs.getBoolean("is_repeat")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student: " + e.getMessage());
        }
        return null;
    }

    // Undergraduate may change contact/profile-picture information.
    public boolean updateContactDetails(String studentId, String email,
                                        String contactNo, String profilePic) {
        String sql = "UPDATE users SET email = ?, contact_no = ?, "
                + "profile_pic = ? WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, contactNo);
            ps.setString(3, profilePic);
            ps.setString(4, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student profile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Provides the seven columns expected by UndergraduateEligibilityForm:
     * Course ID, Course Title, CA %, CA Status, Attendance %,
     * Attendance Status, Final Exam Status.
     *
     * The Lecturer module stores CA as a percentage out of 100 in result.ca_total.
     * If a saved result is available, use that exact value. Otherwise use the
     * shared CACalculator (including the best-two-quizzes rule).
     */
    public List<Object[]> getDetailedEligibilityData(String studentId) {
        List<Object[]> rows = new ArrayList<>();
        if (studentId == null || studentId.isBlank()) {
            return rows;
        }

        try {
            List<Mark> studentMarks = new MarkDAOImpl().getMarksByStudent(studentId);
            Map<String, Result> savedResults = new HashMap<>();
            for (Result r : new ResultDAOImpl().getResultsByStudent(studentId)) {
                savedResults.put(r.getCourseId(), r);
            }

            LecturerReadOnlyDAO attendanceDAO = new LecturerReadOnlyDAO();

            // Include courses with enrollment, attendance, CA marks, or final results.
            // In particular, a saved lecturer result appears even if no attendance
            // sessions have been entered for that student yet.
            String sql = "SELECT c.course_id, c.course_name FROM course c WHERE "
                    + "EXISTS (SELECT 1 FROM student_course sc "
                    + "        WHERE sc.course_id = c.course_id AND sc.student_id = ?) "
                    + "OR EXISTS (SELECT 1 FROM attendance at "
                    + "           WHERE at.course_id = c.course_id AND at.student_id = ?) "
                    + "OR EXISTS (SELECT 1 FROM assessment a JOIN mark m "
                    + "           ON m.assessment_id = a.assessment_id "
                    + "           WHERE a.course_id = c.course_id AND m.student_id = ?) "
                    + "OR EXISTS (SELECT 1 FROM result r "
                    + "           WHERE r.course_id = c.course_id AND r.student_id = ?) "
                    + "ORDER BY c.course_id";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                for (int i = 1; i <= 4; i++) {
                    ps.setString(i, studentId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String courseId = rs.getString("course_id");
                        String courseName = rs.getString("course_name");

                        // CA: saved lecturer result is already normalized to 0..100.
                        Double caPercent = null;
                        Result saved = savedResults.get(courseId);
                        if (saved != null) {
                            caPercent = saved.getCaTotal();
                        } else {
                            List<Assessment> assessments =
                                    new AssessmentDAOImpl().getAssessmentsByCourse(courseId);
                            double caWeight = CACalculator.countedWeight(assessments);

                            Set<Integer> ids = new HashSet<>();
                            for (Assessment a : assessments) {
                                ids.add(a.getAssessmentId());
                            }
                            boolean hasMarks = false;
                            for (Mark m : studentMarks) {
                                if (ids.contains(m.getAssessmentId())) {
                                    hasMarks = true;
                                    break;
                                }
                            }
                            if (hasMarks && caWeight > 0.0) {
                                double earned = CACalculator.earned(
                                        studentId, assessments, studentMarks);
                                caPercent = Math.min(100.0,
                                        Math.max(0.0, earned / caWeight * 100.0));
                            }
                        }

                        String caText = caPercent == null
                                ? "-" : String.format(Locale.US, "%.1f", caPercent);
                        String caStatus = caPercent == null
                                ? "PENDING"
                                : (caPercent >= 40.0 ? "ELIGIBLE" : "NOT ELIGIBLE");

                        // Use the same attendance/approved-medical calculation
                        // as LecturerReadOnlyDAO. No recorded sessions -> PENDING.
                        LecturerReadOnlyDAO.AttendanceSummary att =
                                attendanceDAO.getAttendance(studentId, courseId);
                        Double attendancePercent = att.hasData()
                                ? att.combinedAdjusted() : null;

                        String attendanceText = attendancePercent == null
                                ? "-"
                                : String.format(Locale.US, "%.1f%%", attendancePercent);
                        String attendanceStatus = attendancePercent == null
                                ? "PENDING"
                                : (attendancePercent >= 80.0
                                    ? "ELIGIBLE" : "NOT ELIGIBLE");

                        String finalStatus;
                        if (caPercent == null || attendancePercent == null) {
                            finalStatus = "PENDING";
                        } else {
                            finalStatus = (caPercent >= 40.0 && attendancePercent >= 80.0)
                                    ? "ELIGIBLE" : "NOT ELIGIBLE";
                        }

                        rows.add(new Object[]{
                                courseId, courseName, caText, caStatus,
                                attendanceText, attendanceStatus, finalStatus
                        });
                    }
                }
            }
        } catch (SQLException | DatabaseException e) {
            System.err.println("Error loading eligibility data: " + e.getMessage());
        }
        return rows;
    }

    /**
     * Lecturer results already stored in result table. Returns a course-ID map
     * so Student GUI can show Final Exam / Total / Grade / GPV without editing
     * the eligibility-data array used by the eligibility engine dialog.
     *
     * No stored result for a course means that its result cells stay blank ("-").
     */
    public Map<String, Result> getSavedResultsByCourse(String studentId)
            throws DatabaseException {
        Map<String, Result> byCourse = new HashMap<>();
        if (studentId == null || studentId.isBlank()) {
            return byCourse;
        }
        for (Result result : new ResultDAOImpl().getResultsByStudent(studentId)) {
            byCourse.put(result.getCourseId(), result);
        }
        return byCourse;
    }

    /**
     * Level 2, Semester 1: calculate the credit-weighted average of saved
     * results for the courses confirmed to belong to this semester.
     *
     * Temporary mapping: the current course table does not contain a semester
     * column. Keep this list in sync with the official semester course list;
     * preferably replace it with a semester field in a future schema update.
     * Courses without saved results are not counted (provisional SGPA).
     */
    private static final List<String> LEVEL_2_SEMESTER_1_COURSES = List.of(
            "ENG2122", "ICT2113", "ICT2121", "ICT2132",
            "ICT2142", "ICT2152", "ICT2162", "TCS2112"
    );

    public String getStudentSGPA(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return "N/A";
        }

        String placeholders = String.join(", ",
                Collections.nCopies(LEVEL_2_SEMESTER_1_COURSES.size(), "?"));
        String sql = "SELECT SUM(r.grade_point * c.credit) / "
                + "NULLIF(SUM(c.credit), 0) AS sgpa "
                + "FROM result r JOIN course c ON r.course_id = c.course_id "
                + "WHERE r.student_id = ? AND c.credit > 0 "
                + "AND c.course_id IN (" + placeholders + ")";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            for (int i = 0; i < LEVEL_2_SEMESTER_1_COURSES.size(); i++) {
                ps.setString(i + 2, LEVEL_2_SEMESTER_1_COURSES.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double sgpa = rs.getDouble("sgpa");
                    if (!rs.wasNull()) {
                        return String.format(Locale.US, "%.2f", sgpa);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating SGPA: " + e.getMessage());
        }
        return "N/A";
    }

    // Overall credit-weighted GPA, based on saved final results.
    public String getStudentCGPA(String studentId) {
        String sql = "SELECT SUM(r.grade_point * c.credit) / "
                + "NULLIF(SUM(c.credit), 0) AS cgpa "
                + "FROM result r JOIN course c ON r.course_id = c.course_id "
                + "WHERE r.student_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double cgpa = rs.getDouble("cgpa");
                    if (!rs.wasNull()) {
                        return String.format(Locale.US, "%.2f", cgpa);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating CGPA: " + e.getMessage());
        }
        return "N/A";
    }
}
