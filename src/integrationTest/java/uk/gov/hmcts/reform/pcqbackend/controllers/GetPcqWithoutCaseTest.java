package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;
import uk.gov.hmcts.reform.pcqbackend.utils.ConversionUtil;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
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
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqWithoutCase();

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
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqWithoutCase();

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
            answerRequest.setCompletedDate(ConversionUtil.convertTimeStampToString(ConversionUtil.getDateTimeInPast(
                DAYS_LIMIT + 1)));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqWithoutCase();

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
            answerRequest.setCompletedDate(ConversionUtil.convertTimeStampToString(ConversionUtil.getDateTimeInPast(
                DAYS_LIMIT)));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqWithoutCase();

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
            answerRequest.setCompletedDate(ConversionUtil.convertTimeStampToString(ConversionUtil.getDateTimeInPast(
                DAYS_LIMIT - 1)));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqWithoutCase();

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
            pcqBackEndClient.createPcqAnswer(answerRequest);

            answerRequest.setPcqId("INTEG-TEST-11");
            pcqBackEndClient.createPcqAnswer(answerRequest);

            answerRequest.setPcqId("INTEG-TEST-12");
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqWithoutCase();

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
        assertNotNull(responseMap.get(RESPONSE_KEY_1), ASSERT_MESSAGE_PCQ);
        assertEquals(ASSERT_MESSAGE_STATUS_CODE, HTTP_OK, responseMap.get(RESPONSE_KEY_2));
        assertEquals(ASSERT_MESSAGE_STATUS, RESPONSE_SUCCESS_MSG,
                     responseMap.get(RESPONSE_KEY_3));
        List<String> pcqIdsActual = (List<String>) responseMap.get(RESPONSE_KEY_1);
        assertEquals("PcqIds size not matching", recordsExpected, pcqIdsActual.size());

        for (int i = 0; i < pcqIdsActual.size(); i++) {
            assertTrue(pcqIdsActual.contains(pcqIds[i]), "Pcq Id not found");
        }
    }

}
