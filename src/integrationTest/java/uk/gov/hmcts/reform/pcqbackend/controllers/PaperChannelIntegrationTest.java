package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
@SuppressWarnings({"PMD.TooManyMethods"})
public class PaperChannelIntegrationTest extends PcqIntegrationTest {

    public static final String RESPONSE_KEY_1 = "pcqId";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String RESPONSE_KEY_4 = "response_body";
    public static final String HTTP_CREATED = "201";
    public static final String HTTP_BAD_REQUEST = "400";
    public static final String HTTP_INVALID_REQUEST = "403";
    public static final String HTTP_UNAUTHORISED = "401";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";
    public static final String RESPONSE_INVALID_MSG = "Invalid Request";
    public static final String TEST_PCQ_ID = "Integ-Test-1";
    public static final String RESPONSE_STATUS_CODE_MSG = "Response Status Code not valid";
    private static final String PCQ_ID_INVALID_MSG = "PCQId not valid";
    private static final String RESPONSE_STATUS_MSG = "Response Status not valid";
    private static final String RECORD_NOT_FOUND_MSG = "Record Not found";
    private static final String TIMESTAMPS_NOT_MATCH_MSG =
        "Completed date does not match last updated timestamp, when they should";

    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    @Test
    public void createPcqAnswersSuccess() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitDcnAnswer.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_ID_INVALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), RESPONSE_STATUS_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        ProtectedCharacteristics pcq = protectedCharacteristicsOptional.get();
        assertEquals(pcq.getCompletedDate(), pcq.getLastUpdatedTimestamp(), TIMESTAMPS_NOT_MATCH_MSG);

        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        checkLogsForKeywords();
    }

    @Test
    public void authorisationValidationTest() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitDcnAnswer.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.checkAuthValidation(answerRequest);
        assertNull(response.get(RESPONSE_KEY_1), PCQ_ID_INVALID_MSG);
        assertEquals(HTTP_UNAUTHORISED, response.get("http_status"), RESPONSE_STATUS_CODE_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), RECORD_NOT_FOUND_MSG);
    }

    @Test
    public void createPcqFullAnswersSuccess() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/SubmitDcnAllAnswers.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_ID_INVALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), RESPONSE_STATUS_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        ProtectedCharacteristics pcq = protectedCharacteristicsOptional.get();
        assertEquals(pcq.getCompletedDate(), pcq.getLastUpdatedTimestamp(), TIMESTAMPS_NOT_MATCH_MSG);

        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        checkLogsForKeywords();
    }

    @Test
    public void duplicateDcnRecordNotCreated() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/SubmitDcnAllAnswers.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_ID_INVALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), RESPONSE_STATUS_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        ProtectedCharacteristics pcq = protectedCharacteristicsOptional.get();
        assertEquals(pcq.getCompletedDate(), pcq.getLastUpdatedTimestamp(), TIMESTAMPS_NOT_MATCH_MSG);

        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        checkLogsForKeywords();

        // Try to submit another record with same DCN number
        String newPcqId = UUID.randomUUID().toString();
        answerRequest.setPcqId(newPcqId);
        response = pcqBackEndClient.createPcqAnswer(answerRequest);
        Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));
        assertEquals(newPcqId, responseBody.get(RESPONSE_KEY_1), PCQ_ID_INVALID_MSG);
        assertEquals("409", responseBody.get(RESPONSE_KEY_2), RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_INVALID_MSG, responseBody.get(RESPONSE_KEY_3), RESPONSE_STATUS_MSG);

        protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(newPcqId, getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), RECORD_NOT_FOUND_MSG);

        checkLogsForKeywords();
    }

    @Test
    public void createPcqSqlInjectionAnswersSuccess() throws IOException {

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/SubmitDcnSqlInjection.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_ID_INVALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), RESPONSE_STATUS_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        ProtectedCharacteristics pcq = protectedCharacteristicsOptional.get();
        assertEquals(pcq.getCompletedDate(), pcq.getLastUpdatedTimestamp(), TIMESTAMPS_NOT_MATCH_MSG);

        checkLogsForKeywords();
    }

    @Test
    public void createPcqInvalidAnswersSuccess() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/SubmitDcnAllAnswers.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        assertEquals(TEST_PCQ_ID, response.get(RESPONSE_KEY_1), PCQ_ID_INVALID_MSG);
        assertEquals(HTTP_CREATED, response.get(RESPONSE_KEY_2), RESPONSE_STATUS_CODE_MSG);
        assertEquals(RESPONSE_CREATED_MSG, response.get(RESPONSE_KEY_3), RESPONSE_STATUS_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        ProtectedCharacteristics pcq = protectedCharacteristicsOptional.get();
        assertEquals(pcq.getCompletedDate(), pcq.getLastUpdatedTimestamp(), TIMESTAMPS_NOT_MATCH_MSG);

        checkAssertionsOnResponse(protectedCharacteristicsOptional.get(), answerRequest);
        checkLogsForKeywords();
    }

    @Test
    public void invalidDob() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/invalidDcnDob.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));

        assertEquals(TEST_PCQ_ID, responseBody.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_INVALID_MSG, responseBody.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);

        checkLogsForKeywords();
    }

    @Test
    public void invalidAnswersRange() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/invalidDcnAnswerRange.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));

        assertEquals(TEST_PCQ_ID, responseBody.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_INVALID_MSG, responseBody.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);

        checkLogsForKeywords();
    }

    @Test
    public void dcnMissing() throws IOException {
        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/invalidDcnMissing.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));

        assertEquals(TEST_PCQ_ID, responseBody.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_INVALID_MSG, responseBody.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);

        checkLogsForKeywords();
    }

    @Test
    public void dcnBlank() throws IOException {

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/invalidDcnBlank.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));

        assertEquals(TEST_PCQ_ID, responseBody.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_BAD_REQUEST, responseBody.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_INVALID_MSG, responseBody.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);

        checkLogsForKeywords();
    }

    @Test
    public void invalidVersion() throws IOException {

        String jsonStringRequest = jsonStringFromFile("JsonTestFiles/InvalidVersionForPaper.json");
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

        Map<String, Object> response = pcqBackEndClient.createPcqAnswer(answerRequest);
        Map<String, Object> responseBody = jsonMapFromString((String) response.get(RESPONSE_KEY_4));

        assertEquals("T1234", responseBody.get(RESPONSE_KEY_1), PCQ_NOT_VALID_MSG);
        assertEquals(HTTP_INVALID_REQUEST, responseBody.get(RESPONSE_KEY_2), STATUS_CODE_INVALID_MSG);
        assertEquals(RESPONSE_INVALID_MSG, responseBody.get(RESPONSE_KEY_3), STATUS_INVALID_MSG);

        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(TEST_PCQ_ID,getEncryptionKey());

        assertTrue(protectedCharacteristicsOptional.isEmpty(), NOT_FOUND_MSG);

        checkLogsForKeywords();
    }

    private void checkLogsForKeywords() {
        assertTrue(capture.getAll().contains("Co-Relation Id : " + CO_RELATION_ID_FOR_TEST),
                   "Co-Relation Id was not logged in log files.");
    }
}
