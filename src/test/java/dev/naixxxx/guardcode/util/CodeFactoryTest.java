package dev.naixxxx.guardcode.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeFactoryTest {
    @Test
    void createsNumericCodeWithRequestedLength() {
        String code = new CodeFactory().numeric(6);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
    }

    @Test
    void rejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> new CodeFactory().numeric(2));
        assertThrows(IllegalArgumentException.class, () -> new CodeFactory().numeric(11));
    }
}
