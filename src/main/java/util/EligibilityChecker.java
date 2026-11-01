package util;

public class EligibilityChecker {

    private EligibilityChecker() { }

    /** CA part must be 40% or more to sit for the final exam. */
    public static boolean isCaEligible(double caTotal) {
        return caTotal >= 40.0;
    }

    /** Overloaded: attendance (80%+, or approved medicals) + CA. */
    public static boolean isEligible(double caTotal, double attendancePercentage) {
        return isCaEligible(caTotal) && attendancePercentage >= 80.0;
    }
}
