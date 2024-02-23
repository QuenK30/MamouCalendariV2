package fr.qmn.mamoucalendari.tasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TasksReminder {

    private List<Tasks> tasksList = new ArrayList<>();
    private HashMap<Tasks, Integer> lastReminder = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void startReminder() {
        final Runnable reminder = new Runnable() {
            public void run() {
                try {
                    loadTasksFromDB();
                    checkAndSendReminder();
                    System.out.println("Reminder checked");
                } catch (Exception e) {
                    System.out.println("Error: When checking reminder");
                    e.printStackTrace();
                }
            }
        };

        scheduler.scheduleAtFixedRate(reminder, 0, 1, TimeUnit.MINUTES);
    }

    public void loadTasksFromDB() {
        String sql = "SELECT * FROM USERS";
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/fr/qmn/mamoucalendari/bdd/UserRegistre.db");
                 PreparedStatement pstmt = connection.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                if(tasksList != null)
                    tasksList.clear();
                else
                    tasksList = new ArrayList<>();

                while (rs.next()) {
                    Tasks task = new Tasks(rs.getString("DATE"), rs.getInt("HOURS"), rs.getInt("MINUTES"), rs.getString("TASKS"), rs.getBoolean("ISDONE"));
                    tasksList.add(task);
                }
            }
            System.out.println("Tasks loaded with "+ tasksList.size() + " tasks");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void checkAndSendReminder() {
        System.out.println("Checking reminder");
        LocalDateTime now = LocalDateTime.now();
        for (Tasks task : tasksList) {
            LocalDateTime taskTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), task.getHours(), task.getMinutes());
            long difference = ChronoUnit.MINUTES.between(now, taskTime);
            if(shouldSendReminder(difference, task)) {
                sendReminder(task);
                lastReminder.put(task, task.getHours());
                System.out.println("Reminder sent");
            }
        }
    }

    private boolean shouldSendReminder(long difference, Tasks task) {
        Integer lastSent = lastReminder.getOrDefault(task, -1);

        return isWithinRange(difference, 30, 5) && (lastSent == null || lastSent > difference) ||
                isWithinRange(difference, 15, 5) && (lastSent == null || lastSent > difference) ||
                isWithinRange(difference, 5, 2) && (lastSent == null || lastSent > difference) ||
                isWithinRange(difference, 1, 1) && (lastSent == null || lastSent > difference);
    }

    private boolean isWithinRange(long difference, long target, long flexibility) {
        return difference >= (target - flexibility) && difference <= (target + flexibility);
    }

    private void sendReminder(Tasks task) {
        System.out.println("Sending reminder for task: " + task.getTasks());
        // Votre logique d'envoi de rappel
    }
}
