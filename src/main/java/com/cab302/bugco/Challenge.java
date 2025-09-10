package com.cab302.bugco;

public record Challenge(int id, Difficulty difficulty, String prompt, String correctAnswer, int basePoints) {
}
