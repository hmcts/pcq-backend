package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import uk.gov.hmcts.reform.pcqbackend.domain.ProtectedCharacteristics;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
public class AddCaseForPcqTest extends PcqIntegrationTest {

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
    public void addCaseForPcqSuccess() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.addCaseForPcq(answerRequest.getPcqId(), TEST_CASE_ID);

            //Test the assertions
            assertResponse(responseMap, answerRequest.getPcqId(), HTTP_OK, RESPONSE_SUCCESS_MSG);
            checkLogsForKeywords();

            //Fetch the record from database and verify the answers.
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findById(answerRequest.getPcqId());

            assertEquals(ASSERT_MESSAGE_CASE_ID, TEST_CASE_ID, protectedCharacteristicsOptional.get().getCaseId());


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void addCaseForInvalidPcq() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.addCaseForPcq("JunkPCQ", TEST_CASE_ID);

            //Test the assertions
            assertEquals(ASSERT_MESSAGE_STATUS_CODE, HTTP_BAD_REQUEST, responseMap.get("http_status"));
            checkLogsForKeywords();

            //Fetch the record from database and verify the answers.
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findById(answerRequest.getPcqId());

            assertEquals(ASSERT_MESSAGE_CASE_ID, null, protectedCharacteristicsOptional.get().getCaseId());


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void addCaseForNullParams() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.addCaseForPcq(null, null);

            //Test the assertions
            assertEquals(ASSERT_MESSAGE_STATUS_CODE, HTTP_BAD_REQUEST, responseMap.get("http_status"));
            checkLogsForKeywords();

            //Fetch the record from database and verify the answers.
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findById(answerRequest.getPcqId());

            assertEquals(ASSERT_MESSAGE_CASE_ID, null, protectedCharacteristicsOptional.get().getCaseId());


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void addCaseForPcqInjectionTest() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.addCaseForPcq(answerRequest.getPcqId(),
                                                                             "CCD-CASE-121';--");

            //Test the assertions
            assertResponse(responseMap, answerRequest.getPcqId(), HTTP_OK, RESPONSE_SUCCESS_MSG);
            checkLogsForKeywords();

            //Fetch the record from database and verify the answers.
            Optional<ProtectedCharacteristics> protectedCharacteristicsOptional =
                protectedCharacteristicsRepository.findById(answerRequest.getPcqId());

            assertEquals(ASSERT_MESSAGE_CASE_ID, "CCD-CASE-121&#39;;--",
                         protectedCharacteristicsOptional.get().getCaseId());


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    private void checkLogsForKeywords() {
        assertTrue(capture.getAll().contains("Co-Relation Id : " + CO_RELATION_ID_FOR_TEST),
                   "Co-Relation Id was not logged in log files.");
    }

    @SuppressWarnings("unchecked")
    private void assertResponse(Map<String, Object> responseMap, String pcqId, String httpStatus, String response) {
        assertNotNull(responseMap.get(RESPONSE_KEY_1), ASSERT_MESSAGE_PCQ);
        assertEquals(ASSERT_MESSAGE_STATUS_CODE, httpStatus, responseMap.get(RESPONSE_KEY_2));
        assertEquals(ASSERT_MESSAGE_STATUS, response, responseMap.get(RESPONSE_KEY_3));
        assertEquals(ASSERT_MESSAGE_PCQ, pcqId, responseMap.get(RESPONSE_KEY_1));
    }
}
