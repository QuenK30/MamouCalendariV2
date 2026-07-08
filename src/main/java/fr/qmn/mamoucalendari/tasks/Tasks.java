package fr.qmn.mamoucalendari.tasks;

public class Tasks extends Task {

    public Tasks(String date, int hours, int minutes, String tasks, boolean isDone) {
        super(0, null, date, hours, minutes, tasks, isDone, null, null);
    }

    public Tasks(int id, String uuid, String date, int hours, int minutes,
                 String tasks, boolean isDone, String createdAt, String updatedAt) {
        super(id, uuid, date, hours, minutes, tasks, isDone, createdAt, updatedAt);
    }
}
