import com.cab302.bugco.Question;
import com.cab302.bugco.QuestionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QuestionRepositoryTest {

    @Test
    void testLoadQuestionsByDifficulty() {
        Map<String, List<Question>> repo = QuestionRepository.loadQuestions();

        assertTrue(repo.containsKey("Easy"));
        assertTrue(repo.containsKey("Medium"));
        assertTrue(repo.containsKey("Hard"));

        assertFalse(repo.get("Easy").isEmpty());
        assertEquals("Missing Semicolon", repo.get("Medium").get(0).getTitle());
    }
}
