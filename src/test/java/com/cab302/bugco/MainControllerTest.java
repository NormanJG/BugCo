package com.cab302.bugco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class MainControllerTest {

    private MainController controller;

    @BeforeEach
    void setUp() {
        controller = new MainController();
    }

    @Test
    void testCodeAnswerValidation() {
        assertTrue(controller.validateAnswer("System.out.println(\"Hello World\");", 0));
        assertTrue(controller.validateAnswer("int num = 5;\nSystem.out.println(num);", 1));
        assertTrue(controller.validateAnswer("System.out.println( \"Hello World\" );", 0));
        assertEquals("System.out.println(\"Hello World\");", controller.getCorrectAnswer(0));
        assertEquals("input", controller.getQuestionType(0));
    }

    @Test
    void testCodeAnswerRandomWrongOutput() {
        assertFalse(controller.validateAnswer("println(\"Hello World\");", 0));
        assertFalse(controller.validateAnswer("System.out.print(\"Hello World\");", 0));
        assertFalse(controller.validateAnswer("System.out.println('Hello World');", 0));
        assertFalse(controller.validateAnswer("console.log(\"Hello World\");", 0));
        assertFalse(controller.validateAnswer("System.out.println(\"Hello World\")", 0));
    }

    @Test
    void testMultipleChoiceValidation() {
        assertTrue(controller.validateAnswer("B", 3));
        assertTrue(controller.validateAnswer("b", 3));
        assertTrue(controller.validateAnswer("6", 4));
        assertTrue(controller.validateAnswer("A", 5));
        assertTrue(controller.validateAnswer("a", 5));
        assertEquals("choice", controller.getQuestionType(3));
    }

    @Test
    void testMultipleChoiceRandomWrongOutput() {
        assertFalse(controller.validateAnswer("A", 3));
        assertFalse(controller.validateAnswer("C", 3));
        assertFalse(controller.validateAnswer("D", 3));
        assertFalse(controller.validateAnswer("3", 4));
        assertFalse(controller.validateAnswer("5", 4));
        assertFalse(controller.validateAnswer("B", 5));
        assertFalse(controller.validateAnswer("C", 5));
    }

    @Test
    void testScoreCalculationCorrect() {
        assertEquals(0, controller.getCurrentScore());
        controller.addScore(10);
        assertEquals(10, controller.getCurrentScore());
        controller.addScore(20);
        assertEquals(30, controller.getCurrentScore());
        controller.setCurrentScore(60);
        assertEquals(60, controller.getCurrentScore());
    }

    @Test
    void testScoreCalculationWrong() {
        controller.setCurrentScore(20);
        assertEquals(20, controller.getCurrentScore());
        controller.addScore(0);
        assertEquals(20, controller.getCurrentScore());
        controller.setCurrentScore(0);
        assertTrue(controller.getCurrentScore() >= 0);
        assertFalse(controller.validateAnswer("wrong", 0));
    }
}