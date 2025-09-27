import com.cab302.bugco.GameSession;
import com.cab302.bugco.QuestionRepository;
import com.cab302.bugco.Question;
import com.cab302.bugco.db.Database;
import com.cab302.bugco.db.DatabaseTestUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    private GameSession session;

    @BeforeEach
    void setUp() {
        // Force fresh in-memory DB
        DatabaseTestUtil.resetForTests();

        // Ensure schema is created
        Database.init();

        Map<String, List<Question>> questionsByDifficulty = QuestionRepository.loadQuestions();
        session = new GameSession("testuser", questionsByDifficulty);
    }

    // ---------------- Example Test ----------------
    @Test
    void testHelloWorldAnswer() {
        // First run
        assertTrue(session.checkAnswer("Medium", 1, "System.out.println(\"Hello World\");"));
        session.resetProgress("Medium");
        assertTrue(session.checkAnswer("Medium", 1, "System.out.println( \"Hello World\" );")); // extra spaces
        session.resetProgress("Medium");
        assertFalse(session.checkAnswer("Medium", 1, "println(\"Hello World\");"));
    }

    @Test
    void testWrongVariableName() {
        assertTrue(session.checkAnswer("Medium", 2, "int num = 5;\nSystem.out.println(num);"));
        session.resetProgress("Medium");
        assertFalse(session.checkAnswer("Medium", 2, "int num = 5;\nSystem.out.println(number);"));
    }

    @Test
    void testMissingColon() {
        assertTrue(session.checkAnswer("Medium", 3, "if (x == 10):\n    System.out.println(\"Found!\");"));
        session.resetProgress("Medium");
        assertFalse(session.checkAnswer("Medium", 3, "if (x == 10)\n    System.out.println(\"Found!\");"));
    }

    // ---------------- Multiple Choice ----------------
    @Test
    void testLoopTypeQuestion() {
        assertTrue(session.checkAnswer("Medium", 4, "B"));
        session.resetProgress("Medium");
        assertTrue(session.checkAnswer("Medium", 4, "b"));
        session.resetProgress("Medium");
        assertFalse(session.checkAnswer("Medium", 4, "A"));
        session.resetProgress("Medium");
        assertFalse(session.checkAnswer("Medium", 4, "C"));
    }

    @Test
    void testWhatPrintsHere() {
        assertTrue(session.checkAnswer("Medium", 5, "6"));
        session.resetProgress("Medium");
        assertFalse(session.checkAnswer("Medium", 5, "5"));
        session.resetProgress("Medium");
        assertFalse(session.checkAnswer("Medium", 5, "x * 2"));
    }

    @Test
    void testWhichIsCorrect() {
        assertTrue(session.checkAnswer("Medium", 6, "A"));
        session.resetProgress("Medium");
        assertTrue(session.checkAnswer("Medium", 6, "a"));
        session.resetProgress("Medium");
        assertFalse(session.checkAnswer("Medium", 6, "B"));
        session.resetProgress("Medium");
        assertFalse(session.checkAnswer("Medium", 6, "C"));
    }
}
