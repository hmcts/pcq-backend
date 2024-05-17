package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcq.commons.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class PcqRecordWithoutCaseTest extends PcqBaseFunctionalTest {

    public static final String RESPONSE_KEY_1 = "pcqRecord";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_OK = "200";
    public static final String HTTP_CREATED = "201";
    public static final String RESPONSE_SUCCESS_MSG = "Success";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";

    private static final String STATUS_CODE_INVALID_MSG = "Response Status Code not valid";
    private static final String STATUS_INVALID_MSG = "Response Status not valid";

    @Test
    @SuppressWarnings("unchecked")
    public void testPcqRecordWithoutCase() throws IOException {
        //Create 2 records in database with the current completed date.
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        String firstUuid = generateUuid();
        answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));
        answerRequest.setPcqId(firstUuid);

        Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        //Prepare for clearing down.
        clearTestPcqAnswers.add(answerRequest);

        String secondUuid = generateUuid();
        answerRequest = jsonObjectFromString(jsonStringRequest);
        answerRequest.setPcqId(secondUuid);
        answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

        response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        //Prepare for clearing down.
        clearTestPcqAnswers.add(answerRequest);

        //Create 3rd record in database with a completed date past the limit.
        String thirdUuid = generateUuid();
        answerRequest = jsonObjectFromString(jsonStringRequest);
        answerRequest.setPcqId(thirdUuid);
        answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
            Integer.parseInt(daysLimit) + 1L)));

        response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        //Prepare for clearing down.
        clearTestPcqAnswers.add(answerRequest);

        //Now call the pcqWithoutCaseAPI
        PcqRecordWithoutCaseResponse getResponse = pcqBackEndServiceClient.getAnswerRecordsWithoutCase(
            HttpStatus.OK);

        assertEquals(HTTP_OK, getResponse.getResponseStatusCode(), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_SUCCESS_MSG, getResponse.getResponseStatus(), STATUS_INVALID_MSG);

        PcqAnswerResponse[] answerResponses = getResponse.getPcqRecord();
        List<String> pcqIdsInResponse = new ArrayList<>(3);
        for (PcqAnswerResponse answerResponse : answerResponses) {
            pcqIdsInResponse.add(answerResponse.getPcqId());
        }

        assertTrue(pcqIdsInResponse.contains(firstUuid), "First PCQ Id should have been picked up");
        assertTrue(pcqIdsInResponse.contains(secondUuid), "Second PCQ Id should have been picked up");
        assertFalse(pcqIdsInResponse.contains(thirdUuid), "Third PCQ Id should not have been picked up");

    }
}
