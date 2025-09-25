package com.cab302.bugco;

// one question item
public record Challenge(
        long id,
        Difficulty difficulty,
        int ordinal,
        String title,
        String prompt,
        String buggyCode,
        String correctAnswer,
        int basePoints
) {}
