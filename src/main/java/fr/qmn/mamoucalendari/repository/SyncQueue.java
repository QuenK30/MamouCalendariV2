package fr.qmn.mamoucalendari.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.qmn.mamoucalendari.bdd.DBConfig;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyncQueue {

    private final ObjectMapper mapper = new ObjectMapper();

    public record SyncEntry(int id, String operation, String payload) {}

    public void enqueue(String operation, Map<String, Object> payload) {
        String sql = "INSERT INTO SYNC_QUEUE (OPERATION, PAYLOAD, CREATED_AT) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, operation);
            pstmt.setString(2, mapper.writeValueAsString(payload));
            pstmt.setString(3, Instant.now().toString());
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("[SyncQueue] enqueue error: " + e.getMessage());
        }
    }

    public List<SyncEntry> getPending() {
        List<SyncEntry> entries = new ArrayList<>();
        String sql = "SELECT ID, OPERATION, PAYLOAD FROM SYNC_QUEUE WHERE STATUS = 'PENDING' ORDER BY ID ASC";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                entries.add(new SyncEntry(rs.getInt("ID"), rs.getString("OPERATION"), rs.getString("PAYLOAD")));
            }
        } catch (SQLException e) {
            System.out.println("[SyncQueue] getPending error: " + e.getMessage());
        }
        return entries;
    }

    public void markDone(int id) {
        String sql = "DELETE FROM SYNC_QUEUE WHERE ID = ?";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[SyncQueue] markDone error: " + e.getMessage());
        }
    }

    public void markFailed(int id) {
        // STATUS reste PENDING pour être retenté au prochain cycle
        String sql = "UPDATE SYNC_QUEUE SET ATTEMPTS = ATTEMPTS + 1 WHERE ID = ?";
        try (Connection conn = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[SyncQueue] markFailed error: " + e.getMessage());
        }
    }
}
