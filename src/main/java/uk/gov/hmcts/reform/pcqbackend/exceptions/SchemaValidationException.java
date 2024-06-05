package uk.gov.hmcts.reform.pcqbackend.exceptions;


import lombok.Getter;

import java.io.Serial;

/**
 * This exception class is for capturing the JSON Schema validation.
 */
@Getter
public class SchemaValidationException extends Exception {

    @Serial
    private static final long serialVersionUID = 432874322;

    /**
     * -- GETTER --
     *  This method will collate all the validation messages and return a formatted String.
     *
     * @return the validation errors formatted as a String.
     */
    private final String formattedError;

    /**
     * Constructor to be called with the schema validation errors.
     * @param errorMessage - The error message thrown by the calling code.
     * @param formattedErrorMessage - The schema validation errors.
     */
    public SchemaValidationException(String errorMessage, String formattedErrorMessage) {
        super(errorMessage);
        formattedError = formattedErrorMessage;
    }

}
