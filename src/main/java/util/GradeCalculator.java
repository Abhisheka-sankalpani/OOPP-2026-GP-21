package util;

import exception.InvalidMarkException;

public class GradeCalculator {

    private GradeCalculator() { }

    public static Grade getGrade(double marks) throws InvalidMarkException {
        if (marks < 0 || marks > 100) {
            throw new InvalidMarkException("Marks must be between 0 and 100, but got " + marks);
        }
        if (marks >= 85) return Grade.A_PLUS;
        if (marks >= 75) return Grade.A;
        if (marks >= 70) return Grade.A_MINUS;
        if (marks >= 65) return Grade.B_PLUS;
        if (marks >= 60) return Grade.B;
        if (marks >= 55) return Grade.B_MINUS;
        if (marks >= 50) return Grade.C_PLUS;
        if (marks >= 45) return Grade.C;
        if (marks >= 40) return Grade.C_MINUS;
        if (marks >= 35) return Grade.D;
        return Grade.E;
    }

    /** Overloaded: in a repeat attempt the highest grade possible is C. */
    public static Grade getGrade(double marks, boolean isRepeat) throws InvalidMarkException {
        Grade grade = getGrade(marks);
        if (isRepeat && grade.ordinal() < Grade.C.ordinal()) {
            return Grade.C;
        }
        return grade;
    }
}
