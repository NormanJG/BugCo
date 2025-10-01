package com.cab302.bugco;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.cab302.bugco.db.Database;
import com.cab302.bugco.Players;

public class HomeController {

    @FXML private TextArea terminalArea;
    @FXML private TextArea leaderboardArea;
    @FXML private Pane imagePane;
    @FXML private ImageView imageView;
    @FXML private Label welcomeLabel;
    @FXML private Slider volSlider;
    @FXML private CheckBox muteChk;

    private ObservableList<Players> players = FXCollections.observableArrayList();

    @FXML private TableView<Players> leaderboardTable;
    @FXML private TableColumn<Players, String> usernameColumn;
    @FXML private TableColumn<Players, String> achievementColumn;

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

        if (volSlider != null) {
            double v = MusicService.getVolume();
            volSlider.setValue(v > 0 ? v : 0.35);
            volSlider.valueProperty().addListener((obs, ov, nv) ->
                    MusicService.setVolume(nv.doubleValue()));
        }
        if (muteChk != null) {
            muteChk.setSelected(MusicService.isMute());
        }

        if (welcomeLabel != null) {
            String who = Session.isLoggedIn() ? Session.getCurrentUser() : "Guest";
            int theScore = Database.getPointsForUser(who);
            welcomeLabel.setText("Welcome, " + who + "  |  Points: " + theScore);
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

        // Setup TableView columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        achievementColumn.setCellValueFactory(new PropertyValueFactory<>("achievement"));

        leaderboardTable.setItems(players);
        loadPlayersFromDB();
    }

    @FXML
    private void onStart(ActionEvent event) {
        appendTerminal("Initialising hacking protocols... stand by.");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        try {
            String currentUser = Session.isLoggedIn() ? Session.getCurrentUser() : "Guest";
            BugcoTerminalApp app = new BugcoTerminalApp(currentUser);

            Parent terminalRoot = app.createContent();
            Scene scene = stage.getScene();

            scene.setRoot(terminalRoot);

            String css = Objects.requireNonNull(
                    getClass().getResource("/terminal-styles.css")
            ).toExternalForm();
            terminalRoot.getStylesheets().add(css);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendTerminal(String line) {
        terminalArea.appendText("\n" + line);
    }

    public void onGameInfo(ActionEvent actionEvent) {
        appendTerminal("Welcome to BugCo Industries, a Java-based desktop application designed to test and improve your debugging skills through interactive code challenges. Built for CAB302, this project simulates a fun and competitive environment where users identify and fix bugs in Java code snippets of varying difficulty levels.");
    }

    @FXML
    private void onLogout() {
        try {
            Session.logout();

            var url = getClass().getResource("/com/cab302/bugco/login-view.fxml");
            if (url == null) throw new IllegalStateException("login-view.fxml not found on classpath");

            var root = FXMLLoader.load(url);

            Scene scene = terminalArea.getScene();
            scene.setRoot((Parent) root);
            MusicService.playLogin();

            var css = getClass().getResource("/com/cab302/bugco/styles.css");
            if (css != null) {
                String cssUrl = css.toExternalForm();
                if (!scene.getStylesheets().contains(cssUrl))
                    scene.getStylesheets().add(cssUrl);
            }

            ((Stage) scene.getWindow()).setTitle("BugCo – Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void AccountInfo() {
        try {
            var url = getClass().getResource("/com/cab302/bugco/account-info.fxml");
            if (url == null) throw new IllegalStateException("account-info.fxml not found");
            Parent root = javafx.fxml.FXMLLoader.load(url);
            javafx.scene.Scene scene = terminalArea.getScene(); // or any @FXML node’s scene
            scene.setRoot(root);
            var css = getClass().getResource("/com/cab302/bugco/styles.css");
            if (css != null && !scene.getStylesheets().contains(css.toExternalForm()))
                scene.getStylesheets().add(css.toExternalForm());
            ((javafx.stage.Stage) scene.getWindow()).setTitle("BugCo – Account");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Players> getPlayers() { return new ArrayList<>(players); }
  
    public boolean isEmpty() { return players.isEmpty(); }

    public void addPlayer(String username, String achievement) {
        if (username == null || username.isEmpty()) throw new IllegalArgumentException();

        for (Players p : players) {
            if (p.getUsername().equals(username)) {
                p.setAchievement(achievement);
                return;
            }
        }
        players.add(new Players(username, achievement));
    }

    private void loadPlayersFromDB() {
        players.clear();
        List<Players> dbPlayers = Database.getAllPlayers();
        players.addAll(dbPlayers);
    }

    public void refreshLeaderboard() {
        loadPlayersFromDB();
        leaderboardTable.refresh();
    }

    public void updateAchievement(String username, String newAchievement) {
        for (Players p : players) {
            if (p.getUsername().equals(username)) {
                p.setAchievement(newAchievement);
                leaderboardTable.refresh();
                Database.updatePlayerAchievement(username, newAchievement);
                break;
            }
        }
    }

    // Audio Controls
    @FXML
    private void onTogglePlayPause() { MusicService.toggle(); }

    @FXML
    private void onToggleMute() {
        MusicService.setMute(muteChk != null && muteChk.isSelected());
    }

    // Username change feature — one clean method
    @FXML
    private void onChangeUsername() {
        try {
            String oldName = Session.isLoggedIn() ? Session.getCurrentUser() : null;
            if (oldName == null) {
                appendTerminal("You must login first to change username.");
                return;
            }
            TextInputDialog ask = new TextInputDialog(oldName);
            ask.setTitle("Change Username");
            ask.setHeaderText("Type your new username:");
            ask.setContentText("New username:");
            Optional<String> theUpdated = ask.showAndWait();
            if (theUpdated.isEmpty()) return;

            String newName = theUpdated.get().trim();
            if (!newName.matches("^[A-Za-z0-9]{1,20}$")) {
                appendTerminal("Username must be letters or numbers (max 20).");
                return;
            }

            com.cab302.bugco.db.UserDao dao = new com.cab302.bugco.db.UserDao();
            if (dao.updateUsernameByUsername(oldName, newName)) {
                Session.setCurrentUser(newName);
                int theScore = Database.getPointsForUser(newName);
                welcomeLabel.setText("Welcome, " + newName + "  |  Points: " + theScore);
                appendTerminal("Username changed: " + oldName + " -> " + newName);
                refreshLeaderboard();
            } else {
                appendTerminal("Could not change username.");
            }
        } catch (IllegalStateException dup) {
            appendTerminal("That username already exists. Try a different one.");
        } catch (Exception ex) {
            ex.printStackTrace();
            appendTerminal("Problem changing username.");
        }
    }
}
