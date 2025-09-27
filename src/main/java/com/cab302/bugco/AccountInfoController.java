package com.cab302.bugco;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import com.cab302.bugco.auth.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AccountInfoController {

    @FXML private Label statusLabel;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private PasswordField deleteConfirmPasswordField;

    @FXML private Alert themedAlert;

    private final AuthService auth = new AuthService();

    @FXML
    private void initialize() {
        if (statusLabel != null) statusLabel.setText("");
    }

    private Alert themedAlert(Alert.AlertType type, String content) {
        Alert a = new Alert(type, content, ButtonType.OK, ButtonType.CANCEL);
        var dp = a.getDialogPane();
        var css = getClass().getResource("/com/cab302/bugco/styles.css");
        if (css != null && !dp.getStylesheets().contains(css.toExternalForm()))
            dp.getStylesheets().add(css.toExternalForm());
        dp.getStyleClass().add("bugco-dialog");
        return a;
    }

    @FXML
    private void handleChangePassword() {
        String user = Session.getCurrentUser();
        if (user == null) { show("Not logged in."); return; }

        String current = val(currentPasswordField);
        String next    = val(newPasswordField);
        String confirm = val(confirmPasswordField);

        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
            show("All password fields are required.");
            return;
        }
        if (!next.equals(confirm)) {
            show("New passwords do not match.");
            return;
        }
        if (!auth.isPasswordStrong(next)) {
            show("Password too simple (12+ chars, upper, lower, digit, special).");
            return;
        }
        // verify current password by authenticating
        if (!auth.authenticate(user, current)) {
            show("Current password is incorrect.");
            return;
        }

        try {
            auth.changePassword(user, next); // method added below
            show("Password updated.");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (Exception e) {
            show("Failed to change password.");
        }
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        String user = Session.getCurrentUser();
        if (user == null) { show("Not logged in."); return; }

        String pwd = val(deleteConfirmPasswordField);
        if (pwd.isEmpty()) { show("Password required."); return; }

        if (!auth.authenticate(user, pwd)) { show("Invalid password."); return; }

        try {
            auth.deleteAccount(user);
            Session.logout();

            var loginFxml = getClass().getResource("/com/cab302/bugco/login-view.fxml");
            if (loginFxml == null) throw new IllegalStateException("login-view.fxml not found");

            Parent login = FXMLLoader.load(loginFxml);

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(login);

            var css = getClass().getResource("/com/cab302/bugco/styles.css");
            if (css != null) {
                String url = css.toExternalForm();
                if (!scene.getStylesheets().contains(url)) scene.getStylesheets().add(url);
            }

            ((Stage) scene.getWindow()).setTitle("BugCo – Login");
        } catch (Exception ex) {
            ex.printStackTrace();
            show("Failed to delete account.");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            var fxml = getClass().getResource("/com/cab302/bugco/home-view.fxml");
            if (fxml == null) throw new IllegalStateException("home-view.fxml not found");

            Parent home = FXMLLoader.load(fxml);

            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(home);

            var css = getClass().getResource("/com/cab302/bugco/styles.css");
            if (css != null) {
                String url = css.toExternalForm();
                if (!scene.getStylesheets().contains(url)) scene.getStylesheets().add(url);
            }

            ((Stage) scene.getWindow()).setTitle("BugCo – Home");
        } catch (Exception ex) {
            ex.printStackTrace();
            show("Failed to return to Home.");
        }
    }

    private String val(TextInputControl c) {
        return (c == null || c.getText() == null) ? "" : c.getText().trim();
    }

    private void show(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
}
