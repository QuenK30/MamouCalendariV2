package fr.qmn.mamoucalendari.tasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TasksSelect {

    public List<Tasks> getAllTasksByDate(String date) {
        ArrayList<Tasks> tasksList = new ArrayList<>();
        int countTasks = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
            if (connection != null) {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM USERS WHERE DATE = '" + date + "'");
                while (resultSet.next()) {
                    Tasks tasks = new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"));
                    tasksList.add(tasks);
                    countTasks++;
                }
                System.out.println("Tasks of the day: " + countTasks);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tasksList;
    }

}