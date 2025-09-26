package com.cab302.bugco;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;

import java.util.Objects;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;import com.cab302.bugco.db.Database;
import com.cab302.bugco.db.Database;

import com.cab302.bugco.Players;

public class HomeController {

    @FXML
    private TextArea terminalArea;
    @FXML
    private TextArea leaderboardArea;
    @FXML
    private Pane imagePane;
    @FXML
    private ImageView imageView;
    @FXML
    private Label welcomeLabel;

    private ObservableList<Players> players = FXCollections.observableArrayList();

    @FXML
    private ImageView logoImage;

    @FXML
    private TableView<Players> leaderboardTable;

    @FXML
    private TableColumn<Players, String> usernameColumn;

    @FXML
    private TableColumn<Players, String> achievementColumn;


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


        // Setup TableView columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        achievementColumn.setCellValueFactory(new PropertyValueFactory<>("achievement"));

        leaderboardTable.setItems(players);

    }


    @FXML
    private void onStart(ActionEvent event) {
        appendTerminal("Initialising hacking protocols... stand by.");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        try {
            // ✅ Pass the logged-in user into the constructor
            String currentUser = Session.isLoggedIn() ? Session.getCurrentUser() : "Guest";
            BugcoTerminalApp app = new BugcoTerminalApp(currentUser);

            Parent terminalRoot = app.createContent(); // ✅ no args here
            Scene scene = new Scene(terminalRoot, 1400, 900);

            // attach stylesheet
            String css = Objects.requireNonNull(
                    getClass().getResource("/terminal-styles.css")
            ).toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.show();
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

            var root = FXMLLoader.load(url);

            Scene scene = terminalArea.getScene();
            scene.setRoot((Parent) root);

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


    public List<Players> getPlayers() {
        return new ArrayList<>(players);
    }


    public boolean isEmpty() {
        return players.isEmpty();
    }


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

}


