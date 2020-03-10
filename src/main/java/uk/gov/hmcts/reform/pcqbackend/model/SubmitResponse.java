package uk.gov.hmcts.reform.pcqbackend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
public class SubmitResponse implements Serializable {

    public static final long serialVersionUID = 5328745L;

    private int pcqId;

    private String responseStatus;

    private String responseStatusCode;

}
