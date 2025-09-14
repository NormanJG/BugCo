package com.cab302.bugco;

import com.cab302.bugco.auth.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.stage.Stage;

public class HomeController {

    @FXML private ImageView imageView;
    @FXML private TextArea leaderboardArea;
    @FXML private TextArea terminalArea;
    @FXML private TextField terminalInput;

    private final AuthService auth = new AuthService();

    private enum FlowStep {
        NONE,
        LOGIN_USER, LOGIN_PASS,
        REG_USER, REG_PASS, REG_CONFIRM
    }
    private FlowStep step = FlowStep.NONE;
    private String tmpUser = "";
    private String tmpPass = "";

    public boolean isUsernameValid(String u) {
        return u != null && u.matches("^[A-Za-z0-9]{1,20}$");
    }

    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 12) return false;
        if (!password.matches(".*[A-Z].*")) return false;
        if (!password.matches(".*[a-z].*")) return false;
        if (!password.matches(".*\\d.*")) return false;
        return password.matches(".*[^a-zA-Z0-9].*");
    }

    @FXML
    private void initialize() {
        imageView.setImage(new Image(getClass().getResource("image.png").toExternalForm()));

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

        terminalInput.setOnAction(e -> handleTerminalSubmit());
        showTerminalInput(false);
    }

    @FXML
    private void onLogin() {
        startLoginFlow();
    }

    @FXML
    private void onRegister() {
        startRegisterFlow();
    }

    @FXML
    private void onGameInfo() {
        appendLine("C:\\USER\\ADMIN> GAME INFO:");
        appendLine(" - Solve hacking challenges to raise your rank.");
        appendLine(" - Use logic, patterns, and a bit of luck.");
        appendLine("C:\\USER\\ADMIN> ");
    }

    @FXML
    private void onStart() {
        if (!AppState.ME.isLoggedIn()) {
            appendLine("C:\\USER\\ADMIN> INITIALISING HACKING PROTOCOLS...");
            appendLine("C:\\USER\\ADMIN> READY.");
            appendLine("C:\\USER\\ADMIN> ");
            return;
        }
        try {
            var stage = (Stage) terminalArea.getScene().getWindow();
            stage.setScene(SceneFactory.loadScene("gameplay-view.fxml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            appendLine("C:\\USER\\ADMIN> ERROR: Could not load gameplay-view.fxml");
            appendLine("C:\\USER\\ADMIN> ");
        }
    }

    private void startLoginFlow() {
        step = FlowStep.LOGIN_USER;
        tmpUser = "";
        tmpPass = "";
        showTerminalInput(true);
        terminalInput.clear();
        appendLine("C:\\USER\\ADMIN> INITIALISING LOGIN...");
        appendLine("");
        appendLine("C:\\USER\\ADMIN> ENTER USERNAME");
        appendPrompt();
        terminalInput.requestFocus();
    }

    private void startRegisterFlow() {
        step = FlowStep.REG_USER;
        tmpUser = "";
        tmpPass = "";
        showTerminalInput(true);
        terminalInput.clear();
        appendLine("C:\\USER\\ADMIN> INITIALISING REGISTRATION...");
        appendLine("");
        appendLine("C:\\USER\\ADMIN> ENTER USERNAME");
        appendPrompt();
        terminalInput.requestFocus();
    }

    private void handleTerminalSubmit() {
        String input = terminalInput.getText() == null ? "" : terminalInput.getText();

        switch (step) {
            case LOGIN_USER -> {
                tmpUser = input.trim();
                if (!isUsernameValid(tmpUser)) {
                    appendLine("> " + tmpUser);
                    appendLine("");
                    appendLine("C:\\USER\\ADMIN> ERROR: USERNAME MUST BE 1–20 LETTERS OR DIGITS (NO SPECIALS).");
                    appendLine("C:\\USER\\ADMIN> ENTER USERNAME");
                    appendPrompt();
                    terminalInput.clear();
                    step = FlowStep.LOGIN_USER;
                    return;
                }
                appendLine("> " + tmpUser);
                appendLine("");
                appendLine("C:\\USER\\ADMIN> ENTER PASSWORD");
                appendPrompt();
                terminalInput.clear();
                step = FlowStep.LOGIN_PASS;
            }

            case LOGIN_PASS -> {
                tmpPass = input;
                appendLine("> " + mask(tmpPass.length()));
                appendLine("");
                appendLine("C:\\USER\\ADMIN> AUTHENTICATING...");

                boolean ok = auth.authenticate(tmpUser, tmpPass);
                if (ok) {
                    appendLine("C:\\USER\\ADMIN> Logged in as " + tmpUser);
                    endFlow();
                } else {
                    appendLine("C:\\USER\\ADMIN> ERROR: INVALID CREDENTIALS.");
                    appendLine("C:\\USER\\ADMIN> ENTER USERNAME");
                    appendPrompt();
                    terminalInput.clear();
                    step = FlowStep.LOGIN_USER;
                }
            }

            case REG_USER -> {
                tmpUser = input.trim();
                if (!isUsernameValid(tmpUser)) {
                    appendLine("> " + tmpUser);
                    appendLine("");
                    appendLine("C:\\USER\\ADMIN> ERROR: USERNAME MUST BE 1–20 LETTERS OR DIGITS (NO SPECIALS).");
                    appendLine("C:\\USER\\ADMIN> ENTER USERNAME");
                    appendPrompt();
                    terminalInput.clear();
                    step = FlowStep.REG_USER;
                    return;
                }
                appendLine("> " + tmpUser);
                appendLine("");
                appendLine("C:\\USER\\ADMIN> ENTER PASSWORD");
                appendPrompt();
                terminalInput.clear();
                step = FlowStep.REG_PASS;
            }

            case REG_PASS -> {
                tmpPass = input;
                appendLine("> " + mask(tmpPass.length()));
                appendLine("");

                if (!auth.isPasswordStrong(tmpPass)) {
                    appendLine("C:\\USER\\ADMIN> ERROR: Password must be at least 12 characters, "
                            + "include upper & lower case, a number, and a special character.");
                    appendLine("C:\\USER\\ADMIN> ENTER PASSWORD");
                    appendPrompt();
                    terminalInput.clear();
                    step = FlowStep.REG_PASS;
                    return;
                }

                appendLine("C:\\USER\\ADMIN> RE-ENTER PASSWORD");
                appendPrompt();
                terminalInput.clear();
                step = FlowStep.REG_CONFIRM;
            }

            case REG_CONFIRM -> {
                appendLine("> " + mask(input.length()));
                appendLine("");
                if (!input.equals(tmpPass)) {
                    appendLine("C:\\USER\\ADMIN> ERROR: PASSWORDS DO NOT MATCH.");
                    appendLine("C:\\USER\\ADMIN> RE-ENTER PASSWORD");
                    appendPrompt();
                    terminalInput.clear();
                    step = FlowStep.REG_CONFIRM;
                    return;
                }
                try {
                    auth.register(tmpUser, tmpPass);
                    appendLine("C:\\USER\\ADMIN> CREATING ACCOUNT...");
                    appendLine("C:\\USER\\ADMIN> Registered user " + tmpUser);
                    endFlow();
                } catch (IllegalStateException dup) {
                    appendLine("C:\\USER\\ADMIN> ERROR: USERNAME ALREADY EXISTS.");
                    appendLine("C:\\USER\\ADMIN> ENTER USERNAME");
                    appendPrompt();
                    terminalInput.clear();
                    step = FlowStep.REG_USER;
                } catch (IllegalArgumentException weak) {
                    appendLine("C:\\USER\\ADMIN> ERROR: " + weak.getMessage());
                    appendLine("C:\\USER\\ADMIN> ENTER PASSWORD");
                    appendPrompt();
                    terminalInput.clear();
                    step = FlowStep.REG_PASS;
                }
            }

            default -> {}
        }
    }

    private void appendLine(String s) {
        String text = terminalArea.getText();
        if (!text.isEmpty() && !text.endsWith("\n")) {
            terminalArea.appendText("\n");
        }
        terminalArea.appendText(s + "\n");
    }

    private void appendPrompt() {
        terminalArea.appendText("> ");
    }

    private String mask(int n) {
        return "*".repeat(Math.max(0, n));
    }

    private void showTerminalInput(boolean show) {
        terminalInput.setVisible(show);
        terminalInput.setManaged(show);
        if (show) {
            Platform.runLater(terminalInput::requestFocus);
        }
    }

    private void endFlow() {
        step = FlowStep.NONE;
        terminalInput.clear();
        showTerminalInput(false);
        appendLine("C:\\USER\\ADMIN> ");
    }
}