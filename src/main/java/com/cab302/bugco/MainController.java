package com.cab302.bugco;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;

public class MainController {

    @FXML private TextField usernameField;
    @FXML private ImageView logoImageView;
    @FXML private Label scoreLabel;
    @FXML private GridPane questionsGrid;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private Label questionTitle;
    @FXML private TextArea codeDisplay;
    @FXML private TextArea answerInput;
    @FXML private Button submitBtn;
    @FXML private Label questionNumber;
    @FXML private Label questionDifficulty;

    private int currentScore = 0;
    private int currentQuestionNum = 1;
    private ArrayList<Button> questionButtons = new ArrayList<>();

    private String[] questionTitles = {
            "Missing Semicolon",
            "Wrong Variable Name",
            "Missing Colon",
            "Choose the Loop Type",
            "What Prints Here?",
            "Which is Correct?"
    };

    private String[] questionTypes = {
            "input", "input", "input", "choice", "choice", "choice"
    };

    private String[] buggyCode = {
            "System.out.println(\"Hello World\")",
            "int num = 5;\nSystem.out.println(number);",
            "if (x == 10)\n    System.out.println(\"Found!\");",
            "Which loop runs a specific number of times?\nA) while loop\nB) for loop\nC) do-while loop",
            "int x = 3;\nint y = x * 2;\nSystem.out.println(y);\n\nWhat does this print?",
            "Which declares an integer variable?\nA) int x = 5;\nB) string x = 5;\nC) float x = 5;"
    };

    private String[] correctAnswers = {
            "System.out.println(\"Hello World\");",
            "int num = 5;\nSystem.out.println(num);",
            "if (x == 10):\n    System.out.println(\"Found!\");",
            "B",
            "6",
            "A"
    };

    private String[] choiceOptions = {
            "", "", "",
            "A) while loop|B) for loop|C) do-while loop",
            "A) 3|B) 5|C) 6",
            "A) int x = 5;|B) string x = 5;|C) float x = 5;"
    };

    @FXML
    public void initialize() {
        setupUI();
        loadFirstQuestion();
    }

    private void setupUI() {
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard");
        difficultyCombo.setValue("Easy");

        for(int i = 0; i < 6; i++) {
            Button btn = new Button((i + 1) + "...");
            btn.setPrefSize(60, 30);
            final int questionIndex = i;
            btn.setOnAction(e -> loadQuestion(questionIndex));
            questionsGrid.add(btn, i % 3, i / 3);
            questionButtons.add(btn);
        }

        try {
            Image logo = new Image(getClass().getResourceAsStream("bugco.png"));
            logoImageView.setImage(logo);
            logoImageView.setFitWidth(150);
            logoImageView.setFitHeight(150);
            logoImageView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Logo loading failed: " + e.getMessage());
        }

        updateScore();
    }

    private void loadFirstQuestion() {
        loadQuestion(0);
    }

    private void loadQuestion(int index) {
        if(index < questionTitles.length) {
            currentQuestionNum = index + 1;
            questionNumber.setText("Question " + currentQuestionNum + ":");
            questionDifficulty.setText("Easy:");
            questionTitle.setText(questionTitles[index]);
            codeDisplay.setText(buggyCode[index]);
            answerInput.clear();

            if (questionTypes[index].equals("choice")) {
                answerInput.setPromptText("Enter A, B, or C...");
            } else {
                answerInput.setPromptText("Fix the code...");
            }

            for(int i = 0; i < questionButtons.size(); i++) {
                if(i == index) {
                    questionButtons.get(i).setStyle("-fx-background-color: #4ade80; -fx-text-fill: black;");
                } else {
                    questionButtons.get(i).setStyle("-fx-background-color: transparent; -fx-text-fill: #4ade80;");
                }
            }
        }
    }

    @FXML
    private void handleSubmit() {
        String userAnswer = answerInput.getText().trim();
        String expected = correctAnswers[currentQuestionNum - 1];
        String questionType = questionTypes[currentQuestionNum - 1];

        boolean isCorrect = false;

        if (questionType.equals("choice")) {
            isCorrect = userAnswer.equalsIgnoreCase(expected);
        } else {
            isCorrect = userAnswer.equals(expected) ||
                    userAnswer.replaceAll("\\s+", "").equals(expected.replaceAll("\\s+", ""));
        }

        if(isCorrect) {
            currentScore += 10;
            updateScore();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Correct!");
            alert.setContentText("Good job! You earned 10 points.");
            alert.showAndWait();

            questionButtons.get(currentQuestionNum - 1).setStyle("-fx-background-color: #22c55e;");

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Try Again");
            if (questionType.equals("choice")) {
                alert.setContentText("Wrong answer. Try A, B, or C!");
            } else {
                alert.setContentText("That's not quite right. Check for missing punctuation!");
            }
            alert.showAndWait();
        }
    }

    private void updateScore() {
        scoreLabel.setText(currentScore + " points");
    }
}