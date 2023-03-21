import javax.swing.*;
import java.sql.*;

public class SQLiteDatabase {
    Connection connection;
    Statement statement;
    // Initialize the database connection
    SQLiteDatabase(String name) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + name);
            connection.setAutoCommit(true);
            statement = connection.createStatement();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Initialize the table
    public void createTable() {
        try {
            String sql =
                    "CREATE TABLE USERS" +
                            "(USERNAME TEXT PRIMARY KEY NOT NULL," +
                            "AGE INT NOT NULL," +
                            "GENDER TEXT NOT NULL," +
                            "ADDRESS TEXT NOT NULL," +
                            "PASSWORD TEXT NOT NULL)";
            statement.executeUpdate(sql);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Check if table exists
    public boolean tableExists() {
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getTables(null, null, "USERS", new String[]{"TABLE"});
            return resultSet.next();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }
    // Query login
    public boolean loginQuery(String username, String password) {
        try {
            String sql = "SELECT * FROM USERS WHERE USERNAME='" + username + "'";
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                // Password match
                if (resultSet.getString("PASSWORD").equals(password)) {
                    close();
                    return true;
                }
                // Password mismatch
                else {
                    close();
                    return false;
                }
            }
            // No user
            else {
                close();
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerQuery(String username, int age, String gender, String address, String password) {
        try {
            // Find if available
            String sql = "SELECT * FROM USERS WHERE USERNAME='" + username + "'";
            ResultSet resultSet = statement.executeQuery(sql);
            // User already exists
            if (resultSet.next()) {
                close();
                return false;
            }
            // Insert into it
            else {
                sql = "INSERT INTO USERS (USERNAME, AGE, GENDER, ADDRESS, PASSWORD)" +
                        "VALUES ('" + username + "'," + age + ",'" + gender + "', '" + address + "', '" + password + "');";
                statement.executeUpdate(sql);
                close();
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // Close the connection
    public void close() {
        try {
            statement.close();
            connection.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
