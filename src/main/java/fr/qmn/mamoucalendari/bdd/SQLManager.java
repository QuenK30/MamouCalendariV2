package fr.qmn.mamoucalendari.bdd;

import fr.qmn.mamoucalendari.utils.SoundLib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class SQLManager {
    public void createTask(String date, int hours, int minutes, String tasks, boolean isDone) {
        String sql = "INSERT INTO USERS (DATE, HOURS, MINUTES, TASKS, ISDONE) VALUES (?, ?, ?, ?, ?)";
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
                 PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, date);
                pstmt.setInt(2, hours);
                pstmt.setInt(3, minutes);
                pstmt.setString(4, tasks);
                pstmt.setBoolean(5, isDone);

                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
