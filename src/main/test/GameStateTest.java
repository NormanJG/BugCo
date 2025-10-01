package com.cab302.bugco;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void constructorSetsDefaults_andGettersSettersWork() {
        GameState gs = new GameState(null);

        assertEquals("Guest", gs.getMyName());
        assertEquals("Easy", gs.getTheDifficulty());
        assertEquals(0, gs.getTheScoreNow());

        gs.setMyName("Alice");
        gs.setTheDifficulty("Hard");
        gs.setTheScoreNow(15);

        assertEquals("Alice", gs.getMyName());
        assertEquals("Hard", gs.getTheDifficulty());
        assertEquals(15, gs.getTheScoreNow());
    }

    @Test
    void addScoreAddsOnlyNonNegative_andAccumulates() {
        GameState gs = new GameState("Bob");

        gs.addScore(10);
        assertEquals(10, gs.getTheScoreNow());

        gs.addScore(0);
        assertEquals(10, gs.getTheScoreNow());

        gs.addScore(-50);
        assertEquals(10, gs.getTheScoreNow());

        gs.addScore(5);
        assertEquals(15, gs.getTheScoreNow());
    }
}
