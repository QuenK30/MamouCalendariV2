package fr.qmn.mamoucalendari.controller.setup;

import fr.qmn.mamoucalendari.MCMain;
import fr.qmn.mamoucalendari.bdd.ScreenConfigManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

public class SetupController {

    @FXML private VBox root;
    @FXML private Label screenCountLabel;
    @FXML private GridPane assignGrid;
    @FXML private Button buttonValider;

    private ToggleGroup groupVisual;
    private ToggleGroup groupCalendar;
    private ToggleGroup groupOcr;

    private final List<Stage> screenOverlays = new ArrayList<>();

    private static final String[] WINDOW_LABELS = {"Horloge", "Calendrier", "Saisie OCR"};

    public void initialize() {
        var screens = Screen.getScreens();
        int screenCount = screens.size();
        screenCountLabel.setText("Écrans disponibles : " + screenCount);

        groupVisual   = new ToggleGroup();
        groupCalendar = new ToggleGroup();
        groupOcr      = new ToggleGroup();

        ToggleGroup[] groups = {groupVisual, groupCalendar, groupOcr};

        for (int row = 0; row < 3; row++) {
            Label windowLabel = new Label(WINDOW_LABELS[row]);
            windowLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-min-width: 280;");
            windowLabel.setAlignment(Pos.CENTER_RIGHT);
            GridPane.setConstraints(windowLabel, 0, row);

            HBox btnBox = new HBox(16);
            btnBox.setAlignment(Pos.CENTER_LEFT);
            ToggleGroup group = groups[row];

            for (int s = 0; s < screenCount; s++) {
                ToggleButton btn = new ToggleButton("Écran " + (s + 1));
                btn.setToggleGroup(group);
                btn.setUserData(s);
                btn.setPrefWidth(170);
                btn.setPrefHeight(80);
                btn.setStyle(inactiveStyle());
                btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    btn.setStyle(isSelected ? activeStyle() : inactiveStyle());
                    updateValiderState();
                });
                btnBox.getChildren().add(btn);
            }

            GridPane.setConstraints(btnBox, 1, row);
            assignGrid.getChildren().addAll(windowLabel, btnBox);
        }

        buttonValider.setDisable(true);

        // Afficher un badge numéroté sur chaque écran pour les identifier
        for (int i = 0; i < screenCount; i++) {
            showScreenBadge(i, screens.get(i).getVisualBounds());
        }
    }

    private void showScreenBadge(int index, Rectangle2D bounds) {
        Label number = new Label(String.valueOf(index + 1));
        number.setStyle("-fx-font-size: 200px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label hint = new Label("Écran " + (index + 1));
        hint.setStyle("-fx-font-size: 48px; -fx-text-fill: rgba(255,255,255,0.75);");

        VBox box = new VBox(8, number, hint);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(20,20,40,0.82);");
        box.setPrefSize(bounds.getWidth(), bounds.getHeight());

        Scene scene = new Scene(box, bounds.getWidth(), bounds.getHeight());
        scene.setFill(Color.TRANSPARENT);

        Stage badge = new Stage();
        badge.initStyle(StageStyle.TRANSPARENT);
        badge.setScene(scene);
        badge.setX(bounds.getMinX());
        badge.setY(bounds.getMinY());
        badge.show();

        screenOverlays.add(badge);
    }

    private void closeOverlays() {
        screenOverlays.forEach(Stage::close);
        screenOverlays.clear();
    }

    @FXML
    private void onValider() {
        int visual   = selectedIndex(groupVisual);
        int calendar = selectedIndex(groupCalendar);
        int ocr      = selectedIndex(groupOcr);
        ScreenConfigManager.saveConfig(visual, calendar, ocr);
        closeOverlays();
        ((Stage) root.getScene().getWindow()).close();
        MCMain.openMainWindows();
    }

    private void updateValiderState() {
        boolean allSelected = groupVisual.getSelectedToggle() != null
                           && groupCalendar.getSelectedToggle() != null
                           && groupOcr.getSelectedToggle() != null;
        buttonValider.setDisable(!allSelected);
    }

    private int selectedIndex(ToggleGroup group) {
        Toggle selected = group.getSelectedToggle();
        return selected == null ? 0 : (int) selected.getUserData();
    }

    private String inactiveStyle() {
        return "-fx-font-size: 26px; -fx-background-color: #f0f0f0;" +
               " -fx-border-color: #ccc; -fx-border-width: 2;" +
               " -fx-background-radius: 10; -fx-border-radius: 10;" +
               " -fx-text-fill: #333;";
    }

    private String activeStyle() {
        return "-fx-font-size: 26px; -fx-background-color: #4a90d9;" +
               " -fx-border-color: #2a70b9; -fx-border-width: 2;" +
               " -fx-background-radius: 10; -fx-border-radius: 10;" +
               " -fx-text-fill: white;";
    }
}
