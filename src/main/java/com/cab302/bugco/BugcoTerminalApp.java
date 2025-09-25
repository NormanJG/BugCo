package com.cab302.bugco;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;

import java.io.InputStream;
import java.util.*;

public class BugcoTerminalApp extends Application {

    // State variables using JavaFX Properties
    private final IntegerProperty selectedQuestion = new SimpleIntegerProperty(1);
    private final StringProperty difficulty = new SimpleStringProperty("Easy");
    private final StringProperty codeAnswer = new SimpleStringProperty("");

    // UI Components
    private TextArea codeAnswerArea;
    private Label questionTitleLabel;
    private Label difficultyBadgeLabel;
    private ComboBox<String> difficultyCombo;
    private List<ToggleButton> questionButtons;
    private Label hintLabel; // inline hint terminal
    private Question currentQuestion; // track current question
    private Button submitButton; // need reference for dynamic text changes

    // Data
    private Map<String, List<Question>> questionsByDifficulty;
    private final Map<String, Set<Integer>> solvedQuestionsByDifficulty = new HashMap<>();
    private final Map<String, Map<Integer, String>> solvedAnswersByDifficulty = new HashMap<>();

    // Question model class
    public static class Question {
        private final int id;
        private final String title;
        private final String difficulty;

        public Question(int id, String title, String difficulty) {
            this.id = id;
            this.title = title;
            this.difficulty = difficulty;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDifficulty() { return difficulty; }
    }

    @Override
    public void start(Stage primaryStage) {
        initializeData();

        VBox root = new VBox();
        root.getStyleClass().add("root-terminal");

        // Header Banner
        HBox header = createHeader();

        // Main content area
        HBox mainContent = new HBox();

        // Left sidebar
        VBox sidebar = createSidebar();

        // Main content panel
        VBox contentPanel = createContentPanel();

        mainContent.getChildren().addAll(sidebar, contentPanel);
        HBox.setHgrow(contentPanel, Priority.ALWAYS);

        root.getChildren().addAll(header, mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        Scene scene = new Scene(root, 1400, 900);

        // Load CSS
        try {
            String cssPath = getClass().getResource("/terminal-styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Warning: Could not load terminal-styles.css - " + e.getMessage());
        }

        primaryStage.setTitle("BUGCO INDUSTRIES™ - CODE FIXING TERMINAL");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();

        // Initialize UI state and bind properties
        initializeBindings();
        rebuildSidebar();
        updateQuestionDisplay();
    }

    private void initializeData() {
        questionsByDifficulty = new HashMap<>();

        for (String diff : List.of("Easy", "Medium", "Hard")) {
            List<Question> set = new ArrayList<>();
            for (int i = 1; i <= 9; i++) {
                set.add(new Question(i, diff + " Question " + i, diff));
            }
            questionsByDifficulty.put(diff, set);

            // Initialize solved maps per difficulty
            solvedQuestionsByDifficulty.put(diff, new HashSet<>());
            solvedAnswersByDifficulty.put(diff, new HashMap<>());
        }

        questionButtons = new ArrayList<>();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("banner-panel");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setSpacing(20);

        HBox leftSection = new HBox();
        leftSection.setAlignment(Pos.CENTER_LEFT);
        leftSection.setSpacing(10);

        HBox dots = new HBox();
        dots.setSpacing(3);
        Circle redDot = new Circle(6);
        redDot.getStyleClass().add("dot-red");
        Circle amberDot = new Circle(6);
        amberDot.getStyleClass().add("dot-amber");
        Circle greenDot = new Circle(6);
        greenDot.getStyleClass().add("dot-green");
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

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("terminal-panel");
        sidebar.setPrefWidth(320);

        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("panel-titlebar");
        titleBar.setPadding(new Insets(8, 10, 8, 10));
        Label titleLabel = new Label("CONTROL PANEL");
        titleLabel.getStyleClass().add("panel-title");
        titleBar.getChildren().add(titleLabel);

        VBox body = new VBox();
        body.getStyleClass().add("panel-body");
        body.setPadding(new Insets(12));
        body.setSpacing(24);

        VBox usernameSection = new VBox();
        usernameSection.setSpacing(8);
        Label usernameLabel = new Label("USERNAME");
        usernameLabel.getStyleClass().add("terminal-label");

        VBox usernameDisplay = new VBox();
        usernameDisplay.getStyleClass().add("terminal-panel");
        usernameDisplay.setPadding(new Insets(12));
        usernameDisplay.setAlignment(Pos.CENTER_LEFT);

        Label usernameText = new Label("Hanan");
        usernameText.getStyleClass().add("terminal-label");
        usernameDisplay.getChildren().add(usernameText);

        usernameSection.getChildren().addAll(usernameLabel, usernameDisplay);

        VBox logoSection = new VBox();
        logoSection.setSpacing(8);
        Label logoLabel = new Label("LOGO");
        logoLabel.getStyleClass().add("terminal-label");

        VBox logoContainer = new VBox();
        logoContainer.getStyleClass().add("terminal-panel");
        logoContainer.setPadding(new Insets(16));
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPrefHeight(200);

        try {
            InputStream logoStream = getClass().getResourceAsStream("/com/cab302/bugco/image.png");
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

        VBox questionsSection = new VBox();
        questionsSection.setSpacing(8);
        Label questionsLabel = new Label("QUESTIONS:");
        questionsLabel.getStyleClass().add("terminal-label");
        questionsSection.getChildren().add(questionsLabel);
        questionsSection.setId("questions-section");

        body.getChildren().addAll(usernameSection, logoSection, questionsSection);
        sidebar.getChildren().addAll(titleBar, body);

        return sidebar;
    }

    private VBox createContentPanel() {
        VBox contentPanel = new VBox();
        contentPanel.getStyleClass().add("terminal-panel");

        // Title bar with menu aligned right
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
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/cab302/bugco/home-view.fxml")
                );
                Parent homeRoot = loader.load();

                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                Scene scene = new Scene(homeRoot);

                // ✅ Attach the main menu stylesheet
                try {
                    String cssPath = getClass()
                            .getResource("/com/cab302/bugco/styles.css") // adjust this filename!
                            .toExternalForm();
                    scene.getStylesheets().add(cssPath);
                } catch (Exception cssEx) {
                    System.out.println("Warning: could not load main menu CSS - " + cssEx.getMessage());
                }

                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        titleBar.getChildren().addAll(titleLabel, spacer, mainMenuButton);

        VBox body = new VBox();
        body.getStyleClass().add("panel-body");
        body.setPadding(new Insets(12));
        body.setSpacing(24);

        VBox difficultySection = new VBox();
        difficultySection.setSpacing(8);
        Label difficultyLabel = new Label("DIFFICULTY (Easy/Medium/Hard)");
        difficultyLabel.getStyleClass().add("terminal-label");

        difficultyCombo = new ComboBox<>();
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

    private VBox createQuestionArea() {
        VBox questionArea = new VBox();
        questionArea.setSpacing(16);

        HBox questionHeader = new HBox();
        questionHeader.setSpacing(8);
        questionHeader.setAlignment(Pos.CENTER_LEFT);

        Label questionLabel = new Label("QUESTION");
        questionLabel.getStyleClass().add("terminal-label");
        questionHeader.getChildren().add(questionLabel);

        VBox questionContent = new VBox();
        questionContent.getStyleClass().add("terminal-panel");
        questionContent.setPadding(new Insets(16));
        questionContent.setSpacing(12);

        HBox badgeBox = new HBox();
        badgeBox.setSpacing(8);

        Label questionBadge = new Label();
        questionBadge.getStyleClass().add("question-badge");
        questionBadge.textProperty().bind(selectedQuestion.asString("Question %d"));

        difficultyBadgeLabel = new Label();
        difficultyBadgeLabel.getStyleClass().add("question-badge");

        // Hint button + inline terminal
        Button hintButton = new Button("?");
        hintButton.getStyleClass().add("hint-button");

        hintLabel = new Label();
        hintLabel.getStyleClass().add("hint-terminal");
        hintLabel.setVisible(false);

        hintButton.setOnAction(e -> {
            if (currentQuestion != null) {
                hintLabel.setVisible(!hintLabel.isVisible());
                if (hintLabel.isVisible()) {
                    int qId = currentQuestion.getId();
                    hintLabel.setText("This is a hint for question " + qId);
                }
            }
        });

        badgeBox.getChildren().addAll(questionBadge, difficultyBadgeLabel, hintButton, hintLabel);

        questionTitleLabel = new Label();
        questionTitleLabel.getStyleClass().add("terminal-label");

        questionContent.getChildren().addAll(badgeBox, questionTitleLabel);
        questionArea.getChildren().addAll(questionHeader, questionContent);

        return questionArea;
    }

    private VBox createCodeSection() {
        VBox codeSection = new VBox();
        codeSection.setSpacing(16);

        Label codeLabel = new Label("REWRITE CODE WITHOUT BUGS");
        codeLabel.getStyleClass().add("terminal-label");

        VBox codeContent = new VBox();
        codeContent.getStyleClass().add("terminal-panel");
        codeContent.setPadding(new Insets(16));
        codeContent.setSpacing(16);

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
        submitButton = new Button("Submit"); // assign to field for dynamic text
        submitButton.getStyleClass().add("primary-btn");
        submitButton.setOnAction(e -> handleSubmit());
        buttonBox.getChildren().add(submitButton);

        codeContent.getChildren().addAll(codeAnswerArea, buttonBox);
        codeSection.getChildren().addAll(codeLabel, codeContent);

        return codeSection;
    }

    private void initializeBindings() {
        selectedQuestion.addListener((obs, oldVal, newVal) -> updateQuestionDisplay());

        difficulty.addListener((obs, oldVal, newVal) -> {
            rebuildSidebar();
            selectedQuestion.set(1);
        });
    }

    private void rebuildSidebar() {
        VBox sidebar = (VBox) ((HBox) ((VBox) ((Scene) questionTitleLabel.getScene()).getRoot()).getChildren().get(1)).getChildren().get(0);
        VBox body = (VBox) sidebar.getChildren().get(1);
        VBox questionsSection = (VBox) body.lookup("#questions-section");

        if (questionsSection.getChildren().size() > 1) {
            questionsSection.getChildren().remove(1);
        }

        GridPane questionsGrid = new GridPane();
        questionsGrid.setHgap(8);
        questionsGrid.setVgap(8);

        ToggleGroup questionGroup = new ToggleGroup();
        questionButtons.clear();

        List<Question> currentSet = questionsByDifficulty.getOrDefault(difficulty.get(), List.of());
        Set<Integer> solvedSet = solvedQuestionsByDifficulty.get(difficulty.get());

        for (int i = 0; i < currentSet.size(); i++) {
            Question q = currentSet.get(i);
            ToggleButton btn = new ToggleButton("Q" + q.getId());
            btn.getStyleClass().add("question-btn");
            btn.setToggleGroup(questionGroup);

            final int questionId = q.getId();
            btn.setOnAction(e -> selectedQuestion.set(questionId));

            if (solvedSet.contains(q.getId())) {
                if (!btn.getStyleClass().contains("correct")) {
                    btn.getStyleClass().add("correct");
                }
            }

            questionButtons.add(btn);
            questionsGrid.add(btn, i % 3, i / 3);

            if (i == 0) btn.setSelected(true);
        }

        questionsSection.getChildren().add(questionsGrid);
    }

    private void updateQuestionDisplay() {
        List<Question> currentSet = questionsByDifficulty.getOrDefault(difficulty.get(), List.of());
        Set<Integer> solvedSet = solvedQuestionsByDifficulty.get(difficulty.get());
        Map<Integer, String> answersMap = solvedAnswersByDifficulty.get(difficulty.get());

        currentQuestion = currentSet.stream()
                .filter(q -> q.getId() == selectedQuestion.get())
                .findFirst()
                .orElse(null);

        if (currentQuestion == null) return;

        questionTitleLabel.setText(currentQuestion.getTitle());
        difficultyBadgeLabel.setText(currentQuestion.getDifficulty());

        for (int i = 0; i < questionButtons.size(); i++) {
            ToggleButton btn = questionButtons.get(i);
            btn.setSelected(currentSet.get(i).getId() == selectedQuestion.get());
        }

        if (solvedSet.contains(currentQuestion.getId())) {
            codeAnswerArea.setEditable(false);
            codeAnswerArea.setText(answersMap.get(currentQuestion.getId()));
        } else {
            codeAnswerArea.setEditable(true);
            codeAnswerArea.clear();
            codeAnswerArea.setPromptText("Write your bug-free code here...");
        }

        // Hide hint when changing question
        if (hintLabel != null) {
            hintLabel.setVisible(false);
        }
    }

    private void handleSubmit() {
        List<Question> currentSet = questionsByDifficulty.getOrDefault(difficulty.get(), List.of());
        Set<Integer> solvedSet = solvedQuestionsByDifficulty.get(difficulty.get());
        Map<Integer, String> answersMap = solvedAnswersByDifficulty.get(difficulty.get());

        if (currentSet.isEmpty()) return;

        Question currentQuestion = currentSet.stream()
                .filter(q -> q.getId() == selectedQuestion.get())
                .findFirst()
                .orElse(null);

        if (currentQuestion == null || solvedSet.contains(currentQuestion.getId())) return;

        String input = codeAnswerArea.getText().trim();
        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        if (input.equals(String.valueOf(currentQuestion.getId()))) { // Correct answer check
            solvedSet.add(currentQuestion.getId());
            answersMap.put(currentQuestion.getId(), input);

            for (ToggleButton btn : questionButtons) {
                if (btn.getText().equals("Q" + currentQuestion.getId())) {
                    if (!btn.getStyleClass().contains("correct")) {
                        btn.getStyleClass().add("correct");
                    }
                    break;
                }
            }

            submitButton.setText("Correct");
            pause.setOnFinished(e -> submitButton.setText("Submit"));
            pause.play();

            int currentIndex = currentSet.indexOf(currentQuestion);
            int nextIndex = (currentIndex + 1) % currentSet.size();
            selectedQuestion.set(currentSet.get(nextIndex).getId());

        } else {
            System.out.println("Incorrect answer for Q" + currentQuestion.getId());
            submitButton.setText("Try Again");
            pause.setOnFinished(e -> submitButton.setText("Submit"));
            pause.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
