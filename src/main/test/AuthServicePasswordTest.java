
import com.cab302.bugco.auth.AuthService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServicePasswordTest {

    private final AuthService auth = new AuthService();

    @Test
    void tooShort_fails() {
        assertFalse(auth.isPasswordStrong("Ab1!abcd")); // 8 chars, rule is 12+
    }

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
}

