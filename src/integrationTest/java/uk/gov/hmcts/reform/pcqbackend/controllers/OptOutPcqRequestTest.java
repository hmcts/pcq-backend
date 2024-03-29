package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.web.util.HtmlUtils;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswers;
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
public class OptOutPcqRequestTest extends PcqIntegrationTest {

    public static final String RESPONSE_KEY_1 = "pcqId";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_OK = "200";
    public static final String HTTP_BAD_REQUEST = "400";
    public static final String HTTP_CREATED = "201";
    public static final String RESPONSE_UPDATED_MSG = "Successfully updated";
    public static final String TEST_PCQ_ID = "UPDATE-INTEG-TEST";
    public static final String RESPONSE_INVALID_MSG = "Invalid Request";
    public static final String RESPONSE_STATUS_CODE_MSG = "Response Status Code valid";
    public static final String RESPONSE_STATUS_MSG = "Response Status valid";
    public static final String RESPONSE_KEY_4 = "response_body";
    private static final String TEST_DUP_PCQ_ID = "UPDATE-DUP-INTEG-TEST";
    private static final String IO_EXCEPTION_MSG = "IOException while executing test";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";


    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    @Test
    public void submitAnswersInvalidOptOutValue() {
        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidJsonOptOut1.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));
            assertEquals("PCQId not valid", "T1234", responseBody.get(RESPONSE_KEY_1));
            assertEquals(RESPONSE_STATUS_CODE_MSG, HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2));
            assertEquals("Response Status not valid", RESPONSE_INVALID_MSG,
                         responseBody.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertTrue(protectedCharacteristicsOptional.isEmpty(), "Record found");


        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }


    }

    @Test
    public void optOutWithSuccess() {
        // Create an record first.
        createTestRecord();
        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OptOutRequested.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(RESPONSE_STATUS_CODE_MSG, HTTP_OK, response.get(RESPONSE_KEY_2));
            assertEquals(RESPONSE_STATUS_MSG, RESPONSE_UPDATED_MSG, response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), "Pcq Record exist");
        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }


    }

    @Test
    public void optOutRecordNotFound() {

        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OptOutRequested.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            //Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));
            assertEquals(PCQ_VALID_MSG, TEST_PCQ_ID, response.get(RESPONSE_KEY_1));
            assertEquals(RESPONSE_STATUS_CODE_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(RESPONSE_STATUS_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), "Record found");

        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }


    }

    @Test
    public void optOutForSqlInjectionTest() {
        // Create multiple records first.
        createMultipleTestRecords();
        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OptOutRequested.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setPcqId(TEST_PCQ_ID + "' OR 'x'='x");

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            //Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));
            assertEquals(PCQ_VALID_MSG, HtmlUtils.htmlEscape(TEST_PCQ_ID + "' OR 'x'='x"),
                         response.get(RESPONSE_KEY_1));
            assertEquals(RESPONSE_CREATED_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals("Response Status valid", RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), "Pcq Record is Deleted!");

            //Check the Duplicate test record also exists
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptionalDup =
                protectedCharacteristicsRepository.findByPcqId(TEST_DUP_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptionalDup.isEmpty(), "Other Pcq Record Deleted!");

        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }

    }

    @Test
    public void optOutInvalidJson() {
        // Create an record first.
        createTestRecord();
        try {

            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/OptOutRequestedInvalidJson.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
            Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));
            assertEquals(PCQ_NOT_VALID_MSG, TEST_PCQ_ID, responseBody.get(RESPONSE_KEY_1));
            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_INVALID_MSG,
                         responseBody.get(RESPONSE_KEY_3));

            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

            assertFalse(protectedCharacteristicsOptional.isEmpty(), "Pcq Record Deleted");

        } catch (IOException e) {
            log.error(IO_EXCEPTION_MSG, e);
        }


    }


    private void createTestRecord() {
        PcqAnswerRequest testRequest = createAnswerRequestForTest(TEST_PCQ_ID);
        pcqBackEndClient.createPcqAnswer(testRequest);
    }

    public PcqAnswerRequest createAnswerRequestForTest(String pcqId) {
        PcqAnswerRequest answerRequest = new PcqAnswerRequest();
        answerRequest.setPcqId(pcqId);
        answerRequest.setCaseId("CCD-Case-2");
        answerRequest.setPartyId("23");
        answerRequest.setChannel(1);
        answerRequest.setServiceId("PROBATE");
        answerRequest.setCompletedDate("2020-03-05T09:13:45.000Z");
        answerRequest.setActor("RESPONDENT");
        answerRequest.setVersionNo(1);
        PcqAnswers answers = new PcqAnswers();
        answers.setDobProvided(1);

        answerRequest.setPcqAnswers(answers);

        return answerRequest;
    }

    private void createMultipleTestRecords() {
        PcqAnswerRequest testRequest = createAnswerRequestForTest(TEST_PCQ_ID);
        pcqBackEndClient.createPcqAnswer(testRequest);

        testRequest = createAnswerRequestForTest(TEST_DUP_PCQ_ID);
        pcqBackEndClient.createPcqAnswer(testRequest);
    }

}
