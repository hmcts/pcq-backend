package uk.gov.hmcts.reform.pcqbackend.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * This exception class is for any invalid request content including the headers
 * and parameters except the JSON Schema validation.
 */
@Getter
public class InvalidRequestException extends Exception {

    @Serial
    private static final long serialVersionUID = 43287432;

    /**
     * -- GETTER --
     *  Returns the error code for the message as passed by the caller.
     *
     * @return error Code.
     */
    private final HttpStatus errorCode;

    /**
     * Constructor with the error message string.
     * @param errorMessage - the error message.
     * @param errCode - the error code.
     */
    public InvalidRequestException(String errorMessage, HttpStatus errCode) {

        super(errorMessage);
        errorCode = errCode;
    }

    /**
     * Constructor to be called in case the parent exception is to be logged.
     * @param errorMessage - The error message.
     * @param errCode - The error code.
     * @param exception - The parent exception.
     */
    public InvalidRequestException(String errorMessage, HttpStatus errCode, Throwable exception) {

        super(errorMessage, exception);
        errorCode = errCode;
    }

}
