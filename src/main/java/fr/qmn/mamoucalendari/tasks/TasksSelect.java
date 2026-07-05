package fr.qmn.mamoucalendari.tasks;

import fr.qmn.mamoucalendari.bdd.DBConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TasksSelect {

    public List<Tasks> getAllTasksByDate(String date) {
        ArrayList<Tasks> tasksList = new ArrayList<>();
        String sql = "SELECT * FROM USERS WHERE DATE = ?";
        try (Connection connection = DriverManager.getConnection(DBConfig.URL);
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
        try (Connection connection = DriverManager.getConnection(DBConfig.URL);
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
        try (Connection connection = DriverManager.getConnection(DBConfig.URL);
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

    private static final int CURRENT_WINDOW_MINUTES = 30;

    public Tasks[] getClosestTaskByTime(String date, int hours, int minutes) {
        Tasks previousTask = null, currentTask = null, nextTask = null;
        int totalMinutes = hours * 60 + minutes;

        try (Connection connection = DriverManager.getConnection(DBConfig.URL)) {
            previousTask = getTaskByTotal(connection, date, totalMinutes, "<", "DESC");
            // "en cours" = la tâche la plus proche dans la fenêtre [maintenant, maintenant + 30 min]
            currentTask  = getTaskInWindow(connection, date, totalMinutes, totalMinutes + CURRENT_WINDOW_MINUTES);
            // "prochaine" = strictement après la tâche en cours (ou après maintenant si rien en cours)
            int afterMinutes = currentTask != null
                ? currentTask.getHours() * 60 + currentTask.getMinutes()
                : totalMinutes;
            nextTask = getTaskByTotal(connection, date, afterMinutes, ">", "ASC");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return new Tasks[]{previousTask, currentTask, nextTask};
    }

    private Tasks getTaskInWindow(Connection connection, String date, int fromMinutes, int toMinutes) throws SQLException {
        String query = "SELECT * FROM USERS WHERE DATE = ? AND (HOURS * 60 + MINUTES) >= ? AND (HOURS * 60 + MINUTES) <= ? ORDER BY (HOURS * 60 + MINUTES) ASC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, fromMinutes);
            pstmt.setInt(3, toMinutes);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return taskFromRow(rs);
            }
        }
        return null;
    }

    private Tasks getTaskByTotal(Connection connection, String date, int totalMinutes, String op, String order) throws SQLException {
        String query = "SELECT * FROM USERS WHERE DATE = ? AND (HOURS * 60 + MINUTES) " + op
                     + " ? ORDER BY (HOURS * 60 + MINUTES) " + order + " LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, totalMinutes);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return taskFromRow(rs);
            }
        }
        return null;
    }

    private Tasks taskFromRow(ResultSet rs) throws SQLException {
        return new Tasks(rs.getString("DATE"), rs.getInt("HOURS"), rs.getInt("MINUTES"),
                         rs.getString("TASKS"), rs.getBoolean("ISDONE"));
    }

    //Set tasks done
    public void setTasksDone(String date, int hours, int minutes) {
        String url = DBConfig.URL;
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