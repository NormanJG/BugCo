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

    @Test
    void validUsernames() {
        assertTrue(controller.isUsernameValid("A"));
        assertTrue(controller.isUsernameValid("Alice01"));
        assertTrue(controller.isUsernameValid("Z9"));
        assertTrue(controller.isUsernameValid("User1234567890123"));
        assertTrue(controller.isUsernameValid("A2345678901234567890"));
    }

    @Test
    void invalidWhenEmptyOrTooLong() {
        assertFalse(controller.isUsernameValid(""));
        assertFalse(controller.isUsernameValid("A23456789012345678901"));
    }

    @Test
    void invalidWithSpecialCharacters() {
        assertFalse(controller.isUsernameValid("bob_the_builder"));
        assertFalse(controller.isUsernameValid("alice!"));
        assertFalse(controller.isUsernameValid("john.doe"));
        assertFalse(controller.isUsernameValid(" space "));
        assertFalse(controller.isUsernameValid("Üser"));
    }

    @Test
    void invalidWithWhitespace() {
        assertFalse(controller.isUsernameValid("has space"));
        assertFalse(controller.isUsernameValid(" tab\tname"));
        assertFalse(controller.isUsernameValid("newline\nname"));
    }
}
