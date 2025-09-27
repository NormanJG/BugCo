package com.cab302.bugco;

import com.cab302.bugco.db.Database;
import com.cab302.bugco.db.ProgressDAO;

import java.util.*;

public class GameSession {

    private final String username;
    private final Map<String, List<Question>> questionsByDifficulty;
    private final Map<String, Set<Integer>> solvedQuestionsByDifficulty = new HashMap<>();
    private final Map<String, Map<Integer, String>> solvedAnswersByDifficulty = new HashMap<>();

    private String currentDifficulty = "Easy";
    private int currentQuestionId = 1;

    // --- Canonical expected answers ---
    private static final Map<Integer, String> expectedEasyAnswers = new HashMap<>();
    private static final Map<Integer, String> expectedMediumAnswers = new HashMap<>();
    private static final Map<Integer, String> expectedHardAnswers = new HashMap<>();

    static {
        // Easy: expected answers are just "1", "2", ..., "9"
        for (int i = 1; i <= 9; i++) {
            expectedEasyAnswers.put(i, String.valueOf(i));
        }

        // Medium:
        expectedMediumAnswers.put(1, "System.out.println(\"Hello World\");");
        expectedMediumAnswers.put(2, "int num = 5;\nSystem.out.println(num);");
        expectedMediumAnswers.put(3, "if (x == 10):\n    System.out.println(\"Found!\");");
        expectedMediumAnswers.put(4, "B");
        expectedMediumAnswers.put(5, "6");
        expectedMediumAnswers.put(6, "A");

        // Hard:
        expectedHardAnswers.put(1, "for (int i = 1; i <= 10; i++) {\n    System.out.println(i);\n}");
        expectedHardAnswers.put(2, "String s = \"\";\nSystem.out.println(s.length());");
        expectedHardAnswers.put(3, "String s = \"hello\";\nif (s.equals(\"hello\")) {\n    System.out.println(\"Match!\");\n}");
        expectedHardAnswers.put(4, "int a = 5, b = 2;\nSystem.out.println((double) a / b);");
        expectedHardAnswers.put(5, "int[] nums = {1,2,3};\nfor (int i = 0; i < nums.length; i++) {\n    System.out.println(nums[i]);\n}");
        expectedHardAnswers.put(6, "String s = \"hello\";\ns = s.toUpperCase();\nSystem.out.println(s);");
        expectedHardAnswers.put(7, "if (Math.abs((0.1 + 0.2) - 0.3) < 1e-9) {\n    System.out.println(\"Equal\");\n}");
        expectedHardAnswers.put(8, "class Counter {\n    private int count = 0;\n    public synchronized void increment() {\n        count++;\n    }\n}");
        expectedHardAnswers.put(9,
                """
                        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
                            String line = br.readLine();
                            System.out.println(line);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }""");
    }

    public GameSession(String username, Map<String, List<Question>> questionsByDifficulty) {
        this.username = username;
        this.questionsByDifficulty = questionsByDifficulty;

        // Load saved progress from DB
        Map<String, Map<Integer, String>> saved = ProgressDAO.loadProgress(username);

        for (String diff : List.of("Easy", "Medium", "Hard")) {
            Map<Integer, String> answers = saved.getOrDefault(diff, Map.of());
            solvedQuestionsByDifficulty.put(diff, new HashSet<>(answers.keySet()));
            solvedAnswersByDifficulty.put(diff, new HashMap<>(answers));
        }
    }

    // --- Gameplay state management ---

    public void setDifficulty(String difficulty) {
        this.currentDifficulty = difficulty;
        this.currentQuestionId = 1; // reset to first question of new difficulty
    }

    public String getDifficulty() {
        return currentDifficulty;
    }


    public Question getQuestion(String difficulty, int questionId) {
        return questionsByDifficulty
                .getOrDefault(difficulty, List.of())
                .stream()
                .filter(q -> q.getId() == questionId)
                .findFirst()
                .orElse(null);
    }

    public List<Question> getQuestionsByDifficulty(String difficulty) {
        return questionsByDifficulty.getOrDefault(difficulty, List.of());
    }

    // --- Answer logic ---

    public boolean checkAnswer(String difficulty, int questionId, String input) {
        Question q = getQuestion(difficulty, questionId);
        if (q == null) return false;

        Set<Integer> solvedSet = solvedQuestionsByDifficulty.get(difficulty);
        Map<Integer, String> answersMap = solvedAnswersByDifficulty.get(difficulty);

        if (solvedSet.contains(q.getId())) return false; // already solved

        String expected;
        boolean isCorrect = false;

        if ("Easy".equals(difficulty)) {
            expected = expectedEasyAnswers.get(q.getId());
            isCorrect = expected != null &&
                    normalize(expected).equals(normalize(input));
        } else if ("Medium".equals(difficulty)) {
            expected = expectedMediumAnswers.get(q.getId());
            isCorrect = expected != null &&
                    normalize(expected).equals(normalize(input));
        } else if ("Hard".equals(difficulty)) {
            expected = expectedHardAnswers.get(q.getId());
            isCorrect = expected != null &&
                    normalize(expected).equals(normalize(input));
        }

        if (isCorrect) {
            solvedSet.add(q.getId());
            answersMap.put(q.getId(), input);

            // Persist progress
            ProgressDAO.saveProgress(username, difficulty, q.getId(), input);

            // Update leaderboard
            String achievementText = "Solved Q" + q.getId() + " in " + difficulty;
            Database.updatePlayerAchievement(username, achievementText);

            // Advance to next question
            List<Question> currentSet = getQuestionsByDifficulty(difficulty);
            int currentIndex = currentSet.indexOf(q);
            int nextIndex = (currentIndex + 1) % currentSet.size();
            currentQuestionId = currentSet.get(nextIndex).getId();
        }

        return isCorrect;
    }

    // --- Reset logic ---

    public void resetProgress(String difficulty) {
        solvedQuestionsByDifficulty.get(difficulty).clear();
        solvedAnswersByDifficulty.get(difficulty).clear();

        ProgressDAO.resetProgress(username, difficulty);

        this.currentDifficulty = difficulty;
        this.currentQuestionId = 1;
    }

    // --- Helpers ---

    public Map<Integer, String> getSolvedAnswers(String difficulty) {
        return solvedAnswersByDifficulty.getOrDefault(difficulty, Map.of());
    }

    public Set<Integer> getSolvedQuestions(String difficulty) {
        return solvedQuestionsByDifficulty.getOrDefault(difficulty, Set.of());
    }

    private String normalize(String code) {
        if (code == null) return "";
        return code
                .replaceAll("\\s+", "")   // remove whitespace
                .toLowerCase();           // ignore case
    }
}
