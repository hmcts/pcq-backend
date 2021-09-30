package uk.gov.hmcts.reform.pcqbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.pcq.commons.model.PcqAnswerRequest;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonObjectFromString;
import static uk.gov.hmcts.reform.pcq.commons.tests.utils.TestUtils.jsonStringFromFile;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class AddCaseForPcqTest extends PcqBaseFunctionalTest {

    public static final String RESPONSE_KEY_1 = "pcqId";
    public static final String RESPONSE_KEY_2 = "responseStatusCode";
    public static final String RESPONSE_KEY_3 = "responseStatus";
    public static final String HTTP_OK = "200";
    public static final String HTTP_CREATED = "201";
    public static final String RESPONSE_CREATED_MSG = "Successfully created";
    public static final String RESPONSE_UPDATED_MSG = "Successfully updated";

    private static final String STATUS_CODE_INVALID_MSG = "Response Status Code not valid";
    private static final String STATUS_INVALID_MSG = "Response Status not valid";
    private static final String TEST_CASE_ID = "TEST_CCD_FUNC";

    @Test
    public void testAddPcqForCase() {

        try {

            //Create a records in database without the case Id.
            String jsonStringRequest = jsonStringFromFile("JsonTestFiles/FirstSubmitAnswer.json");
            PcqAnswerRequest answerRequest = jsonObjectFromString(jsonStringRequest);

            String firstUuid = generateUuid();
            answerRequest.setPcqId(firstUuid);

            Map<String, Object> response = pcqBackEndServiceClient.createAnswersRecord(answerRequest);

            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_CREATED, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_CREATED_MSG,
                         response.get(RESPONSE_KEY_3));

            log.info("PCQ Id = {}", answerRequest.getPcqId());

            //Prepare for clearing down.
            clearTestPcqAnswers.add(answerRequest);

            //Now call the addPcqForCase API.
            response = pcqBackEndServiceClient.addCaseForPcq(firstUuid, TEST_CASE_ID, HttpStatus.OK);


            assertEquals(STATUS_CODE_INVALID_MSG, HTTP_OK, response.get(RESPONSE_KEY_2));
            assertEquals(STATUS_INVALID_MSG, RESPONSE_UPDATED_MSG,
                         response.get(RESPONSE_KEY_3));
            assertEquals("Invalid PcqId in response", firstUuid, response.get(RESPONSE_KEY_1));

            //Get the record
            Map<String, Object> validateGetResponse = pcqBackEndServiceClient.getAnswersRecord(
                firstUuid, HttpStatus.OK);

            assertEquals("CaseId not matching", validateGetResponse.get("ccdCaseId"), TEST_CASE_ID);

        } catch (IOException e) {
            log.error("Error during test execution", e);
        }

    }
}
