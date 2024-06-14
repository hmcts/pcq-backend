package uk.gov.hmcts.reform.pcqbackend.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils.getTimeFromString;

class ConversionUtilTest {

    @Test
    void convertJsonToDomainCopiesLastUpdatedTimestampFromCompletedDate() {
        PcqAnswerRequest answerRequest = new PcqAnswerRequest("123");
        answerRequest.setActor("actor");
        answerRequest.setCaseId("case-id");
        answerRequest.setChannel(1);
        String timestampAsString = "2024-05-31T13:47:32.000Z";
        answerRequest.setCompletedDate(timestampAsString);

        answerRequest.setPartyId("party-id");
        answerRequest.setServiceId("service-id");
        answerRequest.setVersionNo(1);
        answerRequest.setOptOut("Y");

        ProtectedCharacteristics pcq = ConversionUtil.convertJsonToDomain(answerRequest);
        Timestamp expectedTimestamp = getTimeFromString(timestampAsString);
        assertThat(pcq.getLastUpdatedTimestamp()).isEqualTo(expectedTimestamp);
    }
}
