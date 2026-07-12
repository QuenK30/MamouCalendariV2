package fr.qmn.mamoucalendari.bdd;

import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLInit {
    public void createNewDatabase() {
        String path = DBConfig.URL.replace("jdbc:sqlite:", "");
        Paths.get(path).getParent().toFile().mkdirs();

        try (Connection connection = DriverManager.getConnection(DBConfig.URL)) {
            if (!doesTableIsCreate(connection, "USERS")) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(
                        "CREATE TABLE USERS(" +
                        "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "DATE TEXT NOT NULL," +
                        "HOURS INTEGER NOT NULL," +
                        "MINUTES INTEGER NOT NULL," +
                        "TASKS TEXT NOT NULL," +
                        "ISDONE BOOLEAN NOT NULL," +
                        "UUID TEXT," +
                        "CREATED_AT TEXT," +
                        "UPDATED_AT TEXT)");
                }
                System.out.println("| La base de données a été créée avec succès |");
            } else {
                System.out.println("| La base de données existe déjà             |");
                migrateUsersTable(connection);
            }
            if (!doesTableIsCreate(connection, "SCREEN_CONFIG")) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(
                        "CREATE TABLE SCREEN_CONFIG(" +
                        "ID INTEGER PRIMARY KEY," +
                        "screen_visual INTEGER NOT NULL DEFAULT 0," +
                        "screen_calendar INTEGER NOT NULL DEFAULT 0," +
                        "screen_ocr INTEGER NOT NULL DEFAULT 0)");
                }
            }
            if (!doesTableIsCreate(connection, "SYNC_QUEUE")) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(
                        "CREATE TABLE SYNC_QUEUE(" +
                        "ID         INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "OPERATION  TEXT    NOT NULL," +
                        "PAYLOAD    TEXT    NOT NULL," +
                        "STATUS     TEXT    DEFAULT 'PENDING'," +
                        "ATTEMPTS   INTEGER DEFAULT 0," +
                        "CREATED_AT TEXT    NOT NULL)");
                }
            }
            if (!doesTableIsCreate(connection, "AUTH_TOKENS")) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(
                        "CREATE TABLE AUTH_TOKENS(" +
                        "ID            INTEGER PRIMARY KEY," +
                        "ACCESS_TOKEN  TEXT," +
                        "REFRESH_TOKEN TEXT," +
                        "EXPIRES_AT    TEXT)");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: When creating db");
            e.printStackTrace();
        }
    }

    private void migrateUsersTable(Connection connection) {
        addColumnIfMissing(connection, "UUID",       "TEXT");
        addColumnIfMissing(connection, "CREATED_AT", "TEXT");
        addColumnIfMissing(connection, "UPDATED_AT", "TEXT");
        backfillMissingUuids(connection);
    }

    private void addColumnIfMissing(Connection connection, String column, String type) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("ALTER TABLE USERS ADD COLUMN " + column + " " + type);
            System.out.println("| Migration : colonne " + column + " ajoutée         |");
        } catch (SQLException e) {
            // La colonne existe déjà — ignoré silencieusement
        }
    }

    private void backfillMissingUuids(Connection connection) {
        List<Integer> ids = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs  = stmt.executeQuery("SELECT ID FROM USERS WHERE UUID IS NULL")) {
            while (rs.next()) ids.add(rs.getInt("ID"));
        } catch (SQLException e) {
            System.out.println("Error: backfill UUID SELECT — " + e.getMessage());
            return;
        }
        if (ids.isEmpty()) return;

        String now = Instant.now().toString();
        String sql = "UPDATE USERS SET UUID = ?, CREATED_AT = ?, UPDATED_AT = ? WHERE ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int id : ids) {
                pstmt.setString(1, UUID.randomUUID().toString());
                pstmt.setString(2, now);
                pstmt.setString(3, now);
                pstmt.setInt(4, id);
                pstmt.executeUpdate();
            }
            System.out.println("| Migration : " + ids.size() + " tâche(s) backfillée(s)              |");
        } catch (SQLException e) {
            System.out.println("Error: backfill UUID UPDATE — " + e.getMessage());
        }
    }

    public boolean doesTableIsCreate(Connection connection, String tableName) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, tableName, null)) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
