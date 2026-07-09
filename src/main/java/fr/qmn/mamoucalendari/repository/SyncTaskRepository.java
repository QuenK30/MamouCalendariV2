package fr.qmn.mamoucalendari.repository;

import fr.qmn.mamoucalendari.tasks.Tasks;

import java.util.List;
import java.util.Map;

public class SyncTaskRepository implements TaskRepository {

    private final SQLiteTaskRepository local;
    private final SyncQueue            queue;

    public SyncTaskRepository(SQLiteTaskRepository local, SyncQueue queue) {
        this.local = local;
        this.queue = queue;
    }

    // ── Lectures : toujours locales ──────────────────────────────────────────

    @Override
    public List<Tasks> getAllTasksByDate(String date) {
        return local.getAllTasksByDate(date);
    }

    @Override
    public List<Tasks> getTasksByDate(String date) {
        return local.getTasksByDate(date);
    }

    @Override
    public Tasks[] getClosestTaskByTime(String date, int hours, int minutes) {
        return local.getClosestTaskByTime(date, hours, minutes);
    }

    // ── Écritures : local d'abord, puis file de sync ─────────────────────────

    @Override
    public void createTask(String date, int hours, int minutes, String tasks, boolean isDone) {
        String uuid = local.createTaskAndGetUuid(date, hours, minutes, tasks, isDone);
        queue.enqueue("CREATE", Map.of(
            "uuid",    uuid,
            "date",    date,
            "hours",   hours,
            "minutes", minutes,
            "task",    tasks,
            "isDone",  isDone
        ));
    }

    @Override
    public void deleteTask(String uuid) {
        local.deleteTask(uuid);
        queue.enqueue("DELETE", Map.of("uuid", uuid));
    }

    @Override
    public void markTaskDone(String uuid) {
        local.markTaskDone(uuid);
        queue.enqueue("MARK_DONE", Map.of("uuid", uuid));
    }
}
