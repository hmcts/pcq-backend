package uk.gov.hmcts.reform.pcqbackend.exceptions;

public class SecurityConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SecurityConfigurationException(String message) {
        super(message);
    }

    public SecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
