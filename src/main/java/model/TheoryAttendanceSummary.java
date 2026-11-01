package model;

/**
 * Concrete subclass of AttendanceSummary for THEORY sessions.
 *
 * Demonstrates Polymorphism — overrides calculatePercentage() with
 * the theory-specific formula.
 *
 * Theory formula:
 *   Theoretical Attendance % = (theoryAttended / theoryTotal) × 100
 */
public class TheoryAttendanceSummary extends AttendanceSummary {

    public TheoryAttendanceSummary() {
        super();
    }

    public TheoryAttendanceSummary(String studentId, String courseId,
                                   String courseName, int attended,
                                   int total, int medicalApproved) {
        super(studentId, courseId, courseName, attended, total, medicalApproved);
    }

    /**
     * Polymorphic override — calculates THEORY attendance percentage.
     */
    @Override
    public double calculatePercentage() {
        if (total == 0) return 0.0;
        return ((double) attended / total) * 100.0;
    }

    @Override
    public String toString() {
        return String.format(
            "Theory [%s - %s] Attended: %d/%d  = %.2f%%",
            courseId, courseName, attended, total, calculatePercentage()
        );
    }
}
