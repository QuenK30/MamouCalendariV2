package fr.qmn.mamoucalendari.controller.calendar;

import fr.qmn.mamoucalendari.MCMain;
import fr.qmn.mamoucalendari.bdd.SQLManager;
import fr.qmn.mamoucalendari.bdd.ScreenConfigManager;
import fr.qmn.mamoucalendari.controller.tact.OCRController;
import fr.qmn.mamoucalendari.tasks.Tasks;
import fr.qmn.mamoucalendari.tasks.TasksSelect;
import fr.qmn.mamoucalendari.utils.TimeLib;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Calendar;
import java.util.List;

public class CalendarController {

    public Button buttonPreviousMonth;
    public Text textActualMonth;
    public Button buttonNextMonth;
    public Text textDayMonday;
    public Text textDayTuesday;
    public Text textDayWednesday;
    public Text textDayThursday;
    public Text textDayFriday;
    public Text textDaySaturday;
    public Text textDaySunday;

    private final String[] days   = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
    private final String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                                     "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};

    private int displayedYear;
    private int displayedMonth;
    private String beforeMonth = "";
    private String afterMonth  = "";
    private String year        = "";

    public AnchorPane calendar;
    private HBox luOverlay  = null;
    private VBox dayOverlay = null;

    public void initialize() {
        MCMain.activeCalendarController = this;
        Calendar now = Calendar.getInstance();
        displayedYear  = now.get(Calendar.YEAR);
        displayedMonth = now.get(Calendar.MONTH);
        setTextDays();
        setActionOnDayButton();
        setActionOnMonthButtons();
        calendar.getStylesheets().add(
            getClass().getResource("/fr/qmn/mamoucalendari/css/calendar.css").toExternalForm());
    }

    public void setTextDays() {
        Font font = Font.loadFont(
            getClass().getResourceAsStream("/fr/qmn/mamoucalendari/font/Ubuntu-Bold.ttf"), 32);
        Button[]    buttons    = new Button[43];
        Rectangle[] rectangles = new Rectangle[43];

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), actionEvent -> {
            int month = displayedMonth;
            year = String.valueOf(displayedYear);

            textActualMonth.setFont(font);
            textActualMonth.setText(months[month]);
            buttonNextMonth.setText(months[month == 11 ? 0 : month + 1]);
            buttonPreviousMonth.setText(months[month == 0 ? 11 : month - 1]);

            beforeMonth = months[month == 0 ? 11 : month - 1];
            afterMonth  = months[month == 11 ? 0 : month + 1];

            Calendar todayCal  = Calendar.getInstance();
            int todayDay   = todayCal.get(Calendar.DAY_OF_MONTH);
            int todayMonth = todayCal.get(Calendar.MONTH);
            int todayYear  = todayCal.get(Calendar.YEAR);
            boolean isCurrentMonth = (displayedYear == todayYear && displayedMonth == todayMonth);
            boolean isPastMonth    = displayedYear < todayYear
                || (displayedYear == todayYear && displayedMonth < todayMonth);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, displayedYear);
            cal.set(Calendar.MONTH, displayedMonth);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int daysInMonth  = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int rawDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int firstDay     = (rawDayOfWeek == 1) ? 7 : rawDayOfWeek - 1;

            Calendar prevCal = Calendar.getInstance();
            prevCal.set(Calendar.YEAR,  month == 0 ? displayedYear - 1 : displayedYear);
            prevCal.set(Calendar.MONTH, month == 0 ? 11 : month - 1);
            int daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int i = 1; i < buttons.length; i++) {
                buttons[i]    = (Button)    calendar.lookup("#j" + i);
                rectangles[i] = (Rectangle) calendar.lookup("#rect" + i);
                if (buttons[i] == null || rectangles[i] == null) continue;

                buttons[i].setFont(font);
                rectangles[i].setStroke(Color.web("#ff9d9d"));
                rectangles[i].setStrokeWidth(6);

                if (i >= firstDay && i < daysInMonth + firstDay) {
                    int dayNum = i - firstDay + 1;
                    buttons[i].setText(String.valueOf(dayNum));
                    if (isCurrentMonth) {
                        buttons[i].setOpacity(dayNum < todayDay ? 0.3 : 1.0);
                        if (dayNum == todayDay) {
                            rectangles[i].setFill(Color.rgb(255, 217, 216));
                        } else if (dayNum == todayDay - 1) {
                            rectangles[i].setFill(Color.WHITE);
                        } else {
                            rectangles[i].setFill(Color.TRANSPARENT);
                        }
                    } else {
                        buttons[i].setOpacity(isPastMonth ? 0.3 : 1.0);
                        rectangles[i].setFill(Color.TRANSPARENT);
                    }
                } else if (i < firstDay) {
                    int dayNum = daysInPrevMonth - firstDay + i + 1;
                    buttons[i].setText(String.valueOf(dayNum));
                    buttons[i].setOpacity(0.3);
                    rectangles[i].setFill(Color.rgb(158, 158, 158));
                } else {
                    int dayNum = i - daysInMonth - firstDay + 1;
                    buttons[i].setText(String.valueOf(dayNum));
                    buttons[i].setOpacity(0.3);
                    rectangles[i].setFill(Color.rgb(158, 158, 158, 0.3));
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setActionOnMonthButtons() {
        buttonPreviousMonth.setOnAction(e -> {
            hideDayOverlay();
            if (displayedMonth == 0) { displayedMonth = 11; displayedYear--; }
            else displayedMonth--;
        });
        buttonNextMonth.setOnAction(e -> {
            hideDayOverlay();
            if (displayedMonth == 11) { displayedMonth = 0; displayedYear++; }
            else displayedMonth++;
        });
    }

    public void setActionOnDayButton() {
        Button[]    buttons    = new Button[43];
        Rectangle[] rectangles = new Rectangle[43];
        TimeLib timeLib = new TimeLib();
        for (int i = 1; i < buttons.length; i++) {
            buttons[i]    = (Button)    calendar.lookup("#j" + i);
            rectangles[i] = (Rectangle) calendar.lookup("#rect" + i);
            int finalI = i;
            buttons[i].setOnAction(actionEvent -> {
                try {
                    Rectangle rect = (Rectangle) calendar.lookup("#rect" + finalI);
                    String month;
                    int cellYear;
                    int cellMonth;
                    if (rect.getFill().equals(Color.rgb(158, 158, 158))) {
                        cellMonth = (displayedMonth == 0) ? 11 : displayedMonth - 1;
                        cellYear  = (displayedMonth == 0) ? displayedYear - 1 : displayedYear;
                        month     = beforeMonth;
                    } else if (rect.getFill().equals(Color.rgb(158, 158, 158, 0.3))) {
                        cellMonth = (displayedMonth == 11) ? 0 : displayedMonth + 1;
                        cellYear  = (displayedMonth == 11) ? displayedYear + 1 : displayedYear;
                        month     = afterMonth;
                    } else {
                        cellMonth = displayedMonth;
                        cellYear  = displayedYear;
                        month     = textActualMonth.getText();
                    }

                    int dayNum = Integer.parseInt(buttons[finalI].getText());
                    java.util.Calendar today = java.util.Calendar.getInstance();
                    int todayYear  = today.get(java.util.Calendar.YEAR);
                    int todayMonth = today.get(java.util.Calendar.MONTH);
                    int todayDay   = today.get(java.util.Calendar.DAY_OF_MONTH);
                    boolean isPast;
                    if (cellYear != todayYear)        isPast = cellYear  < todayYear;
                    else if (cellMonth != todayMonth) isPast = cellMonth < todayMonth;
                    else                              isPast = dayNum    < todayDay;

                    int dayIndex = (finalI - 1) % 7;
                    String displayDate = days[dayIndex] + " " + buttons[finalI].getText() + " " + month;
                    String realDate    = displayDate + " " + cellYear;
                    String convertDate = timeLib.convertDate(realDate);

                    showDayOverlay(displayDate, convertDate, isPast);
                } catch (Exception e) {
                    showError("Impossible d'ouvrir le jour : " + e.getMessage());
                }
            });
        }
    }

    // ── Overlay tâches du jour ────────────────────────────────────────────────

    private void showDayOverlay(String displayDate, String convertDate, boolean isPast) {
        hideDayOverlay();

        dayOverlay = new VBox(20);
        dayOverlay.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #ff9d9d;" +
            "-fx-border-width: 4;" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;" +
            "-fx-padding: 30;");
        dayOverlay.setAlignment(Pos.TOP_CENTER);

        Label title = new Label(displayDate);
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: black;");

        VBox taskList = new VBox(12);
        refreshTaskList(taskList, convertDate, isPast);

        ScrollPane scroll = new ScrollPane(taskList);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(380);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        HBox btnRow = new HBox(24);
        btnRow.setAlignment(Pos.CENTER);

        if (isPast) {
            Label readOnly = new Label("🔒 Lecture seule");
            readOnly.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-text-fill: #999;" +
                "-fx-padding: 14 36;");
            btnRow.getChildren().add(readOnly);
        } else {
            Button btnAdd = new Button("＋  Ajouter une tâche");
            btnAdd.setStyle(
                "-fx-font-size: 26px;" +
                "-fx-background-color: #00cc66;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 14 36;" +
                "-fx-background-radius: 10;");
            btnAdd.setOnAction(e -> {
                hideDayOverlay();
                Stage current = (Stage) calendar.getScene().getWindow();
                current.close();
                try {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fr/qmn/mamoucalendari/design/SecondScreenTact.fxml"));
                    Parent root = loader.load();
                    OCRController ocrController = loader.getController();
                    Stage stage = new Stage();
                    stage.initStyle(StageStyle.UNDECORATED);
                    stage.setScene(new Scene(root));
                    ScreenConfigManager.applyScreen(stage, ScreenConfigManager.getConfig()[2]);
                    stage.show();
                    ocrController.setTextActualDay(displayDate, convertDate);
                } catch (Exception ex) {
                    showError("Impossible d'ouvrir le clavier : " + ex.getMessage());
                }
            });
            btnRow.getChildren().add(btnAdd);
        }

        Button btnClose = new Button("Fermer");
        btnClose.setStyle(
            "-fx-font-size: 26px;" +
            "-fx-background-color: #f0f0f0;" +
            "-fx-padding: 14 36;" +
            "-fx-background-radius: 10;");
        btnClose.setOnAction(e -> hideDayOverlay());

        btnRow.getChildren().add(btnClose);
        dayOverlay.getChildren().addAll(title, scroll, btnRow);

        AnchorPane.setTopAnchor(dayOverlay,    80.0);
        AnchorPane.setBottomAnchor(dayOverlay, 80.0);
        AnchorPane.setLeftAnchor(dayOverlay,  120.0);
        AnchorPane.setRightAnchor(dayOverlay, 120.0);
        calendar.getChildren().add(dayOverlay);
    }

    private void refreshTaskList(VBox taskList, String convertDate, boolean isPast) {
        taskList.getChildren().clear();
        SQLManager sqlManager = new SQLManager();
        List<Tasks> tasks = new TasksSelect().getTasksbyDate(convertDate);

        if (tasks.isEmpty()) {
            Label empty = new Label("Aucune tâche pour ce jour");
            empty.setStyle("-fx-font-size: 24px; -fx-text-fill: #555;");
            taskList.getChildren().add(empty);
            return;
        }

        for (Tasks task : tasks) {
            HBox row = new HBox(16);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10 14; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");

            Label time = new Label(String.format("%02dh%02d", task.getHours(), task.getMinutes()));
            time.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-min-width: 100; -fx-text-fill: black;");

            Label name = new Label(task.getTasks());
            name.setStyle("-fx-font-size: 24px; -fx-text-fill: black;");
            HBox.setHgrow(name, Priority.ALWAYS);

            row.getChildren().addAll(time, name);

            if (!isPast) {
                Button btnDelete = new Button("✕");
                btnDelete.setStyle(
                    "-fx-font-size: 22px;" +
                    "-fx-background-color: #ff4444;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 6 18;" +
                    "-fx-background-radius: 6;");
                btnDelete.setOnAction(e -> {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Supprimer la tâche");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Supprimer \"" + task.getTasks() + "\" ?");
                    confirm.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                sqlManager.deleteTask(task.getDate(), task.getHours(), task.getMinutes());
                                refreshTaskList(taskList, convertDate, false);
                            } catch (Exception ex) {
                                showError("Impossible de supprimer : " + ex.getMessage());
                            }
                        }
                    });
                });
                row.getChildren().add(btnDelete);
            }

            taskList.getChildren().add(row);
        }
    }

    private void hideDayOverlay() {
        if (dayOverlay != null) {
            calendar.getChildren().remove(dayOverlay);
            dayOverlay = null;
        }
    }

    // ── Overlay rappel Lu ────────────────────────────────────────────────────

    public void showLuOverlay(Tasks task, int minutesBefore, Runnable onLu) {
        hideLuOverlay();

        luOverlay = new HBox(24);
        luOverlay.setAlignment(Pos.CENTER);
        luOverlay.setStyle(
            "-fx-background-color: #ffd9d8;" +
            "-fx-border-color: #ff9d9d;" +
            "-fx-border-width: 4;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 18 36;");

        String delayLabel = minutesBefore == 0 ? "Maintenant !" : "dans " + minutesBefore + " min";
        Label lbl = new Label("⏰  " + task.getTasks()
            + "  " + String.format("%02d", task.getHours()) + "h"
            + String.format("%02d", task.getMinutes())
            + "  " + delayLabel);
        lbl.setStyle("-fx-font-size: 28px; -fx-text-fill: black;");

        Button btnLu = new Button("Lu ✓");
        btnLu.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-background-color: #00cc66;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 10 40;" +
            "-fx-background-radius: 8;");
        btnLu.setOnAction(e -> onLu.run());

        luOverlay.getChildren().addAll(lbl, btnLu);
        AnchorPane.setBottomAnchor(luOverlay, 20.0);
        AnchorPane.setLeftAnchor(luOverlay, 200.0);
        AnchorPane.setRightAnchor(luOverlay, 20.0);
        calendar.getChildren().add(luOverlay);
    }

    public void hideLuOverlay() {
        if (luOverlay != null) {
            calendar.getChildren().remove(luOverlay);
            luOverlay = null;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
