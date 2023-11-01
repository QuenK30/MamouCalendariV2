package fr.qmn.mamoucalendari.bdd;

import fr.qmn.mamoucalendari.tasks.Tasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    public List<Tasks> getTasksbyDate(String date)
    {
        List<Tasks> tasksList = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
            if (connection != null) {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM USERS WHERE DATE = '" + date + "'");
                while (resultSet.next()) {
                    Tasks tasks = new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"));
                    tasksList.add(tasks);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tasksList;
    }
}
