package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
      public static Connection getConnection() throws SQLException{
          Properties props = new Properties();

          try (FileInputStream file = new FileInputStream("C:\\Users\\wwwsa\\OneDrive\\Desktop\\sem 3\\oop project\\OOPP-2026-GP-21\\src\\resources\\db.properties")) {
              props.load(file);
          }
          catch(IOException e){
              throw new SQLException("Cannot read properties file",e);

          }
          String url = props.getProperty("db.url");
          String user = props.getProperty("db.username");
          String password = props.getProperty("db.password","");

          return DriverManager.getConnection(url,user,password);
      }

}
