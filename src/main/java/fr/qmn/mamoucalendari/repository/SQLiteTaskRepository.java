package fr.qmn.mamoucalendari.repository;

import fr.qmn.mamoucalendari.bdd.DBConfig;
import fr.qmn.mamoucalendari.tasks.Tasks;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteTaskRepository implements TaskRepository {

    private static final int CURRENT_WINDOW_MINUTES = 30;

    @Override
    public List<Tasks> getAllTasksByDate(String date) {
        List<Tasks> list = new ArrayList<>();
        String sql = "SELECT * FROM USERS WHERE DATE = ?";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(taskFromRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Tasks> getTasksByDate(String date) {
        List<Tasks> list = new ArrayList<>();
        String sql = "SELECT * FROM USERS WHERE DATE = ?";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(taskFromRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Tasks[] getClosestTaskByTime(String date, int hours, int minutes) {
        int totalMinutes = hours * 60 + minutes;
        Tasks previous = null, current = null, next = null;
        try (Connection conn = DriverManager.getConnection(DBConfig.URL)) {
            previous = getTaskByTotal(conn, date, totalMinutes, "<", "DESC");
            current  = getTaskInWindow(conn, date, totalMinutes, totalMinutes + CURRENT_WINDOW_MINUTES);
            int afterMinutes = current != null
                ? current.getHours() * 60 + current.getMinutes()
                : totalMinutes;
            next = getTaskByTotal(conn, date, afterMinutes, ">", "ASC");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return new Tasks[]{previous, current, next};
    }

    @Override
    public void createTask(String date, int hours, int minutes, String tasks, boolean isDone) {
        String sql = "INSERT INTO USERS (DATE, HOURS, MINUTES, TASKS, ISDONE, UUID, CREATED_AT, UPDATED_AT) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String now  = Instant.now().toString();
        String uuid = UUID.randomUUID().toString();
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, hours);
            pstmt.setInt(3, minutes);
            pstmt.setString(4, tasks);
            pstmt.setBoolean(5, isDone);
            pstmt.setString(6, uuid);
            pstmt.setString(7, now);
            pstmt.setString(8, now);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTask(String date, int hours, int minutes) {
        String sql = "DELETE FROM USERS WHERE DATE = ? AND HOURS = ? AND MINUTES = ?";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, hours);
            pstmt.setInt(3, minutes);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void markTaskDone(String uuid) {
        String sql = "UPDATE USERS SET ISDONE = true, UPDATED_AT = ? WHERE UUID = ?";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Instant.now().toString());
            pstmt.setString(2, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Tasks getTaskInWindow(Connection conn, String date, int from, int to) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE DATE = ? AND (HOURS * 60 + MINUTES) >= ? AND (HOURS * 60 + MINUTES) <= ? ORDER BY (HOURS * 60 + MINUTES) ASC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, from);
            pstmt.setInt(3, to);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return taskFromRow(rs);
            }
        }
        return null;
    }

    private Tasks getTaskByTotal(Connection conn, String date, int totalMinutes, String op, String order) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE DATE = ? AND (HOURS * 60 + MINUTES) " + op
                   + " ? ORDER BY (HOURS * 60 + MINUTES) " + order + " LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, totalMinutes);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return taskFromRow(rs);
            }
        }
        return null;
    }

    private Tasks taskFromRow(ResultSet rs) throws SQLException {
        return new Tasks(
            rs.getInt("ID"),
            rs.getString("UUID"),
            rs.getString("DATE"),
            rs.getInt("HOURS"),
            rs.getInt("MINUTES"),
            rs.getString("TASKS"),
            rs.getBoolean("ISDONE"),
            rs.getString("CREATED_AT"),
            rs.getString("UPDATED_AT")
        );
    }
}
