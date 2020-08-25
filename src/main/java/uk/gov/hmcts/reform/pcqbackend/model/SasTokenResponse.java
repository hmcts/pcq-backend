package uk.gov.hmcts.reform.pcqbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SasTokenResponse implements Serializable {

    public static final long serialVersionUID = 1328741L;

    @JsonProperty("sas_token")
    private String sasToken;

    public SasTokenResponse(String sasToken) {
        this.sasToken = sasToken;
    }
}
