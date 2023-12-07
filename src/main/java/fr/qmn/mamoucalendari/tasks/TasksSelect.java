package fr.qmn.mamoucalendari.tasks;

import fr.qmn.mamoucalendari.utils.TimeLib;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tasksList;
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

    public List<Tasks> getTasksbyDateAndHours(String date, int hours)
    {
        List<Tasks> tasksList = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
            if (connection != null) {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM USERS WHERE DATE = '" + date + "' AND HOURS = '" + hours + "'");
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

    //Get closest task by actual time and date
    public Tasks[] getClosestTaskByTime(String date, String hours, String minutes){
        Tasks previousTask = null,currentTask = null, nextTask = null;
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
            if (connection != null) {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM USERS WHERE DATE = '" + date + "' AND HOURS = '" + hours + "' AND MINUTES = '" + minutes + "'");
                while (resultSet.next()) {
                    currentTask = new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"));
                }
                resultSet = connection.createStatement().executeQuery("SELECT * FROM USERS WHERE DATE = '" + date + "' AND HOURS = '" + hours + "' AND MINUTES < '" + minutes + "' ORDER BY MINUTES DESC LIMIT 1");
                while (resultSet.next()) {
                    previousTask = new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"));
                }
                resultSet = connection.createStatement().executeQuery("SELECT * FROM USERS WHERE DATE = '" + date + "' AND HOURS = '" + hours + "' AND MINUTES > '" + minutes + "' ORDER BY MINUTES ASC LIMIT 1");
                while (resultSet.next()) {
                    nextTask = new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Tasks[]{previousTask, currentTask, nextTask};
    }


}