package fr.qmn.mamoucalendari.bdd;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLManager {
    public void createTask(String date, int hours, int minutes, String tasks) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
            if (connection != null) {
                connection.createStatement().executeUpdate("INSERT INTO USERS (DATE, HOURS, MINUTES, TASKS) VALUES ('" + date + "', " + hours + ", " + minutes + ", '" + tasks + "')");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
