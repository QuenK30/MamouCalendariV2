package fr.qmn.mamoucalendari.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.qmn.mamoucalendari.config.RemoteApiClient;
import fr.qmn.mamoucalendari.tasks.Tasks;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class RemoteTaskRepository implements TaskRepository {

    private static final int CURRENT_WINDOW_MINUTES = 30;

    private final RemoteApiClient client;

    public RemoteTaskRepository(RemoteApiClient client) {
        this.client = client;
    }

    @Override
    public List<Tasks> getAllTasksByDate(String date) {
        return fetchByDate(date);
    }

    @Override
    public List<Tasks> getTasksByDate(String date) {
        return fetchByDate(date);
    }

    @Override
    public Tasks[] getClosestTaskByTime(String date, int hours, int minutes) {
        List<Tasks> all = fetchByDate(date);
        int totalMinutes = hours * 60 + minutes;
        int windowEnd    = totalMinutes + CURRENT_WINDOW_MINUTES;

        List<Tasks> sorted = all.stream()
            .sorted(Comparator.comparingInt(t -> t.getHours() * 60 + t.getMinutes()))
            .toList();

        Tasks previous = null;
        Tasks current  = null;

        for (Tasks t : sorted) {
            int taskTotal = t.getHours() * 60 + t.getMinutes();
            if (taskTotal < totalMinutes) {
                previous = t;
            } else if (taskTotal <= windowEnd && current == null) {
                current = t;
            }
        }

        int afterTotal = current != null ? current.getHours() * 60 + current.getMinutes() : totalMinutes;
        Tasks next = null;
        for (Tasks t : sorted) {
            int taskTotal = t.getHours() * 60 + t.getMinutes();
            if (taskTotal > afterTotal) {
                next = t;
                break;
            }
        }

        return new Tasks[]{previous, current, next};
    }

    @Override
    public void createTask(String date, int hours, int minutes, String tasks, boolean isDone) {
        Map<String, Object> body = Map.of(
            "date",    date,
            "hours",   hours,
            "minutes", minutes,
            "task",    tasks,
            "isDone",  isDone
        );
        client.post("/api/tasks", body);
    }

    @Override
    public void deleteTask(String uuid) {
        client.delete("/api/tasks/" + uuid);
    }

    @Override
    public void markTaskDone(String uuid) {
        client.patch("/api/tasks/" + uuid, Map.of("isDone", true));
    }

    private List<Tasks> fetchByDate(String date) {
        HydraCollection collection = client.get("/api/tasks?date=" + date, HydraCollection.class);
        if (collection.member == null) return Collections.emptyList();
        return collection.member.stream().map(TaskDto::toTasks).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HydraCollection {
        @JsonProperty("hydra:member")
        public List<TaskDto> member;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TaskDto {
        public int     id;
        public String  uuid;
        public String  date;
        public int     hours;
        public int     minutes;
        public String  tasks;
        public boolean isDone;
        public String  createdAt;
        public String  updatedAt;

        Tasks toTasks() {
            return new Tasks(id, uuid, date, hours, minutes, tasks, isDone, createdAt, updatedAt);
        }
    }
}
