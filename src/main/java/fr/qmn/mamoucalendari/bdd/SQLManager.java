package fr.qmn.mamoucalendari.bdd;

import fr.qmn.mamoucalendari.utils.SoundLib;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLManager {
    public void createTask(String date, int hours, int minutes, String tasks, boolean isDone) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
            if (connection != null) {
                connection.createStatement().executeUpdate("INSERT INTO USERS (DATE, HOURS, MINUTES, TASKS, ISDONE) VALUES ('" + date + "', '" + hours + "', '" + minutes + "', '" + tasks + "', '" + isDone + "')");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
