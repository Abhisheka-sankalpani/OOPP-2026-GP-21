package dao;

import db.DBConnection;
import model.Timetable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TimetableDAO {

    public List<Timetable> getTimetableByDepartment(String department) {
        List<Timetable> list = new ArrayList<>();

        String sql = "SELECT t.timetable_id, t.course_id, c.course_name, t.department, "
                + "t.session_type, t.day_of_week, t.start_time, t.end_time, t.venue_id "
                + "FROM timetable t "
                + "LEFT JOIN course c ON t.course_id = c.course_id "
                + "WHERE t.department = ? "
                + "ORDER BY "
                + "FIELD(t.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'), "
                + "t.start_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timetable entry = new Timetable(
                            rs.getInt("timetable_id"),
                            rs.getString("course_id"),
                            rs.getString("course_name"),
                            rs.getString("department"),
                            rs.getString("session_type"),
                            rs.getString("day_of_week"),
                            String.valueOf(rs.getTime("start_time")),
                            String.valueOf(rs.getTime("end_time")),
                            rs.getString("venue_id")
                    );
                    list.add(entry);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching timetable: " + e.getMessage());
        }
        return list;
    }
}