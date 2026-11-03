package util;

import model.Assessment;
import model.Mark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CA rules: assessments (quizzes, mid exam, assignments ...) carry a weightage as a % of the whole course
 * (e.g. CA = 30). All quizzes share ONE weightage (e.g. 10) and only the best two quiz marks are counted.
 */
public class CACalculator {

    public static final int BEST_QUIZZES_COUNTED = 2;

    private CACalculator() { }

    public static boolean isQuiz(Assessment assessment) {
        return assessment.getName().toLowerCase().startsWith("quiz");
    }

    /** Weight that counts towards CA: every non-quiz assessment plus the quiz part (counted once). */
    public static double countedWeight(List<Assessment> assessments) {
        double total = 0;
        double quizWeight = 0;
        for (Assessment a : assessments) {
            if (isQuiz(a)) {
                quizWeight = Math.max(quizWeight, a.getWeightage());
            } else {
                total += a.getWeightage();
            }
        }
        return total + quizWeight;
    }

    /** CA marks earned by one student, out of the CA weight (e.g. out of 30). */
    public static double earned(String studentId, List<Assessment> assessments, List<Mark> marks) {
        double total = 0;
        double quizWeight = 0;
        List<Double> quizScores = new ArrayList<>();

        for (Assessment a : assessments) {
            Double score = findScore(a, studentId, marks);
            if (isQuiz(a)) {
                quizWeight = Math.max(quizWeight, a.getWeightage());
                if (score != null) {
                    quizScores.add(score);
                }
            } else if (score != null) {
                total += score * a.getWeightage() / 100.0;
            }
        }

        // best two quizzes are averaged; a missing quiz counts as 0
        Collections.sort(quizScores, Collections.reverseOrder());
        double sum = 0;
        for (int i = 0; i < BEST_QUIZZES_COUNTED && i < quizScores.size(); i++) {
            sum += quizScores.get(i);
        }
        total += (sum / BEST_QUIZZES_COUNTED) * quizWeight / 100.0;
        return total;
    }

    /** Latest mark of the student for the assessment, or null if there is none. */
    private static Double findScore(Assessment assessment, String studentId, List<Mark> marks) {
        Double found = null;
        for (Mark m : marks) {
            if (m.getAssessmentId() == assessment.getAssessmentId() && m.getStudentId().equals(studentId)) {
                found = m.getScore();
            }
        }
        return found;
    }
}


