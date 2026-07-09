package fr.qmn.mamoucalendari.repository;

import fr.qmn.mamoucalendari.config.AppConfig;
import fr.qmn.mamoucalendari.config.RemoteApiClient;

public class TaskRepositoryFactory {

    public static TaskRepository create() {
        return switch (AppConfig.getMode()) {
            case "sqlite" -> new SQLiteTaskRepository();
            case "remote" -> new RemoteTaskRepository(
                new RemoteApiClient(AppConfig.getApiUrl(), AppConfig.getApiKey())
            );
            case "sync" -> new SyncTaskRepository(
                new SQLiteTaskRepository(),
                new SyncQueue()
            );
            default -> throw new IllegalStateException("Mode inconnu : " + AppConfig.getMode());
        };
    }
}
