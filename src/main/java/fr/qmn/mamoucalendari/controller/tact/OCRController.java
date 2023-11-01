package fr.qmn.mamoucalendari.controller.tact;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;

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
    public Slider sliderHours;
    @FXML
    public Slider sliderMinutes;
    @FXML
    public Button buttonCancel;
    @FXML
    public Button buttonCheck;
    @FXML
    public AnchorPane ocrFxml;

    public void initialize() {
        //load css
        ocrFxml.getStylesheets().add(getClass().getResource("/fr/qmn/mamoucalendari/css/ocr.css").toExternalForm());
        onPressedButtonCancel();
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

    public void onMouseDragged(MouseEvent mouseEvent) {
    }

    public void onMouseRelease(MouseEvent mouseEvent) {
    }

    public void onMousePressed(MouseEvent mouseEvent) {
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
}
