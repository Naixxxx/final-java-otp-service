package dev.naixxxx.guardcode.security;

import org.mindrot.jbcrypt.BCrypt;

public class Passwords {
    public String hash(String raw) {
        if (raw == null || raw.length() < 6) throw new IllegalArgumentException("Password must contain at least 6 chars");
        return BCrypt.hashpw(raw, BCrypt.gensalt(12));
    }
    public boolean matches(String raw, String hash) {
        return raw != null && hash != null && BCrypt.checkpw(raw, hash);
    }
}
