
import com.cab302.bugco.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerUsernameTest {

    private AuthController controller;
    private Method isUsernameValid;

    @BeforeEach
    void setup() throws Exception {
        controller = new AuthController();
        isUsernameValid = AuthController.class.getDeclaredMethod("isUsernameValid", String.class);
        isUsernameValid.setAccessible(true);
    }

    private boolean call(String s) {
        try {
            return (boolean) isUsernameValid.invoke(controller, s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void accepts_1_to_20_alnum() {
        assertTrue(call("A"));
        assertTrue(call("Alice01"));
        assertTrue(call("Z9"));
        assertTrue(call("User1234567890123")); // 16 chars
        assertTrue(call("A2345678901234567890")); // 20 chars
    }

    @Test
    void rejects_empty_or_over_20() {
        assertFalse(call(""));
        assertFalse(call("A23456789012345678901")); // 21 chars
    }

    @Test
    void rejects_specials_and_whitespace() {
        assertFalse(call("bob_the_builder"));
        assertFalse(call("john.doe"));
        assertFalse(call("alice!"));
        assertFalse(call("has space"));
        assertFalse(call(" tab\tname"));
        assertFalse(call("newline\nname"));
    }

    @Test
    void rejects_nonAsciiLetters() {
        assertFalse(call("Üser"));
        assertFalse(call("名字123"));
    }
}