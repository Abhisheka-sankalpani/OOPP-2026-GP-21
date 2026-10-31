package model;

/**
 * Holds the combined (overall) attendance summary for one student in one course.
 *
 * Combined % = weighted average of Theory % and Practical %:
 *   Combined % = (theoryPct + practicalPct) / 2
 *
 * Demonstrates Polymorphism — overrides calculatePercentage() with the
 * combined (blended) formula.
 */
public class CombinedAttendanceSummary extends AttendanceSummary {

    private TheoryAttendanceSummary   theorySummary;
    private PracticalAttendanceSummary practicalSummary;

    public CombinedAttendanceSummary() {
        super();
    }

    public CombinedAttendanceSummary(String studentId, String courseId,
                                     String courseName,
                                     TheoryAttendanceSummary theorySummary,
                                     PracticalAttendanceSummary practicalSummary) {
        super(studentId, courseId, courseName,
              (theorySummary   != null ? theorySummary.getAttended()   : 0)
            + (practicalSummary != null ? practicalSummary.getAttended() : 0),
              (theorySummary   != null ? theorySummary.getTotal()       : 0)
            + (practicalSummary != null ? practicalSummary.getTotal()   : 0),
              (theorySummary   != null ? theorySummary.getMedicalApproved()   : 0)
            + (practicalSummary != null ? practicalSummary.getMedicalApproved() : 0));
        this.theorySummary    = theorySummary;
        this.practicalSummary = practicalSummary;
    }

    /**
     * Polymorphic override — calculates COMBINED attendance percentage.
     * Uses simple average of theory % and practical %.
     */
    @Override
    public double calculatePercentage() {
        double theoryPct    = (theorySummary    != null) ? theorySummary.calculatePercentage()    : 0.0;
        double practicalPct = (practicalSummary != null) ? practicalSummary.calculatePercentage() : 0.0;

        // If course has only one component, use that component's percentage
        boolean hasTheory    = theorySummary    != null && theorySummary.getTotal()    > 0;
        boolean hasPractical = practicalSummary != null && practicalSummary.getTotal() > 0;

        if (hasTheory && hasPractical) return (theoryPct + practicalPct) / 2.0;
        if (hasTheory)    return theoryPct;
        if (hasPractical) return practicalPct;
        return 0.0;
    }

    @Override
    public double calculatePercentageWithMedical() {
        double theoryPctM    = (theorySummary    != null) ? theorySummary.calculatePercentageWithMedical()    : 0.0;
        double practicalPctM = (practicalSummary != null) ? practicalSummary.calculatePercentageWithMedical() : 0.0;

        boolean hasTheory    = theorySummary    != null && theorySummary.getTotal()    > 0;
        boolean hasPractical = practicalSummary != null && practicalSummary.getTotal() > 0;

        if (hasTheory && hasPractical) return (theoryPctM + practicalPctM) / 2.0;
        if (hasTheory)    return theoryPctM;
        if (hasPractical) return practicalPctM;
        return 0.0;
    }

    // Accessors for the sub-summaries
    public TheoryAttendanceSummary    getTheorySummary()    { return theorySummary; }
    public PracticalAttendanceSummary getPracticalSummary() { return practicalSummary; }
}
