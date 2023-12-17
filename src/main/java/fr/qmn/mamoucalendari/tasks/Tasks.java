package fr.qmn.mamoucalendari.tasks;

public class Tasks {
    private final String date;
    private final int hours;
    private final int minutes;
    private final String tasks;
    private final boolean isDone;

    public Tasks(String date, int hours, int minutes, String tasks, boolean isDone) {
        this.date = date;
        this.hours = hours;
        this.minutes = minutes;
        this.tasks = tasks;
        this.isDone = isDone;
    }

    public String getDate() {
        return date;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getTasks() {
        return tasks;
    }

    public boolean getIsDone() {
        return isDone;
    }
}
