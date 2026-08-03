package fr.qmn.mamoucalendari.service;

import fr.qmn.mamoucalendari.repository.TaskRepository;
import fr.qmn.mamoucalendari.repository.TaskRepositoryFactory;
import fr.qmn.mamoucalendari.tasks.Tasks;

import java.util.List;

public class TaskService {

    private final TaskRepository repository;

    public TaskService() {
        this(TaskRepositoryFactory.create());
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
        TaskChangeNotifier.getInstance().notifyChange();
    }

    public void deleteTask(String uuid) {
        repository.deleteTask(uuid);
        TaskChangeNotifier.getInstance().notifyChange();
    }

    public void markTaskDone(String uuid) {
        repository.markTaskDone(uuid);
        TaskChangeNotifier.getInstance().notifyChange();
    }
}
