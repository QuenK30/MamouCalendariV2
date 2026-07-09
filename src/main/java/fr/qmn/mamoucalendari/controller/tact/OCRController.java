package fr.qmn.mamoucalendari.controller.tact;

import fr.qmn.mamoucalendari.bdd.ScreenConfigManager;
import fr.qmn.mamoucalendari.service.TaskService;
import fr.qmn.mamoucalendari.ocr.CloudVisionOCR;
import fr.qmn.mamoucalendari.tasks.TasksPrefab;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
    @FXML public AnchorPane canvasPane;

    private GraphicsContext gc;
    private double startY;
    private int startIndex;
    private String hoursSelected   = null;
    private String minutesSelected = null;
    private String dateConverted;

    private String     prefilledTask = null;
    private AnchorPane categoryPanel = null;

    private final TaskService taskService = new TaskService();

    private static final String BTN_CATEGORY_STYLE =
        "-fx-font-size: 28px; -fx-background-color: #ff9d9d; " +
        "-fx-border-color: #ff5555; -fx-border-width: 2; " +
        "-fx-background-radius: 12; -fx-border-radius: 12; -fx-cursor: hand;";

    private static final String BTN_TASK_STYLE =
        "-fx-font-size: 24px; -fx-background-color: #ffd9d8; " +
        "-fx-border-color: #ff9d9d; -fx-border-width: 2; " +
        "-fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;";

    private static final String BTN_OCR_STYLE =
        "-fx-font-size: 22px; -fx-background-color: #f0f0f0; " +
        "-fx-border-color: #ccc; -fx-border-width: 2; " +
        "-fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;";

    private static final String BTN_BACK_STYLE =
        "-fx-font-size: 22px; -fx-background-color: #ffe0b2; " +
        "-fx-border-color: #ffb74d; -fx-border-width: 2; " +
        "-fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;";

    public void initialize() {
        ocrFxml.getStylesheets().add(
            getClass().getResource("/fr/qmn/mamoucalendari/css/ocr.css").toExternalForm());
        onPressedButtonCancel();
        onValidate();
        setHoursOnList();
        setMinutesOnList();
        buildKeyboard();
        initCanvas();
        buildCategoryPanel();
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

    // -----------------------------------------------------------------------
    //  Panneau catégories / tâches prédéfinies
    // -----------------------------------------------------------------------

    private void buildCategoryPanel() {
        categoryPanel = new AnchorPane();
        categoryPanel.setPrefSize(1704, 752);
        categoryPanel.setLayoutX(217);
        categoryPanel.setLayoutY(213);
        categoryPanel.setStyle("-fx-background-color: #fff5f5;");
        showCategoryView();
        ocrFxml.getChildren().add(categoryPanel);
    }

    private void showCategoryView() {
        categoryPanel.getChildren().clear();

        Label title = new Label("Quel type de tâche ?");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #cc3333;");
        AnchorPane.setTopAnchor(title, 40.0);
        AnchorPane.setLeftAnchor(title, 0.0);
        AnchorPane.setRightAnchor(title, 0.0);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        String[] categories = {"Médical", "Administratif", "Famille", "Loisir"};

        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(40);
        grid.setAlignment(Pos.CENTER);

        for (int i = 0; i < categories.length; i++) {
            final String cat = categories[i];
            Button btn = new Button(cat);
            btn.setStyle(BTN_CATEGORY_STYLE);
            btn.setPrefSize(350, 220);
            btn.setWrapText(true);
            btn.setAlignment(Pos.CENTER);
            btn.setOnAction(e -> showTaskView(cat));
            grid.add(btn, i % 2, i / 2);
        }

        AnchorPane.setTopAnchor(grid, 120.0);
        AnchorPane.setLeftAnchor(grid, 0.0);
        AnchorPane.setRightAnchor(grid, 0.0);
        AnchorPane.setBottomAnchor(grid, 100.0);

        Button btnOcr = new Button("Saisie manuelle");
        btnOcr.setStyle(BTN_OCR_STYLE);
        btnOcr.setPrefSize(260, 70);
        btnOcr.setOnAction(e -> switchToOcr());
        AnchorPane.setBottomAnchor(btnOcr, 20.0);
        AnchorPane.setRightAnchor(btnOcr, 30.0);

        categoryPanel.getChildren().addAll(title, grid, btnOcr);
    }

    private void showTaskView(String category) {
        categoryPanel.getChildren().clear();

        Label title = new Label(category);
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #cc3333;");
        AnchorPane.setTopAnchor(title, 30.0);
        AnchorPane.setLeftAnchor(title, 0.0);
        AnchorPane.setRightAnchor(title, 0.0);
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);

        VBox taskBox = new VBox(20);
        taskBox.setAlignment(Pos.CENTER);
        taskBox.setPadding(new Insets(20));

        for (String taskText : TasksPrefab.getTasksByCategory(category)) {
            Button btn = new Button(taskText);
            btn.setStyle(BTN_TASK_STYLE);
            btn.setPrefSize(700, 80);
            btn.setWrapText(true);
            btn.setAlignment(Pos.CENTER);
            final String captured = taskText;
            btn.setOnAction(e -> selectTask(captured));
            taskBox.getChildren().add(btn);
        }

        AnchorPane.setTopAnchor(taskBox, 110.0);
        AnchorPane.setLeftAnchor(taskBox, 0.0);
        AnchorPane.setRightAnchor(taskBox, 0.0);

        Button btnBack = new Button("← Retour");
        btnBack.setStyle(BTN_BACK_STYLE);
        btnBack.setPrefSize(200, 60);
        btnBack.setOnAction(e -> showCategoryView());
        AnchorPane.setBottomAnchor(btnBack, 20.0);
        AnchorPane.setLeftAnchor(btnBack, 30.0);

        Button btnOcr = new Button("Saisie manuelle");
        btnOcr.setStyle(BTN_OCR_STYLE);
        btnOcr.setPrefSize(260, 60);
        btnOcr.setOnAction(e -> switchToOcr());
        AnchorPane.setBottomAnchor(btnOcr, 20.0);
        AnchorPane.setRightAnchor(btnOcr, 30.0);

        categoryPanel.getChildren().addAll(title, taskBox, btnBack, btnOcr);
    }

    private void switchToOcr() {
        if (categoryPanel != null) ocrFxml.getChildren().remove(categoryPanel);
        canvasPane.setVisible(true);
    }

    private void selectTask(String taskText) {
        if (hoursSelected == null || minutesSelected == null) {
            showError("Veuillez sélectionner une heure et une minute avant de valider la tâche.");
            return;
        }
        prefilledTask = taskText;
        categoryPanel.setVisible(false);
        int hours   = Integer.parseInt(hoursSelected);
        int minutes = Integer.parseInt(minutesSelected);
        checkIfEntryIsCorrect(taskText, dateConverted, hours, minutes);
    }

    // -----------------------------------------------------------------------
    //  Validation
    // -----------------------------------------------------------------------

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

            // Si une tâche prédéfinie a été sélectionnée, bypass OCR
            if (prefilledTask != null) {
                int hours   = Integer.parseInt(hoursSelected);
                int minutes = Integer.parseInt(minutesSelected);
                if (categoryPanel != null) categoryPanel.setVisible(false);
                checkIfEntryIsCorrect(prefilledTask, dateConverted, hours, minutes);
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
                taskService.createTask(date, hours, minutes, text, false);
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
        buttonNo.setOnAction(actionEvent -> {
            popup.hide();
            prefilledTask = null;
            if (categoryPanel != null) categoryPanel.setVisible(true);
        });

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

    // -----------------------------------------------------------------------
    //  Canvas OCR
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    //  Sélecteurs heures / minutes
    // -----------------------------------------------------------------------

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
