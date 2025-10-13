import com.cab302.bugco.db.Database;
import com.cab302.bugco.db.DatabaseTestUtil;
import com.cab302.bugco.db.ProgressDAO;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class ProgressDAOTest {

    @BeforeEach
    void reset() {
        DatabaseTestUtil.resetForTests();
        Database.init();
    }

    @Test
    void testSaveAndLoadProgress() {
        ProgressDAO.saveProgress("testuser", "Easy", 1, "1");
        var progress = ProgressDAO.loadProgress("testuser");
        assertEquals("1", progress.get("Easy").get(1));
    }

    @Test
    void testResetProgress() {
        ProgressDAO.saveProgress("testuser", "Medium", 3, "answer");
        ProgressDAO.resetProgress("testuser", "Medium");
        assertFalse(ProgressDAO.loadProgress("testuser").containsKey("Medium"));
    }
}
