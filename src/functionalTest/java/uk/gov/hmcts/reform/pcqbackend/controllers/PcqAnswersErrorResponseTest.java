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

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class PcqAnswersErrorResponseTest extends PcqBaseFunctionalTest {

    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_CREATED = "201";
    public static final String HTTP_ACCEPTED = "202";
    public static final String HTTP_BAD_REQUEST = "400";
    public static final String HTTP_FORBIDDEN = "403";
    public static final String HTTP_INTERNAL_SERVER_ERROR = "500";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";
    public static final String RESPONSE_ACCEPTED_MSG = "Success";
    public static final String RESPONSE_FAILURE_MSG = "Invalid Request";
    public static final String RESPONSE_UNKNOWN_ERROR_MSG = "Unknown error occurred";

    private static final String ASSERT_RESPONSE_STATUS_CODE_MSG = "Response Status Code not valid";
    private static final String ASSERT_RESPONSE_STATUS_MSG = "Response Status not valid";
    private static final String IO_EXCEPTION_MSG = "Error during test execution";

    @Test
    public void stalePcqAnswers() throws IOException {
        //Create a record before updating.
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        answerRequest.setPcqId(generateUuid());
        Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), ASSERT_RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), ASSERT_RESPONSE_STATUS_MSG);

        //Prepare for clearing down.
        clearTestPcqAnswers.add(answerRequest);

        //Update the record but don't change the completed date.
        jsonStringRequest = jsonStringFromFile("JsonTestFiles/StaleSubmitAnswer.json");
        PcqAnswerRequest updateAnswerRequest = jsonObjectFromString(jsonStringRequest);

        //Use the same PCQ ID as above
        updateAnswerRequest.setPcqId(answerRequest.getPcqId());

        response = pcqBackEndServiceClient.staleAnswersNotRecorded(updateAnswerRequest);

        assertEquals(HTTP_ACCEPTED, response.get(RESPONSE_KEY_2), ASSERT_RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_ACCEPTED_MSG, response.get(RESPONSE_KEY_3), ASSERT_RESPONSE_STATUS_MSG);

        //Get the record
        Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
            updateAnswerRequest.getPcqId(), HttpStatus.OK);

        //Validate against the original request to ensure that the record was not updated.
        checkAssertionsOnResponse(validateGetResponse, answerRequest);
    }

    @Test
    public void invalidJsonRequest() throws IOException {
        //Create a record before updating.
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidJson1.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndServiceClient.invalidJSonRecord(answerRequest);

        assertEquals(HTTP_BAD_REQUEST, response.get(RESPONSE_KEY_2), ASSERT_RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_FAILURE_MSG, response.get(RESPONSE_KEY_3), ASSERT_RESPONSE_STATUS_MSG);

        //Check that the record has not been created.
        Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
            answerRequest.getPcqId(), HttpStatus.NOT_FOUND);

        assertNotNull(validateGetResponse, "Get Response is null");
    }

    @Test
    public void invalidVersion() throws IOException {
        //Create a record before updating.
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidVersion.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndServiceClient.invalidVersion(answerRequest);

        assertEquals(HTTP_FORBIDDEN, response.get(RESPONSE_KEY_2), ASSERT_RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_FAILURE_MSG, response.get(RESPONSE_KEY_3), ASSERT_RESPONSE_STATUS_MSG);

        //Check that the record has not been created.
        Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
            answerRequest.getPcqId(), HttpStatus.NOT_FOUND);

        assertNotNull(validateGetResponse, "Get Response is null");
    }

    @Test
    public void unRecoverableErrorTest() throws IOException {
        //Create a record before updating.
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        //Deliberately set an empty Id to trigger error.
        answerRequest.setPcqId("");

        Map<String, Object> response = pcqBackEndServiceClient.unRecoverableError(answerRequest);

        assertEquals(HTTP_INTERNAL_SERVER_ERROR, response.get(RESPONSE_KEY_2), ASSERT_RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_UNKNOWN_ERROR_MSG, response.get(RESPONSE_KEY_3), ASSERT_RESPONSE_STATUS_MSG);

    }

}
