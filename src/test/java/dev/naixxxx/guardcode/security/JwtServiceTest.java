package dev.naixxxx.guardcode.security;

import dev.naixxxx.guardcode.domain.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    @Test
    void issuesAndVerifiesToken() {
        JwtService jwt = new JwtService("very-long-test-secret-key", 10);
        String token = jwt.issue(new SessionUser(7, "tester", UserRole.USER));
        SessionUser parsed = jwt.verify(token);
        assertEquals(7, parsed.id());
        assertEquals("tester", parsed.login());
        assertEquals(UserRole.USER, parsed.role());
    }
}
