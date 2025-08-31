package com.cab302.bugco;

import javafx.fxml.FXML;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.image.ImageView;

public class HomeController {
    @FXML
    private TextArea terminalArea;
    @FXML
    private TextArea leaderboardArea;
    @FXML
    private ImageView imageView;

    private enum OverlayMode { LOGIN, REGISTER }
    private OverlayMode overlayMode = OverlayMode.LOGIN;

    private boolean overlayBuilt = false;
    private StackPane scrim;
    private VBox dialog;

    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private VBox fieldsBox;
    private Label errorLabel;

    @FXML
    private void initialize() {
        Image img = new Image(
                getClass().getResource("image.png").toExternalForm()
        );
        imageView.setImage(img);

        terminalArea.setText(String.join("\n",
                "C:\\USER\\ADMIN> INITIALISING TERMINAL...",
                "C:\\USER\\ADMIN> LOADING SECURITY PROTOCOLS...",
                "C:\\USER\\ADMIN> SYSTEM READY",
                "",
                "! RESTRICTED ACCESS TERMINAL !",
                "PLEASE ENTER YOUR OPERATIVE DESIGNATION TO BEGIN HACKING CHALLENGES.",
                "",
                "C:\\USER\\ADMIN> "
        ));

        leaderboardArea.setText(String.join("\n",
                "C:\\USER\\ADMIN> INITIALISING LEADERBOARD..",
                "",
                "! EASY CHALLENGE !",
                " > 1ST  NORMAN",
                " > 2ND  BEEBOP123",
                " > 3RD  MR.RUFUS",
                "",
                "! HARD CHALLENGE !",
                " > 1ST  NORMAN",
                " > 2ND  MR.RUFUS",
                " > 3RD  BEEBOP123"
        ));

        Platform.runLater(() -> {
            buildOverlay();

            Scene scene = terminalArea.getScene();
            Parent root = scene.getRoot();

            if (root instanceof StackPane sp) {
                if (!sp.getChildren().contains(scrim)) sp.getChildren().add(scrim);
            } else {
                StackPane host = new StackPane();
                host.getChildren().add(root);
                scene.setRoot(host);
                host.getChildren().add(scrim);
            }
        });
    }

    @FXML
    private void onLogin() {
        overlayMode = OverlayMode.LOGIN;
        showOverlay();
    }
    @FXML
    private void onRegister() {
        overlayMode = OverlayMode.REGISTER;
        showOverlay();
    }

    @FXML
    private void onGameInfo() {
        appendTerminal("Opening game brief: Identify bugs, patch systems, climb the leaderboard.");
    }
    @FXML
    private void onStart() {
        appendTerminal("Initialising hacking protocols... stand by.");
        // TODO: route to game scene
    }
    private String codename() {
        return "<unknown>";
    }

    private void appendTerminal(String line) {
        terminalArea.appendText("\n" + line);
    }

    private void buildOverlay() {
        if (overlayBuilt) return;

        scrim = new StackPane();
        scrim.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        scrim.setVisible(false);
        scrim.setManaged(false);
        scrim.setPickOnBounds(true);
        scrim.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scrim.setAlignment(Pos.CENTER);

        dialog = new VBox(14);
        dialog.getStyleClass().add("dialog-card");
        dialog.setMaxWidth(420);
        dialog.setPadding(new Insets(20));

        Label title = new Label("Account Details");
        title.getStyleClass().add("dialog-title");

        if (usernameField == null) {
            usernameField = new TextField();
            usernameField.setPromptText("Username");
        }
        if (passwordField == null) {
            passwordField = new PasswordField();
            passwordField.setPromptText("Password");
        }
        if (confirmPasswordField == null) {
            confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirm password");
        }

        if (fieldsBox == null) {
            fieldsBox = new VBox(10, usernameField, passwordField, confirmPasswordField);
        } else {
            fieldsBox.getChildren().setAll(usernameField, passwordField, confirmPasswordField);
        }

        if (errorLabel == null) {
            errorLabel = new Label();
            errorLabel.getStyleClass().add("error-label");
            errorLabel.setManaged(false);
            errorLabel.setVisible(false);
        }

        Button cancel = new Button("Cancel");
        Button save   = new Button("Save");
        save.getStyleClass().add("primary");

        HBox actions = new HBox(10, cancel, save);
        actions.setAlignment(Pos.CENTER_RIGHT);

        dialog.getChildren().setAll(title, fieldsBox, errorLabel, actions);

        if (dialog == null) {
            throw new IllegalStateException("Dialog must not be null before adding to scrim.");
        }
        if (!scrim.getChildren().contains(dialog)) {
            scrim.getChildren().add(dialog);
        }

        scrim.setOnMouseClicked(e -> { if (e.getTarget() == scrim) hideOverlay(); });
        cancel.setOnAction(e -> hideOverlay());
        save.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("Username and Password are required.");
                errorLabel.setManaged(true);
                errorLabel.setVisible(true);
                return;
            }

            if (overlayMode == OverlayMode.REGISTER) {
                String confirm = confirmPasswordField.getText().trim();
                if (!pass.equals(confirm)) {
                    errorLabel.setText("Passwords do not match.");
                    errorLabel.setManaged(true);
                    errorLabel.setVisible(true);
                    return;
                }
                appendTerminal("Registered: " + user);
            } else {
                appendTerminal("Logged in as: " + user);
            }

            errorLabel.setManaged(false);
            errorLabel.setVisible(false);
            hideOverlay();
        });

        overlayBuilt = true;
    }


    private void attachOverlayIfNeeded() {
        if (scrim == null) return;
        if (scrim.getScene() != null) return;

        Scene scene = terminalArea.getScene();
        if (scene == null) return;
        Parent root = scene.getRoot();

        if (root instanceof StackPane sp) {
            if (!sp.getChildren().contains(scrim)) sp.getChildren().add(scrim);
        } else {
            StackPane host = new StackPane();
            host.getChildren().add(root);
            scene.setRoot(host);
            host.getChildren().add(scrim);
        }
    }

    private void showOverlay() {
        if (!overlayBuilt) buildOverlay();
        attachOverlayIfNeeded();

        Label title = (Label) dialog.getChildren().get(0);
        if (overlayMode == OverlayMode.LOGIN) {
            title.setText("Login");
            confirmPasswordField.setManaged(false);
            confirmPasswordField.setVisible(false);
        } else {
            title.setText("Register");
            confirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(true);
        }

        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        scrim.setVisible(true);
        dialog.setOpacity(0);
        dialog.setScaleX(0.98);
        dialog.setScaleY(0.98);
        usernameField.requestFocus();

        new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(dialog.opacityProperty(), 0),
                        new KeyValue(dialog.scaleXProperty(), 0.98),
                        new KeyValue(dialog.scaleYProperty(), 0.98)
                ),
                new KeyFrame(Duration.millis(160),
                        new KeyValue(dialog.opacityProperty(), 1),
                        new KeyValue(dialog.scaleXProperty(), 1),
                        new KeyValue(dialog.scaleYProperty(), 1)
                )
        ).play();
    }

    private void hideOverlay() {
        if (scrim == null || !scrim.isVisible()) return;

        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(dialog.opacityProperty(), 1),
                        new KeyValue(dialog.scaleXProperty(), 1),
                        new KeyValue(dialog.scaleYProperty(), 1)
                ),
                new KeyFrame(Duration.millis(120),
                        new KeyValue(dialog.opacityProperty(), 0),
                        new KeyValue(dialog.scaleXProperty(), 0.98),
                        new KeyValue(dialog.scaleYProperty(), 0.98)
                )
        );
        t.setOnFinished(e -> {
                scrim.setVisible(false);
                scrim.setManaged(false);
        });
    }
}