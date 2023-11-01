package fr.qmn.mamoucalendari.tasks;

public class Tasks {
    private String date;
    private int hours;
    private int minutes;
    private String tasks;

    public Tasks(String date, int hours, int minutes, String tasks) {
        this.date = date;
        this.hours = hours;
        this.minutes = minutes;
        this.tasks = tasks;
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
}
