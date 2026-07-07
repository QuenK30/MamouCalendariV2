package fr.qmn.mamoucalendari.controller.tact;

import fr.qmn.mamoucalendari.bdd.SQLManager;
import fr.qmn.mamoucalendari.bdd.ScreenConfigManager;
import fr.qmn.mamoucalendari.ocr.CloudVisionOCR;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.image.BufferedImage;
import java.time.LocalTime;

public class OCRController {
    @FXML public Text textActualDay;
    @FXML public Button buttonPrevDay;
    @FXML public Button buttonNextDay;
    @FXML public Button buttonCancel;
    @FXML public Button buttonCheck;
    @FXML public AnchorPane ocrFxml;
    @FXML public ListView ListHours;
    @FXML public ListView ListMinutes;
    @FXML private AnchorPane keyboardPane;
    @FXML private Canvas drawingCanvas;
    @FXML private Button buttonGomme;

    private GraphicsContext gc;
    private double startY;
    private int startIndex;
    private String hoursSelected   = null;
    private String minutesSelected = null;
    private String dateConverted;

    public void initialize() {
        ocrFxml.getStylesheets().add(
            getClass().getResource("/fr/qmn/mamoucalendari/css/ocr.css").toExternalForm());
        onPressedButtonCancel();
        onValidate();
        setHoursOnList();
        setMinutesOnList();
        buildKeyboard();
        initCanvas();
    }

    public void setTextActualDay(String date, String dateConverted) {
        if (textActualDay == null) {
            System.out.println("textActualDay is null");
            return;
        }
        Font font = Font.loadFont(
            getClass().getResourceAsStream("/fr/qmn/mamoucalendari/font/Ubuntu-Bold.ttf"), 32);
        textActualDay.setFont(font);
        textActualDay.setText(date);
        setTextDayButton();
        this.dateConverted = dateConverted;
    }

    private void setTextDayButton() {
        String day = textActualDay.getText().split(" ")[0];
        switch (day) {
            case "Lundi":    buttonPrevDay.setText("Dimanche"); buttonNextDay.setText("Mardi");    break;
            case "Mardi":    buttonPrevDay.setText("Lundi");    buttonNextDay.setText("Mercredi"); break;
            case "Mercredi": buttonPrevDay.setText("Mardi");    buttonNextDay.setText("Jeudi");    break;
            case "Jeudi":    buttonPrevDay.setText("Mercredi"); buttonNextDay.setText("Vendredi"); break;
            case "Vendredi": buttonPrevDay.setText("Jeudi");    buttonNextDay.setText("Samedi");   break;
            case "Samedi":   buttonPrevDay.setText("Vendredi"); buttonNextDay.setText("Dimanche"); break;
            case "Dimanche": buttonPrevDay.setText("Samedi");   buttonNextDay.setText("Lundi");    break;
        }
    }

    public void onValidate() {
        buttonCheck.setOnAction(actionEvent -> {
            if (hoursSelected == null || minutesSelected == null) {
                Popup popup = new Popup();
                VBox vBox = new VBox();
                vBox.setStyle("-fx-background-color: #ffd9d8");
                vBox.setPrefWidth(600);
                vBox.setPrefHeight(400);
                vBox.setAlignment(Pos.CENTER);
                vBox.setSpacing(20);
                Label label = new Label(
                    "Erreur, veuillez sélectionner une heure et une minute \n avant de valider la tâche.");
                label.setStyle("-fx-font-size: 20px");
                Button buttonOk = new Button("Ok");
                buttonOk.setStyle("-fx-background-color: #00ff00");
                buttonOk.setPrefWidth(100);
                buttonOk.setPrefHeight(50);
                buttonOk.setOnAction(e -> popup.hide());
                vBox.getChildren().addAll(label, buttonOk);
                popup.getContent().add(vBox);
                popup.show(ocrFxml.getScene().getWindow());
                return;
            }
            buttonCheck.setDisable(true);
            WritableImage wi = drawingCanvas.snapshot(null, null);
            BufferedImage bi = SwingFXUtils.fromFXImage(wi, null);
            new Thread(() -> {
                String result = CloudVisionOCR.recognize(bi);
                Platform.runLater(() -> {
                    buttonCheck.setDisable(false);
                    if (result.isEmpty()) {
                        showError("OCR : aucun texte reconnu.\nVérifiez que GOOGLE_APPLICATION_CREDENTIALS est défini.");
                        return;
                    }
                    String cleaned = result.replaceAll("[\\r\\n]+", " ").replaceAll(" {2,}", " ").trim();
                    if (cleaned.isEmpty()) {
                        showError("OCR : texte vide après nettoyage.");
                        return;
                    }
                    checkIfEntryIsCorrect(
                        cleaned,
                        dateConverted,
                        Integer.parseInt(hoursSelected),
                        Integer.parseInt(minutesSelected)
                    );
                });
            }, "ocr-thread").start();
        });
    }

    public void checkIfEntryIsCorrect(String text, String date, int hours, int minutes) {
        // Capturer le Stage avant de créer le Popup — dans le handler du Popup,
        // getSource().getScene().getWindow() retourne la PopupWindow, pas le Stage.
        Stage tactStage = (Stage) ocrFxml.getScene().getWindow();
        Popup popup = new Popup();
        SQLManager sqlManager = new SQLManager();

        VBox vBox = new VBox();
        vBox.setStyle("-fx-background-color: #ffd9d8");
        vBox.setPrefWidth(400);
        vBox.setPrefHeight(300);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);

        Label label  = new Label("Voulez-vous valider cette tâche ?");
        label.setStyle("-fx-font-size: 20px");

        Label result = new Label(date + " à " + String.format("%02dh%02d", hours, minutes) + "\n" + text);
        result.setStyle("-fx-font-size: 20px");
        result.setAlignment(Pos.CENTER);

        Button buttonYes = new Button("Oui");
        buttonYes.setStyle("-fx-background-color: #00ff00");
        buttonYes.setPrefWidth(100);
        buttonYes.setPrefHeight(50);
        buttonYes.setOnAction(actionEvent -> {
            popup.hide();
            try {
                sqlManager.createTask(date, hours, minutes, text, false);
                tactStage.close();
                openCalendarScreen();
            } catch (Exception e) {
                showError("Impossible d'enregistrer la tâche : " + e.getMessage());
            }
        });

        Button buttonNo = new Button("Non");
        buttonNo.setStyle("-fx-background-color: #ff0000");
        buttonNo.setPrefWidth(100);
        buttonNo.setPrefHeight(50);
        buttonNo.setOnAction(actionEvent -> popup.hide());

        vBox.getChildren().addAll(label, result, buttonYes, buttonNo);
        popup.getContent().add(vBox);
        popup.show(ocrFxml.getScene().getWindow());
    }

    public void onPressedButtonCancel() {
        buttonCancel.setOnAction(actionEvent -> {
            Stage current = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            current.close();
            try {
                openCalendarScreen();
            } catch (Exception e) {
                showError("Impossible de revenir au calendrier : " + e.getMessage());
            }
        });
    }

    private void openCalendarScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fr/qmn/mamoucalendari/design/SecondScreenCalendar.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(root));
        ScreenConfigManager.applyScreen(stage, ScreenConfigManager.getConfig()[1]);
        stage.show();
    }

    private void initCanvas() {
        gc = drawingCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(4);
        gc.setLineCap(StrokeLineCap.ROUND);

        drawingCanvas.setOnMousePressed(e -> { gc.beginPath(); gc.moveTo(e.getX(), e.getY()); });
        drawingCanvas.setOnMouseDragged(e -> { gc.lineTo(e.getX(), e.getY()); gc.stroke(); gc.moveTo(e.getX(), e.getY()); });

        final boolean[] eraserMode = {false};
        buttonGomme.setOnAction(e -> {
            eraserMode[0] = !eraserMode[0];
            if (eraserMode[0]) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(40);
                buttonGomme.setStyle("-fx-font-size: 22px; -fx-background-color: #ff9d9d; -fx-border-color: #ff5555; -fx-border-width: 2; -fx-padding: 12 20; -fx-background-radius: 8;");
            } else {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(4);
                buttonGomme.setStyle("-fx-font-size: 22px; -fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 2; -fx-padding: 12 20; -fx-background-radius: 8;");
            }
        });
    }

    private void buildKeyboard() {
        GridPane grid = new GridPane();
        grid.setPrefSize(1704, 632);
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        for (int i = 0; i < 10; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(10);
            grid.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < 5; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(20);
            grid.getRowConstraints().add(rc);
        }

        String btnStyle = "-fx-font-size: 36px; -fx-background-color: #f0f0f0;" +
                          " -fx-border-color: #ccc; -fx-border-width: 1;";

        String[][] rows = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
            {"A", "Z", "E", "R", "T", "Y", "U", "I", "O", "P"},
            {"Q", "S", "D", "F", "G", "H", "J", "K", "L", "M"},
            {"W", "X", "C", "V", "B", "N", "É", "È", "À", "Ù"}
        };

        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length; col++) {
                String ch = rows[row][col];
                Button btn = new Button(ch);
                btn.setStyle(btnStyle);
                btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btn.setOnAction(e -> appendChar(ch));
                GridPane.setHgrow(btn, Priority.ALWAYS);
                GridPane.setVgrow(btn, Priority.ALWAYS);
                grid.add(btn, col, row);
            }
        }

        Button btnSpace = new Button("ESPACE");
        btnSpace.setStyle(btnStyle);
        btnSpace.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btnSpace.setOnAction(e -> appendChar(" "));
        GridPane.setHgrow(btnSpace, Priority.ALWAYS);
        GridPane.setVgrow(btnSpace, Priority.ALWAYS);
        GridPane.setColumnSpan(btnSpace, 6);
        grid.add(btnSpace, 0, 4);

        String[] special = {"Ç", ".", ","};
        for (int i = 0; i < special.length; i++) {
            String ch = special[i];
            Button btn = new Button(ch);
            btn.setStyle(btnStyle);
            btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            btn.setOnAction(e -> appendChar(ch));
            GridPane.setHgrow(btn, Priority.ALWAYS);
            GridPane.setVgrow(btn, Priority.ALWAYS);
            grid.add(btn, 6 + i, 4);
        }

        Button btnBack = new Button("⌫");
        btnBack.setStyle("-fx-font-size: 36px; -fx-background-color: #ff9d9d;" +
                         " -fx-border-color: #ccc; -fx-border-width: 1;");
        btnBack.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btnBack.setOnAction(e -> backspace());
        GridPane.setHgrow(btnBack, Priority.ALWAYS);
        GridPane.setVgrow(btnBack, Priority.ALWAYS);
        grid.add(btnBack, 9, 4);

        AnchorPane.setTopAnchor(grid, 0.0);
        AnchorPane.setBottomAnchor(grid, 0.0);
        AnchorPane.setLeftAnchor(grid, 0.0);
        AnchorPane.setRightAnchor(grid, 0.0);
        keyboardPane.getChildren().add(grid);
    }

    private void appendChar(String c) {}

    private void backspace() {}

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onListViewDragged(MouseEvent mouseEvent) {
        double endY = mouseEvent.getSceneY();
        int offset = (int) ((endY - startY) / 50);
        int newHour = startIndex - offset;
        if (newHour < 0) newHour = 0;
        else if (newHour >= ListHours.getItems().size()) newHour = ListHours.getItems().size() - 1;
        ListHours.scrollTo(newHour);
        ListHours.getSelectionModel().select(newHour);
    }

    public void onListViewReleased(MouseEvent mouseEvent) {
        int idx = ListHours.getSelectionModel().getSelectedIndex();
        ListHours.scrollTo(idx);
        ListHours.getSelectionModel().select(idx);
        hoursSelected = ListHours.getSelectionModel().getSelectedItem().toString();
    }

    public void onListViewClicked(MouseEvent mouseEvent) {
        startY     = mouseEvent.getSceneY();
        startIndex = ListHours.getSelectionModel().getSelectedIndex();
    }

    public void setHoursOnList() {
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) hours.add(String.format("%02d", i));
        ListHours.setItems(hours);
        ListHours.setCellFactory(lv -> new ListCell<String>() {
            @Override public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setAlignment(Pos.CENTER);
            }
        });
        ListHours.setOnScroll(event -> {
            int newIndex = ListHours.getSelectionModel().getSelectedIndex();
            if (event.getDeltaY() < 0) newIndex--;
            else if (event.getDeltaY() > 0) newIndex++;
            newIndex = Math.min(Math.max(newIndex, 0), hours.size() - 1);
            ListHours.getSelectionModel().select(newIndex);
            ListHours.scrollTo(newIndex);
            hoursSelected = hours.get(newIndex);
        });
        int currentHour = LocalTime.now().getHour();
        ListHours.getSelectionModel().select(currentHour);
        ListHours.scrollTo(currentHour);
        hoursSelected = String.format("%02d", currentHour);
    }

    public void setMinutesOnList() {
        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 5) minutes.add(String.format("%02d", i));
        ListMinutes.setItems(minutes);
        ListMinutes.setCellFactory(lv -> new ListCell<String>() {
            @Override public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setAlignment(Pos.CENTER);
            }
        });
        ListMinutes.setOnScroll(event -> {
            int newIndex = ListMinutes.getSelectionModel().getSelectedIndex();
            if (event.getDeltaY() < 0) newIndex--;
            else if (event.getDeltaY() > 0) newIndex++;
            newIndex = Math.min(Math.max(newIndex, 0), minutes.size() - 1);
            ListMinutes.getSelectionModel().select(newIndex);
            ListMinutes.scrollTo(newIndex);
            minutesSelected = minutes.get(newIndex);
        });
        int currentMinuteIdx = LocalTime.now().getMinute() / 5;
        ListMinutes.getSelectionModel().select(currentMinuteIdx);
        ListMinutes.scrollTo(currentMinuteIdx);
        minutesSelected = String.format("%02d", currentMinuteIdx * 5);
    }

    public void onListMinutesViewReleased(MouseEvent mouseEvent) {
        int idx = ListMinutes.getSelectionModel().getSelectedIndex();
        ListMinutes.scrollTo(idx);
        ListMinutes.getSelectionModel().select(idx);
        minutesSelected = ListMinutes.getSelectionModel().getSelectedItem().toString();
    }

    public void onListinutesClicked(MouseEvent mouseEvent) {
        startY     = mouseEvent.getSceneY();
        startIndex = ListMinutes.getSelectionModel().getSelectedIndex();
    }

    public void onListMinutesDragged(MouseEvent mouseEvent) {
        double endY = mouseEvent.getSceneY();
        int offset = (int) ((endY - startY) / 50);
        int newMinutes = startIndex - offset;
        if (newMinutes < 0) newMinutes = 0;
        else if (newMinutes >= ListMinutes.getItems().size()) newMinutes = ListMinutes.getItems().size() - 1;
        ListMinutes.scrollTo(newMinutes);
        ListMinutes.getSelectionModel().select(newMinutes);
    }
}
