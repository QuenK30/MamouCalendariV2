package fr.qmn.mamoucalendari.controller.visual;

import fr.qmn.mamoucalendari.MCMain;
import fr.qmn.mamoucalendari.service.TaskService;
import fr.qmn.mamoucalendari.tasks.Tasks;
import fr.qmn.mamoucalendari.utils.StringLib;
import fr.qmn.mamoucalendari.utils.TimeLib;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class VisualController {
    public Text textDate;
    public Text textHours;
    public Text textCountTasks;
    public Text textBeforeHours;
    public Text textActualHours;
    public Text textAfterHours;
    public Text textBeforeTasks;
    public Text textActualTasks;
    public Text textAfterTasks;
    public AnchorPane visualFxml;

    private VBox reminderOverlay = null;

    private final TaskService taskService = new TaskService();

    public void initialize() {
        MCMain.activeVisualController = this;

        // Mise à jour de l'horloge chaque seconde (sans accès DB)
        Timeline clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e ->
            textHours.setText(new TimeLib().getActualTime())
        ));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

        // Mise à jour des tâches et de la date toutes les 60 secondes (DB)
        Timeline taskTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0),  e -> { updateDate(); setTasks(); }),
            new KeyFrame(Duration.seconds(60))
        );
        taskTimeline.setCycleCount(Timeline.INDEFINITE);
        taskTimeline.play();
    }

    private void updateDate() {
        TimeLib timeLib = new TimeLib();
        StringLib stringLib = new StringLib();
        String actualDate = timeLib.getActualDate();
        textDate.setText(stringLib.capitalizeFirstLetterOfEachWord(timeLib.getActualDateWithoutYear()));
        textHours.setText(timeLib.getActualTime());
        textCountTasks.setText(String.valueOf(taskService.getAllTasksByDate(actualDate).size()));
    }

    private void setTasks() {
        Tasks[] closestTask = getClosestTask();
        updateTaskUI(textBeforeHours, textBeforeTasks, closestTask[0]);
        updateTaskUI(textActualHours, textActualTasks, closestTask[1]);
        updateTaskUI(textAfterHours,  textAfterTasks,  closestTask[2]);
    }

    private Tasks[] getClosestTask() {
        TimeLib timeLib = new TimeLib();

        String actualDate = timeLib.getActualDate();
        String[] timeParts = timeLib.getActualTime().split(":");
        if (timeParts.length != 2) {
            throw new IllegalArgumentException("Le format de l'heure n'est pas valide");
        }

        int actualHoursTime = Integer.parseInt(timeParts[0]);
        int actualMinutesTime = Integer.parseInt(timeParts[1]);

        return taskService.getClosestTaskByTime(actualDate, actualHoursTime, actualMinutesTime);
    }

    private void updateTaskUI(Text timeLabel, Text taskLabel, Tasks task) {
        if (task != null) {
            String formattedTime = String.format("%02d:%02d", task.getHours(), task.getMinutes());
            timeLabel.setText(formattedTime);
            taskLabel.setText(task.getTasks());
        } else {
            timeLabel.setText("00:00");
            taskLabel.setText("Aucune tâche");
        }
    }

    public void showReminderOverlay(Tasks task, int minutesBefore) {
        hideReminderOverlay();

        reminderOverlay = new VBox(16);
        reminderOverlay.setAlignment(Pos.CENTER);
        reminderOverlay.setStyle(
            "-fx-background-color: #ffd9d8;" +
            "-fx-border-color: #ff9d9d;" +
            "-fx-border-width: 4;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 30;"
        );
        reminderOverlay.setPrefSize(760, 280);

        String delayLabel = minutesBefore == 0 ? "Maintenant !" : "dans " + minutesBefore + " min";
        Label title = new Label("Rappel  " + delayLabel);
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: black;");

        Label detail = new Label(task.getTasks() + "  "
            + String.format("%02d", task.getHours()) + "h"
            + String.format("%02d", task.getMinutes()));
        detail.setStyle("-fx-font-size: 28px; -fx-text-fill: black;");

        reminderOverlay.getChildren().addAll(title, detail);

        AnchorPane.setTopAnchor(reminderOverlay, 390.0);
        AnchorPane.setLeftAnchor(reminderOverlay, 580.0);
        visualFxml.getChildren().add(reminderOverlay);
    }

    public void hideReminderOverlay() {
        if (reminderOverlay != null) {
            visualFxml.getChildren().remove(reminderOverlay);
            reminderOverlay = null;
        }
    }
}
