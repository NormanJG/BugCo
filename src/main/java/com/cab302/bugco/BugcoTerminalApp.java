package com.cab302.bugco;

import com.cab302.bugco.db.Database;
import com.cab302.bugco.db.ProgressDAO;
import javafx.animation.PauseTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.InputStream;
import java.util.*;

public class BugcoTerminalApp {

    // Passed in from the main app
    private final String username;

    // Game session (logic + state)
    private final GameSession gameSession;

    // State bindings
    private final IntegerProperty selectedQuestion = new SimpleIntegerProperty(1);
    private final StringProperty difficulty = new SimpleStringProperty("Easy");
    private final StringProperty codeAnswer = new SimpleStringProperty("");

    // UI references
    private TextArea codeAnswerArea;
    private Label questionTitleLabel;
    private Label difficultyBadgeLabel;
    private Label questionDescriptionLabel;
    private List<ToggleButton> questionButtons;
    private Label hintLabel;
    private Button submitButton;

    // Sidebar reference
    private VBox sidebarRef;

    public BugcoTerminalApp(String username) {
        this.username = username;
        this.gameSession = new GameSession(username, QuestionRepository.loadQuestions());
    }

    // Entry point for HomeController
    public Parent createContent() {
        VBox root = new VBox();
        root.getStyleClass().add("root-terminal");

        HBox header = createHeader();
        HBox mainContent = new HBox();
        VBox sidebar = createSidebar();
        VBox contentPanel = createContentPanel();

        mainContent.getChildren().addAll(sidebar, contentPanel);
        HBox.setHgrow(contentPanel, Priority.ALWAYS);

        root.getChildren().addAll(header, mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        initializeBindings();
        rebuildSidebar();
        selectedQuestion.set(1);
        updateQuestionDisplay();

        return root;
    }

    // ----------------- Header -----------------
    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("banner-panel");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setSpacing(20);

        HBox leftSection = new HBox(10);
        leftSection.setAlignment(Pos.CENTER_LEFT);

        HBox dots = new HBox(3);
        Circle redDot = new Circle(6);   redDot.getStyleClass().add("dot-red");
        Circle amberDot = new Circle(6); amberDot.getStyleClass().add("dot-amber");
        Circle greenDot = new Circle(6); greenDot.getStyleClass().add("dot-green");
        dots.getChildren().addAll(redDot, amberDot, greenDot);

        Label title = new Label("BUGCO INDUSTRIES™ - CODE FIXING TERMINAL");
        title.getStyleClass().add("banner-title");

        leftSection.getChildren().addAll(dots, title);

        Label version = new Label("Version 1.0.2 | Security Protocol Activated");
        version.getStyleClass().add("banner-sub");

        header.getChildren().addAll(leftSection, version);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        return header;
    }

    // ----------------- Sidebar -----------------
    private VBox createSidebar() {
        sidebarRef = new VBox();
        sidebarRef.getStyleClass().add("terminal-panel");
        sidebarRef.setPrefWidth(320);

        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("panel-titlebar");
        titleBar.setPadding(new Insets(8, 10, 8, 10));
        Label titleLabel = new Label("CONTROL PANEL");
        titleLabel.getStyleClass().add("panel-title");
        titleBar.getChildren().add(titleLabel);

        VBox body = new VBox(24);
        body.getStyleClass().add("panel-body");
        body.setPadding(new Insets(12));

        // Username
        VBox usernameSection = new VBox(8);
        Label usernameLabel = new Label("USERNAME");
        usernameLabel.getStyleClass().add("terminal-label");

        VBox usernameDisplay = new VBox();
        usernameDisplay.getStyleClass().add("terminal-panel");
        usernameDisplay.setPadding(new Insets(12));
        usernameDisplay.setAlignment(Pos.CENTER_LEFT);

        Label usernameText = new Label(username);
        usernameText.getStyleClass().add("terminal-label");
        usernameDisplay.getChildren().add(usernameText);
        usernameSection.getChildren().addAll(usernameLabel, usernameDisplay);

        // Logo
        VBox logoSection = new VBox(8);
        Label logoLabel = new Label("LOGO");
        logoLabel.getStyleClass().add("terminal-label");

        VBox logoContainer = new VBox();
        logoContainer.getStyleClass().add("terminal-panel");
        logoContainer.setPadding(new Insets(16));
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPrefHeight(200);

        try (InputStream logoStream = getClass().getResourceAsStream("/com/cab302/bugco/image.png")) {
            if (logoStream != null) {
                Image logoImage = new Image(logoStream);
                ImageView logoImageView = new ImageView(logoImage);
                logoImageView.setFitWidth(150);
                logoImageView.setFitHeight(150);
                logoImageView.setPreserveRatio(true);
                logoContainer.getChildren().add(logoImageView);
            } else {
                logoContainer.getChildren().add(new Label("BUGS POSTER"));
            }
        } catch (Exception e) {
            logoContainer.getChildren().add(new Label("BUGS POSTER"));
        }

        logoSection.getChildren().addAll(logoLabel, logoContainer);

        // Questions placeholder
        VBox questionsSection = new VBox(8);
        Label questionsLabel = new Label("QUESTIONS:");
        questionsLabel.getStyleClass().add("terminal-label");
        questionsSection.getChildren().add(questionsLabel);
        questionsSection.setId("questions-section");

        VBox questionsContainer = new VBox(12);
        questionsContainer.getChildren().addAll(questionsSection);

        Button resetButton = new Button("Reset Questions");
        resetButton.getStyleClass().add("danger-btn");
        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setOnAction(e -> {
            gameSession.resetProgress(difficulty.get());
            rebuildSidebar();
            selectedQuestion.set(1);
            updateQuestionDisplay();
        });

        VBox resetSection = new VBox();
        resetSection.setPadding(new Insets(12, 0, 0, 0));
        resetSection.getChildren().add(resetButton);

        body.getChildren().addAll(usernameSection, logoSection, questionsContainer, resetSection);
        sidebarRef.getChildren().addAll(titleBar, body);
        return sidebarRef;
    }

    // ----------------- Content Panel -----------------
    private VBox createContentPanel() {
        VBox contentPanel = new VBox();
        contentPanel.getStyleClass().add("terminal-panel");

        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("panel-titlebar");
        titleBar.setPadding(new Insets(8, 10, 8, 10));

        Label titleLabel = new Label("DEBUGGING INTERFACE");
        titleLabel.getStyleClass().add("panel-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.getStyleClass().add("primary-btn");
        mainMenuButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/bugco/home-view.fxml"));
                Parent homeRoot = loader.load();

                HomeController controller = loader.getController();
                controller.refreshLeaderboard();

                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                Scene scene = stage.getScene();

                try {
                    String cssPath = getClass().getResource("/com/cab302/bugco/styles.css").toExternalForm();
                    scene.getStylesheets().add(cssPath);
                } catch (Exception cssEx) {
                    System.out.println("Warning: could not load main menu CSS - " + cssEx.getMessage());
                }

                scene.setRoot(homeRoot);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        titleBar.getChildren().addAll(titleLabel, spacer, mainMenuButton);

        VBox body = new VBox(24);
        body.getStyleClass().add("panel-body");
        body.setPadding(new Insets(12));

        VBox difficultySection = new VBox(8);
        Label difficultyLabel = new Label("DIFFICULTY (Easy/Medium/Hard)");
        difficultyLabel.getStyleClass().add("terminal-label");

        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getStyleClass().add("terminal-input");
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard");
        difficultyCombo.setValue("Easy");
        difficultyCombo.valueProperty().bindBidirectional(difficulty);

        difficultySection.getChildren().addAll(difficultyLabel, difficultyCombo);

        VBox questionArea = createQuestionArea();
        VBox codeSection = createCodeSection();

        body.getChildren().addAll(difficultySection, questionArea, codeSection);
        VBox.setVgrow(codeSection, Priority.ALWAYS);

        contentPanel.getChildren().addAll(titleBar, body);
        return contentPanel;
    }

    // ----------------- Question Area -----------------
    private VBox createQuestionArea() {
        VBox questionArea = new VBox(16);

        HBox questionHeader = new HBox(8);
        questionHeader.setAlignment(Pos.CENTER_LEFT);
        Label questionLabel = new Label("QUESTION");
        questionLabel.getStyleClass().add("terminal-label");
        questionHeader.getChildren().add(questionLabel);

        VBox questionContent = new VBox(12);
        questionContent.getStyleClass().add("terminal-panel");
        questionContent.setPadding(new Insets(16));

        HBox badgeBox = new HBox(8);
        Label questionBadge = new Label();
        questionBadge.getStyleClass().add("question-badge");
        questionBadge.textProperty().bind(selectedQuestion.asString("Question %d"));

        difficultyBadgeLabel = new Label();
        difficultyBadgeLabel.getStyleClass().add("question-badge");

        Button hintButton = new Button("?");
        hintButton.getStyleClass().add("hint-button");

        hintLabel = new Label();
        hintLabel.getStyleClass().add("hint-terminal");
        hintLabel.setVisible(false);

        hintButton.setOnAction(e -> {
            Question current = gameSession.getQuestion(difficulty.get(), selectedQuestion.get());
            if (current != null) {
                if (!hintLabel.isVisible()) {
                    hintLabel.setText(current.getHint());
                    hintLabel.setVisible(true);
                } else {
                    hintLabel.setVisible(false);
                }
            }
        });

        badgeBox.getChildren().addAll(questionBadge, difficultyBadgeLabel, hintButton, hintLabel);

        questionTitleLabel = new Label();
        questionTitleLabel.getStyleClass().add("terminal-label");

        questionDescriptionLabel = new Label();
        questionDescriptionLabel.getStyleClass().add("terminal-description");
        questionDescriptionLabel.setWrapText(true);

        questionContent.getChildren().addAll(badgeBox, questionTitleLabel, questionDescriptionLabel);
        questionArea.getChildren().addAll(questionHeader, questionContent);

        return questionArea;
    }

    // ----------------- Code Section -----------------
    private VBox createCodeSection() {
        VBox codeSection = new VBox(16);

        Label codeLabel = new Label("REWRITE CODE WITHOUT BUGS");
        codeLabel.getStyleClass().add("terminal-label");

        VBox codeContent = new VBox(16);
        codeContent.getStyleClass().add("terminal-panel");
        codeContent.setPadding(new Insets(16));

        codeAnswerArea = new TextArea();
        codeAnswerArea.getStyleClass().add("terminal-area");
        codeAnswerArea.setPrefHeight(150);
        codeAnswerArea.setStyle(
                "-fx-control-inner-background: #003300;" +
                        "-fx-text-fill: #39ff14;" +
                        "-fx-highlight-fill: #39ff14;" +
                        "-fx-highlight-text-fill: #003300;" +
                        "-fx-font-family: 'Consolas', monospace;"
        );
        VBox.setVgrow(codeAnswerArea, Priority.ALWAYS);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        submitButton = new Button("Submit");
        submitButton.getStyleClass().add("primary-btn");
        submitButton.setOnAction(e -> handleSubmit());
        buttonBox.getChildren().add(submitButton);

        codeContent.getChildren().addAll(codeAnswerArea, buttonBox);
        codeSection.getChildren().addAll(codeLabel, codeContent);

        return codeSection;
    }

    // ----------------- Bindings -----------------
    private void initializeBindings() {
        selectedQuestion.addListener((obs, o, n) -> updateQuestionDisplay());

        difficulty.addListener((obs, o, n) -> {
            // hard reset transient UI state when switching difficulty
            if (hintLabel != null) hintLabel.setVisible(false);
            if (codeAnswerArea != null) {
                codeAnswerArea.clear();
                codeAnswerArea.setEditable(true);
            }
            if (submitButton != null) submitButton.setText("Submit");

            selectedQuestion.set(1);  // set selection first
            rebuildSidebar();         // then rebuild sidebar for the new difficulty
            updateQuestionDisplay();  // and refresh the main panel
        });
    }

    // ----------------- Sidebar Refresh -----------------
    private void rebuildSidebar() {
        if (sidebarRef == null) return;

        VBox body = (VBox) sidebarRef.getChildren().get(1);
        VBox questionsSection = (VBox) body.lookup("#questions-section");

        if (questionsSection.getChildren().size() > 1) {
            questionsSection.getChildren().remove(1);
        }

        GridPane questionsGrid = new GridPane();
        questionsGrid.setHgap(8);
        questionsGrid.setVgap(8);

        ToggleGroup questionGroup = new ToggleGroup();
        questionButtons = new ArrayList<>();

        List<Question> currentSet = gameSession.getQuestionsByDifficulty(difficulty.get());
        Set<Integer> solvedSet = gameSession.getSolvedQuestions(difficulty.get());

        for (int i = 0; i < currentSet.size(); i++) {
            Question q = currentSet.get(i);
            ToggleButton btn = new ToggleButton("Q" + q.getId());
            btn.getStyleClass().add("question-btn");
            btn.setToggleGroup(questionGroup);

            final int questionId = q.getId();
            btn.setOnAction(e -> selectedQuestion.set(questionId));

            if (solvedSet.contains(q.getId()) && !btn.getStyleClass().contains("correct")) {
                btn.getStyleClass().add("correct");
            }

            questionButtons.add(btn);
            questionsGrid.add(btn, i % 3, i / 3);

            if (i == 0) btn.setSelected(true);
        }

        questionsSection.getChildren().add(questionsGrid);
    }

    // ----------------- Question Display -----------------
    private void updateQuestionDisplay() {
        Question current = gameSession.getQuestion(difficulty.get(), selectedQuestion.get());
        if (current == null) return;

        questionTitleLabel.setText(current.getTitle());
        questionDescriptionLabel.setText(current.getDescription());
        difficultyBadgeLabel.setText(current.getDifficulty());

        for (ToggleButton btn : questionButtons) {
            if (btn.getText().equals("Q" + current.getId())) {
                btn.setSelected(true);
            }
        }

        if (gameSession.getSolvedQuestions(difficulty.get()).contains(current.getId())) {
            codeAnswerArea.setEditable(false);
            codeAnswerArea.setText(gameSession.getSolvedAnswers(difficulty.get()).get(current.getId()));
        } else {
            codeAnswerArea.setEditable(true);
            codeAnswerArea.clear();
            codeAnswerArea.setPromptText("Write your bug-free code here...");
        }

        if (hintLabel != null) hintLabel.setVisible(false);
    }

    // ----------------- Submission -----------------
    private void handleSubmit() {
        Question current = gameSession.getQuestion(difficulty.get(), selectedQuestion.get());
        if (current == null) return;

        String inputRaw = codeAnswerArea.getText();
        String input = inputRaw == null ? "" : inputRaw.trim();
        PauseTransition pause = new PauseTransition(Duration.seconds(1));


        boolean isCorrect = gameSession.checkAnswer(difficulty.get(), current.getId(), input);

        if (isCorrect) {
            // Persist progress in DB
            ProgressDAO.saveProgress(username, current.getDifficulty(), current.getId(), input);

            // Leaderboard: overwrite the user's latest achievement
            String achievementText = "Solved Q" + current.getId() + " in " + current.getDifficulty();
            Database.updatePlayerAchievement(username, achievementText);

            // Mark the sidebar button in green
            for (ToggleButton btn : questionButtons) {
                if (btn.getText().equals("Q" + current.getId())) {
                    if (!btn.getStyleClass().contains("correct")) btn.getStyleClass().add("correct");
                    break;
                }
            }

            // Show success feedback
            submitButton.setText("Correct");
            pause.setOnFinished(e -> submitButton.setText("Submit"));
            pause.play();

            // Auto-advance to next question within THIS difficulty
            List<Question> currentSet = gameSession.getQuestionsByDifficulty(current.getDifficulty());
            int currentIndex = -1;
            for (int i = 0; i < currentSet.size(); i++) {
                if (currentSet.get(i).getId() == current.getId()) {
                    currentIndex = i;
                    break;
                }
            }
            int nextIndex = (currentIndex >= 0 ? (currentIndex + 1) % currentSet.size() : 0);
            selectedQuestion.set(currentSet.get(nextIndex).getId());

        } else {
            // Failure feedback
            submitButton.setText("Try Again");
            pause.setOnFinished(e -> submitButton.setText("Submit"));
            pause.play();
        }
    }


}
