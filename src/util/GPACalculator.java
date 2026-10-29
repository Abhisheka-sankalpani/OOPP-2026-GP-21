package util;

import model.Result;
import java.util.List;

public class GPACalculator {

    private GPACalculator() { }

    /** GPA = sum(credits x GPV) / sum(credits), non-GPA modules excluded. */
    public static double calculateGPA(List<Result> results) {
        double points = 0;
        int credits = 0;
        for (Result r : results) {
            if (!r.isGpaModule() || r.getGrade() == null) continue;
            points += r.getGradePoint() * r.getCredits();
            credits += r.getCredits();
        }
        if (credits == 0) return 0.0;
        return Math.round(points / credits * 100.0) / 100.0;
    }

    /** SGPA: pass in the results of ONE semester. */
    public static double calculateSGPA(List<Result> semesterResults) {
        return calculateGPA(semesterResults);
    }

    /** CGPA: pass in the results of ALL semesters. */
    public static double calculateCGPA(List<Result> allResults) {
        return calculateGPA(allResults);
    }

    /** Semester completed: SGPA >= 2.00, no E grade, and at most three C- / D grades. */
    public static boolean hasCompletedSemester(List<Result> semesterResults) {
        int weakGrades = 0;
        for (Result r : semesterResults) {
            if (r.getGrade() == Grade.E) return false;
            if (r.getGrade() == Grade.C_MINUS || r.getGrade() == Grade.D) weakGrades++;
        }
        return calculateSGPA(semesterResults) >= 2.0 && weakGrades <= 3;
    }

    /** Class awarded from the overall GPA. */
    public static String getClassAwarded(double gpa) {
        if (gpa >= 3.70) return "First Class";
        if (gpa >= 3.30) return "Second Class (Upper Division)";
        if (gpa >= 3.00) return "Second Class (Lower Division)";
        return "No class";
    }
}
