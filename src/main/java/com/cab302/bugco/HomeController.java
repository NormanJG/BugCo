package com.cab302.bugco;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;

public class HomeController {

    @FXML private ImageView imageView;
    @FXML private TextArea leaderboardArea;
    @FXML private TextArea terminalArea;
    @FXML private TextField terminalInput;

    private enum FlowStep {
        NONE,
        LOGIN_USER, LOGIN_PASS,
        REG_USER, REG_PASS, REG_CONFIRM
    }
    private FlowStep step = FlowStep.NONE;
    private String tmpUser = "";
    private String tmpPass = "";

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
        appendLine("C:\\USER\\ADMIN> INITIALISING HACKING PROTOCOLS...");
        appendLine("C:\\USER\\ADMIN> READY.");
        appendLine("C:\\USER\\ADMIN> ");
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
                appendLine("C:\\USER\\ADMIN> Logged in as " + tmpUser);
                endFlow();
            }
            case REG_USER -> {
                tmpUser = input.trim();
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
                appendLine("C:\\USER\\ADMIN> RE-ENTER PASSWORD");
                appendPrompt();
                terminalInput.clear();
                step = FlowStep.REG_CONFIRM;
            }
            case REG_CONFIRM -> {
                String confirm = input;
                appendLine("> " + mask(confirm.length()));
                appendLine("");
                if (!confirm.equals(tmpPass)) {
                    appendLine("C:\\USER\\ADMIN> ERROR: PASSWORDS DO NOT MATCH.");
                    appendLine("C:\\USER\\ADMIN> RE-ENTER PASSWORD");
                    appendPrompt();
                    terminalInput.clear();
                    step = FlowStep.REG_CONFIRM;
                    return;
                }
                appendLine("C:\\USER\\ADMIN> CREATING ACCOUNT...");
                appendLine("C:\\USER\\ADMIN> Registered user " + tmpUser);
                endFlow();
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