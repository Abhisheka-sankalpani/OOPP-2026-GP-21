package model;

/**
 * Holds the aggregated attendance statistics for one student in one course.
 *
 * Polymorphism is applied here: calculatePercentage() is overridden in
 * the two concrete subclasses TheoryAttendanceSummary and
 * PracticalAttendanceSummary, each implementing the calculation differently.
 *
 * OOP features demonstrated
 * ─────────────────────────
 *  • Abstraction  – abstract method calculatePercentage()
 *  • Inheritance  – TheoryAttendanceSummary / PracticalAttendanceSummary extend this
 *  • Polymorphism – calculatePercentage() resolved at runtime
 */
public abstract class AttendanceSummary {

    // --- project constants ---
    public static final int TOTAL_SESSIONS     = 15;    // sessions per component
    public static final int SESSION_HOURS      = 2;     // hours per session
    public static final double PASS_THRESHOLD  = 80.0;  // exam eligibility cut-off

    protected String studentId;
    protected String courseId;
    protected String courseName;
    protected int    attended;       // sessions actually attended
    protected int    total;          // total sessions held (≤ 15)
    protected int    medicalApproved;// approved medical absences

    // Default constructor
    protected AttendanceSummary() {}

    protected AttendanceSummary(String studentId, String courseId,
                                String courseName, int attended,
                                int total, int medicalApproved) {
        this.studentId       = studentId;
        this.courseId        = courseId;
        this.courseName      = courseName;
        this.attended        = attended;
        this.total           = total;
        this.medicalApproved = medicalApproved;
    }

    // ── Abstract method (Abstraction + Polymorphism) ──────────────────────
    /**
     * Returns the raw attendance percentage (without medical adjustment).
     * Subclasses override to apply their specific weighting / formula.
     */
    public abstract double calculatePercentage();

    // ── Concrete helper available to all subclasses ───────────────────────
    /**
     * Attendance percentage when approved medicals are counted as present.
     *
     * Formula:  (attended + medicalApproved) / total × 100
     * The result is capped at 100 %.
     */
    public double calculatePercentageWithMedical() {
        if (total == 0) return 0.0;
        double adjusted = attended + medicalApproved;
        if (adjusted > total) adjusted = total;
        return (adjusted / total) * 100.0;
    }

    /** Whether the student reaches 80 % without medical consideration. */
    public boolean isEligible() {
        return calculatePercentage() >= PASS_THRESHOLD;
    }

    /** Whether the student reaches 80 % after medical adjustment. */
    public boolean isEligibleWithMedical() {
        return calculatePercentageWithMedical() >= PASS_THRESHOLD;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────
    public String getStudentId()                     { return studentId; }
    public void   setStudentId(String s)              { this.studentId = s; }

    public String getCourseId()                      { return courseId; }
    public void   setCourseId(String c)               { this.courseId = c; }

    public String getCourseName()                    { return courseName; }
    public void   setCourseName(String n)             { this.courseName = n; }

    public int    getAttended()                      { return attended; }
    public void   setAttended(int a)                  { this.attended = a; }

    public int    getTotal()                         { return total; }
    public void   setTotal(int t)                    { this.total = t; }

    public int    getMedicalApproved()               { return medicalApproved; }
    public void   setMedicalApproved(int m)          { this.medicalApproved = m; }

    /** Returns a human-readable category label based on raw percentage. */
    public String getCategory() {
        double pct = calculatePercentage();
        if (pct > 80.0)  return "> 80%";
        if (pct == 80.0) return "= 80%";
        return "< 80%";
    }
}
