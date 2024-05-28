package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class OptOutPcqAnswersTest extends PcqBaseFunctionalTest {

    public static final String RESPONSE_KEY_1 = "pcqId";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_CREATED = "201";
    public static final String HTTP_OK = "200";
    public static final String HTTP_BAD_REQUEST = "400";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";
    public static final String RESPONSE_UPDATED_MSG = "Successfully updated";
    public static final String RESPONSE_OK_MSG = "Success";
    public static final String RESPONSE_INVALID_MSG = "Invalid Request";
    public static final String RESPONSE_INVALID_STATUS_CODE = "Response Status Code not valid";

    @Test
    public void optOutPcqAnswers() throws IOException {
        //Create a record before updating.
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerOptOutNull.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        answerRequest.setPcqId(generateUuid());
        Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), RESPONSE_INVALID_STATUS_CODE);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), "Response Status valid");

        //invoke the submitAnswers to Opt Out the record.
        jsonStringRequest = jsonStringFromFile("JsonTestFiles/OptOutSubmitAnswer.json");
        PcqAnswerRequest optOutAnswerRequest = jsonObjectFromString(jsonStringRequest);

        //Use the same PCQ ID as above
        optOutAnswerRequest.setPcqId(answerRequest.getPcqId());

        response = pcqBackEndServiceClient.updateAnswersRecord(optOutAnswerRequest, HttpStatus.OK);

        assertEquals("200", response.get(RESPONSE_KEY_2), RESPONSE_INVALID_STATUS_CODE);
        assertEquals(RESPONSE_UPDATED_MSG, response.get(RESPONSE_KEY_3), "Response Status not valid");

        //Get the record
        Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
            optOutAnswerRequest.getPcqId(), HttpStatus.OK);
        checkOptOutOnResponse(validateGetResponse);
    }

    @Test
    public void optOutPcqAnswersRecordFoundWithOptOutTrue() throws IOException {
        //create a record with optOut as true.
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OptOutSubmitAnswer.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        answerRequest.setPcqId(generateUuid());

        Map<String, Object> response = pcqBackEndServiceClient.updateAnswersRecord(answerRequest,
                                                                                   HttpStatus.CREATED);

        assertEquals("201", response.get(RESPONSE_KEY_2), RESPONSE_INVALID_STATUS_CODE);

        //Get the record
        Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
            answerRequest.getPcqId(), HttpStatus.OK);
        checkOptOutOnResponse(validateGetResponse);
    }

}
