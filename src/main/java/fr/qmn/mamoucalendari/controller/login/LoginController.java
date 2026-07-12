package fr.qmn.mamoucalendari.controller.login;

import fr.qmn.mamoucalendari.MCMain;
import fr.qmn.mamoucalendari.config.AppConfig;
import fr.qmn.mamoucalendari.config.AuthException;
import fr.qmn.mamoucalendari.service.AuthService;
import fr.qmn.mamoucalendari.service.AuthService.TokenPair;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;

    public void initialize() {
        emailField.setText(AppConfig.getApiUsername());
        passwordField.setOnAction(e -> onLogin());
    }

    @FXML
    private void onLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        new Thread(() -> {
            try {
                AuthService auth = new AuthService(AppConfig.getApiUrl(), email, password);
                TokenPair pair   = auth.login();
                MCMain.tokenManager.storeTokens(pair);
                Platform.runLater(() -> {
                    ((Stage) loginButton.getScene().getWindow()).close();
                    MCMain.continueStartup();
                });
            } catch (AuthException e) {
                Platform.runLater(() -> {
                    errorLabel.setText("Identifiants incorrects");
                    errorLabel.setVisible(true);
                    loginButton.setDisable(false);
                });
            }
        }, "login-thread").start();
    }
}
