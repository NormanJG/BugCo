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

    public BugcoTerminalApp(String username) {
        this.username = username;
    }

    // State
    private final IntegerProperty selectedQuestion = new SimpleIntegerProperty(1);
    private final StringProperty difficulty = new SimpleStringProperty("Easy");
    private final StringProperty codeAnswer = new SimpleStringProperty("");

    // UI refs
    private TextArea codeAnswerArea;
    private Label questionTitleLabel;
    private Label difficultyBadgeLabel;
    private Label questionDescriptionLabel;
    private ComboBox<String> difficultyCombo;
    private List<ToggleButton> questionButtons;
    private Label hintLabel;
    private Question currentQuestion;
    private Button submitButton;

    // Data
    private Map<String, List<Question>> questionsByDifficulty;
    private final Map<String, Set<Integer>> solvedQuestionsByDifficulty = new HashMap<>();
    private final Map<String, Map<Integer, String>> solvedAnswersByDifficulty = new HashMap<>();

    // Model
    public static class Question {
        private final int id;
        private final String title;
        private final String description; // buggy code or explanation
        private final String hint;        // shown only when ? clicked
        private final String difficulty;

        public Question(int id, String title, String description, String hint, String difficulty) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.hint = hint;
            this.difficulty = difficulty;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getHint() { return hint; }
        public String getDifficulty() { return difficulty; }
    }

    // Entry point for HomeController
    public Parent createContent() {
        initializeData();

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
        selectedQuestion.set(1);   // make sure Q1 is active
        updateQuestionDisplay();

        return root;
    }

    // ---------- helpers ----------

    private void initializeData() {
        questionsByDifficulty = new HashMap<>();

        // Easy & Medium placeholders
        for (String diff : List.of("Easy", "Medium")) {
            List<Question> set = new ArrayList<>();
            for (int i = 1; i <= 9; i++) {
                set.add(new Question(
                        i,
                        diff + " Question " + i,
                        "Buggy code for " + diff + " question " + i,
                        "Hint for " + diff + " question " + i,
                        diff
                ));
            }
            questionsByDifficulty.put(diff, set);
        }

        // Hard difficulty: full questions
        List<Question> hardSet = new ArrayList<>();
        hardSet.add(new Question(
                1,
                "Fix off-by-one error in loop printing 1 to 10",
                "for (int i = 0; i < 10; i++) {\n    System.out.println(i);\n}",
                "Expected output: numbers 1 through 10 (inclusive).",
                "Hard"
        ));
        hardSet.add(new Question(
                2,
                "Fix NullPointerException when accessing string length",
                "String s = null;\nSystem.out.println(s.length());",
                "Hint: `s` must reference a valid String before calling `length()`.",
                "Hard"
        ));
        hardSet.add(new Question(
                3,
                "Fix string comparison to check equality correctly",
                "String s = \"hello\";\nif (s == \"hello\") {\n    System.out.println(\"Match!\");\n}",
                "Hint: Use .equals() for string comparison in Java.",
                "Hard"
        ));
        hardSet.add(new Question(
                4,
                "Fix integer division so result is 2.5",
                "int a = 5, b = 2;\nSystem.out.println(a / b);",
                "Hint: Cast one operand to double before dividing.",
                "Hard"
        ));
        hardSet.add(new Question(
                5,
                "Fix array loop to avoid ArrayIndexOutOfBoundsException",
                "int[] nums = {1,2,3};\nfor (int i = 0; i <= nums.length; i++) {\n    System.out.println(nums[i]);\n}",
                "Hint: Use i < nums.length, not <=.",
                "Hard"
        ));
        hardSet.add(new Question(
                6,
                "Fix immutable string bug so it prints HELLO",
                "String s = \"hello\";\ns.toUpperCase();\nSystem.out.println(s);",
                "Hint: Strings are immutable; assign the result back to s.",
                "Hard"
        ));
        hardSet.add(new Question(
                7,
                "Fix floating point equality check for 0.1 + 0.2",
                "if (0.1 + 0.2 == 0.3) {\n    System.out.println(\"Equal\");\n}",
                "Hint: Compare doubles using a tolerance (Math.abs difference).",
                "Hard"
        ));
        hardSet.add(new Question(
                8,
                "Fix concurrency bug so counter increments are thread-safe",
                "class Counter {\n    private int count = 0;\n    public void increment() {\n        count++;\n    }\n}",
                "Hint: Use synchronized methods or AtomicInteger.",
                "Hard"
        ));
        hardSet.add(new Question(
                9,
                "Fix file handling so file closes properly after reading",
                "BufferedReader br = new BufferedReader(new FileReader(\"data.txt\"));\nString line = br.readLine();\nSystem.out.println(line);\n// missing close",
                "Hint: Use try-with-resources to auto-close the reader.",
                "Hard"
        ));

        questionsByDifficulty.put("Hard", hardSet);

        // Load saved progress from DB
        Map<String, Map<Integer, String>> saved = ProgressDAO.loadProgress(username);
        for (String diff : List.of("Easy", "Medium", "Hard")) {
            Map<Integer, String> answers = saved.getOrDefault(diff, Map.of());
            solvedQuestionsByDifficulty.put(diff, new HashSet<>(answers.keySet()));
            solvedAnswersByDifficulty.put(diff, new HashMap<>(answers));
        }

        Map<Integer, String> hardAnswers = solvedAnswersByDifficulty.get("Hard");
        if (hardAnswers != null) {
            hardAnswers.putIfAbsent(1,
                    "for (int i = 1; i <= 10; i++) {\n    System.out.println(i);\n}");
            hardAnswers.putIfAbsent(2,
                    "String s = \"\";\nSystem.out.println(s.length());");
            hardAnswers.putIfAbsent(3,
                    "String s = \"hello\";\nif (s.equals(\"hello\")) {\n    System.out.println(\"Match!\");\n}");
            hardAnswers.putIfAbsent(4,
                    "int a = 5, b = 2;\nSystem.out.println((double) a / b);");
            hardAnswers.putIfAbsent(5,
                    "int[] nums = {1,2,3};\nfor (int i = 0; i < nums.length; i++) {\n    System.out.println(nums[i]);\n}");
            hardAnswers.putIfAbsent(6,
                    "String s = \"hello\";\ns = s.toUpperCase();\nSystem.out.println(s);");
            hardAnswers.putIfAbsent(7,
                    "if (Math.abs((0.1 + 0.2) - 0.3) < 1e-9) {\n    System.out.println(\"Equal\");\n}");
            hardAnswers.putIfAbsent(8,
                    "class Counter {\n    private int count = 0;\n    public synchronized void increment() {\n        count++;\n    }\n}");
            hardAnswers.putIfAbsent(9,
                    "try (BufferedReader br = new BufferedReader(new FileReader(\"data.txt\"))) {\n    String line = br.readLine();\n    System.out.println(line);\n} catch (IOException e) {\n    e.printStackTrace();\n}");
        }
        questionButtons = new ArrayList<>();
    }


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
    private VBox sidebarRef;
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
        resetButton.setMaxWidth(Double.MAX_VALUE); // full width
        resetButton.setOnAction(e -> resetProgressForCurrentDifficulty());

        VBox resetSection = new VBox();
        resetSection.setPadding(new Insets(12, 0, 0, 0));
        resetSection.getChildren().add(resetButton);


        body.getChildren().addAll(usernameSection, logoSection, questionsContainer, resetSection);

        sidebarRef.getChildren().addAll(titleBar, body);
        return sidebarRef;
    }

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
            if (currentQuestion != null) {
                if (!hintLabel.isVisible()) {
                    hintLabel.setText(currentQuestion.getHint());
                    hintLabel.setVisible(true);
                } else {
                    hintLabel.setVisible(false);
                }
            }
        });

        badgeBox.getChildren().addAll(questionBadge, difficultyBadgeLabel, hintButton, hintLabel);

        questionTitleLabel = new Label();
        questionTitleLabel.getStyleClass().add("terminal-label");

        questionDescriptionLabel = new Label(); // NEW
        questionDescriptionLabel.getStyleClass().add("terminal-description");
        questionDescriptionLabel.setWrapText(true);

        questionContent.getChildren().addAll(badgeBox, questionTitleLabel, questionDescriptionLabel);
        questionArea.getChildren().addAll(questionHeader, questionContent);

        return questionArea;
    }

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

    private void initializeBindings() {
        selectedQuestion.addListener((obs, o, n) -> updateQuestionDisplay());

        difficulty.addListener((obs, o, n) -> {
            rebuildSidebar();
            selectedQuestion.set(1);
            updateQuestionDisplay();
        });
    }

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

            if (solvedSet.contains(q.getId()) && !btn.getStyleClass().contains("correct")) {
                btn.getStyleClass().add("correct");
            }

            questionButtons.add(btn);
            questionsGrid.add(btn, i % 3, i / 3);

            if (i == 0) btn.setSelected(true);
        }

        questionsSection.getChildren().add(questionsGrid);
    }


    private void resetProgressForCurrentDifficulty() {
        String diff = difficulty.get();

        // Clear in-memory progress
        solvedQuestionsByDifficulty.get(diff).clear();
        solvedAnswersByDifficulty.get(diff).clear();

        // Clear from database
        ProgressDAO.resetProgress(username, diff);

        // Refresh sidebar & question display
        rebuildSidebar();
        selectedQuestion.set(1);
        updateQuestionDisplay();

        System.out.println("Progress reset for " + username + " [" + diff + "]");
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
        questionDescriptionLabel.setText(currentQuestion.getDescription());
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

        if (hintLabel != null) hintLabel.setVisible(false);
    }

    private void handleSubmit() {
        List<Question> currentSet = questionsByDifficulty.getOrDefault(difficulty.get(), List.of());
        Set<Integer> solvedSet = solvedQuestionsByDifficulty.get(difficulty.get());
        Map<Integer, String> answersMap = solvedAnswersByDifficulty.get(difficulty.get());
        if (currentSet.isEmpty()) return;

        Question current = currentSet.stream()
                .filter(q -> q.getId() == selectedQuestion.get())
                .findFirst().orElse(null);
        if (current == null || solvedSet.contains(current.getId())) return;

        String input = codeAnswerArea.getText().trim();
        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        boolean isCorrect = false;

        if (difficulty.get().equals("Easy") || difficulty.get().equals("Medium")) {
            // For Easy/Medium, just match the question number
            isCorrect = input.equals(String.valueOf(current.getId()));
        } else if (difficulty.get().equals("Hard")) {
            // For Hard, match the expected code solution
            String expected = solvedAnswersByDifficulty.get("Hard").get(current.getId());
            isCorrect = input.equals(expected);
        }

        if (isCorrect) {
            solvedSet.add(current.getId());
            answersMap.put(current.getId(), input);

            // Persist progress in DB
            ProgressDAO.saveProgress(username, difficulty.get(), current.getId(), input);

            // Update leaderboard achievement
            String achievementText = "Solved Q" + current.getId() + " in " + difficulty.get();
            Database.updatePlayerAchievement(username, achievementText);

            // Mark sidebar button green
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

            // Auto-advance to next question
            int currentIndex = currentSet.indexOf(current);
            int nextIndex = (currentIndex + 1) % currentSet.size();
            selectedQuestion.set(currentSet.get(nextIndex).getId());
        } else {
            // Show failure feedback
            System.out.println("Incorrect answer for Q" + current.getId());
            submitButton.setText("Try Again");
            pause.setOnFinished(e -> submitButton.setText("Submit"));
            pause.play();
        }
    }

    /**
     * Normalize input for comparison:
     * - Remove all spaces/tabs/newlines
     * - Lowercase
     */
    private String normalize(String code) {
        return code.replaceAll("\\s+", "").toLowerCase();
    }
}