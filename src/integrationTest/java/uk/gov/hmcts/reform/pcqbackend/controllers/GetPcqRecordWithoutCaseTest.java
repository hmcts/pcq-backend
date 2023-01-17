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
public class GetPcqRecordWithoutCaseTest extends PcqIntegrationTest {

    public static final String HTTP_OK = "200";
    public static final String RESPONSE_SUCCESS_MSG = "Success";
    public static final String EXCEPTION_MSG = "Exception while executing test";

    private static final String ASSERT_MESSAGE_PCQ = "PCQId not valid";
    private static final String ASSERT_MESSAGE_STATUS = "Response Status not valid";
    private static final String ASSERT_MESSAGE_STATUS_CODE = "Response Status Code not valid";
    private static final int DAYS_LIMIT = 90;
    private static final String JSON_FILE = "JsonTestFiles/FirstSubmitAnswer.json";
    private static final String JSON_DCN_FILE = "JsonTestFiles/FirstSubmitDcnAnswer.json";

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
    public void getDcnRecordWithoutCaseSingleRecord() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_DCN_FILE);
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
    public void getDcnRecordWithoutCaseRecordNotFound() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitDcnAnswerWithCase.json");
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
            answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
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
    public void getDcnRecordWithoutCaseCompletedDatePastBoundary() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_DCN_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //Update the completed date to be in the past.
            answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
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
            answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
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
    public void getDcnRecordWithoutCaseCompletedDateOnBoundary() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_DCN_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //Update the completed date to be in the past.
            answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
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
            answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
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
    public void getDcnRecordWithoutCaseCompletedDatePreBoundary() {
        try {

            //Create the Test Data in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_DCN_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            //Update the completed date to be in the past.
            answerRequest.setCompletedDate(PcqUtils.convertTimeStampToString(PcqUtils.getDateTimeInPast(
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
                                                                "APPLICANT", null);
            pcqBackEndClient.createPcqAnswer(answerRequest2);

            PcqAnswerRequest answerRequest3 = cloneAnswerObject(answerRequest, "INTEG-TEST-12", "DIVORCE",
                                                                "PETITIONER", null);
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

    @Test
    public void getDcnRecordWithoutCaseMultipleIds() {
        try {

            //Create the Test Data 3 times in the database.
            String jsonStringRequest = jsonStringFromFile(JSON_DCN_FILE);
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));
            pcqBackEndClient.createPcqAnswer(answerRequest);

            PcqAnswerRequest answerRequest2 = cloneAnswerObject(answerRequest,"INTEG-TEST-11", "PROBATE",
                                                                "UNKNOWN", "DCN-TEST-11");
            pcqBackEndClient.createPcqAnswer(answerRequest2);

            PcqAnswerRequest answerRequest3 = cloneAnswerObject(answerRequest, "INTEG-TEST-12", "PROBATE",
                                                                "UNKNOWN", "DCN-TEST-12");
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

    //@SuppressWarnings({"unchecked", "PMD.DataflowAnomalyAnalysis"})
    private void assertTestForSuccess(Map<String, Object> responseMap, int recordsExpected,
                                      PcqAnswerRequest... pcqIds) {
        ResponseEntity<PcqRecordWithoutCaseResponse> response = (ResponseEntity<PcqRecordWithoutCaseResponse>)
            responseMap.get("response_body");
        assertNotNull(response.getBody().getPcqRecord(), ASSERT_MESSAGE_PCQ);
        assertEquals(ASSERT_MESSAGE_STATUS_CODE, HTTP_OK, response.getBody().getResponseStatusCode());
        assertEquals(ASSERT_MESSAGE_STATUS, RESPONSE_SUCCESS_MSG, response.getBody().getResponseStatus());
        PcqAnswerResponse[] pcqRecordsActual = response.getBody().getPcqRecord();
        int length = pcqRecordsActual.length;
        assertEquals("Pcq Records size not matching", recordsExpected, length);

        List<String> actualPcqIds = new ArrayList<>(length);
        List<String> actualServiceIds = new ArrayList<>(length);
        List<String> actualActors = new ArrayList<>(length);
        List<String> actualDcnNumbers = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            actualPcqIds.add(pcqRecordsActual[i].getPcqId());
            actualServiceIds.add(pcqRecordsActual[i].getServiceId());
            actualActors.add(pcqRecordsActual[i].getActor());
            actualDcnNumbers.add(pcqRecordsActual[i].getDcnNumber());
        }

        if (recordsExpected > 0) {

            for (PcqAnswerRequest pcqId : pcqIds) {
                assertTrue(actualPcqIds.contains(pcqId.getPcqId()), "Pcq Id not found");
                assertTrue(actualServiceIds.contains(pcqId.getServiceId()), "Service Id not found");
                assertTrue(actualActors.contains(pcqId.getActor()), "Actor not found");
                assertTrue(actualDcnNumbers.contains(pcqId.getDcnNumber()), "DCN Number not found");
            }
        }
    }

    private PcqAnswerRequest cloneAnswerObject(PcqAnswerRequest originalAnswer, String pcqId, String serviceId,
                                               String actor, String dcnNumber) {
        PcqAnswerRequest clonedAnswer = new PcqAnswerRequest();
        clonedAnswer.setPcqId(pcqId);
        clonedAnswer.setDcnNumber(dcnNumber);
        clonedAnswer.setFormId(originalAnswer.getFormId());
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
