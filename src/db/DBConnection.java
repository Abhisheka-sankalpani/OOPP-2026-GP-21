package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a single static method to obtain a JDBC connection
 * to the fot_mis MySQL database.
 *
 * Update the URL / USER / PASSWORD constants to match your local setup.
 */
public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/fot_mis";
    private static final String USER     = "root";
    private static final String PASSWORD = "your_password_here"; // <-- change this

    /** Returns a fresh connection on every call. Caller must close it. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
