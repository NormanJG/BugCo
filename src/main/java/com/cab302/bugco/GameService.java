package com.cab302.bugco;

import java.util.List;
import java.util.Random;

public final class GameService {
    private static final GameService INSTANCE = new GameService();
    public static GameService getInstance() { return INSTANCE; }

    private final Random rnd = new Random();
    private int score = 0;

    private final List<Challenge> challenges = List.of(
            new Challenge(1, Difficulty.EASY,
                    "int x = 5;\nif (x == 10) {\n    System.out.printhln(\"Bug Found!\");\n}\nFix the bug so the program runs correctly!",
                    "==", 10),
            new Challenge(2, Difficulty.MEDIUM, "2 + 2 * 3 = ?", "8", 20),
            new Challenge(3, Difficulty.HARD, "Reverse \"abc\"", "cba", 30)
    );

    public Challenge getRandomByDifficulty(Difficulty d) {
        var pool = challenges.stream().filter(c -> c.difficulty() == d).toList();
        return pool.get(rnd.nextInt(pool.size()));
    }

    public boolean checkAnswer(Challenge c, String ans) {
        boolean ok = c.correctAnswer().equals(ans);
        if (ok) score += c.basePoints();
        return ok;
    }

    public int getScore() { return score; }
}
