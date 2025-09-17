package com.cab302.bugco;

import com.cab302.bugco.auth.AuthService;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AuthController {
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginErrorLabel;

    @FXML private Pane scanlineOverlay;      // from FXML
    @FXML private ImageView backgroundImage; // from FXML

    private final AuthService auth = new AuthService();

    @FXML
    private void initialize() {
        if (loginErrorLabel != null) loginErrorLabel.setText("");
        if (loginPasswordField != null) loginPasswordField.setOnAction(e -> handleLogin());

        // CRT flicker
        Timeline flicker = new Timeline(
                new KeyFrame(Duration.seconds(0.0),  new KeyValue(scanlineOverlay.opacityProperty(), 0.90)),
                new KeyFrame(Duration.seconds(0.12), new KeyValue(scanlineOverlay.opacityProperty(), 0.80)),
                new KeyFrame(Duration.seconds(0.24), new KeyValue(scanlineOverlay.opacityProperty(), 0.92))
        );
        flicker.setCycleCount(Animation.INDEFINITE);
        flicker.setAutoReverse(true);
        flicker.play();

        // Background scaling
        var parent = (javafx.scene.layout.Region) backgroundImage.getParent();
        backgroundImage.fitWidthProperty().bind(parent.widthProperty());
        backgroundImage.fitHeightProperty().bind(parent.heightProperty());
        backgroundImage.setPreserveRatio(false);
    }

    @FXML
    private void handleLogin() {
        String u = safe(loginUsernameField);
        String p = safe(loginPasswordField);

        if (!isUsernameValid(u) || p.isEmpty() || !auth.authenticate(u, p)) {
            showInline("Invalid Credentials");
            return;
        }

        Session.setCurrentUser(u);
        goHome();
    }

    @FXML
    private void handleRegister() {
        String u = safe(loginUsernameField);
        String p = safe(loginPasswordField);

        boolean userValid = isUsernameValid(u);
        boolean passValid = auth.isPasswordStrong(p);

        try {
            if (auth.usernameExists(u)) {
                showInline("Username already exists.");
                return;
            }
        } catch (Exception ignored) {
        }

        if (userValid && !passValid) {
            showInline("Password too simple");
            return;
        }
        if (!userValid && passValid) {
            showInline("Username must be letters or numbers");
            return;
        }
        if (!userValid) {
            showInline("Username and Password invalid");
            return;
        }

        try {
            auth.register(u, p);
            showInline("Registered! Please log in.");
            loginPasswordField.clear();
        } catch (IllegalStateException dup) {
            showInline("Username already exists.");
        } catch (IllegalArgumentException weak) {
            showInline(weak.getMessage());
        } catch (Exception ex) {
            showInline("Registration failed.");
        }
    }

    // helpers
    private String safe(TextInputControl c) { return c == null || c.getText() == null ? "" : c.getText().trim(); }
    private boolean isUsernameValid(String u) { return u != null && u.matches("^[A-Za-z0-9]{1,20}$"); }
    private void showInline(String msg) { if (loginErrorLabel != null) loginErrorLabel.setText(msg); }

    private void goHome() {
        try {
            var fxmlUrl = getClass().getResource("/com/cab302/bugco/home-view.fxml");
            if (fxmlUrl == null) {
                throw new IllegalStateException("home-view.fxml not found on classpath at /com/cab302/bugco/");
            }

            Parent home = FXMLLoader.load(fxmlUrl);

            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            Scene scene = loginUsernameField.getScene();
            scene.setRoot(home);

            var cssUrl = getClass().getResource("/com/cab302/bugco/styles.css");
            if (cssUrl != null) {
                String css = cssUrl.toExternalForm();
                if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
            }

            stage.setTitle("BugCo – Home");
        } catch (Exception e) {
            e.printStackTrace(); // <-- see exact cause in console
            showInline("Failed to open home screen.");
        }
    }
}