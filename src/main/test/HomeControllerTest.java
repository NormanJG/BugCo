import com.cab302.bugco.HomeController;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HomeControllerTest {
    private final HomeController controller = new HomeController();

    @Test
    void testPasswordTooShort() {
        assertFalse(controller.isPasswordStrong("Ab1acaefc####!"));
    }

    @Test
    void testPasswordMissingUppercase() {
        assertFalse(controller.isPasswordStrong("abcd1234!"));
    }

    @Test
    void testPasswordMissingLowercase() {
        assertFalse(controller.isPasswordStrong("ABCD1234!"));
    }

    @Test
    void testPasswordMissingNumber() {
        assertFalse(controller.isPasswordStrong("Abcdefg!"));
    }

    @Test
    void testPasswordMissingSpecialChar() {
        assertFalse(controller.isPasswordStrong("Abcdefg1"));
    }

    @Test
    void testValidPassword() {
        assertTrue(controller.isPasswordStrong("Strong1!"));
    }
}
