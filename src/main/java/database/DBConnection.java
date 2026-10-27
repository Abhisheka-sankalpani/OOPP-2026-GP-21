package database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
      public static Connection getConnection() throws SQLException{
          Properties props = new Properties();

          //File file = new File("src/main/resources/db.properties");

          //System.out.println("File Path: " + file.getAbsolutePath());
         // System.out.println("File Exists: " + file.exists());

          try (FileInputStream file = new FileInputStream("src/resources/db.properties")) {
              props.load(file);
          }
          catch(IOException e){
              throw new SQLException("Cannot read properties file",e);

          }
          String url = props.getProperty("db.url");
          String user = props.getProperty("db.user");
          String password = props.getProperty("db.password","");

          return DriverManager.getConnection(url,user,password);
      }

}
