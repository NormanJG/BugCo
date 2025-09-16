package com.cab302.bugco;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Objects;

public class AuthController {

    // Panes
    @FXML private VBox loginPane;
    @FXML private VBox registerPane;
    @FXML private Pane scanlineOverlay;
    @FXML private ImageView backgroundImage;

    // Login fields
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginErrorLabel;

    // Register fields
    @FXML private TextField regNameField;
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private Label regErrorLabel;

    @FXML
    private void initialize() {
        // CRT flicker
        Timeline flicker = new Timeline(
                new KeyFrame(Duration.seconds(0.0),  new KeyValue(scanlineOverlay.opacityProperty(), 0.90)),
                new KeyFrame(Duration.seconds(0.12), new KeyValue(scanlineOverlay.opacityProperty(), 0.80)),
                new KeyFrame(Duration.seconds(0.24), new KeyValue(scanlineOverlay.opacityProperty(), 0.92))
        );
        flicker.setCycleCount(Animation.INDEFINITE);
        flicker.setAutoReverse(true);
        flicker.play();
        var parent = (javafx.scene.layout.Region) backgroundImage.getParent();
        backgroundImage.fitWidthProperty().bind(parent.widthProperty());
        backgroundImage.fitHeightProperty().bind(parent.heightProperty());
        backgroundImage.setPreserveRatio(false);
    }

    @FXML
    private void showRegister() {
        toggle(registerPane, loginPane);
        clearErrors();
    }

    @FXML
    private void showLogin() {
        toggle(loginPane, registerPane);
        clearErrors();
    }

    @FXML
    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String pwd   = loginPasswordField.getText();

        if (username.isEmpty() || pwd.isEmpty()) {
            loginErrorLabel.setText("Please Enter a Username and Password.");
            return;
        }
        goToHome(loginPane);
    }

    @FXML
    private void handleRegister() {
        String name = regNameField.getText().trim();
        String username = regUsernameField.getText().trim();
        String pwd = regPasswordField.getText();
    }

    private void toggle(Node show, Node hide) {
        show.setVisible(true);
        show.setManaged(true);
        hide.setVisible(false);
        hide.setManaged(false);
    }

    private void clearErrors() {
        loginErrorLabel.setText("");
        regErrorLabel.setText("");
    }

    private void goToHome(Node anyNodeInScene) {
        Stage stage = (Stage) anyNodeInScene.getScene().getWindow();
        try {
            Parent home = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("home-view.fxml")));
            Scene scene = anyNodeInScene.getScene();
            scene.setRoot(home);
            if (!scene.getStylesheets().contains(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm())) {
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
            }
            stage.setTitle("Home");
        } catch (IOException e) {
            e.printStackTrace();
            // show an alert if home fails to load
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to load Home view.", ButtonType.OK);
            alert.showAndWait();
        }
    }

}