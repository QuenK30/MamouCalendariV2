package fr.qmn.mamoucalendari.service;

import fr.qmn.mamoucalendari.repository.SQLiteTaskRepository;
import fr.qmn.mamoucalendari.repository.TaskRepository;
import fr.qmn.mamoucalendari.tasks.Tasks;

import java.util.List;

public class TaskService {

    private final TaskRepository repository;

    public TaskService() {
        this(new SQLiteTaskRepository());
    }

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Tasks> getAllTasksByDate(String date) {
        return repository.getAllTasksByDate(date);
    }

    public List<Tasks> getTasksByDate(String date) {
        return repository.getTasksByDate(date);
    }

    public Tasks[] getClosestTaskByTime(String date, int hours, int minutes) {
        return repository.getClosestTaskByTime(date, hours, minutes);
    }

    public void createTask(String date, int hours, int minutes, String tasks, boolean isDone) {
        repository.createTask(date, hours, minutes, tasks, isDone);
    }

    public void deleteTask(String date, int hours, int minutes) {
        repository.deleteTask(date, hours, minutes);
    }

    public void markTaskDone(String uuid) {
        repository.markTaskDone(uuid);
    }
}
