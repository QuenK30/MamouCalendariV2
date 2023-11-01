package fr.qmn.mamoucalendari.controller.visual;

import fr.qmn.mamoucalendari.bdd.SQLManager;
import fr.qmn.mamoucalendari.tasks.TasksSelect;
import fr.qmn.mamoucalendari.utils.StringLib;
import fr.qmn.mamoucalendari.utils.TimeLib;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.AnchorPane;
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

    public void initialize() {
        updateDate();
    }
    private void updateDate() {
        TimeLib timeLib = new TimeLib();
        StringLib stringLib = new StringLib();
        String actualDate = timeLib.getActualDate();
        SQLManager sqlUtils = new SQLManager();
        TasksSelect tasksSelect = new TasksSelect();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), event -> {
                    textDate.setText(stringLib.capitalizeFirstLetterOfEachWord(timeLib.getActualDateWithoutYear()));
                    textHours.setText(timeLib.getActualTime());
                    textCountTasks.setText(String.valueOf(tasksSelect.getAllTasksByDate(actualDate).size()));
                }),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}