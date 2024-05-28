package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
public class AddCaseForPcqIntegrationTest extends PcqIntegrationTest {

    public static final String RESPONSE_KEY_1 = "pcqId";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_OK = "200";
    public static final String HTTP_BAD_REQUEST = "400";
    public static final String RESPONSE_SUCCESS_MSG = "Successfully updated";
    public static final String EXCEPTION_MSG = "Exception while executing test";

    private static final String ASSERT_MESSAGE_PCQ = "PCQId not valid";
    private static final String ASSERT_MESSAGE_STATUS = "Response Status not valid";
    private static final String ASSERT_MESSAGE_STATUS_CODE = "Response Status Code not valid";
    private static final String ASSERT_MESSAGE_CASE_ID = "Case Id Not Matching";
    private static final String TEST_CASE_ID = "CCD-121212";
    private static final String JSON_FILE = "JsonTestFiles/FirstSubmitAnswer.json";

    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    @Test
    public void addCaseForPcqSuccess() throws IOException {
        //Create the Test Data in the database.
        String jsonStringRequest = jsonStringFromFile(JSON_FILE);
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        pcqBackEndClient.createPcqAnswer(answerRequest);

        //Fetch the record from database and verify the answers.
        Optional<ProtectedCharacteristics> pcqOpt =
            protectedCharacteristicsRepository.findByPcqId(answerRequest.getPcqId(), getEncryptionKey());

        if (pcqOpt.isPresent()) {
            ProtectedCharacteristics pcq = pcqOpt.get();
            String msg = "Completed date does not match last updated timestamp, when they should";
            assertEquals(pcq.getCompletedDate(), pcq.getLastUpdatedTimestamp(), msg);
        } else {
            fail("Inserted PCQ was not found");
        }

        //Now call the actual method.
        Map<String, Object> responseMap = pcqBackEndClient.addCaseForPcq(answerRequest.getPcqId(), TEST_CASE_ID);

        //Test the assertions
        assertResponse(responseMap, answerRequest.getPcqId(), HTTP_OK, RESPONSE_SUCCESS_MSG);
        checkLogsForKeywords();

        //Fetch the record from database and verify the answers.
        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(answerRequest.getPcqId(), getEncryptionKey());
        assertThat(protectedCharacteristicsOptional.get().getLastUpdatedTimestamp())
            .withFailMessage("Last updated timestamp should be now, but it is not close enough")
            .isCloseTo(Instant.now(), SECONDS.toMillis(2));

        assertEquals(TEST_CASE_ID, protectedCharacteristicsOptional.get().getCaseId(), ASSERT_MESSAGE_CASE_ID);
    }

    @Test
    public void addCaseForInvalidPcq() throws IOException {
        //Create the Test Data in the database.
        String jsonStringRequest = jsonStringFromFile(JSON_FILE);
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        pcqBackEndClient.createPcqAnswer(answerRequest);

        //Now call the actual method.
        Map<String, Object> responseMap = pcqBackEndClient.addCaseForPcq("JunkPCQ", TEST_CASE_ID);

        //Test the assertions
        assertEquals(HTTP_BAD_REQUEST, responseMap.get("http_status"), ASSERT_MESSAGE_STATUS_CODE);
        checkLogsForKeywords();

        //Fetch the record from database and verify the answers.
        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(answerRequest.getPcqId(),getEncryptionKey());
        ProtectedCharacteristics pcq = protectedCharacteristicsOptional.get();

        assertNull(pcq.getCaseId(), ASSERT_MESSAGE_CASE_ID);
        String msg = "Completed date does not match last updated timestamp, when they should";
        assertEquals(pcq.getCompletedDate(), pcq.getLastUpdatedTimestamp(), msg);
    }

    @Test
    public void addCaseForNullParams() throws IOException {
        //Create the Test Data in the database.
        String jsonStringRequest = jsonStringFromFile(JSON_FILE);
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        pcqBackEndClient.createPcqAnswer(answerRequest);

        //Now call the actual method.
        Map<String, Object> responseMap = pcqBackEndClient.addCaseForPcq(null, null);

        //Test the assertions
        assertEquals(HTTP_BAD_REQUEST, responseMap.get("http_status"), ASSERT_MESSAGE_STATUS_CODE);
        checkLogsForKeywords();

        //Fetch the record from database and verify the answers.
        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(answerRequest.getPcqId(),getEncryptionKey());
        ProtectedCharacteristics pcq = protectedCharacteristicsOptional.get();

        assertNull(pcq.getCaseId(), ASSERT_MESSAGE_CASE_ID);
        String msg = "Completed date does not match last updated timestamp, when they should";
        assertEquals(pcq.getCompletedDate(), pcq.getLastUpdatedTimestamp(), msg);
    }

    @Test
    public void addCaseForPcqInjectionTest() throws IOException {
        //Create the Test Data in the database.
        String jsonStringRequest = jsonStringFromFile(JSON_FILE);
        PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
        pcqBackEndClient.createPcqAnswer(answerRequest);

        //Now call the actual method.
        Map<String, Object> responseMap = pcqBackEndClient.addCaseForPcq(
            answerRequest.getPcqId(), "CCD-CASE-121';--");

        //Test the assertions
        assertResponse(responseMap, answerRequest.getPcqId(), HTTP_OK, RESPONSE_SUCCESS_MSG);
        checkLogsForKeywords();

        //Fetch the record from database and verify the answers.
        Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
            protectedCharacteristicsRepository.findByPcqId(answerRequest.getPcqId(),getEncryptionKey());

        ProtectedCharacteristics pcq = protectedCharacteristicsOptional.get();

        assertEquals("CCD-CASE-121&#39;;--", pcq.getCaseId(), ASSERT_MESSAGE_CASE_ID);

        assertThat(pcq.getLastUpdatedTimestamp())
            .withFailMessage("Last updated timestamp should be now, but it is not close enough")
            .isCloseTo(Instant.now(), SECONDS.toMillis(2));
    }

    private void checkLogsForKeywords() {
        assertTrue(capture.getAll().contains("Co-Relation Id : " + CO_RELATION_ID_FOR_TEST),
                   "Co-Relation Id was not logged in log files.");
    }

    @SuppressWarnings("unchecked")
    private void assertResponse(Map<String, Object> responseMap, String pcqId, String httpStatus, String response) {
        assertNotNull(responseMap.get(RESPONSE_KEY_1), ASSERT_MESSAGE_PCQ);
        assertEquals(httpStatus, responseMap.get(RESPONSE_KEY_2), ASSERT_MESSAGE_STATUS_CODE);
        assertEquals(response, responseMap.get(RESPONSE_KEY_3), ASSERT_MESSAGE_STATUS);
        assertEquals(pcqId, responseMap.get(RESPONSE_KEY_1), ASSERT_MESSAGE_PCQ);
    }
}
