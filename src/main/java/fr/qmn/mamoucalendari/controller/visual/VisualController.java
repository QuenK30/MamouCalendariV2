package fr.qmn.mamoucalendari.controller.visual;

import fr.qmn.mamoucalendari.tasks.Tasks;
import fr.qmn.mamoucalendari.tasks.TasksSelect;
import fr.qmn.mamoucalendari.utils.SoundLib;
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

    private void setTasks() {
        Tasks[] closestTask = getClosestTask();

        updateTaskUI(textBeforeHours, textBeforeTasks, closestTask[0]);


        Tasks currentTask = closestTask[1] != null ? closestTask[1] : closestTask[2];
        updateTaskUI(textActualHours, textActualTasks, currentTask);
        if (closestTask[2] == currentTask) {
            updateTaskUI(textAfterHours, textAfterTasks, null);
        }else {
            updateTaskUI(textAfterHours, textAfterTasks, closestTask[2]);
        }
    }

    private Tasks[] getClosestTask() {
        TasksSelect tasksSelect = new TasksSelect();
        TimeLib timeLib = new TimeLib();

        String actualDate = timeLib.getActualDate();
        String[] timeParts = timeLib.getActualTime().split(":");
        if (timeParts.length != 2) {
            throw new IllegalArgumentException("Le format de l'heure n'est pas valide");
        }

        int actualHoursTime = Integer.parseInt(timeParts[0]);
        int actualMinutesTime = Integer.parseInt(timeParts[1]);

        // Get closest task by actual time
        return tasksSelect.getClosestTaskByTime(actualDate, actualHoursTime, actualMinutesTime);
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

    //TODO: task reminder -> 30 minutes before | 15 minutes before | 5 minutes before | 1 minute before | 30 seconds before | 15 seconds before | 5 seconds before | 1 second before


}