package dev.naixxxx.guardcode.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordsTest {
    @Test
    void hashesAndChecksPassword() {
        Passwords passwords = new Passwords();
        String hash = passwords.hash("secret123");
        assertNotEquals("secret123", hash);
        assertTrue(passwords.matches("secret123", hash));
        assertFalse(passwords.matches("wrong", hash));
    }
}
