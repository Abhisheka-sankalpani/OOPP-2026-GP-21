package database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    public static Connection getConnection() throws SQLException {

        Properties props = new Properties();

        try (InputStream input = loadPropertiesFile()) {

            if (input == null) {
                throw new SQLException(
                        "db.properties file not found!"
                );
            }

            props.load(input);

        } catch (IOException e) {
            throw new SQLException(
                    "Cannot read database properties file", e
            );
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password", "");

        if (url == null || url.isBlank()
                || user == null || user.isBlank()) {

            throw new SQLException(
                    "Database URL or username is missing!"
            );
        }

        return DriverManager.getConnection(
                url,
                user,
                password
        );
    }

    private static InputStream loadPropertiesFile()
            throws IOException {

        // First try loading from classpath
        InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties");

        if (input != null) {
            return input;
        }

        // Existing project resources location
        Path path = Path.of(
                "src", "resources", "db.properties"
        );

        if (Files.isRegularFile(path)) {
            return Files.newInputStream(path);
        }

        // Standard resources location
        Path standardPath = Path.of(
                "src", "main", "resources", "db.properties"
        );

        if (Files.isRegularFile(standardPath)) {
            return Files.newInputStream(standardPath);
        }

        return null;
    }
}