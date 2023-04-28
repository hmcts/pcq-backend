package uk.gov.hmcts.reform.pcqbackend.exceptionhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.pcq.commons.model.ErrorResponse;
import uk.gov.hmcts.reform.pcqbackend.exceptions.InvalidAuthenticationException;
import uk.gov.hmcts.reform.pcqbackend.exceptions.UnableToGenerateSasTokenException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.ResponseEntity.status;

@Slf4j
@ControllerAdvice
public class ResponseExceptionHandler {

    @ExceptionHandler(UnableToGenerateSasTokenException.class)
    protected ResponseEntity<ErrorResponse> handleUnableToGenerateSasTokenException() {
        return status(INTERNAL_SERVER_ERROR).body(new ErrorResponse("Exception occurred while generating SAS Token"));
    }

    @ExceptionHandler(InvalidAuthenticationException.class)
    protected ResponseEntity<Void> handleUnAuthenticatedException() {
        return status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(InvalidTokenException.class)
    protected ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException exc) {
        return status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(exc.getMessage()));
    }
}
