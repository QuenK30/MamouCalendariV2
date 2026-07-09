package fr.qmn.mamoucalendari.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.qmn.mamoucalendari.config.RemoteApiClient;
import fr.qmn.mamoucalendari.config.RemoteApiException;
import fr.qmn.mamoucalendari.repository.SQLiteTaskRepository;
import fr.qmn.mamoucalendari.repository.SyncQueue;
import fr.qmn.mamoucalendari.repository.SyncQueue.SyncEntry;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SyncWorker {

    private final SyncQueue               queue;
    private final RemoteApiClient         client;
    private final SQLiteTaskRepository    localRepo;
    private final ObjectMapper            mapper    = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sync-worker");
        t.setDaemon(true);
        return t;
    });

    public SyncWorker(SyncQueue queue, RemoteApiClient client, SQLiteTaskRepository localRepo) {
        this.queue     = queue;
        this.client    = client;
        this.localRepo = localRepo;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::sync, 0, 30, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    private void sync() {
        for (SyncEntry entry : queue.getPending()) {
            try {
                dispatch(entry);
                queue.markDone(entry.id());
                System.out.println("[SyncWorker] OK  " + entry.operation() + " id=" + entry.id());
            } catch (RemoteApiException e) {
                queue.markFailed(entry.id());
                System.out.println("[SyncWorker] FAIL " + entry.operation() + " id=" + entry.id() + " → " + e.getMessage());
            } catch (Exception e) {
                queue.markFailed(entry.id());
                System.out.println("[SyncWorker] ERR " + entry.operation() + " id=" + entry.id() + " → " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatch(SyncEntry entry) throws Exception {
        Map<String, Object> payload = mapper.readValue(entry.payload(), new TypeReference<>() {});
        String localUuid = (String) payload.get("uuid");

        switch (entry.operation()) {
            case "CREATE" -> {
                // Le serveur génère son propre UUID — on récupère le remote UUID et on
                // met à jour l'entrée locale pour que DELETE/MARK_DONE suivants soient cohérents
                TaskCreatedResponse response = client.postAndGet("/api/tasks", payload, TaskCreatedResponse.class);
                if (response.uuid != null && !response.uuid.equals(localUuid)) {
                    localRepo.replaceUuid(localUuid, response.uuid);
                    System.out.println("[SyncWorker] UUID local remplacé : " + localUuid + " → " + response.uuid);
                }
            }
            case "DELETE" -> {
                try {
                    client.delete("/api/tasks/" + localUuid);
                } catch (RemoteApiException e) {
                    // 404 = tâche jamais arrivée sur le remote (CREATE avait échoué avant le sync)
                    if (e.getStatusCode() != 404) throw e;
                    System.out.println("[SyncWorker] DELETE 404 ignoré (tâche absente du remote) id=" + entry.id());
                }
            }
            case "MARK_DONE" -> {
                try {
                    client.patch("/api/tasks/" + localUuid, Map.of("isDone", true));
                } catch (RemoteApiException e) {
                    if (e.getStatusCode() != 404) throw e;
                }
            }
            default -> System.out.println("[SyncWorker] Opération inconnue : " + entry.operation());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TaskCreatedResponse {
        public String uuid;
    }
}
