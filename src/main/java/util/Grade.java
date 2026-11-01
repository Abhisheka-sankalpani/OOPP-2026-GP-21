package util;

/** UGC grades and Grade Point Values (4-point scale). Order: best to worst. */
public enum Grade {
    A_PLUS("A+", 4.0),
    A("A", 4.0),
    A_MINUS("A-", 3.7),
    B_PLUS("B+", 3.3),
    B("B", 3.0),
    B_MINUS("B-", 2.7),
    C_PLUS("C+", 2.3),
    C("C", 2.0),
    C_MINUS("C-", 1.7),
    D("D", 1.3),
    E("E", 0.0);

    private final String label;
    private final double gradePoint;

    Grade(String label, double gradePoint) {
        this.label = label;
        this.gradePoint = gradePoint;
    }

    public String getLabel() { return label; }
    public double getGradePoint() { return gradePoint; }

    /** Grade D or above is a pass. */
    public boolean isPass() { return this != E; }

    /** Converts "A+", "B-" ... (as stored in the database) back to a Grade. */
    public static Grade fromLabel(String label) {
        for (Grade g : values()) {
            if (g.label.equals(label)) return g;
        }
        throw new IllegalArgumentException("Unknown grade: " + label);
    }
}
