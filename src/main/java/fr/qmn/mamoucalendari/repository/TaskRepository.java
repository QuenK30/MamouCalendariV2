package fr.qmn.mamoucalendari.repository;

import fr.qmn.mamoucalendari.tasks.Tasks;

import java.util.List;

public interface TaskRepository {
    List<Tasks> getAllTasksByDate(String date);
    List<Tasks> getTasksByDate(String date);
    Tasks[] getClosestTaskByTime(String date, int hours, int minutes);
    void createTask(String date, int hours, int minutes, String tasks, boolean isDone);
    void deleteTask(String date, int hours, int minutes);
    void markTaskDone(String uuid);
}
