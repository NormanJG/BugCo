import com.cab302.bugco.*;
import com.cab302.bugco.db.Database;
import com.cab302.bugco.db.DatabaseTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BugcoTerminalAppTest {

    private GameSession session;

    @BeforeEach
    void setUp() {
        // Reset in-memory DB
        DatabaseTestUtil.resetForTests();
        Database.init();

        // Prepare fresh GameSession
        Map<String, List<Question>> questionsByDifficulty = QuestionRepository.loadQuestions();
        session = new GameSession("testuser", questionsByDifficulty);
    }

    @Test
    void testCorrectSubmissionMarksProgress() {
        // Start on Medium
        session.setDifficulty("Medium");

        int qId = 1; // Medium Q1 is "Hello World"
        Question q = session.getQuestion("Medium", qId);

        boolean result = session.checkAnswer("Medium", q.getId(), "System.out.println(\"Hello World\");");
        assertTrue(result);

        assertTrue(session.getSolvedQuestions("Medium").contains(q.getId()));
    }

    @Test
    void testWrongSubmissionDoesNotSaveProgress() {
        session.setDifficulty("Medium");

        int qId = 1;
        Question q = session.getQuestion("Medium", qId);

        boolean result = session.checkAnswer("Medium", q.getId(), "wrong answer");
        assertFalse(result);

        assertFalse(session.getSolvedQuestions("Medium").contains(q.getId()));
    }

    @Test
    void testResetProgressClearsSolved() {
        session.setDifficulty("Medium");

        int qId = 1;
        Question q = session.getQuestion("Medium", qId);

        session.checkAnswer("Medium", q.getId(), "System.out.println(\"Hello World\");");
        assertTrue(session.getSolvedQuestions("Medium").contains(q.getId()));

        session.resetProgress("Medium");
        assertFalse(session.getSolvedQuestions("Medium").contains(q.getId()));
    }
}
