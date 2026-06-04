package uk.gov.hmcts.reform.pcqbackend.exceptions;

public class SecurityConfigurationException extends RuntimeException {
    public SecurityConfigurationException(String message) {
        super(message);
    }

    public SecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
