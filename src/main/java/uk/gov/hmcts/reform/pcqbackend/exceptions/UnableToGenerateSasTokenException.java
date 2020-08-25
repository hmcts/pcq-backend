package uk.gov.hmcts.reform.pcqbackend.exceptions;

public class UnableToGenerateSasTokenException extends RuntimeException {
    private static final long serialVersionUID = -4L;

    public UnableToGenerateSasTokenException(Throwable exception) {
        super(exception);
    }
}
