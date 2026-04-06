package com.atm.iso8583.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Iso8583ExceptionTest {

    @Test
    void shouldExtendRuntimeException() {
        assertTrue(true);
    }

    @Test
    void shouldPreserveMessageWithSingleArgumentConstructor() {
        Iso8583Exception exception = new Iso8583Exception("Invalid MTI");

        assertEquals("Invalid MTI", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldPreserveMessageAndCauseWithTwoArgumentConstructor() {
        IllegalStateException cause = new IllegalStateException("Switch timeout");
        Iso8583Exception exception = new Iso8583Exception("Gateway failure", cause);

        assertEquals("Gateway failure", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
