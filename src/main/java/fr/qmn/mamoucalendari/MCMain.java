package fr.qmn.mamoucalendari;

import fr.qmn.mamoucalendari.bdd.SQLInit;
import fr.qmn.mamoucalendari.bdd.ScreenConfigManager;
import fr.qmn.mamoucalendari.config.AppConfig;
import fr.qmn.mamoucalendari.config.RemoteApiClient;
import fr.qmn.mamoucalendari.controller.calendar.CalendarController;
import fr.qmn.mamoucalendari.controller.visual.VisualController;
import fr.qmn.mamoucalendari.repository.SQLiteTaskRepository;
import fr.qmn.mamoucalendari.repository.SyncQueue;
import fr.qmn.mamoucalendari.service.SyncWorker;
import fr.qmn.mamoucalendari.tasks.TasksReminder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MCMain extends Application {

    public static volatile VisualController   activeVisualController   = null;
    public static volatile CalendarController activeCalendarController = null;

    private static TasksReminder tasksReminder;
    private static SyncWorker    syncWorker;

    @Override
    public void start(Stage ignored) throws IOException {
        if (!ScreenConfigManager.isConfigured()) {
            showSetupWizard();
        } else {
            openMainWindows();
        }
    }

    private void showSetupWizard() throws IOException {
        FXMLLoader loader = new FXMLLoader(MCMain.class.getResource(
            "/fr/qmn/mamoucalendari/design/SetupScreen.fxml"));
        Parent root = loader.load();
        Stage setup = new Stage();
        setup.initStyle(StageStyle.UNDECORATED);
        setup.setScene(new Scene(root));
        ScreenConfigManager.applyScreen(setup, 0);
        setup.setAlwaysOnTop(true);
        setup.show();
    }

    public static void openMainWindows() {
        int[] cfg = ScreenConfigManager.getConfig();
        Platform.runLater(() -> {
            try {
                FXMLLoader mainFxml = new FXMLLoader(MCMain.class.getResource(
                    "/fr/qmn/mamoucalendari/design/MainScreen.fxml"));
                Scene mainScene = new Scene(mainFxml.load(), 1920, 1080);
                mainScene.getStylesheets().add(MCMain.class.getResource(
                    "/fr/qmn/mamoucalendari/css/visual.css").toExternalForm());
                Stage mainStage = new Stage();
                mainStage.setScene(mainScene);
                mainStage.setTitle("Visualisation des tâches");
                mainStage.initStyle(StageStyle.UNDECORATED);
                ScreenConfigManager.applyScreen(mainStage, cfg[0]);
                mainStage.show();

                FXMLLoader calFxml = new FXMLLoader(MCMain.class.getResource(
                    "/fr/qmn/mamoucalendari/design/SecondScreenCalendar.fxml"));
                Scene calScene = new Scene(calFxml.load(), 1920, 1080);
                calScene.getStylesheets().add(MCMain.class.getResource(
                    "/fr/qmn/mamoucalendari/css/calendar.css").toExternalForm());
                Stage calStage = new Stage();
                calStage.setScene(calScene);
                calStage.setTitle("Calendrier");
                calStage.initStyle(StageStyle.UNDECORATED);
                ScreenConfigManager.applyScreen(calStage, cfg[1]);
                calStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stop() {
        if (tasksReminder != null) tasksReminder.shutdown();
        if (syncWorker    != null) syncWorker.shutdown();
    }

    public static void main(String[] args) {
        new SQLInit().createNewDatabase();
        tasksReminder = new TasksReminder();
        tasksReminder.startReminder();
        if (AppConfig.getMode().equals("sync")) {
            syncWorker = new SyncWorker(
                new SyncQueue(),
                new RemoteApiClient(AppConfig.getApiUrl(), AppConfig.getApiKey()),
                new SQLiteTaskRepository()
            );
            syncWorker.start();
        }
        launch();
    }
}
