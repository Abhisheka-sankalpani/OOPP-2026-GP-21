package database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestDBConnection {

    public static void main(String[] args) {

        try (Connection conn = DBConnection.getConnection()) {

            System.out.println("Database Connected Successfully!");

        } catch (SQLException e) {

            System.out.println("Database Connection Failed!");
            System.out.println(e.getMessage());
        }
    }
}