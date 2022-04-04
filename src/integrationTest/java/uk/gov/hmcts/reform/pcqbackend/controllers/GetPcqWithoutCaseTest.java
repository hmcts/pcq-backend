package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcq.commons.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcq.commons.utils.PcqUtils;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
@SuppressWarnings("PMD.LinguisticNaming")
public class GetPcqWithoutCaseTest extends PcqIntegrationTest {

    public static final String RESPONSE_KEY_1 = "pcqId";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_OK = "200";
    public static final String RESPONSE_SUCCESS_MSG = "Success";
    public static final String EXCEPTION_MSG = "Exception while executing test";

    private static final String ASSERT_MESSAGE_PCQ = "PCQId not valid";
    private static final String ASSERT_MESSAGE_STATUS = "Response Status not valid";
    private static final String ASSERT_MESSAGE_STATUS_CODE = "Response Status Code not valid";
    private static final int DAYS_LIMIT = 90;
    private static final String JSON_FILE = "JsonTestFiles/FirstSubmitAnswer.json";

    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();

    @Test
    public void getPcqWithoutCaseSingleId() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 1, answerRequest.getPcqId());
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqWithoutCaseRecordNotFound() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerWithCase.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 0, answerRequest.getPcqId());
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqWithoutCaseCompletedDatePastBoundary() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //Update the completed date to be in the past.
            answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
                DAYS_LIMIT + 1)));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 0, answerRequest.getPcqId());
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqWithoutCaseCompletedDateOnBoundary() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //Update the completed date to be in the past.
            answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
                DAYS_LIMIT)));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 0, answerRequest.getPcqId());
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqWithoutCaseCompletedDatePreBoundary() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //Update the completed date to be in the past.
            answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
                DAYS_LIMIT - 1)));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 1, answerRequest.getPcqId());
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqWithoutCaseMultipleIds() {
        try {

            //Create the Test Data 3 times in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            answerRequest.setPcqId("INTEG-TEST-11");
            pcqBackEndClient.createPcqAnswer(answerRequest);

            answerRequest.setPcqId("INTEG-TEST-12");
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 3, answerRequest.getPcqId(), "INTEG-TEST-11", "INTEG-TEST-12");
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }


    private void checkLogsForKeywords() {
        assertTrue(capture.getAll().contains("Co-Relation Id : " + CO_RELATION_ID_FOR_TEST),
                   "Co-Relation Id was not logged in log files.");
    }

    @SuppressWarnings("unchecked")
    private void assertTestForSuccess(Map<String, Object> responseMap, int recordsExpected, String... pcqIds) {
        ResponseEntity<PcqRecordWithoutCaseResponse> response = (ResponseEntity<PcqRecordWithoutCaseResponse>)
            responseMap.get("response_body");
        assertNotNull(response.getBody().getPcqRecord(), ASSERT_MESSAGE_PCQ);
        assertEquals(ASSERT_MESSAGE_STATUS_CODE, HTTP_OK, response.getBody().getResponseStatusCode());
        assertEquals(ASSERT_MESSAGE_STATUS, RESPONSE_SUCCESS_MSG, response.getBody().getResponseStatus());

        PcqAnswerResponse[] pcqRecordsActual = response.getBody().getPcqRecord();
        int length = pcqRecordsActual.length;
        assertEquals("Pcq Records size not matching", recordsExpected, length);

        List<String> actualPcqIds = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            actualPcqIds.add(pcqRecordsActual[i].getPcqId());
        }
        if (recordsExpected > 0) {
            for (String pcqId : pcqIds) {
                assertTrue(actualPcqIds.contains(pcqId), "Pcq Id not found");
            }
        }

    }

}
