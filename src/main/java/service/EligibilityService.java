package service;

import model.EligibilityResult;

public class EligibilityService {

    // Threshold constants
    public static final double CA_PASS_THRESHOLD = 40.0;
    public static final double ATTENDANCE_PASS_THRESHOLD = 80.0;

    // Core method checking eligibility
    public EligibilityResult checkEligibility(String courseId, double caMark, double attendancePercentage) {
        boolean caEligible = caMark >= CA_PASS_THRESHOLD;
        boolean attendanceEligible = attendancePercentage >= ATTENDANCE_PASS_THRESHOLD;
        boolean finalEligible = caEligible && attendanceEligible;

        return new EligibilityResult(
                courseId,
                caMark,
                attendancePercentage,
                caEligible,
                attendanceEligible,
                finalEligible
        );
    }
}