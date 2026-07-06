package fr.qmn.mamoucalendari.tasks;

import fr.qmn.mamoucalendari.MCMain;
import fr.qmn.mamoucalendari.bdd.DBConfig;
import fr.qmn.mamoucalendari.controller.calendar.CalendarController;
import fr.qmn.mamoucalendari.controller.visual.VisualController;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TasksReminder {

    private static final int[] TRIGGERS = {60, 30, 15, 5, 0};

    private List<Tasks> tasksList = new ArrayList<>();
    private final Set<String> sentReminders = new HashSet<>();
    private String lastCheckedDate = "";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // AudioClip initialisé paresseusement sur le fil JavaFX
    private AudioClip notifClip = null;
    private boolean clipInitialized = false;

    public void shutdown() {
        scheduler.shutdownNow();
    }

    public void startReminder() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Réinitialise les rappels envoyés au changement de jour
                String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                if (!today.equals(lastCheckedDate)) {
                    sentReminders.clear();
                    lastCheckedDate = today;
                }
                loadTasksFromDB();
                checkAndSendReminder();
                System.out.println("Reminder checked");
            } catch (Exception e) {
                System.out.println("Error: When checking reminder");
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void loadTasksFromDB() {
        String sql = "SELECT * FROM USERS";
        try (Connection connection = DriverManager.getConnection(DBConfig.URL);
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            tasksList.clear();
            while (rs.next()) {
                tasksList.add(new Tasks(
                    rs.getString("DATE"), rs.getInt("HOURS"), rs.getInt("MINUTES"),
                    rs.getString("TASKS"), rs.getBoolean("ISDONE")));
            }
            System.out.println("Tasks loaded with " + tasksList.size() + " tasks");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void checkAndSendReminder() {
        System.out.println("Checking reminder");
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDateTime now = LocalDateTime.now();

        for (Tasks task : tasksList) {
            if (!task.getDate().equals(today)) continue;

            LocalDateTime taskTime = LocalDate.now().atTime(task.getHours(), task.getMinutes());
            long diff = ChronoUnit.MINUTES.between(now, taskTime);

            for (int trigger : TRIGGERS) {
                if (diff >= trigger && diff <= trigger + 1) {
                    String key = task.getDate() + ":" + task.getHours() + ":" + task.getMinutes() + ":" + trigger;
                    if (!sentReminders.contains(key)) {
                        sentReminders.add(key);
                        sendReminder(task, trigger);
                        System.out.println("Reminder sent for: " + task.getTasks() + " (dans " + trigger + " min)");
                    }
                }
            }
        }
    }

    private void sendReminder(Tasks task, int minutesBefore) {
        Platform.runLater(() -> {
            // Initialisation paresseuse du clip sonore (nécessite le toolkit JavaFX)
            if (!clipInitialized) {
                clipInitialized = true;
                URL url = TasksReminder.class.getResource("/fr/qmn/mamoucalendari/sounds/notif.mp3");
                if (url != null) {
                    try {
                        notifClip = new AudioClip(url.toExternalForm());
                    } catch (Exception e) {
                        System.out.println("[TasksReminder] Son indisponible : " + e.getMessage());
                    }
                }
            }
            if (notifClip != null) notifClip.play();

            VisualController vc   = MCMain.activeVisualController;
            CalendarController cc = MCMain.activeCalendarController;

            Runnable dismiss = () -> {
                if (vc  != null) vc.hideReminderOverlay();
                if (cc  != null) cc.hideLuOverlay();
            };

            if (vc != null) vc.showReminderOverlay(task, minutesBefore);
            if (cc != null) cc.showLuOverlay(task, minutesBefore, dismiss);
        });
    }
}
