package fr.qmn.mamoucalendari;

import fr.qmn.mamoucalendari.bdd.SQLInit;
import fr.qmn.mamoucalendari.bdd.SQLManager;
import fr.qmn.mamoucalendari.tasks.Tasks;
import fr.qmn.mamoucalendari.tasks.TasksSelect;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MCMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader mainScreenFXML = new FXMLLoader(MCMain.class.getResource("/fr/qmn/mamoucalendari/design/MainScreen.fxml"));
        FXMLLoader calendarScreenFXML = new FXMLLoader(MCMain.class.getResource("/fr/qmn/mamoucalendari/design/SecondScreenCalendar.fxml"));
        Scene mainScreenScene = new Scene(mainScreenFXML.load(), 1920, 1080);
        Scene calendarScreenScene = new Scene(calendarScreenFXML.load(), 1920, 1080);
        mainScreenScene.getStylesheets().add(MCMain.class.getResource("/fr/qmn/mamoucalendari/css/visual.css").toExternalForm());
        calendarScreenScene.getStylesheets().add(MCMain.class.getResource("/fr/qmn/mamoucalendari/css/calendar.css").toExternalForm());

        Stage mainScreenStage = new Stage();
        mainScreenStage.setScene(mainScreenScene);
        mainScreenStage.setTitle("Visualisation des tâches");
        mainScreenStage.initStyle(StageStyle.UNDECORATED);
        mainScreenStage.setMaximized(true);

        Stage calendarScreenStage = new Stage();
        calendarScreenStage.setScene(calendarScreenScene);
        calendarScreenStage.setTitle("Calendrier");
        calendarScreenStage.initStyle(StageStyle.UNDECORATED);
        calendarScreenStage.setMaximized(true);

        mainScreenStage.show();
        calendarScreenStage.show();
    }

    public static void main(String[] args) {
        SQLInit sqlInit = new SQLInit();
        /*SQLManager sqlManager = new SQLManager();
        TasksSelect tasksSelect = new TasksSelect();
        sqlManager.createTask("2023-09-28", 12, 30, "Test");
        sqlManager.createTask("2023-09-28", 12, 00, "Test");
        sqlManager.createTask("2023-09-28", 12, 10, "Test");*/
        sqlInit.createNewDatabase();
        launch();
    }
}