package dao;

import database.DBConnection;

import model.User;
import model.Admin;
import model.Lecturer;
import model.TechnicalOfficer;
import model.Student;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // CREATE - Add New User
    public boolean addUser(User user) throws SQLException {

        // Validate User
        if (user == null || user.getRole() == null) {
            throw new IllegalArgumentException("Invalid user");
        }

        if (user.getUserId() == null || user.getUserId().isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (user.getPasswordHash() == null ||
                user.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }

        // SQL Query
        String sql = "INSERT INTO users " +
                "(user_id, password, first_name, last_name, " +
                "email, contact_no, profile_pic, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Database Connection
        try (Connection con = DBConnection.getConnection()) {

            // Start Transaction
            con.setAutoCommit(false);

            try {

                // Insert Common User Details
                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, user.getUserId());
                    ps.setString(2, user.getPasswordHash());
                    ps.setString(3, user.getFirstName());
                    ps.setString(4, user.getLastName());
                    ps.setString(5, user.getEmail());
                    ps.setString(6, user.getContactNo());
                    ps.setString(7, user.getProfilePic());
                    ps.setString(8, user.getRole());

                    ps.executeUpdate();
                }

                // Insert Role-Specific Details
                insertRoleDetails(con, user);

                // Save Changes
                con.commit();

                return true;

            } catch (SQLException | RuntimeException e) {

                // Undo Changes if Error Occurs
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    // Insert Details According to User Role
    private void insertRoleDetails(Connection con, User user)
            throws SQLException {

        String sql;

        switch (user.getRole()) {

            case "ADMIN":

                if (!(user instanceof Admin)) {
                    throw new IllegalArgumentException("Invalid Admin Object");
                }

                sql = "INSERT INTO admin (admin_id) VALUES (?)";
                break;

            case "LECTURER":

                if (!(user instanceof Lecturer)) {
                    throw new IllegalArgumentException("Invalid Lecturer Object");
                }

                sql = "INSERT INTO lecturer " +
                        "(lecturer_id, department) VALUES (?, ?)";
                break;

            case "TECHNICAL_OFFICER":

                if (!(user instanceof TechnicalOfficer)) {
                    throw new IllegalArgumentException(
                            "Invalid Technical Officer Object"
                    );
                }

                sql = "INSERT INTO technical_officer " +
                        "(to_id, department) VALUES (?, ?)";
                break;

            case "STUDENT":

                if (!(user instanceof Student)) {
                    throw new IllegalArgumentException("Invalid Student Object");
                }

                sql = "INSERT INTO undergraduate " +
                        "(student_id, reg_no, department, batch, is_repeat) " +
                        "VALUES (?, ?, ?, ?, ?)";
                break;

            default:
                throw new IllegalArgumentException("Unknown User Role");
        }

        // Execute Role-Specific INSERT
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUserId());

            switch (user.getRole()) {

                case "LECTURER":

                    Lecturer lecturer = (Lecturer) user;

                    ps.setString(2, lecturer.getDepartment());
                    break;

                case "TECHNICAL_OFFICER":

                    TechnicalOfficer to = (TechnicalOfficer) user;

                    ps.setString(2, to.getDepartment());
                    break;

                case "STUDENT":

                    Student student = (Student) user;

                    ps.setString(2, student.getRegNo());
                    ps.setString(3, student.getDepartment());
                    ps.setString(4, student.getBatch());
                    ps.setBoolean(5, student.isRepeat());
                    break;
            }

            ps.executeUpdate();
        }
    }

    public User getUserById(String userId) throws SQLException {
        String sql =  "SELECT u.*, " +
                "l.department AS lecturer_dept, " +
                "t.department AS to_dept, " +
                "s.reg_no, s.department AS student_dept, " +
                "s.batch, s.is_repeat " +
                "FROM users u " +
                "LEFT JOIN lecturer l ON u.user_id = l.lecturer_id " +
                "LEFT JOIN technical_officer t ON u.user_id = t.to_id " +
                "LEFT JOIN undergraduate s ON u.user_id = s.student_id " +
                "WHERE u.user_id = ?";

        try (Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user;

                switch (rs.getString("role")) {

                    case "ADMIN":
                        user = new Admin();
                        break;

                    case "LECTURER":
                        Lecturer lecturer = new Lecturer();
                        lecturer.setDepartment(rs.getString("lecturer_dept"));
                        user = lecturer;
                        break;

                    case "TECHNICAL_OFFICER":
                        TechnicalOfficer to = new TechnicalOfficer();
                        to.setDepartment(rs.getString("to_dept"));
                        user = to;
                        break;

                    case "STUDENT":
                        Student student = new Student();
                        student.setRegNo(rs.getString("reg_no"));
                        student.setDepartment(rs.getString("student_dept"));
                        student.setBatch(rs.getString("batch"));
                        student.setRepeat(rs.getBoolean("is_repeat"));

                        user = student;
                        break;

                    default:
                        throw new SQLException("Unknown User Role");
                }
                user.setUserId(rs.getString("user_id"));
                user.setPasswordHash(rs.getString("password"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setContactNo(rs.getString("contact_no"));
                user.setProfilePic(rs.getString("profile_pic"));
                user.setRole(rs.getString("role"));

                return user;
            }
    }
    }

    // READ - Get All Users
    public List<User> getAllUsers() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = "SELECT user_id FROM users ORDER BY user_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String userId = rs.getString("user_id");

                User user = getUserById(userId);

                if (user != null) {
                    users.add(user);
                }
            }
        }

        return users;
    }

    // UPDATE - Update Existing User
    public boolean updateUser(User user) throws SQLException {

        if (user == null || user.getUserId() == null ||
                user.getUserId().isBlank()) {
            throw new IllegalArgumentException("Invalid User ID");
        }

        if (user.getRole() == null) {
            throw new IllegalArgumentException("User role is required");
        }

        String sql = "UPDATE users SET " +
                "first_name = ?, " +
                "last_name = ?, " +
                "email = ?, " +
                "contact_no = ?, " +
                "profile_pic = ? " +
                "WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                // Check whether user exists and verify their role
                String checkSql =
                        "SELECT role FROM users WHERE user_id = ? FOR UPDATE";

                try (PreparedStatement checkPs =
                             con.prepareStatement(checkSql)) {

                    checkPs.setString(1, user.getUserId());

                    try (ResultSet rs = checkPs.executeQuery()) {

                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }

                        String existingRole = rs.getString("role");

                        if (!existingRole.equals(user.getRole())) {
                            throw new IllegalArgumentException(
                                    "Changing user roles is not supported"
                            );
                        }
                    }
                }

                // Update common user details
                try (PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, user.getFirstName());
                    ps.setString(2, user.getLastName());
                    ps.setString(3, user.getEmail());
                    ps.setString(4, user.getContactNo());
                    ps.setString(5, user.getProfilePic());
                    ps.setString(6, user.getUserId());

                    ps.executeUpdate();
                }

                // Update role-specific details
                updateRoleDetails(con, user);

                // Save changes
                con.commit();

                return true;

            } catch (SQLException | RuntimeException e) {

                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    // UPDATE - Role-Specific Details
    private void updateRoleDetails(Connection con, User user)
            throws SQLException {

        String sql;

        switch (user.getRole()) {

            case "ADMIN":

                if (!(user instanceof Admin)) {
                    throw new IllegalArgumentException(
                            "Invalid Admin Object"
                    );
                }

                // Admin table has no additional editable fields
                return;

            case "LECTURER":

                if (!(user instanceof Lecturer)) {
                    throw new IllegalArgumentException(
                            "Invalid Lecturer Object"
                    );
                }

                sql = "UPDATE lecturer SET department = ? " +
                        "WHERE lecturer_id = ?";
                break;

            case "TECHNICAL_OFFICER":

                if (!(user instanceof TechnicalOfficer)) {
                    throw new IllegalArgumentException(
                            "Invalid Technical Officer Object"
                    );
                }

                sql = "UPDATE technical_officer SET department = ? " +
                        "WHERE to_id = ?";
                break;

            case "STUDENT":

                if (!(user instanceof Student)) {
                    throw new IllegalArgumentException(
                            "Invalid Student Object"
                    );
                }

                sql = "UPDATE undergraduate SET " +
                        "reg_no = ?, department = ?, " +
                        "batch = ?, is_repeat = ? " +
                        "WHERE student_id = ?";
                break;

            default:
                throw new IllegalArgumentException("Unknown User Role");
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            switch (user.getRole()) {

                case "LECTURER":

                    Lecturer lecturer = (Lecturer) user;

                    ps.setString(1, lecturer.getDepartment());
                    ps.setString(2, user.getUserId());
                    break;

                case "TECHNICAL_OFFICER":

                    TechnicalOfficer to = (TechnicalOfficer) user;

                    ps.setString(1, to.getDepartment());
                    ps.setString(2, user.getUserId());
                    break;

                case "STUDENT":

                    Student student = (Student) user;

                    ps.setString(1, student.getRegNo());
                    ps.setString(2, student.getDepartment());
                    ps.setString(3, student.getBatch());
                    ps.setBoolean(4, student.isRepeat());
                    ps.setString(5, user.getUserId());
                    break;
            }

            ps.executeUpdate();
        }
    }
    // DELETE - Delete Existing User
    public boolean deleteUser(String userId) throws SQLException {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Invalid User ID");
        }

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                String role;

                // Find the user's role
                String checkSql =
                        "SELECT role FROM users WHERE user_id = ? FOR UPDATE";

                try (PreparedStatement ps =
                             con.prepareStatement(checkSql)) {

                    ps.setString(1, userId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }

                        role = rs.getString("role");
                    }
                }

                // Select the role-specific table
                String roleSql;

                switch (role) {

                    case "ADMIN":
                        roleSql = "DELETE FROM admin WHERE admin_id = ?";
                        break;

                    case "LECTURER":
                        roleSql = "DELETE FROM lecturer WHERE lecturer_id = ?";
                        break;

                    case "TECHNICAL_OFFICER":
                        roleSql = "DELETE FROM technical_officer WHERE to_id = ?";
                        break;

                    case "STUDENT":
                        roleSql = "DELETE FROM undergraduate WHERE student_id = ?";
                        break;

                    default:
                        throw new SQLException("Unknown User Role");
                }

                // Delete role-specific record
                try (PreparedStatement ps =
                             con.prepareStatement(roleSql)) {

                    ps.setString(1, userId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Role-specific user record not found"
                        );
                    }
                }

                // Delete common user record
                String sql = "DELETE FROM users WHERE user_id = ?";

                try (PreparedStatement ps =
                             con.prepareStatement(sql)) {

                    ps.setString(1, userId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("User deletion failed");
                    }
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException e) {

                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

}