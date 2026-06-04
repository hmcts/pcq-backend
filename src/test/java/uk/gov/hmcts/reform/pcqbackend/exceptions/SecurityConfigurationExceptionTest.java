package uk.gov.hmcts.reform.pcqbackend.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

class SecurityConfigurationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Test message";
        SecurityConfigurationException ex = new SecurityConfigurationException(message);
        assertEquals(message, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Test message";
        Throwable cause = new RuntimeException("Cause");
        SecurityConfigurationException ex = new SecurityConfigurationException(message, cause);
        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}

