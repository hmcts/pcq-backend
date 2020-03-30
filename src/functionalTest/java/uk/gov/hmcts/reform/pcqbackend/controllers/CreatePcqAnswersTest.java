package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class CreatePcqAnswersTest extends PcqBaseFunctionalTest {

    public static final String RESPONSE_KEY_1 = "pcqId";

    public static final String RESPONSE_KEY_2 = "responseStatusCode";

    public static final String RESPONSE_KEY_3 = "responseStatus";

    public static final String HTTP_CREATED = "201";

    public static final String RESPONSE_CREATED_MSG = "Successfully created";

    @Test
    public void createPcqAnswersWithoutCaseId() {

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setPcqId(generateUuid());
            Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

            assertEquals("Response Status Code not valid", HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
                answerRequest.getPcqId(), HttpStatus.OK);

            checkAssertionsOnResponse(validateGetResponse, answerRequest);


        } catch (IOException e) {
            log.error("Error during test execution", e);
        }

    }

    @Test
    public void createPcqAnswersWithCaseId() {

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerWithCase.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setPcqId(generateUuid());
            Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

            assertEquals("Response Status Code not valid", HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
                answerRequest.getPcqId(), HttpStatus.OK);

            checkAssertionsOnResponse(validateGetResponse, answerRequest);

        } catch (IOException e) {
            log.error("Error during test execution", e);
        }

    }
}
