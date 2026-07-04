package fr.qmn.mamoucalendari.bdd;

import java.sql.*;

public class SQLInit {
    public void createNewDatabase()
    {
        String url = "jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db";
        try (Connection connection = DriverManager.getConnection(url)) {
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
                System.out.println("| La base de données a été créée avec succès            |");
            } else {
                System.out.println("| La base de données existe déjà                |");
            }
        } catch (Exception e) {
            System.out.println("Error: When creating db");
            e.printStackTrace();
        }
    }
    public boolean doesTableIsCreate(Connection connection, String tableName)
    {
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
