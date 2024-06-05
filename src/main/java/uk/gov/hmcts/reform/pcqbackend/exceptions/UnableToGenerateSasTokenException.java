package uk.gov.hmcts.reform.pcqbackend.exceptions;

import java.io.Serial;

public class UnableToGenerateSasTokenException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4L;

    public UnableToGenerateSasTokenException(Throwable exception) {
        super(exception);
    }
}
