import exception.InvalidMarkException;
import model.*;
import util.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Lecturer lec = new Lecturer("lec1", "lec123", "Kamal", "Silva",
                "kamal@fot.ruh.ac.lk", null, null, "ICT");
        lec.showDashboard();

        try {
            double[] totals = {88, 72, 58, 41};
            int[] credits = {3, 2, 3, 2};
            List<Result> results = new ArrayList<>();

            for (int i = 0; i < totals.length; i++) {
                Result r = new Result("tg1001", "COURSE" + (i + 1), credits[i], false);
                r.setCaTotal(totals[i]);
                r.setFinalExamScore(totals[i]);
                r.calculateTotal(40);
                System.out.println(r.getCourseId() + " -> " + r.getTotalScore()
                        + " " + r.getGrade().getLabel() + " (" + r.getGradePoint() + ")");
                results.add(r);
            }

            double sgpa = GPACalculator.calculateSGPA(results);
            System.out.println("SGPA = " + sgpa);
            System.out.println("Semester completed = " + GPACalculator.hasCompletedSemester(results));

            new Mark(1, 1, "tg1001", 105);   // throws InvalidMarkException
        } catch (InvalidMarkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
