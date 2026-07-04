package fr.qmn.mamoucalendari.tasks;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TasksSelect {

    public List<Tasks> getAllTasksByDate(String date) {
        ArrayList<Tasks> tasksList = new ArrayList<>();
        String sql = "SELECT * FROM USERS WHERE DATE = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    tasksList.add(new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"), false));
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
        String sql = "SELECT * FROM USERS WHERE DATE = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    tasksList.add(new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"), resultSet.getBoolean("ISDONE")));
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
        String sql = "SELECT * FROM USERS WHERE DATE = ? AND HOURS = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, hours);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    tasksList.add(new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"), resultSet.getBoolean("ISDONE")));
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
                    return new Tasks(resultSet.getString("DATE"), resultSet.getInt("HOURS"), resultSet.getInt("MINUTES"), resultSet.getString("TASKS"), resultSet.getBoolean("ISDONE"));
                }
            }
        }
        return null;
    }

    //Set tasks done
    public void setTasksDone(String date, int hours, int minutes) {
        String url = "jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db";
        String query = "UPDATE USERS SET ISDONE = ? WHERE DATE = ? AND HOURS = ? AND MINUTES = ?";
        try (Connection connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setBoolean(1, true);
                    pstmt.setString(2, date);
                    pstmt.setInt(3, hours);
                    pstmt.setInt(4, minutes);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}