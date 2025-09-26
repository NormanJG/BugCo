package com.cab302.bugco;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import com.cab302.bugco.BugcoTerminalApp;

import java.util.Objects;

public class HomeController {

    @FXML private TextArea terminalArea;
    @FXML private TextArea leaderboardArea;
    @FXML private Pane imagePane;
    @FXML private ImageView imageView;
    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        Image img = new Image(Objects.requireNonNull(getClass().getResource("image.png")).toExternalForm());
        imageView.setImage(img);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setViewport(null);
        imageView.fitWidthProperty().bind(imagePane.widthProperty());
        imageView.setFitHeight(0);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imagePane.widthProperty());
        clip.heightProperty().bind(imagePane.heightProperty());
        imagePane.setClip(clip);

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
    private void onStart(ActionEvent event) {
        appendTerminal("Initialising hacking protocols... stand by.");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Launch BugcoTerminalApp in the same Stage
        BugcoTerminalApp app = new BugcoTerminalApp();
        try {
            app.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendTerminal(String line) {
        terminalArea.appendText("\n" + line);
    }

    public void onGameInfo(ActionEvent actionEvent) {
        appendTerminal("Find all the bugs... before they find you.");
        // TODO: navigate to game info
    }

    @FXML
    private void onLogout() {
        try {
            Session.logout();

            var url = getClass().getResource("/com/cab302/bugco/login-view.fxml");
            if (url == null) throw new IllegalStateException("login-view.fxml not found on classpath");

            var root = javafx.fxml.FXMLLoader.load(url);

            javafx.scene.Scene scene = terminalArea.getScene();
            scene.setRoot((Parent) root);

            var css = getClass().getResource("/com/cab302/bugco/styles.css");
            if (css != null) {
                String cssUrl = css.toExternalForm();
                if (!scene.getStylesheets().contains(cssUrl)) scene.getStylesheets().add(cssUrl);
            }

            ((javafx.stage.Stage) scene.getWindow()).setTitle("BugCo – Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}