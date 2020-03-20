package uk.gov.hmcts.reform.pcqbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings({"PMD.TooManyFields"})
public class PcqAnswerResponse implements Serializable {

    public static final long serialVersionUID = 1328743L;

    @JsonProperty("pcq_id")
    private String pcqId;

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("party_id")
    private String partyId;

    @JsonProperty("channel")
    private Integer channel;

    @JsonProperty("completed_date")
    private String completedDate;

    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("actor")
    private Integer actor;

    @JsonProperty("version_number")
    private Integer versionNo;

    private PcqAnswers pcqAnswers;

}
