package fr.qmn.mamoucalendari.controller.calendar;

import fr.qmn.mamoucalendari.controller.tact.OCRController;
import fr.qmn.mamoucalendari.utils.TimeLib;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
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
    private final String[] days = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
    private final String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet",
                               "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
    private String beforeMonth = "";
    private String afterMonth = "";
    private String year = "";
    public AnchorPane calendar;


    public void initialize() {
        setTextDays();
        setActionOnDayButton();
        calendar.getStylesheets().add(getClass().getResource("/fr/qmn/mamoucalendari/css/calendar.css").toExternalForm());
    }
    // set text j1 -> j35
    public void setTextDays() {
        Font font = Font.loadFont(getClass().getResourceAsStream("/fr/qmn/mamoucalendari/font/Ubuntu-Bold.ttf"), 32);
        Button[] buttons = new Button[43];
        Rectangle[] rectangles = new Rectangle[43];
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), actionEvent ->
                {
                    Calendar getCalendar = Calendar.getInstance();
                    int month = getCalendar.get(Calendar.MONTH);
                    year = String.valueOf(getCalendar.get(Calendar.YEAR));
                    textActualMonth.setFont(font);
                    textActualMonth.setText(months[month]);
                    if (month == 11) { // if the month is December
                        buttonNextMonth.setText(months[0]);
                    } else {
                        buttonNextMonth.setText(months[month + 1]);
                    }
                    if (month == 0) { // if the month is January
                        buttonPreviousMonth.setText(months[11]);
                    } else {
                        buttonPreviousMonth.setText(months[month - 1]);
                    }

                    // set button with id j1 -> j35
                    getCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    int daysInMonth = getCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    int rawDayOfWeek = getCalendar.get(Calendar.DAY_OF_WEEK);
                    int firstDay = rawDayOfWeek - 1;
                    if (firstDay == 0) {
                        firstDay = 7;
                    }

                    for (int i = 1; i < buttons.length; i++) {
                        buttons[i] = (Button) calendar.lookup("#j" + i);
                        buttons[i].setFont(font);
                        if (i >= firstDay && i < daysInMonth + firstDay) {
                            buttons[i].setText(String.valueOf(i - firstDay + 1));
                            if (i - firstDay + 1 < Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                                buttons[i].setOpacity(0.3);
                            } else {
                                buttons[i].setOpacity(1);
                            }
                            if (i - firstDay + 1 == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) { // Today
                                rectangles[i] = (Rectangle) calendar.lookup("#rect" + i);
                                rectangles[i].setFill(Color.rgb(255,217,216));
                                rectangles[i].setStroke(Color.web("#ff9d9d"));
                                rectangles[i].setStrokeWidth(6);
                            }else if (i - firstDay + 1 == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1) { // Yesterday
                                rectangles[i] = (Rectangle) calendar.lookup("#rect" + i);
                                rectangles[i].setFill(Color.rgb(255,255,255));
                                rectangles[i].setStroke(Color.web("#ff9d9d"));
                                rectangles[i].setStrokeWidth(6);
                            }
                        } else {
                            if (i < firstDay) { // days before the month
                                buttons[i].setText(String.valueOf(daysInMonth - firstDay + i + 1));
                                for (int j = 1; j < rectangles.length && j < firstDay; j++) {
                                    rectangles[j] = (Rectangle) calendar.lookup("#rect" + j);
                                    rectangles[j].setFill(Color.rgb(158, 158, 158));
                                    rectangles[j].setStroke(Color.web("#ff9d9d"));
                                    rectangles[j].setStrokeWidth(6);
                                    switch (month) {
                                        case 0 -> beforeMonth = months[month + 1];
                                        case 11 -> beforeMonth = months[month - 1];
                                        default -> {
                                            beforeMonth = months[month - 1];
                                            afterMonth = months[month + 1];
                                        }
                                    }
                                }
                            } else if (i >= daysInMonth) { // days after the month
                                buttons[i].setText(String.valueOf(i - daysInMonth - firstDay + 1));
                                rectangles[i] = (Rectangle) calendar.lookup("#rect" + i);
                                rectangles[i].setFill(Color.rgb(158, 158, 158, 0.3));
                                rectangles[i].setStroke(Color.web("#ff9d9d"));
                                rectangles[i].setStrokeWidth(6);
                                switch (month) {
                                    case 0 -> afterMonth = months[month + 1];
                                    case 11 -> beforeMonth = months[month - 1];
                                    default -> {
                                        beforeMonth = months[month - 1];
                                        afterMonth = months[month + 1];
                                    }
                                }
                            }
                        }
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    public void setActionOnDayButton() {
        Button[] buttons = new Button[43];
        Rectangle[] rectangles = new Rectangle[43];
        TimeLib timeLib = new TimeLib();
        for (int i = 1; i < buttons.length; i++) {
            buttons[i] = (Button) calendar.lookup("#j" + i);
            int finalI = i;
            buttons[i].setOnAction(actionEvent -> {
                rectangles[finalI] = (Rectangle) calendar.lookup("#rect" + finalI);
                String month;
                if (rectangles[finalI].getFill().equals(Color.rgb(158, 158, 158))) {
                    month = beforeMonth;
                } else if (rectangles[finalI].getFill().equals(Color.rgb(158, 158, 158, 0.3))) {
                    month = afterMonth;
                } else {
                    month = textActualMonth.getText();
                }

                int dayIndex = (finalI - 1) % 7; // 0 = Monday, 1 = Tuesday, etc.
                String date = days[dayIndex] + " " + buttons[finalI].getText() + " " + month; // Monday 1 January
                String realDate =  days[dayIndex] + " " + buttons[finalI].getText() + " " + month + " " + year; // Monday 1 January 2021
                System.out.println(date);
                System.out.println(realDate);
                String convertDate = timeLib.convertDate(realDate); // 2021-01-01
                //open ocr window
                ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
                try {
                    InputStream fxmlStream = getClass().getResourceAsStream("/fr/qmn/mamoucalendari/design/SecondScreenTact.fxml");
                    FXMLLoader loader = new FXMLLoader();
                    Parent root = loader.load(fxmlStream);
                    OCRController ocrController = loader.getController();

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.show();
                    ocrController.setTextActualDay(date, convertDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
