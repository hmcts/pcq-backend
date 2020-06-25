package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerRequest;
import uk.gov.hmcts.reform.pcqbackend.model.PcqAnswerResponse;
import uk.gov.hmcts.reform.pcqbackend.model.PcqRecordWithoutCaseResponse;
import uk.gov.hmcts.reform.pcqbackend.utils.ConversionUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class PcqRecordWithoutCaseTest extends PcqBaseFunctionalTest {

    public static final String RESPONSE_KEY_1 = "pcqRecord";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_OK = "200";
    public static final String HTTP_CREATED = "201";
    public static final String RESPONSE_SUCCESS_MSG = "Success";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";
    public static final int DAYS_LIMIT = 90;

    private static final String STATUS_CODE_INVALID_MSG = "Response Status Code not valid";
    private static final String STATUS_INVALID_MSG = "Response Status not valid";

    @Test
    @SuppressWarnings("unchecked")
    public void testPcqRecordWithoutCase() {

        try {

            //Create 2 records in database with the current completed date.
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            String firstUuid = generateUuid();
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));
            answerRequest.setPcqId(firstUuid);

            Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            String secondUuid = generateUuid();
            answerRequest.setPcqId(secondUuid);
            answerRequest.setCompletedDate(updateCompletedDate(answerRequest.getCompletedDate()));

            response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            //Create 3rd record in database with a completed date past the limit.
            String thirdUuid = generateUuid();
            answerRequest.setPcqId(thirdUuid);
            answerRequest.setCompletedDate(ConversionUtil.convertTimeStampToString(ConversionUtil.getDateTimeInPast(
                DAYS_LIMIT + 1)));

            response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            //Now call the pcqWithoutCaseAPI
            PcqRecordWithoutCaseResponse getResponse = pcqBackEndServiceClient.getAnswerRecordsWithoutCase(
                HttpStatus.OK);


            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_OK, getResponse.getResponseStatusCode());
            assertEquals(STATUS_INVALID_MSG, RESPONSE_SUCCESS_MSG, getResponse.getResponseStatus());

            PcqAnswerResponse[] answerResponses = getResponse.getPcqRecord();
            List<String> pcqIdsInResponse = new ArrayList<>(3);
            for (PcqAnswerResponse answerResponse : answerResponses) {
                pcqIdsInResponse.add(answerResponse.getPcqId());
            }

            assertTrue("First PCQ Id should have been picked up", pcqIdsInResponse.contains(firstUuid));
            assertTrue("Second PCQ Id should have been picked up", pcqIdsInResponse.contains(secondUuid));
            assertFalse("Third PCQ Id should not have been picked up", pcqIdsInResponse.contains(thirdUuid));


        } catch (IOException e) {
            log.error("Error during test execution", e);
        }

    }
}
