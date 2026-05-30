package dev.naixxxx.guardcode.util;

import java.security.SecureRandom;

public class CodeFactory {
    private final SecureRandom random = new SecureRandom();

    public String numeric(int length) {
        if (length < 4 || length > 10) throw new IllegalArgumentException("Code length must be 4..10");
        StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++) out.append(random.nextInt(10));
        return out.toString();
    }
}
