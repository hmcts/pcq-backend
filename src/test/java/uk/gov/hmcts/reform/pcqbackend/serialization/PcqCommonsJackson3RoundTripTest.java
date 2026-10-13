package uk.gov.hmcts.reform.pcqbackend.serialization;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PcqCommonsJackson3RoundTripTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void preservesAllPcqAnswerFieldsWhenUsingJackson3() throws Exception {
        String json = """
            {
              "pcqId": "pcq-123",
              "dcnNumber": "dcn-123",
              "formId": "form-123",
              "ccdCaseId": "case-123",
              "partyId": "party-123",
              "channel": 2,
              "completedDate": "2026-09-29T12:34:56Z",
              "serviceId": "service-123",
              "actor": "actor-123",
              "versionNo": 1,
              "optOut": "N",
              "pcqAnswers": {
                "dob_provided": 1,
                "dob": "1990-01-02",
                "language_main": 1,
                "language_other": "Welsh",
                "english_language_level": 2,
                "sex": 3,
                "gender_different": 4,
                "gender_other": "Other gender",
                "sexuality": 5,
                "sexuality_other": "Other sexuality",
                "marriage": 6,
                "ethnicity": 7,
                "ethnicity_other": "Other ethnicity",
                "religion": 8,
                "religion_other": "Other religion",
                "disability_conditions": 9,
                "disability_impact": 10,
                "disability_vision": 11,
                "disability_hearing": 12,
                "disability_mobility": 13,
                "disability_dexterity": 14,
                "disability_learning": 15,
                "disability_memory": 16,
                "disability_mental_health": 17,
                "disability_stamina": 18,
                "disability_social": 19,
                "disability_other": 20,
                "disability_other_details": "Other disability details",
                "disability_none": 21,
                "pregnancy": 22,
                "opt_out": true
              }
            }
            """;

        JsonNode input = objectMapper.readTree(json);
        PcqAnswerRequest request = objectMapper.readValue(json, PcqAnswerRequest.class);
        JsonNode roundTripped = objectMapper.valueToTree(request);

        assertThat(roundTripped).isEqualTo(input);
    }
}
