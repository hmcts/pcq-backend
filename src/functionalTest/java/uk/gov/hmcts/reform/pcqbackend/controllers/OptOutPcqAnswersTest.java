package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
    public static final String RESPONSE_OK_MSG = "Success";
    public static final String RESPONSE_INVALID_MSG = "Invalid Request";

    @Test
    public void optOutPcqAnswers() {

        try {

            //Create a record before updating.
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerOptOutNull.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setPcqId(generateUuid());
            Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

            assertEquals("Response Status Code not valid", HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            //invoke the submitAnswers to Opt Out the record.
            jsonStringRequest = jsonStringFromFile("JsonTestFiles/OptOutSubmitAnswer.json");
            PcqAnswerRequest optOutAnswerRequest = jsonObjectFromString(jsonStringRequest);

            //Use the same PCQ ID as above
            optOutAnswerRequest.setPcqId(answerRequest.getPcqId());

            response = pcqBackEndServiceClient.updateAnswersRecord(optOutAnswerRequest, HttpStatus.OK);

            assertEquals("Response Status Code not valid", HTTP_OK, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_OK_MSG,
                         response.get(RESPONSE_KEY_3));

            //Get the record
            Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
                optOutAnswerRequest.getPcqId(), HttpStatus.NOT_FOUND);

            assertEquals("Error value not valid", "Not Found",
                         validateGetResponse.get("error"));

        } catch (IOException e) {
            log.error("Error during test execution.", e);
        }

    }

    @Test
    public void optOutPcqAnswersRecordNotFound() {

        try {

            //Don't create a record, directly invoke the OptOut request.
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OptOutSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setPcqId(generateUuid());

            Map<String, Object> response = pcqBackEndServiceClient.updateAnswersRecord(answerRequest,
                                                                                       HttpStatus.BAD_REQUEST);

            assertEquals("Response Status Code not valid", HTTP_BAD_REQUEST, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_INVALID_MSG,
                         response.get(RESPONSE_KEY_3));

        } catch (IOException e) {
            log.error("Error during test execution", e);
        }

    }

}
