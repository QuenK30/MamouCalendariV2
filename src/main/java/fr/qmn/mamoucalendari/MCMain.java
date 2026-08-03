package fr.qmn.mamoucalendari;

import fr.qmn.mamoucalendari.bdd.SQLInit;
import fr.qmn.mamoucalendari.bdd.ScreenConfigManager;
import fr.qmn.mamoucalendari.config.AppConfig;
import fr.qmn.mamoucalendari.config.RemoteApiClient;
import fr.qmn.mamoucalendari.controller.calendar.CalendarController;
import fr.qmn.mamoucalendari.controller.visual.VisualController;
import fr.qmn.mamoucalendari.repository.SQLiteTaskRepository;
import fr.qmn.mamoucalendari.repository.SyncQueue;
import fr.qmn.mamoucalendari.service.AuthService;
import fr.qmn.mamoucalendari.service.SyncWorker;
import fr.qmn.mamoucalendari.service.TokenManager;
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

    public static TokenManager tokenManager = null;

    private static TasksReminder tasksReminder;
    private static SyncWorker    syncWorker;

    @Override
    public void start(Stage ignored) throws IOException {
        if (tokenManager != null && !tokenManager.hasValidToken()) {
            showLoginScreen();
        } else if (!ScreenConfigManager.isConfigured()) {
            showSetupWizard();
        } else {
            openMainWindows();
        }
    }

    private void showLoginScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(MCMain.class.getResource(
            "/fr/qmn/mamoucalendari/design/LoginScreen.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(MCMain.class.getResource(
            "/fr/qmn/mamoucalendari/css/login.css").toExternalForm());
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        ScreenConfigManager.applyScreen(stage, root, 0);
        stage.setAlwaysOnTop(true);
        stage.show();
    }

    public static void showSetupWizard() throws IOException {
        FXMLLoader loader = new FXMLLoader(MCMain.class.getResource(
            "/fr/qmn/mamoucalendari/design/SetupScreen.fxml"));
        Parent root = loader.load();
        Stage setup = new Stage();
        setup.initStyle(StageStyle.UNDECORATED);
        setup.setScene(new Scene(root));
        ScreenConfigManager.applyScreen(setup, root, 0);
        setup.setAlwaysOnTop(true);
        setup.show();
    }

    public static void continueStartup() {
        Platform.runLater(() -> {
            try {
                if (!ScreenConfigManager.isConfigured()) {
                    showSetupWizard();
                } else {
                    openMainWindows();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void openMainWindows() {
        int[] cfg = ScreenConfigManager.getConfig();
        Platform.runLater(() -> {
            try {
                FXMLLoader mainFxml = new FXMLLoader(MCMain.class.getResource(
                    "/fr/qmn/mamoucalendari/design/MainScreen.fxml"));
                Parent mainRoot = mainFxml.load();
                Scene mainScene = new Scene(mainRoot);
                mainScene.getStylesheets().add(MCMain.class.getResource(
                    "/fr/qmn/mamoucalendari/css/visual.css").toExternalForm());
                Stage mainStage = new Stage();
                mainStage.setScene(mainScene);
                mainStage.setTitle("Visualisation des tâches");
                mainStage.initStyle(StageStyle.UNDECORATED);
                ScreenConfigManager.applyScreen(mainStage, mainRoot, cfg[0]);
                mainStage.show();

                FXMLLoader calFxml = new FXMLLoader(MCMain.class.getResource(
                    "/fr/qmn/mamoucalendari/design/SecondScreenCalendar.fxml"));
                Parent calRoot = calFxml.load();
                Scene calScene = new Scene(calRoot);
                calScene.getStylesheets().add(MCMain.class.getResource(
                    "/fr/qmn/mamoucalendari/css/calendar.css").toExternalForm());
                Stage calStage = new Stage();
                calStage.setScene(calScene);
                calStage.setTitle("Calendrier");
                calStage.initStyle(StageStyle.UNDECORATED);
                ScreenConfigManager.applyScreen(calStage, calRoot, cfg[1]);
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
        if (!AppConfig.getMode().equals("sqlite")) {
            AuthService authService = new AuthService(
                AppConfig.getApiUrl(),
                AppConfig.getApiUsername(),
                AppConfig.getApiPassword()
            );
            tokenManager = new TokenManager(authService);
            tokenManager.ensureAuthenticated();
        }
        tasksReminder = new TasksReminder();
        tasksReminder.startReminder(AppConfig.getReminderInterval());
        if (AppConfig.getMode().equals("sync")) {
            syncWorker = new SyncWorker(
                new SyncQueue(),
                new RemoteApiClient(
                    AppConfig.getApiUrl(),
                    AppConfig.getApiKey(),
                    tokenManager != null ? tokenManager::getAccessToken : null
                ),
                new SQLiteTaskRepository()
            );
            syncWorker.start(AppConfig.getSyncPushInterval(), AppConfig.getSyncPullInterval());
        }
        launch();
    }
}
