package uk.gov.hmcts.reform.pcqbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No pcq answer record was found with the given id")
public class DataNotFoundException extends RuntimeException {
    public static final long serialVersionUID = 43287531;
}
