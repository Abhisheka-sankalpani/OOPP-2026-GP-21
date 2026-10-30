package dao;

import db.DBConnection;
import exception.MedicalSubmissionException;
import model.Medical;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for medical-certificate submissions.
 *
 * Exception Handling (OOP requirement):
 *   - MedicalSubmissionException is thrown for invalid submissions.
 *   - SQLException is caught and surfaced with meaningful messages.
 */
public class MedicalDAO {

    // ─── Submit a new medical certificate ────────────────────────────────

    /**
     * Inserts a new medical submission.
     *
     * @param medical the medical record to persist
     * @throws MedicalSubmissionException if the submission is invalid
     *         (e.g. same session already has an approved medical)
     */
    public void submitMedical(Medical medical) throws MedicalSubmissionException {
        // Business rule: cannot submit for session_no outside [1,15]
        if (medical.getSessionNo() < 1 || medical.getSessionNo() > 15) {
            throw new MedicalSubmissionException(
                "Invalid session number: " + medical.getSessionNo() +
                ". Must be between 1 and 15.");
        }

        // Business rule: cannot submit duplicate APPROVED medicals for same session
        if (hasApprovedMedical(medical.getStudentId(),
                               medical.getCourseId(),
                               medical.getSessionNo(),
                               medical.getSessionType())) {
            throw new MedicalSubmissionException(
                "An approved medical already exists for session " +
                medical.getSessionNo() + " (" + medical.getSessionType() + ").");
        }

        String sql = "INSERT INTO medical (student_id, course_id, session_no, session_type, " +
                     "medical_date, submission_date, description, approval_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, medical.getStudentId());
            stmt.setString(2, medical.getCourseId());
            stmt.setInt   (3, medical.getSessionNo());
            stmt.setString(4, medical.getSessionType());
            stmt.setDate  (5, medical.getMedicalDate());
            stmt.setDate  (6, medical.getSubmissionDate() != null
                              ? medical.getSubmissionDate()
                              : new java.sql.Date(System.currentTimeMillis()));
            stmt.setString(7, medical.getDescription());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error submitting medical: " + e.getMessage());
        }
    }

    // ─── Approval / Rejection (Technical Officer action) ─────────────────

    /**
     * Updates the approval_status of a medical submission.
     *
     * @param medicalId the PK of the medical record
     * @param status    "APPROVED" or "REJECTED"
     * @throws MedicalSubmissionException if status value is not valid
     */
    public void updateApprovalStatus(int medicalId, String status)
            throws MedicalSubmissionException {

        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new MedicalSubmissionException(
                "Invalid approval status: '" + status +
                "'. Must be APPROVED or REJECTED.");
        }

        String sql = "UPDATE medical SET approval_status = ? WHERE medical_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt   (2, medicalId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating medical status: " + e.getMessage());
        }
    }

    // ─── Reads ────────────────────────────────────────────────────────────

    /**
     * Returns all medical submissions for a given student and course.
     */
    public List<Medical> getMedicalsByStudentAndCourse(String studentId, String courseId) {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT * FROM medical WHERE student_id = ? AND course_id = ? " +
                     "ORDER BY session_type, session_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching medicals: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns all PENDING medical submissions across all students for a course.
     * Used by the Technical Officer to process approvals.
     */
    public List<Medical> getPendingMedicalsByCourse(String courseId) {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT * FROM medical WHERE course_id = ? AND approval_status = 'PENDING' " +
                     "ORDER BY student_id, session_type, session_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending medicals: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns ALL medical submissions for a course (any status).
     */
    public List<Medical> getAllMedicalsByCourse(String courseId) {
        List<Medical> list = new ArrayList<>();
        String sql = "SELECT * FROM medical WHERE course_id = ? " +
                     "ORDER BY student_id, session_type, session_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all medicals: " + e.getMessage());
        }
        return list;
    }

    /**
     * Returns the count of APPROVED medical absences for a student in a course/type.
     * Used during attendance percentage calculation.
     */
    public int countApprovedMedicals(String studentId, String courseId, String sessionType) {
        String sql = "SELECT COUNT(*) FROM medical " +
                     "WHERE student_id = ? AND course_id = ? " +
                     "AND session_type = ? AND approval_status = 'APPROVED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            stmt.setString(3, sessionType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting approved medicals: " + e.getMessage());
        }
        return 0;
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    private Medical mapRow(ResultSet rs) throws SQLException {
        return new Medical(
            rs.getInt   ("medical_id"),
            rs.getString("student_id"),
            rs.getString("course_id"),
            rs.getInt   ("session_no"),
            rs.getString("session_type"),
            rs.getDate  ("medical_date"),
            rs.getDate  ("submission_date"),
            rs.getString("description"),
            rs.getString("approval_status")
        );
    }

    /** True if an APPROVED medical already exists for the same session. */
    private boolean hasApprovedMedical(String studentId, String courseId,
                                       int sessionNo, String sessionType) {
        String sql = "SELECT COUNT(*) FROM medical " +
                     "WHERE student_id = ? AND course_id = ? " +
                     "AND session_no = ? AND session_type = ? " +
                     "AND approval_status = 'APPROVED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            stmt.setInt   (3, sessionNo);
            stmt.setString(4, sessionType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking approved medical: " + e.getMessage());
        }
        return false;
    }
}
