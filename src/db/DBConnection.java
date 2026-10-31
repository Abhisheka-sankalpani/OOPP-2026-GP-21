package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.util.Properties;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/fot_mis";
    private static final String USER = "root";
    public static Connection getConnection() throws SQLException {
        String password = "";

        try {
            Properties props = new Properties();
            props.load(new FileInputStream("db.properties"));
            password = props.getProperty("db.password");

            if (password == null) {
                password = "";
            }
        } catch (Exception e) {
            System.out.println("Properties file error: " + e.getMessage());
        }

        return DriverManager.getConnection(URL, USER, password);
    }

    /*//Test Database Connection
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();
            if (con != null) {
                System.out.println("Connection successful to fot_mis!");
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }*/
}
