package dao;

import database.DBConnection;
import model.Timetable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimetableDAO {

    // Common SELECT Query
    private static final String BASE_SELECT =
            "SELECT t.timetable_id, t.course_id, c.course_name, " +
                    "t.department, t.session_type, t.day_of_week, " +
                    "t.start_time, t.end_time, t.venue_id " +
                    "FROM timetable t " +
                    "LEFT JOIN course c ON t.course_id = c.course_id ";


    // CREATE - Add New Timetable Entry
    public boolean addTimetableEntry(Timetable entry)
            throws SQLException {

        validateTimetable(entry);

        String sql = "INSERT INTO timetable " +
                "(course_id, department, session_type, " +
                "day_of_week, start_time, end_time, venue_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            String conflict = checkSchedulingConflict(con, entry);

            if (conflict != null) {
                throw new IllegalArgumentException(conflict);
            }

            try (PreparedStatement ps = con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1,
                        entry.getCourseId().trim().toUpperCase(Locale.ROOT));

                ps.setString(2, entry.getDepartment().trim());

                ps.setString(3,
                        entry.getSessionType().trim().toUpperCase(Locale.ROOT));

                ps.setString(4,
                        entry.getDayOfWeek().trim().toUpperCase(Locale.ROOT));

                ps.setTime(5, entry.getStartTime());

                ps.setTime(6, entry.getEndTime());

                ps.setString(7, normalizeVenue(entry.getVenueId()));

                int rows = ps.executeUpdate();

                if (rows > 0) {

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (rs.next()) {
                            entry.setTimetableId(rs.getInt(1));
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }


    // READ - Get Timetable Entry By ID
    public Timetable getTimetableById(int timetableId)
            throws SQLException {

        String sql = BASE_SELECT +
                "WHERE t.timetable_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, timetableId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapResultSetToTimetable(rs);
                }
            }
        }

        return null;
    }


    // READ - Get All Timetable Entries
    public List<Timetable> getAllTimetableEntries()
            throws SQLException {

        List<Timetable> timetables = new ArrayList<>();

        String sql = BASE_SELECT +
                "ORDER BY FIELD(t.day_of_week, " +
                "'MONDAY', 'TUESDAY', 'WEDNESDAY', " +
                "'THURSDAY', 'FRIDAY'), t.start_time";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Timetable entry = mapResultSetToTimetable(rs);

                timetables.add(entry);
            }
        }

        return timetables;
    }


    // UPDATE - Update Existing Timetable Entry
    public boolean updateTimetableEntry(Timetable entry)
            throws SQLException {

        validateTimetable(entry);

        if (entry.getTimetableId() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Timetable ID"
            );
        }

        String sql = "UPDATE timetable SET " +
                "course_id = ?, " +
                "department = ?, " +
                "session_type = ?, " +
                "day_of_week = ?, " +
                "start_time = ?, " +
                "end_time = ?, " +
                "venue_id = ? " +
                "WHERE timetable_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            String conflict = checkSchedulingConflict(con, entry);

            if (conflict != null) {
                throw new IllegalArgumentException(conflict);
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1,
                        entry.getCourseId().trim().toUpperCase(Locale.ROOT));

                ps.setString(2, entry.getDepartment().trim());

                ps.setString(3,
                        entry.getSessionType().trim().toUpperCase(Locale.ROOT));

                ps.setString(4,
                        entry.getDayOfWeek().trim().toUpperCase(Locale.ROOT));

                ps.setTime(5, entry.getStartTime());

                ps.setTime(6, entry.getEndTime());

                ps.setString(7, normalizeVenue(entry.getVenueId()));

                ps.setInt(8, entry.getTimetableId());

                return ps.executeUpdate() > 0;
            }
        }
    }


    // DELETE - Delete Timetable Entry
    public boolean deleteTimetableEntry(int timetableId)
            throws SQLException {

        if (timetableId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid Timetable ID"
            );
        }

        String sql =
                "DELETE FROM timetable WHERE timetable_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, timetableId);

            return ps.executeUpdate() > 0;
        }
    }


    // SEARCH - Search Timetable Entries
    public List<Timetable> searchTimetable(
            String keyword,
            String dayFilter,
            String deptFilter) throws SQLException {

        List<Timetable> timetables = new ArrayList<>();

        StringBuilder sql = new StringBuilder(BASE_SELECT);

        sql.append("WHERE 1=1 ");

        boolean hasKeyword =
                keyword != null && !keyword.isBlank();

        boolean hasDay =
                dayFilter != null &&
                        !dayFilter.isBlank() &&
                        !dayFilter.equalsIgnoreCase("ALL");

        boolean hasDept =
                deptFilter != null &&
                        !deptFilter.isBlank() &&
                        !deptFilter.equalsIgnoreCase("ALL");

        if (hasKeyword) {
            sql.append(
                    "AND (LOWER(t.course_id) LIKE ? " +
                            "OR LOWER(c.course_name) LIKE ? " +
                            "OR LOWER(t.venue_id) LIKE ?) "
            );
        }

        if (hasDay) {
            sql.append("AND t.day_of_week = ? ");
        }

        if (hasDept) {
            sql.append("AND UPPER(t.department) = UPPER(?) ");
        }

        sql.append(
                "ORDER BY FIELD(t.day_of_week, " +
                        "'MONDAY', 'TUESDAY', 'WEDNESDAY', " +
                        "'THURSDAY', 'FRIDAY'), t.start_time"
        );

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql.toString())) {

            int index = 1;

            if (hasKeyword) {

                String pattern = "%" +
                        keyword.trim().toLowerCase(Locale.ROOT) + "%";

                ps.setString(index++, pattern);
                ps.setString(index++, pattern);
                ps.setString(index++, pattern);
            }

            if (hasDay) {
                ps.setString(
                        index++,
                        dayFilter.trim().toUpperCase(Locale.ROOT)
                );
            }

            if (hasDept) {
                ps.setString(index++, deptFilter.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    timetables.add(mapResultSetToTimetable(rs));
                }
            }
        }

        return timetables;
    }


    // CHECK - Scheduling Conflicts
    public String checkSchedulingConflict(Timetable entry)
            throws SQLException {

        validateTimetable(entry);

        try (Connection con = DBConnection.getConnection()) {
            return checkSchedulingConflict(con, entry);
        }
    }


    // Internal Conflict Checking
    private String checkSchedulingConflict(
            Connection con,
            Timetable entry) throws SQLException {

        String sql =
                "SELECT timetable_id, course_id, department, " +
                        "venue_id, start_time, end_time " +
                        "FROM timetable " +
                        "WHERE day_of_week = ? " +
                        "AND start_time < ? " +
                        "AND end_time > ? " +
                        "AND timetable_id <> ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(
                    1,
                    entry.getDayOfWeek().trim().toUpperCase(Locale.ROOT)
            );

            ps.setTime(2, entry.getEndTime());

            ps.setTime(3, entry.getStartTime());

            ps.setInt(4, entry.getTimetableId());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String existingVenue =
                            rs.getString("venue_id");

                    String existingDepartment =
                            rs.getString("department");

                    String existingCourse =
                            rs.getString("course_id");


                    // 1. Venue Conflict
                    if (entry.getVenueId() != null &&
                            !entry.getVenueId().isBlank() &&
                            existingVenue != null &&
                            entry.getVenueId().trim()
                                    .equalsIgnoreCase(existingVenue.trim())) {

                        return "Venue Conflict: " +
                                entry.getVenueId() +
                                " is already booked at this time.";
                    }


                    // 2. Department Conflict
                    if (!entry.getDepartment().equalsIgnoreCase(
                            "MULTIDISCIPLINARY") &&
                            existingDepartment != null &&
                            entry.getDepartment().trim()
                                    .equalsIgnoreCase(
                                            existingDepartment.trim())) {

                        return "Department Conflict: " +
                                entry.getDepartment() +
                                " already has a session at this time.";
                    }


                    // 3. Course Conflict
                    if (existingCourse != null &&
                            entry.getCourseId().trim()
                                    .equalsIgnoreCase(
                                            existingCourse.trim())) {

                        return "Course Conflict: " +
                                entry.getCourseId() +
                                " already has a session at this time.";
                    }
                }
            }
        }

        return null;
    }


    // COUNT - Get Total Timetable Entries
    public int getTotalTimetableCount()
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM timetable";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }


    // Convert ResultSet to Timetable Object
    private Timetable mapResultSetToTimetable(ResultSet rs)
            throws SQLException {

        Timetable timetable = new Timetable(

                rs.getInt("timetable_id"),

                rs.getString("course_id"),

                rs.getString("department"),

                rs.getString("session_type"),

                rs.getString("day_of_week"),

                rs.getTime("start_time"),

                rs.getTime("end_time"),

                rs.getString("venue_id")
        );

        timetable.setCourseName(
                rs.getString("course_name")
        );

        return timetable;
    }


    // Validate Timetable Details
    private void validateTimetable(Timetable entry) {

        if (entry == null) {
            throw new IllegalArgumentException(
                    "Timetable entry cannot be null"
            );
        }

        if (entry.getCourseId() == null ||
                entry.getCourseId().isBlank() ||
                entry.getCourseId().trim().length() > 10) {

            throw new IllegalArgumentException(
                    "Valid Course ID is required (maximum 10 characters)"
            );
        }

        if (entry.getDepartment() == null ||
                entry.getDepartment().isBlank() ||
                entry.getDepartment().trim().length() > 50) {

            throw new IllegalArgumentException(
                    "Valid Department is required (maximum 50 characters)"
            );
        }

        String sessionType = entry.getSessionType();

        if (sessionType == null ||
                !(sessionType.trim().equalsIgnoreCase("THEORY") ||
                        sessionType.trim().equalsIgnoreCase("PRACTICAL"))) {

            throw new IllegalArgumentException(
                    "Session Type must be THEORY or PRACTICAL"
            );
        }

        String day = entry.getDayOfWeek();

        if (day == null ||
                !(day.trim().equalsIgnoreCase("MONDAY") ||
                        day.trim().equalsIgnoreCase("TUESDAY") ||
                        day.trim().equalsIgnoreCase("WEDNESDAY") ||
                        day.trim().equalsIgnoreCase("THURSDAY") ||
                        day.trim().equalsIgnoreCase("FRIDAY"))) {

            throw new IllegalArgumentException(
                    "Day must be MONDAY to FRIDAY"
            );
        }

        if (!entry.isTimeWindowValid()) {

            throw new IllegalArgumentException(
                    "End Time must be later than Start Time"
            );
        }

        if (entry.getVenueId() != null &&
                entry.getVenueId().trim().length() > 50) {

            throw new IllegalArgumentException(
                    "Venue ID cannot exceed 50 characters"
            );
        }
    }


    // Normalize Optional Venue
    private String normalizeVenue(String venueId) {

        if (venueId == null || venueId.isBlank()) {
            return null;
        }

        return venueId.trim();
    }
}