package fr.qmn.mamoucalendari.controller.tact;

import fr.qmn.mamoucalendari.ocr.InitOCR;
import fr.qmn.mamoucalendari.utils.TimeLib;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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
    public VBox vBoxSliderHours;
    public VBox vBoxSliderMinutes;
    public ScrollPane scrollPane;
    @FXML
    private GraphicsContext graphicsContext;

    public void initialize() {
        //load css
        ocrFxml.getStylesheets().add(getClass().getResource("/fr/qmn/mamoucalendari/css/ocr.css").toExternalForm());
        onPressedButtonCancel();
        onValidate();

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
        parameters.setFill(Color.TRANSPARENT);
        canvas.snapshot(parameters, image);
        String path = "C:/Users/quenk/IdeaProjects/MamouCalendariV2/src/main/resources/fr/qmn/mamoucalendari/ocr/";
        File file = new File(path + filename + ".tiff");
        System.out.println("File path: " + file.getAbsolutePath());

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "tiff", file);
            System.out.println("Image saved");
            initOCR.receiveImageForOCR(file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("OCR Error: " + e);
        }
    }

    public void onMouseDragged(MouseEvent mouseEvent) {
        graphicsContext.lineTo(mouseEvent.getX(), mouseEvent.getY());
        graphicsContext.stroke();
    }

    public void onMouseRelease(MouseEvent mouseEvent) {
        graphicsContext.lineTo(mouseEvent.getX(), mouseEvent.getY());
        graphicsContext.stroke();
        graphicsContext.closePath();
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
}
