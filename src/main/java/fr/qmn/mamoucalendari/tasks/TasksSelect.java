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
    public Tasks[] getClosestTaskByTime(String date, int hours, int minutes) {
        Tasks previousTask = null, currentTask = null, nextTask = null;

        String url = "jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db";

        try (Connection connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                currentTask = getTask(connection, date, hours, minutes, "=");
                previousTask = getTask(connection, date, hours, minutes, "<");
                nextTask = getTask(connection, date, hours, minutes, ">");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return new Tasks[]{previousTask, currentTask, nextTask};
    }

    private Tasks getTask(Connection connection, String date, int hours, int minutes, String operator) throws SQLException {
        String query = "SELECT * FROM USERS WHERE DATE = ? AND HOURS = ? AND MINUTES " + operator + " ? ORDER BY MINUTES " + ("=".equals(operator) ? "" : (">".equals(operator) ? "ASC" : "DESC")) + " LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, hours);
            pstmt.setInt(3, minutes);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"));
                }
            }
        }
        return null;
    }


}