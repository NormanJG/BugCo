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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;

import java.io.InputStream;
import java.util.*;


public class BugcoTerminalApp {

    // who is playing
    private final GameState myState;

    public BugcoTerminalApp(String username) {
        this.myState = new GameState(username);
    }

    // Root & overlay
    private StackPane rootLayer;
    private VBox overlayCard;
    private Pane overlayDim;
    private VBox overlayContentBox;

    // Screen state
    private final IntegerProperty selectedQuestion = new SimpleIntegerProperty(1);
    private final StringProperty difficulty = new SimpleStringProperty("Easy");

    // UI refs
    private TextArea codeAnswerArea;
    private VBox mcqBox;
    private ToggleGroup mcqGroup;

    private Label questionTitleLabel;
    private Label difficultyBadgeLabel;
    private Label questionDescriptionLabel;
    private Label hintLabel;
    private Label theWhyLabel;                       // “why incorrect”
    private Label doneCountLabel;                    // completed count badge
    private List<ToggleButton> questionButtons;
    private Button submitButton;

    private ComboBox<String> difficultyCombo;
    private VBox sidebarRef;

    private Label pointsValueLabel;

    // Points breakdown labels
    private Label easyPtsLabel;
    private Label mediumPtsLabel;
    private Label hardPtsLabel;

    // Data
    private Map<String, List<Question>> questionsByDifficulty;
    private final Map<String, Set<Integer>> solvedQuestionsByDifficulty = new HashMap<>();
    private final Map<String, Map<Integer, String>> solvedAnswersByDifficulty = new HashMap<>();
    private Question currentQuestion;

    // Model
    private enum AnswerType { MCQ, TEXT }
    public static class Question {
        private final int id;
        private final String title;
        private final String prompt;
        private final String hint;
        private final String whyWrong;
        private final String difficulty;
        private final AnswerType type;
        private final String expectedText;
        private final List<String> options;
        private final int correctIndex;

        // MCQ
        public Question(int id, String title, String prompt, String hint, String whyWrong,
                        String difficulty, List<String> options, int correctIndex) {
            this.id = id; this.title = title; this.prompt = prompt;
            this.hint = hint; this.whyWrong = whyWrong; this.difficulty = difficulty;
            this.type = AnswerType.MCQ;
            this.options = options == null ? List.of() : List.copyOf(options);
            this.correctIndex = correctIndex;
            this.expectedText = null;
        }
        // TEXT
        public Question(int id, String title, String prompt, String hint, String whyWrong,
                        String difficulty, String expectedText) {
            this.id = id; this.title = title; this.prompt = prompt;
            this.hint = hint; this.whyWrong = whyWrong; this.difficulty = difficulty;
            this.type = AnswerType.TEXT;
            this.expectedText = expectedText;
            this.options = List.of();
            this.correctIndex = -1;
        }
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getPrompt() { return prompt; }
        public String getHint() { return hint; }
        public String getWhyWrong() { return whyWrong; }
        public String getDifficulty() { return difficulty; }
        public AnswerType getType() { return type; }
        public List<String> getOptions() { return options; }
        public int getCorrectIndex() { return correctIndex; }
        public String getExpectedText() { return expectedText; }
    }

    // ENTRY
    public Parent createContent() {
        initializeData();

        // root stack for overlay
        rootLayer = new StackPane();
        rootLayer.getStyleClass().add("root-terminal");

        VBox main = new VBox();
        HBox header = createHeader();
        HBox mainContent = new HBox();
        VBox sidebar = createSidebar();
        VBox contentPanel = createContentPanel();

        mainContent.getChildren().addAll(sidebar, contentPanel);
        HBox.setHgrow(contentPanel, Priority.ALWAYS);

        main.getChildren().addAll(header, mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // build overlay pieces (hidden by default)
        buildOverlay();

        rootLayer.getChildren().addAll(main, overlayDim, overlayCard);

        initializeBindings();
        rebuildSidebar();
        selectedQuestion.set(1);
        updateQuestionDisplay();

        return rootLayer;
    }

    // data setup
    private void initializeData() {
        questionsByDifficulty = new HashMap<>();

        // EASY: 10 MCQ
        List<Question> easy = new ArrayList<>();
        easy.add(new Question(1, "Variables", "Which is a valid Java variable name?",
                "Letters/numbers/_ allowed, cannot start with number.",
                "Variable names cannot start with a digit.", "Easy",
                List.of("1name", "my_name", "class", "new-variable"), 1));
        easy.add(new Question(2, "Loops", "Which loop repeats a known number of times?",
                "‘for’ is good when you know count.", "Use for when count is known.", "Easy",
                List.of("while", "for", "do-while", "switch"), 1));
        easy.add(new Question(3, "Strings", "How to compare two strings in Java?",
                "Use equals()", "== compares references; equals() compares content.", "Easy",
                List.of("a == b", "a.equals(b)", "a.compare(b)", "a.same(b)"), 1));
        easy.add(new Question(4, "Arrays", "What is valid array access?",
                "Index start 0 to length-1.", "Last index is length-1.", "Easy",
                List.of("arr[arr.length]", "arr[-1]", "arr[0]", "arr[length()]"), 2));
        easy.add(new Question(5, "Exceptions", "Which block always runs if added?",
                "finally always run", "finally runs even with early return.", "Easy",
                List.of("try", "catch", "finally", "throws"), 2));
        easy.add(new Question(6, "OOP", "Which keyword for inheritance?",
                "class B extends A", "extends is for inheritance.", "Easy",
                List.of("implements", "inherits", "extends", "super"), 2));
        easy.add(new Question(7, "Collections", "Which is ordered and allows duplicates?",
                "List is ordered + allows dup.", "Set has no duplicates.", "Easy",
                List.of("Set", "Map", "List", "HashSet"), 2));
        easy.add(new Question(8, "Types", "Which type holds true/false?",
                "It is boolean", "boolean type for true/false.", "Easy",
                List.of("int", "char", "boolean", "double"), 2));
        easy.add(new Question(9, "Methods", "Correct main signature?",
                "public static void main(String[] args)", "This is standard entry.", "Easy",
                List.of("public void main()", "static void main()", "public static void main(String[] args)", "void main(String)"), 2));
        easy.add(new Question(10, "Files", "Best way to auto-close resources?",
                "try-with-resources", "try(...) { } closes it for you.", "Easy",
                List.of("manual close()", "System.gc()", "try-with-resources", "finalize()"), 2));
        questionsByDifficulty.put("Easy", easy);

        // MEDIUM: 4 TEXT
        List<Question> medium = new ArrayList<>();
        medium.add(new Question(1, "Fix integer division to 2.5",
                "int a=5,b=2; System.out.println(a/b);",
                "Cast one side to double.", "Need double division, not int.",
                "Medium", "(double)a / b"));
        medium.add(new Question(2, "Null check before length()",
                "String s=null; System.out.println(s.length());",
                "s must not be null", "Create a real String before length().",
                "Medium", "String s=\"\"; System.out.println(s.length());"));
        medium.add(new Question(3, "Equals not ==",
                "String s=\"hello\"; if (s==\"hello\") System.out.println(\"Match\");",
                "Use equals()", "Use s.equals(\"hello\") for content compare.",
                "Medium", "String s=\"hello\"; if (s.equals(\"hello\")) System.out.println(\"Match\");"));
        medium.add(new Question(4, "Array bounds",
                "int[] n={1,2,3}; for(int i=0;i<=n.length;i++) System.out.println(n[i]);",
                "Use i < n.length", "Stop at length-1.",
                "Medium", "int[] n={1,2,3}; for(int i=0;i<n.length;i++) System.out.println(n[i]);"));
        questionsByDifficulty.put("Medium", medium);

        // HARD: 3 TEXT (code-fix)
        List<Question> hard = new ArrayList<>();
        hard.add(new Question(1, "Off-by-one (1..10 inclusive)",
                "for (int i = 0; i < 10; i++) { System.out.println(i); }",
                "Start from 1, end at 10 inclusive.", "Need i=1; i<=10;",
                "Hard", "for (int i = 1; i <= 10; i++) {\n    System.out.println(i);\n}"));
        hard.add(new Question(2, "Immutable string toUpperCase()",
                "String s=\"hello\"; s.toUpperCase(); System.out.println(s);",
                "Assign back to s", "Strings are immutable.",
                "Hard", "String s=\"hello\"; s = s.toUpperCase(); System.out.println(s);"));
        hard.add(new Question(3, "Thread-safe increment",
                "class Counter { private int c=0; public void inc(){ c++; } }",
                "Use synchronized or AtomicInteger", "make it thread-safe",
                "Hard", "class Counter { private int c=0; public synchronized void inc(){ c++; } }"));
        questionsByDifficulty.put("Hard", hard);

        // Load saved progress from DB and bring into memory
        Map<String, Map<Integer, String>> saved = ProgressDAO.loadProgress(myState.getMyName());
        for (String diff : List.of("Easy", "Medium", "Hard")) {
            Map<Integer, String> answers = saved.getOrDefault(diff, Map.of());
            solvedQuestionsByDifficulty.put(diff, new HashSet<>(answers.keySet()));
            solvedAnswersByDifficulty.put(diff, new HashMap<>(answers));
        }

        questionButtons = new ArrayList<>();
    }

    // header
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

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button sprintBtn = new Button("Sprint Run");
        sprintBtn.getStyleClass().add("primary-btn");
        sprintBtn.setOnAction(e -> showSprintRun());

        Label version = new Label("Version 1.1.1 | Security Protocol Activated");
        version.getStyleClass().add("banner-sub");

        header.getChildren().addAll(leftSection, spacer, sprintBtn, version);
        return header;
    }

    // sidebar
    private VBox createSidebar() {
        sidebarRef = new VBox();
        sidebarRef.getStyleClass().add("terminal-panel");
        sidebarRef.setPrefWidth(340);

        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("panel-titlebar");
        titleBar.setPadding(new Insets(8, 10, 8, 10));
        Label titleLabel = new Label("CONTROL PANEL");
        titleLabel.getStyleClass().add("panel-title");
        titleBar.getChildren().add(titleLabel);

        VBox body = new VBox(18);
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
        Label usernameText = new Label(myState.getMyName());
        usernameText.getStyleClass().add("terminal-label");
        usernameDisplay.getChildren().add(usernameText);

        Button renameBtn = new Button("Rename");
        renameBtn.getStyleClass().add("primary-btn");
        renameBtn.setOnAction(e -> renameUsernameOverlay(usernameText)); // in-app overlay

        // Points row
        HBox pointsRow = new HBox(8);
        Label pointsLabel = new Label("POINTS");
        pointsLabel.getStyleClass().add("terminal-label");
        pointsValueLabel = new Label();
        pointsValueLabel.getStyleClass().add("terminal-label");
        refreshPointsBadge();
        Region pointsSpacer = new Region();
        HBox.setHgrow(pointsSpacer, Priority.ALWAYS);
        pointsRow.getChildren().addAll(pointsLabel, pointsSpacer, pointsValueLabel);

        usernameSection.getChildren().setAll(
                usernameLabel, usernameDisplay, pointsRow, renameBtn
        );

        // Per-difficulty breakdown
        VBox breakdownBox = new VBox(4);
        easyPtsLabel = new Label("Easy: 0");
        mediumPtsLabel = new Label("Medium: 0");
        hardPtsLabel = new Label("Hard: 0");
        easyPtsLabel.getStyleClass().add("terminal-label");
        mediumPtsLabel.getStyleClass().add("terminal-label");
        hardPtsLabel.getStyleClass().add("terminal-label");
        breakdownBox.getChildren().addAll(easyPtsLabel, mediumPtsLabel, hardPtsLabel);
        refreshPointsBreakdown();

        usernameSection.getChildren().setAll(
                usernameLabel, usernameDisplay, pointsRow, breakdownBox, renameBtn
        );

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

        // Questions grid container
        VBox questionsSection = new VBox(8);
        questionsSection.setId("questions-section");
        Label questionsLabel = new Label("QUESTIONS:");
        questionsLabel.getStyleClass().add("terminal-label");
        questionsSection.getChildren().add(questionsLabel);

        // Completed counter + “View Completed”
        HBox doneRow = new HBox(8);
        doneCountLabel = new Label("Completed: 0");
        doneCountLabel.setId("doneCountLabel");
        doneCountLabel.getStyleClass().add("terminal-label");
        Button viewCompleted = new Button("View Completed");
        viewCompleted.getStyleClass().add("primary-btn");
        viewCompleted.setOnAction(e -> showCompletedOverlay());
        doneRow.getChildren().addAll(doneCountLabel, viewCompleted);

        // Reset
        Button resetButton = new Button("Reset All");
        resetButton.getStyleClass().add("danger-btn");
        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setOnAction(e -> resetAllProgressAndPoints());

        VBox bodyContent = new VBox(
                usernameSection, logoSection, questionsSection, doneRow, resetButton
        );
        bodyContent.setSpacing(18);
        body.getChildren().add(bodyContent);

        sidebarRef.getChildren().addAll(titleBar, body);
        return sidebarRef;
    }

    // content
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

                Scene scene = ((Node) e.getSource()).getScene();
                try {
                    String cssPath = getClass().getResource("/com/cab302/bugco/styles.css").toExternalForm();
                    if (!scene.getStylesheets().contains(cssPath)) scene.getStylesheets().add(cssPath);
                } catch (Exception cssEx) { /* ignore */ }

                scene.setRoot(homeRoot);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        titleBar.getChildren().addAll(titleLabel, spacer, mainMenuButton);

        VBox body = new VBox(24);
        body.getStyleClass().add("panel-body");
        body.setPadding(new Insets(12));

        // DIFFICULTY (Easy/Medium/Hard)
        VBox difficultySection = new VBox(8);
        Label difficultyLabel = new Label("DIFFICULTY (Easy/Medium/Hard)");
        difficultyLabel.getStyleClass().add("terminal-label");

        difficultyCombo = new ComboBox<>();
        difficultyCombo.getStyleClass().add("terminal-input");
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard");
        difficultyCombo.setValue(difficulty.get()); // keep current state if returning
        difficultyCombo.valueProperty().bindBidirectional(difficulty);

        difficultySection.getChildren().addAll(difficultyLabel, difficultyCombo);


        // QUESTION AREA
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
                hintLabel.setText(currentQuestion.getHint());
                hintLabel.setVisible(!hintLabel.isVisible());
            }
        });

        badgeBox.getChildren().addAll(questionBadge, difficultyBadgeLabel, hintButton, hintLabel);

        questionTitleLabel = new Label();
        questionTitleLabel.getStyleClass().add("terminal-label");

        questionDescriptionLabel = new Label();
        // clearer prompt style
        questionDescriptionLabel.getStyleClass().add("question-prompt");
        questionDescriptionLabel.setWrapText(true);

        questionContent.getChildren().addAll(badgeBox, questionTitleLabel, questionDescriptionLabel);
        questionArea.getChildren().addAll(questionHeader, questionContent);

        // ANSWER AREA — MCQ or TEXT
        VBox answerSection = new VBox(16);
        Label codeLabel = new Label("YOUR ANSWER");
        codeLabel.getStyleClass().add("terminal-label");
        VBox answerContent = new VBox(16);
        answerContent.getStyleClass().add("terminal-panel");
        answerContent.setPadding(new Insets(16));

        // MCQ block
        mcqBox = new VBox(8);
        mcqGroup = new ToggleGroup();

        // TEXT block
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

        // HELP + SUBMIT + WHY WRONG
        Button helpButton = new Button("Help");
        helpButton.getStyleClass().add("amber-btn");
        helpButton.setOnAction(e -> showHelp());

        theWhyLabel = new Label("");
        theWhyLabel.getStyleClass().add("terminal-label");
        theWhyLabel.setWrapText(true);
        theWhyLabel.setVisible(false);

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        submitButton = new Button("Submit");
        submitButton.getStyleClass().add("primary-btn");
        submitButton.setOnAction(e -> handleSubmit());
        buttonBox.getChildren().addAll(helpButton, submitButton);

        answerContent.getChildren().addAll(mcqBox, codeAnswerArea, theWhyLabel, buttonBox);
        answerSection.getChildren().addAll(codeLabel, answerContent);

        body.getChildren().addAll(difficultySection, questionArea, answerSection);
        VBox.setVgrow(answerSection, Priority.ALWAYS);

        contentPanel.getChildren().addAll(titleBar, body);
        return contentPanel;
    }

    // Overlay system
    private void buildOverlay() {
        overlayDim = new Pane();
        overlayDim.getStyleClass().add("overlay-dim");
        overlayDim.setVisible(false);
        overlayDim.setMouseTransparent(true);

        overlayCard = new VBox(12);
        overlayCard.getStyleClass().addAll("neon-dialog", "neon-card");
        overlayCard.setVisible(false);
        overlayCard.setMouseTransparent(true);
        overlayCard.setMaxWidth(520);
        overlayCard.setPadding(new Insets(16));

        overlayContentBox = new VBox(12);
        overlayCard.getChildren().add(overlayContentBox);
        StackPane.setAlignment(overlayCard, Pos.CENTER);
    }

    private void openOverlay(Node content) {
        overlayContentBox.getChildren().setAll(content);
        overlayDim.setVisible(true);
        overlayDim.setMouseTransparent(false);
        overlayCard.setVisible(true);
        overlayCard.setMouseTransparent(false);
    }

    private void closeOverlay() {
        overlayCard.setVisible(false);
        overlayCard.setMouseTransparent(true);
        overlayDim.setVisible(false);
        overlayDim.setMouseTransparent(true);
        overlayContentBox.getChildren().clear();
    }

    private Node simpleOverlayContent(String title, String text, boolean showClose) {
        VBox box = new VBox(12);
        Label t = new Label(title);
        t.getStyleClass().add("panel-title");
        Label msg = new Label(text);
        msg.getStyleClass().add("terminal-label");
        msg.setWrapText(true);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button ok = new Button("OK");
        ok.getStyleClass().add("primary-btn");
        ok.setOnAction(e -> closeOverlay());
        if (showClose) actions.getChildren().add(ok);

        box.getChildren().addAll(t, msg, actions);
        return box;
    }

    // Completed levels overlay
    private void showCompletedOverlay() {
        String d = difficulty.get();
        Set<Integer> solvedSet = solvedQuestionsByDifficulty.getOrDefault(d, Set.of());
        StringBuilder sb = new StringBuilder();
        if (solvedSet.isEmpty()) sb.append("No completed levels yet. Keep going!");
        else solvedSet.stream().sorted().forEach(id -> sb.append("• Q").append(id).append("\n"));

        Node content = simpleOverlayContent("Completed Levels (" + d + ")", sb.toString(), true);
        openOverlay(content);
    }

    // Sprint run overlay
    private void showSprintRun() {
        String text = String.join("\n",
                "• Start challenge and pick difficulty",
                "• Solve levels; points auto-save",
                "• View Completed to avoid replays",
                "• Use '?' for hints; Help for commands",
                "• Change username if needed",
                "• Check leaderboard on Home"
        );
        Node content = simpleOverlayContent("Sprint Run", text, true);
        openOverlay(content);
    }

    // Rename overlay with TextField
    private void renameUsernameOverlay(Label usernameText) {
        String oldName = Session.isLoggedIn() ? Session.getCurrentUser() : null;
        if (oldName == null) {
            openOverlay(simpleOverlayContent("Rename Username", "You must login first to rename.", true));
            return;
        }

        VBox box = new VBox(12);
        Label title = new Label("Rename Username");
        title.getStyleClass().add("panel-title");

        TextField tf = new TextField(oldName);
        tf.getStyleClass().add("terminal-input");
        tf.setPromptText("New username");

        Label info = new Label("Letters or numbers only (max 20).");
        info.getStyleClass().add("terminal-label");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("amber-btn");
        cancel.setOnAction(e -> closeOverlay());
        Button save = new Button("Save");
        save.getStyleClass().add("primary-btn");
        save.setOnAction(e -> {
            String newName = tf.getText() == null ? "" : tf.getText().trim();
            if (!newName.matches("^[A-Za-z0-9]{1,20}$")) {
                openOverlay(simpleOverlayContent("Rename Username", "Username must be letters or numbers (max 20).", true));
                return;
            }
            try {
                com.cab302.bugco.db.UserDao dao = new com.cab302.bugco.db.UserDao();
                if (dao.updateUsernameByUsername(oldName, newName)) {
                    Session.setCurrentUser(newName);
                    myState.setMyName(newName);
                    usernameText.setText(newName);

                    refreshPointsBadge();
                    openOverlay(simpleOverlayContent("Success", "Username changed: " + oldName + " -> " + newName, true));
                } else {
                    openOverlay(simpleOverlayContent("Rename Username", "Could not change username.", true));
                }
            } catch (IllegalStateException dup) {
                openOverlay(simpleOverlayContent("Rename Username", "That username already exists. Try a different one.", true));
            } catch (Exception ex) {
                ex.printStackTrace();
                openOverlay(simpleOverlayContent("Rename Username", "Problem changing username.", true));
            }
        });
        actions.getChildren().addAll(cancel, save);

        box.getChildren().addAll(title, tf, info, actions);
        openOverlay(box);
    }

    // logic
    private void initializeBindings() {
        selectedQuestion.addListener((obs, o, n) -> updateQuestionDisplay());

        difficulty.addListener((obs, o, n) -> {
            myState.setTheDifficulty(n);
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
        questionButtons = new ArrayList<>();

        List<Question> currentSet = questionsByDifficulty.getOrDefault(difficulty.get(), List.of());
        Set<Integer> solvedSet = solvedQuestionsByDifficulty.getOrDefault(difficulty.get(), Set.of());

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
        updateDoneCountLabel();
        refreshPointsBreakdown();
    }

    private void resetAllProgressAndPoints() {  // NEW
        String user = myState.getMyName();

        // 1) Clear in-memory + DB progress for each difficulty
        for (String diff : List.of("Easy", "Medium", "Hard")) {
            solvedQuestionsByDifficulty
                    .computeIfAbsent(diff, k -> new HashSet<>())
                    .clear();
            solvedAnswersByDifficulty
                    .computeIfAbsent(diff, k -> new HashMap<>())
                    .clear();
            ProgressDAO.resetProgress(user, diff); // DB clear
        }

        // 2) Zero the user's total points in DB
        try {
            int current = Database.getPointsForUser(user);
            if (current != 0) {
                Database.addPointsForUser(user, -current); // make total 0
            }
        } catch (Exception ignore) { /* if DB read fails, UI will show 0 next launch */ }

        // 3) Rebuild UI & refresh labels
        rebuildSidebar();
        selectedQuestion.set(1);
        updateQuestionDisplay();
        refreshPointsBadge();
        refreshPointsBreakdown();

        // 4) Audit line
        Database.updatePlayerAchievement(user, "Reset all progress & points");
    }

    private void updateQuestionDisplay() {
        List<Question> currentSet = questionsByDifficulty.getOrDefault(difficulty.get(), List.of());
        Set<Integer> solvedSet = solvedQuestionsByDifficulty.getOrDefault(difficulty.get(), Set.of());
        Map<Integer, String> answersMap = solvedAnswersByDifficulty.getOrDefault(difficulty.get(), Map.of());

        currentQuestion = currentSet.stream()
                .filter(q -> q.getId() == selectedQuestion.get())
                .findFirst()
                .orElse(null);
        if (currentQuestion == null) return;

        // header bits
        questionTitleLabel.setText(currentQuestion.getTitle());
        // switch prompt style
        questionDescriptionLabel.getStyleClass().removeAll("question-code");
        if (currentQuestion.getType() == AnswerType.TEXT) {
            questionDescriptionLabel.getStyleClass().add("question-code");
        }
        questionDescriptionLabel.setText(currentQuestion.getPrompt());
        difficultyBadgeLabel.setText(currentQuestion.getDifficulty());

        // select the active button
        for (int i = 0; i < questionButtons.size(); i++) {
            ToggleButton btn = questionButtons.get(i);
            if (i < currentSet.size() && currentSet.get(i).getId() == selectedQuestion.get()) {
                btn.setSelected(true);
            } else {
                btn.setSelected(false);
            }
        }

        // swap answer UI (MCQ vs TEXT)
        theWhyLabel.setVisible(false);
        hintLabel.setVisible(false);
        mcqBox.setVisible(false);
        mcqBox.getChildren().clear();
        codeAnswerArea.setVisible(false);
        codeAnswerArea.setEditable(true);
        codeAnswerArea.clear();
        codeAnswerArea.setPromptText("Write your bug-free code here...");

        if (currentQuestion.getType() == AnswerType.MCQ) {
            mcqBox.setVisible(true);
            mcqGroup = new ToggleGroup();
            for (int i = 0; i < currentQuestion.getOptions().size(); i++) {
                RadioButton rb = new RadioButton((i + 1) + ") " + currentQuestion.getOptions().get(i));
                rb.setToggleGroup(mcqGroup);
                rb.getStyleClass().add("mcq-radio"); // neon style
                mcqBox.getChildren().add(rb);
            }
            // ensure previous selection doesn’t stick
            mcqGroup.selectToggle(null);
        } else {
            codeAnswerArea.setVisible(true);
        }

        // if solved, lock input and show the saved
        if (solvedSet.contains(currentQuestion.getId())) {
            submitButton.setDisable(true);
            if (currentQuestion.getType() == AnswerType.MCQ) {
                int idx = parseSavedIndex(answersMap.get(currentQuestion.getId()));
                if (idx >= 0 && idx < mcqBox.getChildren().size()) {
                    ((RadioButton) mcqBox.getChildren().get(idx)).setSelected(true);
                }
                for (Node n : mcqBox.getChildren()) {
                    n.setDisable(true);
                    n.getStyleClass().add("mcq-radio-solved"); // keep neon look when disabled
                }
            } else {
                codeAnswerArea.setEditable(false);
                codeAnswerArea.setText(answersMap.get(currentQuestion.getId()));
            }
        } else {
            submitButton.setDisable(false);
            if (currentQuestion.getType() == AnswerType.MCQ) {
                for (Node n : mcqBox.getChildren()) n.setDisable(false);
                mcqGroup.selectToggle(null);
            }
        }

        updateDoneCountLabel();
    }

    private int parseSavedIndex(String s) {
        try {
            if (s == null) return -1;
            return Integer.parseInt(s.trim());
        } catch (Exception ignore) { return -1; }
    }

    private void updateDoneCountLabel() {
        Set<Integer> solvedSet = solvedQuestionsByDifficulty.getOrDefault(difficulty.get(), Set.of());
        if (doneCountLabel != null) {
            doneCountLabel.setText("Completed: " + solvedSet.size());
        }
    }

    // submit
    private void handleSubmit() {
        List<Question> currentSet = questionsByDifficulty.getOrDefault(difficulty.get(), List.of());
        Set<Integer> solvedSet = solvedQuestionsByDifficulty.getOrDefault(difficulty.get(), new HashSet<>());
        Map<Integer, String> answersMap = solvedAnswersByDifficulty.getOrDefault(difficulty.get(), new HashMap<>());
        if (currentSet.isEmpty() || currentQuestion == null) return;

        if (solvedSet.contains(currentQuestion.getId())) return; // already done

        boolean isCorrect = false;
        String toSaveAnswer = null;

        if (currentQuestion.getType() == AnswerType.MCQ) {
            int picked = -1;
            for (int i = 0; i < mcqBox.getChildren().size(); i++) {
                RadioButton rb = (RadioButton) mcqBox.getChildren().get(i);
                if (rb.isSelected()) { picked = i; break; }
            }
            if (picked == -1) {
                openOverlay(simpleOverlayContent("Submit", "Please choose an answer first.", true));
                return;
            }
            isCorrect = (picked == currentQuestion.getCorrectIndex());
            toSaveAnswer = String.valueOf(picked);
        } else {
            String input = codeAnswerArea.getText() == null ? "" : codeAnswerArea.getText().trim();
            if (input.isEmpty()) {
                openOverlay(simpleOverlayContent("Submit", "Please write your answer first.", true));
                return;
            }
            isCorrect = isTextCorrect(currentQuestion, input);
            toSaveAnswer = input;
        }

        PauseTransition longTimeWait = new PauseTransition(Duration.seconds(1.6));

        if (isCorrect) {
            theWhyLabel.setVisible(false);

            // record solved
            solvedSet.add(currentQuestion.getId());
            answersMap.put(currentQuestion.getId(), toSaveAnswer);
            solvedQuestionsByDifficulty.put(difficulty.get(), solvedSet);
            solvedAnswersByDifficulty.put(difficulty.get(), answersMap);

            // save progress
            ProgressDAO.saveProgress(myState.getMyName(), difficulty.get(), currentQuestion.getId(), toSaveAnswer);

            // award points
            int thePoints = pointsFor(difficulty.get());
            Database.addPointsForUser(myState.getMyName(), thePoints);

            refreshPointsBadge();
            refreshPointsBreakdown();

            // save a short achievement text
            String achievementText = "Solved Q" + currentQuestion.getId() + " in " + difficulty.get() + " (+" + thePoints + " pts)";
            Database.updatePlayerAchievement(myState.getMyName(), achievementText);

            // green the button
            for (ToggleButton btn : questionButtons) {
                if (btn.getText().equals("Q" + currentQuestion.getId())) {
                    if (!btn.getStyleClass().contains("correct")) btn.getStyleClass().add("correct");
                    break;
                }
            }

            // lock inputs so no replay
            submitButton.setDisable(true);
            if (currentQuestion.getType() == AnswerType.MCQ) {
                for (Node n : mcqBox.getChildren()) n.setDisable(true);
            } else {
                codeAnswerArea.setEditable(false);
            }

            // feedback animation
            submitButton.setText("Correct");
            longTimeWait.setOnFinished(e -> submitButton.setText("Submit"));
            longTimeWait.play();

            // update completed label
            updateDoneCountLabel();

            // auto move to next unsolved
            moveToNextUnsolved();
        } else {
            theWhyLabel.setText("Why incorrect: " + currentQuestion.getWhyWrong());
            theWhyLabel.setVisible(true);
            submitButton.setText("Try Again");
            longTimeWait.setOnFinished(e -> submitButton.setText("Submit"));
            longTimeWait.play();
        }
    }

    // Checker for text answers
    private boolean isTextCorrect(Question q, String inputRaw) {
        String expectedRaw = q.getExpectedText() == null ? "" : q.getExpectedText();
        String input = canonical(inputRaw);
        String expected = canonical(expectedRaw);

        // direct canonical match
        if (input.equals(expected)) return true;

        // very small relaxations by title/id if needed
        if ("Fix integer division to 2.5".equals(q.getTitle())) {
            // Accept various casts or 5.0/2 patterns
            if (input.contains("a") && input.contains("b")) {
                if (input.contains("(double)a/b") || input.contains("a/(double)b") || input.contains("((double)a)/b"))
                    return true;
            }
            if (input.contains("5.0/2") || input.contains("5d/2")) return true;
        }
        if ("Null check before length()".equals(q.getTitle())) {
            if (input.contains("s=\"\"") && input.contains("length()")) return true;
            if (input.contains("if(s!=null)") && input.contains("s.length()")) return true;
        }
        if ("Equals not ==".equals(q.getTitle())) {
            if (input.contains("s.equals(\"hello\")")) return true;
        }
        if ("Array bounds".equals(q.getTitle())) {
            if (input.contains("i<n.length")) return true;
        }
        if ("Off-by-one (1..10 inclusive)".equals(q.getTitle())) {
            if (input.contains("i=1") && (input.contains("i<=10") || input.contains("i<11"))) return true;
        }
        if ("Immutable string toUpperCase()".equals(q.getTitle())) {
            if (input.contains("s=s.toUpperCase()")) return true;
        }
        if ("Thread-safe increment".equals(q.getTitle())) {
            if (input.contains("synchronized void inc()") || input.contains("AtomicInteger")) return true;
        }

        return false;
    }

    // collapse whitespace, remove most formatting differences
    private String canonical(String s) {
        return s.replace("\r", "")
                .replace("\n", "")
                .replace("\t", "")
                .replace(" ", "")
                .replaceAll(";+", ";")       // collapse ;; to ;
                .replaceAll("\\{\\s*", "{")
                .replaceAll("\\s*\\}", "}")
                .trim();
    }

    private void moveToNextUnsolved() {
        List<Question> set = questionsByDifficulty.getOrDefault(difficulty.get(), List.of());
        Set<Integer> solved = solvedQuestionsByDifficulty.getOrDefault(difficulty.get(), Set.of());
        if (set.isEmpty()) return;

        int idx = -1;
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i).getId() == currentQuestion.getId()) { idx = i; break; }
        }
        int n = set.size();
        for (int step = 1; step <= n; step++) {
            int nextIdx = (idx + step) % n;
            int candidate = set.get(nextIdx).getId();
            if (!solved.contains(candidate)) {
                selectedQuestion.set(candidate);
                return;
            }
        }
    }

    private void showHelp() {
        Node content = simpleOverlayContent(
                "HELP",
                String.join("\n",
                        "- Pick difficulty on left.",
                        "- Click Q1..Qn to choose a level.",
                        "- Use '?' to show a hint.",
                        "- For Easy: select the answer.",
                        "- For Medium/Hard: write the fix then Submit.",
                        "- Green Q means done. Points are saved."
                ),
                true
        );
        openOverlay(content);
    }

    private int pointsFor(String diff) {
        if ("Easy".equals(diff)) return 5;
        if ("Medium".equals(diff)) return 10;
        return 20; // Hard
    }

    // Points earned from solved set for a specific difficulty
    private int earnedPointsFor(String diff) {
        int solved = solvedQuestionsByDifficulty.getOrDefault(diff, Set.of()).size();
        return solved * pointsFor(diff);
    }

    private void refreshPointsBadge() {
        try {
            int pts = Database.getPointsForUser(myState.getMyName());
            if (pointsValueLabel != null) pointsValueLabel.setText(String.valueOf(pts));
        } catch (Exception ignore) {
            if (pointsValueLabel != null) pointsValueLabel.setText("0");
        }
    }

    // Refresh per-difficulty labels
    private void refreshPointsBreakdown() {
        if (easyPtsLabel == null) return;
        easyPtsLabel.setText("Easy: " + earnedPointsFor("Easy"));
        mediumPtsLabel.setText("Medium: " + earnedPointsFor("Medium"));
        hardPtsLabel.setText("Hard: " + earnedPointsFor("Hard"));
    }
}
