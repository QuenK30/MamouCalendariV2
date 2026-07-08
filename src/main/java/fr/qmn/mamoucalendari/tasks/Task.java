package fr.qmn.mamoucalendari.tasks;

public class Task {
    private final int     id;
    private final String  uuid;
    private final String  date;
    private final int     hours;
    private final int     minutes;
    private final String  task;
    private final boolean isDone;
    private final String  createdAt;
    private final String  updatedAt;

    public Task(int id, String uuid, String date, int hours, int minutes,
                String task, boolean isDone, String createdAt, String updatedAt) {
        this.id        = id;
        this.uuid      = uuid;
        this.date      = date;
        this.hours     = hours;
        this.minutes   = minutes;
        this.task      = task;
        this.isDone    = isDone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int     getId()        { return id; }
    public String  getUuid()      { return uuid; }
    public String  getDate()      { return date; }
    public int     getHours()     { return hours; }
    public int     getMinutes()   { return minutes; }
    public String  getTasks()     { return task; }
    public boolean getIsDone()    { return isDone; }
    public String  getCreatedAt() { return createdAt; }
    public String  getUpdatedAt() { return updatedAt; }
}
