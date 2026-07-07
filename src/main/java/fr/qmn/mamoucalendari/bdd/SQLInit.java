package fr.qmn.mamoucalendari.bdd;

import java.nio.file.Paths;
import java.sql.*;

public class SQLInit {
    public void createNewDatabase() {
        // Garantir que le répertoire parent existe (nécessaire en production)
        String path = DBConfig.URL.replace("jdbc:sqlite:", "");
        Paths.get(path).getParent().toFile().mkdirs();

        try (Connection connection = DriverManager.getConnection(DBConfig.URL)) {
            if (!doesTableIsCreate(connection, "USERS")) {
                try (Statement statement = connection.createStatement()) {
                    String createUserTable = "CREATE TABLE USERS" +
                            "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "DATE TEXT NOT NULL," +
                            "HOURS INTEGER NOT NULL," +
                            "MINUTES INTEGER NOT NULL," +
                            "TASKS TEXT NOT NULL," +
                            "ISDONE BOOLEAN NOT NULL)";
                    statement.executeUpdate(createUserTable);
                }
                System.out.println("| La base de données a été créée avec succès |");
            } else {
                System.out.println("| La base de données existe déjà             |");
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
        } catch (Exception e) {
            System.out.println("Error: When creating db");
            e.printStackTrace();
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
