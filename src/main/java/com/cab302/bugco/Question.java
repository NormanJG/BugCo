package com.cab302.bugco;

public class Question {
    private final int id;
    private final String title;
    private final String description;   // buggy code or explanation
    private final String hint;          // shown only when ? clicked
    private final String difficulty;
    private final String expectedAnswer;

    public Question(int id, String title, String description, String hint, String difficulty, String expectedAnswer) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.hint = hint;
        this.difficulty = difficulty;
        this.expectedAnswer = expectedAnswer;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getHint() {
        return hint;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }
}