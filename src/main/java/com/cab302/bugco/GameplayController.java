package com.cab302.bugco;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

// Controls the Gameplay screen.
public class GameplayController {

    private static final String LEVEL_TEXT = "Level: Easy";
    private static final String TRY_AGAIN_TEXT = "Try again.";
    private static final String WELL_DONE_TEXT = "Well done! +";

    // UI from FXML
    @FXML private Label titleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label levelLabel;
    @FXML private Label scoreLabel;
    @FXML private Label promptText;
    @FXML private Label feedbackText;
    @FXML private TextArea answerArea;

    // Current challenge
    private final GameService GAME = GameService.getInstance();
    private Challenge NOW_CHALLENGE;

    @FXML
    private void initialize() {
        // If not logged in, stay to home page
        if (!AppState.ME.isLoggedIn()) {
            goHome();
            return;
        }

        usernameLabel.setText("User: " + AppState.ME.userName);
        levelLabel.setText(LEVEL_TEXT);
        scoreLabel.setText("Score: " + GAME.getScore());

        NOW_CHALLENGE = GAME.getRandomByDifficulty(Difficulty.EASY);
        promptText.setText(NOW_CHALLENGE.prompt());

        answerArea.textProperty().addListener((o, a, b) -> feedbackText.setText(""));
        answerArea.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && e.isControlDown()) onSubmit();
        });
    }

    @FXML
    private void onSubmit() {
        final String YOUR_ANSWER = answerArea.getText().trim();
        final boolean POINTS_OK = GAME.checkAnswer(NOW_CHALLENGE, YOUR_ANSWER);

        scoreLabel.setText("Score: " + GAME.getScore());
        feedbackText.getStyleClass().removeAll("ok", "err");

        if (POINTS_OK) {
            feedbackText.setText(WELL_DONE_TEXT + NOW_CHALLENGE.basePoints() + " points");
            feedbackText.getStyleClass().add("ok");

        } else {
            feedbackText.setText(TRY_AGAIN_TEXT);
            feedbackText.getStyleClass().add("err");
        }
    }

    @FXML
    private void onBack() { goHome(); }

    private void goHome() {
        try {
            Stage stage = (Stage) (titleLabel != null ? titleLabel.getScene().getWindow()
                    : answerArea.getScene().getWindow());
            stage.setScene(SceneFactory.loadScene("home-view.fxml"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
