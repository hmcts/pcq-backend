package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
public class CreatePcqRequestTest extends PcqIntegrationTest {

    public static final String RESPONSE_KEY_1 = "pcqId";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_CREATED = "201";
    public static final String HTTP_INTERNAL_ERROR = "500";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";
    public static final String TEST_PCQ_ID = "Integ-Test-1";
    public static final String RESPONSE_UNKNOWN = "Unknown error occurred";


    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    @Test
    public void createPcqAnswersSuccessWithoutCase() {
        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals("PCQId not valid", TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals("Response Status Code not valid", HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findById(TEST_PCQ_ID);

            assertFalse(protectedCharacteristicsOptional.isEmpty(), "Record Not found");
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            checkLogsForKeywords();


        } catch (IOException e) {
            log.error("IOException while executing test", e);
        }


    }

    @Test
    public void createPcqAnswersSuccessWithCase() {
        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerWithCase.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals("PCQId not valid", TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals("Response Status Code not valid", HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findById(TEST_PCQ_ID);

            assertFalse(protectedCharacteristicsOptional.isEmpty(), "Record Not found");
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            checkLogsForKeywords();


        } catch (IOException e) {
            log.error("IOException while executing test", e);
        }


    }

    /**
     * This method tests the submitAnswers API when it is called with all valid parameters but the database call
     * returns an exception. The response status code will be 500.
     */
    @DisplayName("Should return with an 500 error code for transaction error.")
    @Test
    public void testControllerInternalError()  {

        try {
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setPcqId("");
            //logger.info("testSubmitAnswersFirstTime - Generated Json String is " + jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);

            assertEquals("Response Status Code not valid", null, response.get(RESPONSE_KEY_2));

            checkLogsForKeywords();

        } catch (Exception e) {
            log.error("IOException while executing test", e);
        }

    }

    private void checkLogsForKeywords() {
        assertTrue(capture.getAll().contains("Co-Relation Id : " + CO_RELATION_ID_FOR_TEST),
                   "Co-Relation Id was not logged in log files.");
    }

}
