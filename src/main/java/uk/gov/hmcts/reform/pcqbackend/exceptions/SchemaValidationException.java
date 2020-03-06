package uk.gov.hmcts.reform.pcqbackend.exceptions;

import com.networknt.schema.ValidationMessage;

import java.util.Set;

/**
 * This exception class is for capturing the JSON Schema validation.
 */
public class SchemaValidationException extends Exception {

    public static final long serialVersionUID = 432874322;

    private Set<ValidationMessage> errorSet;

    /**
     * Constructor with the error message string.
     * @param errorMessage - the error message.
     */
    public SchemaValidationException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructor to be called in case the parent exception is to be logged.
     * @param errorMessage - The error message.
     * @param exception - The parent exception.
     */
    public SchemaValidationException(String errorMessage, Throwable exception) {
        super(errorMessage, exception);
    }

    /**
     * Constructor to be called with the schema validation errors.
     * @param errorMessage - The error message thrown by the calling code.
     * @param validationErrorSet - The schema validation errors.
     */
    public SchemaValidationException(String errorMessage, Set<ValidationMessage> validationErrorSet) {
        super(errorMessage);
        errorSet = validationErrorSet;
    }

    /**
     * This method will collate all the validation messages and return a formatted String.
     * @return the validation errors formatted as a String.
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public String getFormattedError() {
        StringBuilder strBuilder = new StringBuilder();

        //noinspection RedundantExplicitVariableType
        for (ValidationMessage validationMessage : errorSet) {
            strBuilder.append(validationMessage.getMessage());
            strBuilder.append(" ; ");
        }

        return strBuilder.toString();
    }
}
