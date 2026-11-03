import dao.*;
import exception.DatabaseException;
import exception.InvalidMarkException;
import model.*;

import java.util.List;

/** Quick check that the database connection and DAOs work. Needs XAMPP MySQL running + fot_mis imported. */
public class DBTest {
    public static void main(String[] args) {
        try {
            AssessmentDAO assessmentDAO = new AssessmentDAOImpl();
            MarkDAO markDAO = new MarkDAOImpl();

            assessmentDAO.addAssessment(new Assessment(0, "ICT2132", "Quiz 1", 20));
            List<Assessment> list = assessmentDAO.getAssessmentsByCourse("ICT2132");
            Assessment last = list.get(list.size() - 1);
            System.out.println("Assessment saved: " + last.getName() + " (id " + last.getAssessmentId() + ")");

            markDAO.addMark(new Mark(0, last.getAssessmentId(), "tg1001", 75));
            for (Mark m : markDAO.getMarksByStudent("tg1001")) {
                System.out.println("Mark " + m.getMarkId() + " -> " + m.getScore());
            }
        } catch (DatabaseException | InvalidMarkException e) {
            System.out.println("Error: " + e.getMessage());
            if (e.getCause() != null) System.out.println("Cause: " + e.getCause().getMessage());
        }
    }
}
