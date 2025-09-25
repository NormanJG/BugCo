package com.cab302.bugco;

import com.cab302.bugco.db.UserDao;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Optional;

public class GameplayController {

    // LEFT SIDE UI
    @FXML private TextField usernameField;
    @FXML private ChoiceBox<String> difficultyBox;
    @FXML private ImageView logoView;

    // INFO FROM DATABASE
    @FXML private Label userSinceLabel;
    @FXML private Label totalUsersLabel;

    // RIGHT SIDE UI
    @FXML private Label promptText;
    @FXML private TextArea buggyArea;
    @FXML private TextArea answerArea;
    @FXML private Label feedbackText;
    @FXML private Label pointsLabel;

    // BADGES
    @FXML private Label questionBadge;
    @FXML private Label levelBadge;

    // SUBMIT BTN
    @FXML private Button submit;

    // STATE
    private int myNowQ = 1;
    private String myDifficulty = "Easy";

    // SERVICES
    private final GameService game = GameService.getInstance();
    private final UserDao userDao = new UserDao();

    // helpers
    private String whoNow() {
        if (Session.isLoggedIn()) return Session.getCurrentUser();
        return AppState.ME.userName;
    }
    private void setWhoNow(String theName) {
        Session.setCurrentUser(theName);
        AppState.ME.loginAs(theName);
    }

    // INIT
    @FXML
    private void initialize() {
        game.seedEasyIfMissing();

        usernameField.setText(whoNow());
        usernameField.setEditable(false);

        difficultyBox.getItems().setAll("Easy", "Medium (soon)", "Hard (soon)");
        difficultyBox.getSelectionModel().select(0);
        difficultyBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldIx, newIx) -> {
            int ix = newIx == null ? 0 : newIx.intValue();
            if (ix != 0) {
                feedbackText.setText("MEDIUM / HARD COMING SOON.");
                difficultyBox.getSelectionModel().select(0);
                return;
            }
            myDifficulty = "Easy";
            loadQuestion(myNowQ);
        });

        var theUrl = getClass().getResource("image.png");
        if (theUrl != null) logoView.setImage(new Image(theUrl.toExternalForm()));

        refreshProfileInfo();
        loadQuestion(1);
        updatePoints();
    }

    // small helpers
    private void refreshProfileInfo() {
        String theUser = whoNow();
        Optional<UserDao.UserRow> row = userDao.findByUsername(theUser);
        userSinceLabel.setText("JOINED: " + row.map(UserDao.UserRow::createdAt).orElse("-"));
        totalUsersLabel.setText("USERS: " + userDao.countUsers());
    }

    private void updatePoints() {
        pointsLabel.setText("POINTS: " + game.getTotalPoints(whoNow()));
    }

    private void lockQuestion() {
        answerArea.setEditable(false);
        submit.setDisable(true);
    }
    private void unlockQuestion() {
        answerArea.setEditable(true);
        submit.setDisable(false);
    }

    private void loadQuestion(int q) {
        myNowQ = q;

        questionBadge.setText("Question " + q);
        levelBadge.setText("Easy");

        Challenge ch = game.getChallenge(Difficulty.EASY, q);
        if (ch == null) {
            promptText.setText("NO CHALLENGE FOR EASY #" + q);
            buggyArea.clear();
            answerArea.clear();
            feedbackText.setText("");
            unlockQuestion();
            return;
        }

        promptText.setText(ch.title() + "\n" + ch.prompt());
        buggyArea.setText(ch.buggyCode());
        answerArea.clear();
        feedbackText.setText("");

        // check if already solved before : lock if solved
        if (game.hasSolvedBefore(whoNow(), ch)) {
            feedbackText.setText("ALREADY SOLVED. RESET POINTS TO TRY AGAIN.");
            lockQuestion();
        } else {
            unlockQuestion();
        }
    }

    // actions
    @FXML
    private void onSubmit() {
        Challenge ch = game.getChallenge(Difficulty.EASY, myNowQ);
        if (ch == null) return;

        String fb = game.submitAndMark(whoNow(), ch, answerArea.getText());
        feedbackText.setText(fb);
        updatePoints();

        // if correct, lock so no more submissions
        if (fb.startsWith("✅ CORRECT") || fb.startsWith("ALREADY SOLVED")) {
            lockQuestion();
        }
    }

    @FXML
    private void onResetPoints() {
        String nameNow = whoNow();

        Alert ask = new Alert(Alert.AlertType.CONFIRMATION);
        ask.setTitle("RESET POINTS");
        ask.setHeaderText(null);
        ask.setContentText("RESET YOUR EASY POINTS AND CLEAR SUBMISSIONS?");
        ask.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.OK);

        ask.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    game.resetPoints(nameNow);
                    updatePoints();
                    feedbackText.setText("POINTS RESET TO 0.");
                    unlockQuestion(); // re-enable answering
                    loadQuestion(myNowQ);
                } catch (Exception ex) {
                    feedbackText.setText("ERROR: " + ex.getMessage());
                }
            }
        });
    }

    @FXML
    private void onRenameMe() {
        TextInputDialog dlg = new TextInputDialog(whoNow());
        dlg.setTitle("RENAME");
        dlg.setHeaderText("CHANGE YOUR USERNAME");
        dlg.setContentText("NEW NAME:");

        dlg.showAndWait().ifPresent(theUpdate -> {
            String CLEAN = theUpdate.trim();
            if (CLEAN.isEmpty()) { feedbackText.setText("PLEASE TYPE NEW NAME."); return; }

            var me = userDao.findByUsername(whoNow());
            if (me.isEmpty()) { feedbackText.setText("CANNOT FIND YOU IN DATABASE."); return; }

            try {
                boolean ok = userDao.updateUsername(me.get().id(), CLEAN);
                if (ok) {
                    setWhoNow(CLEAN);
                    usernameField.setText(CLEAN);
                    feedbackText.setText("NAME UPDATED.");
                    refreshProfileInfo();
                    updatePoints();
                } else {
                    feedbackText.setText("NAME NOT UPDATED.");
                }
            } catch (IllegalStateException dup) {
                feedbackText.setText("NAME ALREADY EXISTS.");
            } catch (Exception ex) {
                feedbackText.setText("ERROR: " + ex.getMessage());
            }
        });
    }

    @FXML
    private void onGoHome() {
        try {
            var THE_URL = getClass().getResource("/com/cab302/bugco/home-view.fxml");
            if (THE_URL == null) throw new IllegalStateException("home-view.fxml not found");

            Parent root = FXMLLoader.load(THE_URL);
            Scene sc = promptText.getScene();

            var CSS_URL = getClass().getResource("/com/cab302/bugco/styles.css");
            if (CSS_URL != null) sc.getStylesheets().setAll(CSS_URL.toExternalForm());

            root.setOpacity(0.0);
            sc.setRoot(root);

            FadeTransition fx = new FadeTransition(Duration.millis(220), root);
            fx.setFromValue(0.0);
            fx.setToValue(1.0);
            fx.play();

            ((javafx.stage.Stage) sc.getWindow()).setTitle("BugCo – Home");
        } catch (Exception e) {
            feedbackText.setText("CANNOT OPEN HOME.");
        }
    }

    // question buttons
    @FXML private void onQ1() { loadQuestion(1); }
    @FXML private void onQ2() { loadQuestion(2); }
    @FXML private void onQ3() { loadQuestion(3); }
    @FXML private void onQ4() { loadQuestion(4); }
    @FXML private void onQ5() { loadQuestion(5); }
    @FXML private void onQ6() { loadQuestion(6); }
    @FXML private void onQ7() { loadQuestion(7); }
    @FXML private void onQ8() { loadQuestion(8); }
    @FXML private void onQ9() { loadQuestion(9); }
}
