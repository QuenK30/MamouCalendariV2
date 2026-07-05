package fr.qmn.mamoucalendari;

import fr.qmn.mamoucalendari.bdd.SQLInit;
import fr.qmn.mamoucalendari.controller.calendar.CalendarController;
import fr.qmn.mamoucalendari.controller.visual.VisualController;
import fr.qmn.mamoucalendari.tasks.TasksReminder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MCMain extends Application {

    public static volatile VisualController   activeVisualController   = null;
    public static volatile CalendarController activeCalendarController = null;

    private static TasksReminder tasksReminder;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader mainScreenFXML    = new FXMLLoader(MCMain.class.getResource("/fr/qmn/mamoucalendari/design/MainScreen.fxml"));
        FXMLLoader calendarScreenFXML = new FXMLLoader(MCMain.class.getResource("/fr/qmn/mamoucalendari/design/SecondScreenCalendar.fxml"));
        Scene mainScreenScene    = new Scene(mainScreenFXML.load(), 1920, 1080);
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

    @Override
    public void stop() {
        if (tasksReminder != null) tasksReminder.shutdown();
    }

    public static void main(String[] args) {
        new SQLInit().createNewDatabase();
        tasksReminder = new TasksReminder();
        tasksReminder.startReminder();
        launch();
    }
}
