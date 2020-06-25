package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqbackend.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqbackend.util.PcqIntegrationTest;
import uk.gov.hmcts.reform.pcqbackend.utils.ConversionUtil;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings("PMD.LinguisticNaming")
public class GetPcqRecordWithoutCaseTest extends PcqIntegrationTest {

    public static final String RESPONSE_KEY_1 = "pcqRecord";
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
    public void getPcqRecordWithoutCaseSingleRecord() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 1, answerRequest);
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqRecordWithoutCaseRecordNotFound() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswerWithCase.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 0, answerRequest);
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqRecordWithoutCaseCompletedDatePastBoundary() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //Update the completed date to be in the past.
            answerRequest.setCompletedDate(ConversionUtil.convertTimeStampToString(ConversionUtil.getDateTimeInPast(
                DAYS_LIMIT + 1)));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 0, answerRequest);
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqRecordWithoutCaseCompletedDateOnBoundary() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //Update the completed date to be in the past.
            answerRequest.setCompletedDate(ConversionUtil.convertTimeStampToString(ConversionUtil.getDateTimeInPast(
                DAYS_LIMIT)));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 0, answerRequest);
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqRecordWithoutCaseCompletedDatePreBoundary() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //Update the completed date to be in the past.
            answerRequest.setCompletedDate(ConversionUtil.convertTimeStampToString(ConversionUtil.getDateTimeInPast(
                DAYS_LIMIT - 1)));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 1, answerRequest);
            checkLogsForKeywords();


        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
        }

    }

    @Test
    public void getPcqRecordWithoutCaseMultipleIds() {
        try {

            //Create the Test Data 3 times in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            PcqAnswerRequest answerRequest2 = cloneAnswerObject(answerRequest,"INTEG-TEST-11", "PROBATE1",
                                                                "APPLICANT");
            pcqBackEndClient.createPcqAnswer(answerRequest2);

            PcqAnswerRequest answerRequest3 = cloneAnswerObject(answerRequest, "INTEG-TEST-12", "DIVORCE",
                                                                "PETITIONER");
            pcqBackEndClient.createPcqAnswer(answerRequest3);

            //Now call the actual method.
            Map<String, Object> responseMap = pcqBackEndClient.getPcqRecordWithoutCase();

            //Test the assertions
            assertTestForSuccess(responseMap, 3, answerRequest, answerRequest2, answerRequest3);
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
    private void assertTestForSuccess(Map<String, Object> responseMap, int recordsExpected,
                                      PcqAnswerRequest... pcqIds) {
        ResponseEntity<PcqRecordWithoutCaseResponse> response = (ResponseEntity<PcqRecordWithoutCaseResponse>)
            responseMap.get("response_body");
        assertNotNull(response.getBody().getPcqRecord(), ASSERT_MESSAGE_PCQ);
        assertEquals(ASSERT_MESSAGE_STATUS_CODE, HTTP_OK, response.getBody().getResponseStatusCode());
        assertEquals(ASSERT_MESSAGE_STATUS, RESPONSE_SUCCESS_MSG, response.getBody().getResponseStatus());
        PcqAnswerResponse[] pcqRecordsActual = response.getBody().getPcqRecord();
        assertEquals("Pcq Records size not matching", recordsExpected, pcqRecordsActual.length);

        for (int i = 0; i < pcqRecordsActual.length; i++) {
            assertEquals("Pcq Id not found", pcqIds[i].getPcqId(), pcqRecordsActual[i].getPcqId());
            assertEquals("Service Id not found", pcqIds[i].getServiceId(), pcqRecordsActual[i].getServiceId());
            assertEquals("Actor not found", pcqIds[i].getActor(), pcqRecordsActual[i].getActor());
        }
    }

    private PcqAnswerRequest cloneAnswerObject(PcqAnswerRequest originalAnswer, String pcqId, String serviceId,
                                               String actor) {
        PcqAnswerRequest clonedAnswer = new PcqAnswerRequest();
        clonedAnswer.setPcqId(pcqId);
        clonedAnswer.setPartyId(originalAnswer.getPartyId());
        clonedAnswer.setCompletedDate(originalAnswer.getCompletedDate());
        clonedAnswer.setPcqAnswers(originalAnswer.getPcqAnswers());
        clonedAnswer.setVersionNo(originalAnswer.getVersionNo());
        clonedAnswer.setActor(actor);
        clonedAnswer.setServiceId(serviceId);
        clonedAnswer.setChannel(originalAnswer.getChannel());
        clonedAnswer.setCaseId(originalAnswer.getCaseId());

        return clonedAnswer;
    }

}
