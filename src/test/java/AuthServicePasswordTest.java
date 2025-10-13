
import com.cab302.bugco.auth.AuthService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServicePasswordTest {

    private final AuthService auth = new AuthService();

    @Test
    void nullPassword_fails() { assertFalse(auth.isPasswordStrong(null)); }

    @Test
    void tooShort_fails() { assertFalse(auth.isPasswordStrong("Ab1!abcd"));  } // 8 chars, rule is 12+

    @Test
    void missingUpper_fails() {
        assertFalse(auth.isPasswordStrong("strong1!strong"));
    }

    @Test
    void missingLower_fails() {
        assertFalse(auth.isPasswordStrong("STRONG1!STRONG"));
    }

    @Test
    void missingDigit_fails() {
        assertFalse(auth.isPasswordStrong("Strong!Strong!"));
    }

    @Test
    void missingSpecial_fails() {
        assertFalse(auth.isPasswordStrong("Strong1Strong1"));
    }

    @Test
    void valid_passes() {
        assertTrue(auth.isPasswordStrong("Strong1!Strong1"));
    }

    @Test
    void exactlyMinLength_passes() {
        // 12 chars total: (Ab1!) x3 => includes upper, lower, digit, special
        assertTrue(auth.isPasswordStrong("Ab1!Ab1!Ab1!"));
    }

    @Test
    void oneUnderMinLength_fails() {
        // 11 chars total: (Ab1!) x2 -> technically OK but length = 11
        assertFalse(auth.isPasswordStrong("Ab1!Ab1!Ab1"));
    }

    @Test
    void veryLongStrong_passes() {
        String longPwd = "Ab1!".repeat(50); // 200 chars
        assertTrue(auth.isPasswordStrong(longPwd));
    }
}

