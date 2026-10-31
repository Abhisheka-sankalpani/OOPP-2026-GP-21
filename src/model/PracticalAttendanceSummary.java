package model;

/**
 * Concrete subclass of AttendanceSummary for PRACTICAL sessions.
 *
 * Demonstrates Polymorphism — overrides calculatePercentage() with
 * the practical-specific formula.
 *
 * Practical formula:
 *   Practical Attendance % = (practicalAttended / practicalTotal) × 100
 */
public class PracticalAttendanceSummary extends AttendanceSummary {

    public PracticalAttendanceSummary() {
        super();
    }

    public PracticalAttendanceSummary(String studentId, String courseId,
                                      String courseName, int attended,
                                      int total, int medicalApproved) {
        super(studentId, courseId, courseName, attended, total, medicalApproved);
    }

    /**
     * Polymorphic override — calculates PRACTICAL attendance percentage.
     */
    @Override
    public double calculatePercentage() {
        if (total == 0) return 0.0;
        return ((double) attended / total) * 100.0;
    }

    @Override
    public String toString() {
        return String.format(
            "Practical [%s - %s] Attended: %d/%d  = %.2f%%",
            courseId, courseName, attended, total, calculatePercentage()
        );
    }
}
