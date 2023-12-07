package fr.qmn.mamoucalendari.controller.visual;

import fr.qmn.mamoucalendari.bdd.SQLManager;
import fr.qmn.mamoucalendari.tasks.Tasks;
import fr.qmn.mamoucalendari.tasks.TasksSelect;
import fr.qmn.mamoucalendari.utils.StringLib;
import fr.qmn.mamoucalendari.utils.TimeLib;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

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
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), event -> {
                    updateDate();
                    setTasks();
                }),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    private void updateDate() {
        TimeLib timeLib = new TimeLib();
        StringLib stringLib = new StringLib();
        String actualDate = timeLib.getActualDate();
        TasksSelect tasksSelect = new TasksSelect();
        textDate.setText(stringLib.capitalizeFirstLetterOfEachWord(timeLib.getActualDateWithoutYear()));
        textHours.setText(timeLib.getActualTime());
        textCountTasks.setText(String.valueOf(tasksSelect.getAllTasksByDate(actualDate).size()));
    }

    private void setTasks(){
        TasksSelect tasksSelect = new TasksSelect();
        TimeLib timeLib = new TimeLib();
        SQLManager sqlManager = new SQLManager();

        String actualDate = timeLib.getActualDate(); // 2023-01-21
        String actualHoursTime = timeLib.getActualTime().split(":")[0]; // 12:00 -> 12
        String actualMinutesTime = timeLib.getActualTime().split(":")[1]; // 12:00 -> 00

        // Get all tasks by date
        List<Tasks> allTasksByDate = tasksSelect.getAllTasksByDate(actualDate);
        ArrayList<String> tasksList = new ArrayList<>();
        ArrayList<String> hoursAndMinutesList = new ArrayList<>();
        for (Tasks tasks : allTasksByDate) {
            tasksList.add(tasks.getTasks());
            hoursAndMinutesList.add(tasks.getHours() + ":" + tasks.getMinutes());
        }

        //Get closest task by actual time
        Tasks[] closestTask = tasksSelect.getClosestTaskByTime(actualDate, actualHoursTime, actualMinutesTime);

        if (closestTask[0] != null) {
            textBeforeHours.setText(closestTask[0].getHours() + ":" + closestTask[0].getMinutes());
            textBeforeTasks.setText(closestTask[0].getTasks());
        } else {
            textBeforeHours.setText("00:00");
            textBeforeTasks.setText("Aucune tâche");
        }

        if (closestTask[1] != null) {
            textActualHours.setText(closestTask[1].getHours() + ":" + closestTask[1].getMinutes());
            textActualTasks.setText(closestTask[1].getTasks());
        } else {
            textActualHours.setText("00:00");
            textActualTasks.setText("Aucune tâche");
        }

        if (closestTask[2] != null) {
            textAfterHours.setText(closestTask[2].getHours() + ":" + closestTask[2].getMinutes());
            textAfterTasks.setText(closestTask[2].getTasks());
        } else {
            textAfterHours.setText("00:00");
            textAfterTasks.setText("Aucune tâche");
        }

    }
}