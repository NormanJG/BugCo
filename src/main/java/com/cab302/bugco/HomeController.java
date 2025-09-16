package com.cab302.bugco;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HomeController {

    @FXML private TextArea terminalArea;
    @FXML private TextArea leaderboardArea;
    @FXML private ImageView imageView;
    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        Image img = new Image(getClass().getResource("image.png").toExternalForm());
        if (imageView != null) imageView.setImage(img);

        if (welcomeLabel != null) {
            String who = Session.isLoggedIn() ? Session.getCurrentUser() : "Guest";
            welcomeLabel.setText("Welcome, " + who);
        }

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
    }

    @FXML
    private void onStart() {
        appendTerminal("Initialising hacking protocols... stand by.");
        // TODO: navigate to game scene
    }

    private void appendTerminal(String line) {
        terminalArea.appendText("\n" + line);
    }
}