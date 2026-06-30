package uk.gov.hmcts.reform.pcqbackend.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class SecurityConfigurationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Test message";
        SecurityConfigurationException ex = new SecurityConfigurationException(message);
        assertEquals(message, ex.getMessage(), "Message should match the one provided in the constructor");
        assertNull(ex.getCause(), "Cause should be null when not provided in the constructor");
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Test message";
        Throwable cause = new RuntimeException("Cause");
        SecurityConfigurationException ex = new SecurityConfigurationException(message, cause);
        assertEquals(message, ex.getMessage(),  "Message should match the one provided in the constructor");
        assertEquals(cause, ex.getCause(), "Cause should match the one provided in the constructor");
    }
}
