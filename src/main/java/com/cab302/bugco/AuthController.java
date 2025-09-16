package com.cab302.bugco;

import com.cab302.bugco.auth.AuthService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class AuthController {

    // Panes
    @FXML private VBox loginPane;
    @FXML private VBox registerPane;

    // Overlay + background
    @FXML private Pane scanlineOverlay;
    @FXML private ImageView backgroundImage;

    // Login fields
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginErrorLabel;

    // Register fields
    @FXML private TextField regNameField;       // optional, can be unused by AuthService
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private Label regErrorLabel;

    private final AuthService auth = new AuthService();

    @FXML
    private void initialize() {
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

        clearErrors();
    }

    // --- UI switching ---
    @FXML private void showRegister() { toggle(registerPane, loginPane); clearErrors(); }
    @FXML private void showLogin()    { toggle(loginPane, registerPane); clearErrors(); }

    private void toggle(Node show, Node hide) {
        if (show == null || hide == null) return; // if you only have one pane
        show.setVisible(true);  show.setManaged(true);
        hide.setVisible(false); hide.setManaged(false);
    }

    private void clearErrors() {
        if (loginErrorLabel != null) loginErrorLabel.setText("");
        if (regErrorLabel != null) regErrorLabel.setText("");
    }

    // --- LOGIN ---
    @FXML
    private void handleLogin() {
        String u = safe(loginUsernameField);
        String p = safe(loginPasswordField);

        if (!isUsernameValid(u) || p.isEmpty()) {
            showLoginError("Username (1–20 letters/digits) and password are required.");
            return;
        }

        if (auth.authenticate(u, p)) {
            Session.setCurrentUser(u);
            goToHome(loginPane != null ? loginPane : loginUsernameField);
        } else {
            showLoginError("Invalid credentials.");
        }
    }

    // --- REGISTER ---
    @FXML
    private void handleRegister() {
        if (regUsernameField == null || regPasswordField == null) {
            // If you don’t have a dedicated register pane, re-use login fields:
            String u = safe(loginUsernameField);
            String p = safe(loginPasswordField);
            doRegister(u, p, true); // true => show “Registered! Please log in.”
            return;
        }
        String u = safe(regUsernameField);
        String p = safe(regPasswordField);
        doRegister(u, p, false); // false => keep user on register pane; they can switch to Login
    }

    private void doRegister(String u, String p, boolean onLoginPane) {
        if (!isUsernameValid(u)) {
            showRegisterError("Username must be 1–20 letters or digits.");
            return;
        }
        if (!auth.isPasswordStrong(p)) {
            showRegisterError("Password must be at least 12 chars, include upper, lower, number, special.");
            return;
        }
        try {
            auth.register(u, p);
            if (onLoginPane) {
                showLoginInfo("Registered! Please log in.");
                if (loginPasswordField != null) loginPasswordField.clear();
            } else {
                showRegisterInfo("Registered! Switch to Login to sign in.");
                if (regPasswordField != null) regPasswordField.clear();
            }
        } catch (IllegalStateException dup) {
            showRegisterError("Username already exists.");
        } catch (IllegalArgumentException weak) {
            showRegisterError(weak.getMessage());
        } catch (Exception ex) {
            showRegisterError("Registration failed.");
        }
    }

    // --- helpers ---
    private String safe(TextInputControl c) {
        return c == null || c.getText() == null ? "" : c.getText().trim();
    }

    private boolean isUsernameValid(String u) {
        return u != null && u.matches("^[A-Za-z0-9]{1,20}$");
    }

    private void showLoginError(String msg)  { if (loginErrorLabel != null) loginErrorLabel.setText(msg); else alert(msg, Alert.AlertType.ERROR); }
    private void showLoginInfo(String msg)   { if (loginErrorLabel != null) loginErrorLabel.setText(msg); else alert(msg, Alert.AlertType.INFORMATION); }
    private void showRegisterError(String msg){ if (regErrorLabel != null) regErrorLabel.setText(msg); else alert(msg, Alert.AlertType.ERROR); }
    private void showRegisterInfo(String msg) { if (regErrorLabel != null) regErrorLabel.setText(msg); else alert(msg, Alert.AlertType.INFORMATION); }

    private void alert(String msg, Alert.AlertType type) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }

    private void goToHome(Node anyNodeInScene) {
        try {
            Stage stage = (Stage) anyNodeInScene.getScene().getWindow();
            Parent home = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("home-view.fxml")));
            Scene scene = anyNodeInScene.getScene();
            scene.setRoot(home);
            String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
            if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
            stage.setTitle("BugCo – Home");
        } catch (IOException e) {
            alert("Unable to load Home view.", Alert.AlertType.ERROR);
        }
    }
}