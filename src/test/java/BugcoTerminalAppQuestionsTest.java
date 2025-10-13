package com.cab302.bugco;

import javafx.application.Platform;
import javafx.scene.Parent;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// Tests about the question sets and the text checker in BugcoTerminalApp.
public class BugcoTerminalAppQuestionsTest {

    private static boolean jfxStarted;

    @BeforeAll
    static void startJavaFx() throws Exception {
        // Start JavaFX toolkit once for all tests (needed because createContent builds JavaFX nodes).
        if (!jfxStarted) {
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException ignore) {
                // already started
            }
            jfxStarted = true;
        }
    }

    // Run code on FX thread and wait until done.
    private static void onFx(Runnable r) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try { r.run(); }
            finally { latch.countDown(); }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX task timed out");
    }

    // Uses reflection to read the private field questionsByDifficulty.
    @SuppressWarnings("unchecked")
    private static Map<String, List<BugcoTerminalApp.Question>> getQuestionsMap(BugcoTerminalApp app) throws Exception {
        Field f = BugcoTerminalApp.class.getDeclaredField("questionsByDifficulty");
        f.setAccessible(true);
        return (Map<String, List<BugcoTerminalApp.Question>>) f.get(app);
    }

    @Test
    void questionSets_haveExpectedCounts_andTypes() throws Exception {
        BugcoTerminalApp app = new BugcoTerminalApp("Tester");

        // Build content on FX thread so initializeData() runs.
        onFx(() -> {
            Parent root = app.createContent();
            assertNotNull(root);
        });

        Map<String, List<BugcoTerminalApp.Question>> map = getQuestionsMap(app);

        // Easy: 10 MCQ (options present, expectedText null)
        List<BugcoTerminalApp.Question> easy = map.get("Easy");
        assertNotNull(easy, "Easy list missing");
        assertEquals(10, easy.size(), "Easy should have 10 questions");
        assertTrue(easy.stream().allMatch(q -> !q.getOptions().isEmpty() && q.getExpectedText() == null),
                "Easy should be MCQ only");

        // Medium: 4 TEXT (options empty, expectedText present)
        List<BugcoTerminalApp.Question> medium = map.get("Medium");
        assertNotNull(medium, "Medium list missing");
        assertEquals(4, medium.size(), "Medium should have 4 questions");
        assertTrue(medium.stream().allMatch(q -> q.getOptions().isEmpty() && q.getExpectedText() != null),
                "Medium should be TEXT only");

        // Hard: 3 TEXT (options empty, expectedText present)
        List<BugcoTerminalApp.Question> hard = map.get("Hard");
        assertNotNull(hard, "Hard list missing");
        assertEquals(3, hard.size(), "Hard should have 3 questions");
        assertTrue(hard.stream().allMatch(q -> q.getOptions().isEmpty() && q.getExpectedText() != null),
                "Hard should be TEXT only");

        // IDs inside each difficulty should be unique
        assertEquals(easy.size(), easy.stream().map(BugcoTerminalApp.Question::getId).distinct().count(), "Easy IDs duplicate?");
        assertEquals(medium.size(), medium.stream().map(BugcoTerminalApp.Question::getId).distinct().count(), "Medium IDs duplicate?");
        assertEquals(hard.size(), hard.stream().map(BugcoTerminalApp.Question::getId).distinct().count(), "Hard IDs duplicate?");
    }

    @Test
    void textChecker_accepts_equivalents_for_EqualsNotEqualsQuestion() throws Exception {
        BugcoTerminalApp app = new BugcoTerminalApp("Tester");

        // We don't need UI here, but the checker is private; we'll call it by reflection.
        Method isTextCorrect = BugcoTerminalApp.class.getDeclaredMethod(
                "isTextCorrect",
                BugcoTerminalApp.Question.class,
                String.class
        );
        isTextCorrect.setAccessible(true);

        // Build a Question with the same title the checker looks for:
        BugcoTerminalApp.Question q = new BugcoTerminalApp.Question(
                3,
                "Equals not ==",
                "String s=\"hello\"; if (s==\"hello\") System.out.println(\"Match\");",
                "Use equals()",
                "Use s.equals(\"hello\") for content compare.",
                "Medium",
                "String s=\"hello\"; if (s.equals(\"hello\")) System.out.println(\"Match\");"
        );

        // A correct fixed answer
        String exact = "String s=\"hello\"; if (s.equals(\"hello\")) System.out.println(\"Match\");";
        assertTrue((boolean) isTextCorrect.invoke(app, q, exact));

        // A correct answer with different spacing / newlines (should still pass)
        String spaced = "String s = \"hello\";\nif ( s.equals(\"hello\") ) { System.out.println(\"Match\"); }";
        assertTrue((boolean) isTextCorrect.invoke(app, q, spaced));

        // A wrong answer should fail
        String wrong = "String s=\"hello\"; if (s==\"hello\") System.out.println(\"Match\");";
        assertFalse((boolean) isTextCorrect.invoke(app, q, wrong));
    }
}
