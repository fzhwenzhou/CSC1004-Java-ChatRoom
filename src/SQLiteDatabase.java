import javax.swing.*;
import java.sql.*;

public class SQLiteDatabase {
    Connection connection;
    Statement statement;
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
    public boolean loginQuery(String username, String password) {
        try {
            String sql = "SELECT * FROM USERS WHERE USERNAME='" + username + "'";
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                if (resultSet.getString("PASSWORD").equals(password)) {
                    close();
                    return true;
                }
                else {
                    close();
                    return false;
                }
            }
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
            if (resultSet.next()) {
                close();
                return false;
            }
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
