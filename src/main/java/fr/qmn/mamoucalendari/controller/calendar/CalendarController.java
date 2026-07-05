package fr.qmn.mamoucalendari.controller.calendar;

import fr.qmn.mamoucalendari.MCMain;
import fr.qmn.mamoucalendari.controller.tact.OCRController;
import fr.qmn.mamoucalendari.tasks.Tasks;
import fr.qmn.mamoucalendari.utils.TimeLib;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Calendar;

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
    private HBox luOverlay = null;

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

            // Today reference
            Calendar todayCal  = Calendar.getInstance();
            int todayDay   = todayCal.get(Calendar.DAY_OF_MONTH);
            int todayMonth = todayCal.get(Calendar.MONTH);
            int todayYear  = todayCal.get(Calendar.YEAR);
            boolean isCurrentMonth = (displayedYear == todayYear && displayedMonth == todayMonth);
            boolean isPastMonth    = displayedYear < todayYear
                || (displayedYear == todayYear && displayedMonth < todayMonth);

            // Calendar layout for displayed month
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, displayedYear);
            cal.set(Calendar.MONTH, displayedMonth);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int daysInMonth  = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int rawDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int firstDay     = (rawDayOfWeek == 1) ? 7 : rawDayOfWeek - 1; // 1=Mon ... 7=Sun

            // Previous month's day count for overflow labels
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
                    // Current month
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
                    // Overflow: previous month
                    int dayNum = daysInPrevMonth - firstDay + i + 1;
                    buttons[i].setText(String.valueOf(dayNum));
                    buttons[i].setOpacity(0.3);
                    rectangles[i].setFill(Color.rgb(158, 158, 158));

                } else {
                    // Overflow: next month
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
            if (displayedMonth == 0) { displayedMonth = 11; displayedYear--; }
            else displayedMonth--;
        });
        buttonNextMonth.setOnAction(e -> {
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
                    if (rect.getFill().equals(Color.rgb(158, 158, 158))) {
                        month    = beforeMonth;
                        cellYear = (displayedMonth == 0) ? displayedYear - 1 : displayedYear;
                    } else if (rect.getFill().equals(Color.rgb(158, 158, 158, 0.3))) {
                        month    = afterMonth;
                        cellYear = (displayedMonth == 11) ? displayedYear + 1 : displayedYear;
                    } else {
                        month    = textActualMonth.getText();
                        cellYear = displayedYear;
                    }

                    int dayIndex = (finalI - 1) % 7;
                    String date     = days[dayIndex] + " " + buttons[finalI].getText() + " " + month;
                    String realDate = date + " " + cellYear;
                    String convertDate = timeLib.convertDate(realDate);

                    Stage current = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                    current.close();

                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fr/qmn/mamoucalendari/design/SecondScreenTact.fxml"));
                    Parent root = loader.load();
                    OCRController ocrController = loader.getController();

                    Stage stage = new Stage();
                    stage.initStyle(StageStyle.UNDECORATED);
                    stage.setScene(new Scene(root));
                    stage.setMaximized(true);
                    stage.show();
                    ocrController.setTextActualDay(date, convertDate);
                } catch (Exception e) {
                    showError("Impossible d'ouvrir le clavier : " + e.getMessage());
                }
            });
        }
    }

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
        lbl.setStyle("-fx-font-size: 28px;");

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
