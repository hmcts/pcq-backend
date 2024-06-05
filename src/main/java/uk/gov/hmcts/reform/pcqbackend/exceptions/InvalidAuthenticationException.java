package uk.gov.hmcts.reform.pcqbackend.exceptions;

import java.io.Serial;

public class InvalidAuthenticationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4L;

    public InvalidAuthenticationException(String message) {
        super(message);
    }
}
