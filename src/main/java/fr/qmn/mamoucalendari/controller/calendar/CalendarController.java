package fr.qmn.mamoucalendari.controller.calendar;

import fr.qmn.mamoucalendari.controller.tact.OCRController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
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
                    Calendar getFirstDay = Calendar.getInstance();
                    int month = getFirstDay.get(Calendar.MONTH);
                    textActualMonth.setFont(font);
                    textActualMonth.setText(months[month]);
                    buttonPreviousMonth.setText(months[month - 1]);
                    buttonNextMonth.setText(months[month + 1]);

                    // set button with id j1 -> j35
                    getFirstDay.set(Calendar.DAY_OF_MONTH, 1);
                    Calendar getDaysInMonth = Calendar.getInstance();
                    int daysInMonth = getDaysInMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
                    int firstDay = (getFirstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1;
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
                                    beforeMonth = months[month - 1];
                                }
                            } else if (i >= daysInMonth) { // days after the month
                                buttons[i].setText(String.valueOf(i - daysInMonth - firstDay + 1));
                                rectangles[i] = (Rectangle) calendar.lookup("#rect" + i);
                                rectangles[i].setFill(Color.rgb(158, 158, 158, 0.3));
                                rectangles[i].setStroke(Color.web("#ff9d9d"));
                                rectangles[i].setStrokeWidth(6);
                                afterMonth = months[month + 1];
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
        for (int i = 1; i < buttons.length; i++) {
            buttons[i] = (Button) calendar.lookup("#j" + i);
            int finalI = i;
            buttons[i].setOnAction(actionEvent -> {
                //date = day + number of day + month
                String month = "";
                // if the day is before the month
                if (finalI < 8) {
                    month = beforeMonth;
                } else if (finalI > 35) { // if the day is after the month
                    month = afterMonth;
                } else { // if the day is in the month
                    month = textActualMonth.getText();
                }
                String date = days[finalI % 7] + " " + buttons[finalI].getText() + " " + month;
                System.out.println(date);
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
                    ocrController.setTextActualDay(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
