package uk.gov.hmcts.reform.pcqbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {

    @JsonProperty("message")
    public final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }
}
