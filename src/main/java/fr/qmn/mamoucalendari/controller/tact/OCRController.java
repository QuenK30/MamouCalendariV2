package fr.qmn.mamoucalendari.controller.tact;

import fr.qmn.mamoucalendari.ocr.InitOCR;
import fr.qmn.mamoucalendari.utils.TimeLib;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;

public class OCRController {
    @FXML
    public Text textActualDay;
    @FXML
    public Button buttonPrevDay;
    @FXML
    public Button buttonNextDay;
    @FXML
    public Canvas canvasOCR;
    @FXML
    public Button buttonCancel;
    @FXML
    public Button buttonCheck;
    @FXML
    public AnchorPane ocrFxml;
    @FXML
    public ListView ListHours;
    @FXML
    public ListView ListMinutes;
    @FXML
    private GraphicsContext graphicsContext;
    private double startY;
    private int startIndex;
    private String hoursSelected;
    private String minutesSelected;

    public void initialize() {
        //load css
        ocrFxml.getStylesheets().add(getClass().getResource("/fr/qmn/mamoucalendari/css/ocr.css").toExternalForm());
        onPressedButtonCancel();
        onValidate();
        setHoursOnList();
        setMinutesOnList();

        graphicsContext = canvasOCR.getGraphicsContext2D();
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(3);
    }

    public void setTextActualDay(String date) {
        if (textActualDay == null) {
            System.out.println("textActualDay is null");
            return;
        }
        Font font = Font.loadFont(getClass().getResourceAsStream("/fr/qmn/mamoucalendari/font/Ubuntu-Bold.ttf"), 32);
        textActualDay.setFont(font);
        textActualDay.setText(date);
    }

    public void onValidate() {
        buttonCheck.setOnAction(actionEvent -> {
            System.out.println("Validate");
            TimeLib timeLib = new TimeLib();
            captureScreenshot(canvasOCR, timeLib.getActualTimeWithoutColon()+"_ocr");
            System.out.println("Screenshot saved");
            ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
            try {
                InputStream fxmlStream = getClass().getResourceAsStream("/fr/qmn/mamoucalendari/design/SecondScreenCalendar.fxml");
                FXMLLoader loader = new FXMLLoader();
                Parent root = loader.load(fxmlStream);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void captureScreenshot(Canvas canvas, String filename) {
        InitOCR initOCR = new InitOCR();
        WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.WHITE);
        canvas.snapshot(parameters, image);
        String path = "C:/Users/quenk/IdeaProjects/MamouCalendariV2/src/main/resources/fr/qmn/mamoucalendari/ocr/";
        File file = new File(path + filename + ".png");
        System.out.println("File path: " + file.getAbsolutePath());

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            System.out.println("Image saved");
            initOCR.receiveImageForOCR(file.getAbsolutePath(), hoursSelected, minutesSelected, textActualDay.getText());
        } catch (IOException e) {
            System.out.println("OCR Error: " + e);
        }
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        graphicsContext.lineTo(mouseEvent.getX(), mouseEvent.getY());
        graphicsContext.stroke();
    }

    public void onListViewDragged(MouseEvent mouseEvent) {
        double endY = mouseEvent.getSceneY();
        double deltaY = endY - startY;

        int offset = (int) (deltaY / 50);
        int newHour = startIndex - offset;

        if (newHour < 0) newHour = 0;
        else if (newHour >= ListHours.getItems().size()) newHour = ListHours.getItems().size() - 1;

        ListHours.scrollTo(newHour);
        ListHours.getSelectionModel().select(newHour);
    }

    public void onMouseRelease(MouseEvent mouseEvent) {
        graphicsContext.lineTo(mouseEvent.getX(), mouseEvent.getY());
        graphicsContext.stroke();
        graphicsContext.closePath();
    }

    public void onListViewReleased(MouseEvent mouseEvent) {
        int currentHour = ListHours.getSelectionModel().getSelectedIndex();
        ListHours.scrollTo(currentHour);
        ListHours.getSelectionModel().select(currentHour);
        //get text from list and set it to hoursSelected variable
        hoursSelected = ListHours.getSelectionModel().getSelectedItem().toString();
        System.out.println(hoursSelected);
    }

    public void onListViewClicked(MouseEvent mouseEvent) {
        startY = mouseEvent.getSceneY();
        startIndex = ListHours.getSelectionModel().getSelectedIndex();
    }

    public void onMousePressed(MouseEvent mouseEvent) {
        graphicsContext.beginPath();
        graphicsContext.moveTo(mouseEvent.getX(), mouseEvent.getY());
        graphicsContext.stroke();
    }

    public void onPressedButtonCancel() {
        buttonCancel.setOnAction(actionEvent -> {
            ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
            try {
                InputStream fxmlStream = getClass().getResourceAsStream("/fr/qmn/mamoucalendari/design/SecondScreenCalendar.fxml");
                FXMLLoader loader = new FXMLLoader();
                Parent root = loader.load(fxmlStream);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void setHoursOnList() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }
        ListHours.setItems(hours);

        ListHours.setCellFactory(lv -> new ListCell<String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setAlignment(Pos.CENTER);
            }
        });

        ListHours.setOnScroll(event -> {
            double scrollY = event.getDeltaY();
            int newIndex = ListHours.getSelectionModel().getSelectedIndex();

            // Scroll vers le haut
            if (scrollY < 0) {
                newIndex--;
            }
            // Scroll vers le bas
            else if (scrollY > 0) {
                newIndex++;
            }

            // Vérifier les bornes de la liste
            newIndex = Math.min(Math.max(newIndex, 0), hours.size() - 1);

            ListHours.getSelectionModel().select(newIndex);
            ListHours.scrollTo(newIndex);
        });

        // Sélectionner l'heure actuelle
        int currentHour = LocalTime.now().getHour();
        ListHours.getSelectionModel().select(currentHour);
        ListHours.scrollTo(currentHour);
    }

    public void setMinutesOnList() {
        ObservableList<String> minutes = FXCollections.observableArrayList();

        for (int i = 0; i < 60; i+=5) {
            minutes.add(String.format("%02d", i));
        }

        ListMinutes.setItems(minutes);

        ListMinutes.setCellFactory(lv -> new ListCell<String>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setAlignment(Pos.CENTER);
            }
        });

        ListMinutes.setOnScroll(event -> {
            double scrollY = event.getDeltaY();
            int newIndex = ListMinutes.getSelectionModel().getSelectedIndex();

            // Scroll vers le haut
            if (scrollY < 0) {
                newIndex--;
            }
            // Scroll vers le bas
            else if (scrollY > 0) {
                newIndex++;
            }

            // Vérifier les bornes de la liste
            newIndex = Math.min(Math.max(newIndex, 0), minutes.size() - 1);

            ListMinutes.getSelectionModel().select(newIndex);
            ListMinutes.scrollTo(newIndex);
        });

        // Sélectionner l'heure actuelle
        int currentMinute = LocalTime.now().getMinute();
        ListHours.getSelectionModel().select(currentMinute);
        ListHours.scrollTo(currentMinute);
    }

    public void onListMinutesViewReleased(MouseEvent mouseEvent) {
        int currentMinutes = ListMinutes.getSelectionModel().getSelectedIndex();
        ListMinutes.scrollTo(currentMinutes);
        ListMinutes.getSelectionModel().select(currentMinutes);
        //get text from list and set it to minutesSelected variable
        minutesSelected = ListMinutes.getSelectionModel().getSelectedItem().toString();
        System.out.println(minutesSelected);
    }

    public void onListinutesClicked(MouseEvent mouseEvent) {
        startY = mouseEvent.getSceneY();
        startIndex = ListMinutes.getSelectionModel().getSelectedIndex();
    }

    public void onListMinutesDragged(MouseEvent mouseEvent) {
        double endY = mouseEvent.getSceneY();
        double deltaY = endY - startY;

        int offset = (int) (deltaY / 50);
        int newMinutes = startIndex - offset;

        if (newMinutes < 0) newMinutes = 0;
        else if (newMinutes >= ListMinutes.getItems().size()) newMinutes = ListMinutes.getItems().size() - 1;

        ListMinutes.scrollTo(newMinutes);
        ListMinutes.getSelectionModel().select(newMinutes);
    }

}
