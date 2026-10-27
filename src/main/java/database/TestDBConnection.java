package database;

import database.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Connection;

public class TestDBConnection {
    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()){
            System.out.println("Database Connected Successfully");


        }
        catch (SQLException e){
            System.out.println("Database Connection Failed");
            System.out.println(e.getMessage());
        }
    }
}
