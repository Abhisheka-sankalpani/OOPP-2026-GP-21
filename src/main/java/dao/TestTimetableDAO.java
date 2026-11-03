package dao;

import model.Timetable;

import java.sql.SQLException;
import java.sql.Time;
import java.util.Scanner;

public class TestTimetableDAO {

    public static void main(String[] args) {

        TimetableDAO dao = new TimetableDAO();
        Scanner scanner = new Scanner(System.in);

        try {

            System.out.print("Enter existing Course ID: ");
            String courseId = scanner.nextLine().trim();

            // 1. CREATE
            Timetable timetable = new Timetable(
                    courseId,
                    "TEST_DEPT",
                    "THEORY",
                    "FRIDAY",
                    Time.valueOf("17:00:00"),
                    Time.valueOf("18:00:00"),
                    "TEST-LAB-01"
            );

            boolean added = dao.addTimetableEntry(timetable);

            if (!added) {
                System.out.println("Timetable Add Failed!");
                return;
            }

            int id = timetable.getTimetableId();

            System.out.println("Timetable Added Successfully!");
            System.out.println("Timetable ID: " + id);

            // 2. READ
            Timetable saved = dao.getTimetableById(id);

            if (saved != null) {
                System.out.println("Timetable: " + saved);
            }

            // 3. CHECK SCHEDULING CONFLICT
            Timetable conflicting = new Timetable(
                    courseId,
                    "TEST_DEPT",
                    "PRACTICAL",
                    "FRIDAY",
                    Time.valueOf("17:30:00"),
                    Time.valueOf("18:30:00"),
                    "TEST-LAB-01"
            );

            String conflict = dao.checkSchedulingConflict(conflicting);

            if (conflict != null) {
                System.out.println("Conflict Detected: " + conflict);
            } else {
                System.out.println("No Scheduling Conflict!");
            }

            // 4. UPDATE
            if (saved != null) {

                saved.setStartTime(Time.valueOf("18:00:00"));
                saved.setEndTime(Time.valueOf("19:00:00"));

                boolean updated = dao.updateTimetableEntry(saved);

                if (updated) {
                    System.out.println("Timetable Updated Successfully!");
                }
            }

            // 5. READ ALL
            System.out.println("\nAll Timetable Entries:");

            for (Timetable t : dao.getAllTimetableEntries()) {
                System.out.println(t);
            }

            // 6. COUNT
            System.out.println(
                    "Total Timetable Entries: " +
                            dao.getTotalTimetableCount()
            );

            // 7. DELETE
            boolean deleted = dao.deleteTimetableEntry(id);

            if (deleted) {
                System.out.println("Timetable Deleted Successfully!");
            }

        } catch (SQLException | IllegalArgumentException e) {

            System.out.println("Error: " + e.getMessage());
        }
    }
}