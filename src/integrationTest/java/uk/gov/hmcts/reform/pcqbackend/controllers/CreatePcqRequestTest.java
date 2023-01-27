package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
public class CreatePcqRequestTest extends PcqIntegrationTest {

    public static final String RESPONSE_KEY_1 = "pcqId";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_CREATED = "201";
    public static final String HTTP_INTERNAL_ERROR = "500";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";
    public static final String TEST_PCQ_ID = "Integ-Test-1";
    public static final String RESPONSE_UNKNOWN = "Unknown error occurred";
    private static final String IO_EXCEPTION_MSG = "IOException while executing test";
    public static final String RESPONSE_STATUS_CODE_MSG = "Response Status Code not valid";


    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    @Test
    public void createPcqAnswersSuccessWithoutCase() {
        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals("PCQId not valid", TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(RESPONSE_STATUS_CODE_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), "Record Not found");
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            checkLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }


    }

    @Test
    public void createPcqAnswersSuccessWithCase() {
        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerWithCase.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals("PCQId not valid", TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(RESPONSE_STATUS_CODE_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), "Record Not found");
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            checkLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }


    }

    @Test
    public void createPcqAnswersSuccessWithCaseOptOutExplicitNull() {
        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerWithCaseOptOutNull.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals("PCQId not valid", TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(RESPONSE_STATUS_CODE_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), "Record Not found");
            checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
            checkLogsForKeywords();


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
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

            assertEquals(RESPONSE_STATUS_CODE_MSG, null, response.get(RESPONSE_KEY_2));

            checkLogsForKeywords();

        } catch (Exception e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    private void checkLogsForKeywords() {
        assertTrue(capture.getAll().contains("Co-Relation Id : " + CO_RELATION_ID_FOR_TEST),
                   "Co-Relation Id was not logged in log files.");
    }

}
