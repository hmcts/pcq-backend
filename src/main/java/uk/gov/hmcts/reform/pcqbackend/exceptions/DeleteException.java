package uk.gov.hmcts.reform.pcqbackend.exceptions;

import java.io.Serial;

public class DeleteException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public DeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
